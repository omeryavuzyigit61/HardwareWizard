package com.panda.myapplication.data.repository

import com.panda.myapplication.data.local.dao.HardwareDao
import com.panda.myapplication.data.local.entity.UserSessionEntity
import com.panda.myapplication.data.remote.SupabaseConfig
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.Gender
import com.panda.myapplication.domain.model.SpiritHardware
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.domain.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class UserRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val hardwareDao: HardwareDao? = null,
    private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private data class AccountCredential(
        val profile: UserProfile,
        val password: String,
    )

    private val _registeredAccounts = mutableMapOf<String, AccountCredential>()

    init {
        // Cihaz yeniden başladığında veya uygulama açıldığında yerel oturumu geri yükle
        if (hardwareDao != null) {
            CoroutineScope(ioDispatcher).launch {
                runCatching {
                    val session = hardwareDao.getUserSession()
                    if (session != null) {
                        val profile = UserProfile(
                            id = session.userId,
                            email = session.email,
                            username = session.username,
                            birthDateIso = session.birthDateIso,
                            gender = Gender.fromString(session.gender),
                            spiritHardware = SpiritHardware.fromString(session.spiritHardware),
                            createdAtEpochMs = session.createdAtEpochMs,
                        )
                        _currentUser.value = profile
                        _isLoggedIn.value = true
                    }
                }
            }
        }
    }

    private suspend fun persistSession(profile: UserProfile) {
        runCatching {
            hardwareDao?.saveUserSession(
                UserSessionEntity(
                    userId = profile.id,
                    email = profile.email,
                    username = profile.username,
                    birthDateIso = profile.birthDateIso,
                    gender = profile.gender.name,
                    spiritHardware = profile.spiritHardware.name,
                    createdAtEpochMs = profile.createdAtEpochMs,
                )
            )
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String,
        birthDateIso: String,
        gender: Gender,
        spiritHardware: SpiritHardware,
    ): DomainResult<UserProfile> = withContext(ioDispatcher) {
        val trimmedEmail = email.trim()
        val trimmedUsername = username.trim().removePrefix("@")

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Lütfen geçerli bir e-posta adresi girin.")))
        }
        if (password.length < 6) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Şifre en az 6 karakter olmalıdır.")))
        }
        if (trimmedUsername.isBlank()) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Lütfen bir kullanıcı adı belirleyin.")))
        }

        val profile = UserProfile(
            id = "usr-${kotlin.random.Random.nextInt(100000, 999999)}",
            email = trimmedEmail,
            username = trimmedUsername,
            birthDateIso = birthDateIso.ifBlank { "2001-01-01" },
            gender = gender,
            spiritHardware = spiritHardware,
            createdAtEpochMs = 1713500000000L,
        )

        // 1. Supabase Bulut Veritabanına kaydet
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/user_profiles"
            val body = buildJsonObject {
                put("id", profile.id)
                put("username", profile.username)
                put("email", profile.email)
                put("password_hash", password)
                put("birth_date_iso", profile.birthDateIso)
                put("gender", profile.gender.name)
                put("spirit_hardware", profile.spiritHardware.name)
                put("created_at_epoch_ms", profile.createdAtEpochMs)
            }
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                    append("Prefer", "return=minimal")
                }
                setBody(body.toString())
            }
        }

        val cred = AccountCredential(profile, password)
        _registeredAccounts[trimmedUsername.lowercase()] = cred
        _registeredAccounts[trimmedEmail.lowercase()] = cred

        _currentUser.value = profile
        _isLoggedIn.value = true
        persistSession(profile)

        DomainResult.Success(profile)
    }

    override suspend fun login(
        emailOrUsername: String,
        password: String,
    ): DomainResult<UserProfile> = withContext(ioDispatcher) {
        val input = emailOrUsername.trim().removePrefix("@")

        // 1. Yönetici Hesabı Doğrulaması
        val isAdminIdentifier = input.equals("dombikpanda", ignoreCase = true) || input.equals("yavuzyigitomer52@gmail.com", ignoreCase = true)
        if (isAdminIdentifier) {
            if (password == "TS61trabzonspor54") {
                val adminProfile = UserProfile(
                    id = "usr-dombik-61",
                    email = "yavuzyigitomer52@gmail.com",
                    username = "dombikpanda",
                    birthDateIso = "2000-05-14",
                    gender = Gender.MALE,
                    spiritHardware = SpiritHardware.OVERCLOCKED_CPU,
                    createdAtEpochMs = 1713000000000L,
                )
                _currentUser.value = adminProfile
                _isLoggedIn.value = true
                persistSession(adminProfile)
                return@withContext DomainResult.Success(adminProfile)
            } else {
                return@withContext DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Kullanıcı veya şifre bulunamadı!")))
            }
        }

        // 2. Canlı Supabase Bulut Veritabanından Kullanıcı Sorgulama
        try {
            val encodedInput = input.lowercase()
            val url = "${SupabaseConfig.restApiUrl}/user_profiles?or=(username.ilike.$encodedInput,email.ilike.$encodedInput)&select=*"
            val response = httpClient.get(url) {
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
            }
            if (response.status == HttpStatusCode.OK) {
                val array = json.parseToJsonElement(response.bodyAsText()).jsonArray
                if (array.isNotEmpty()) {
                    val userObj = array[0].jsonObject
                    val storedPassword = userObj["password_hash"]?.jsonPrimitive?.content ?: ""
                    if (storedPassword == password) {
                        val profile = UserProfile(
                            id = userObj["id"]?.jsonPrimitive?.content ?: "usr-0",
                            email = userObj["email"]?.jsonPrimitive?.content ?: "",
                            username = userObj["username"]?.jsonPrimitive?.content ?: "",
                            birthDateIso = userObj["birth_date_iso"]?.jsonPrimitive?.content ?: "2000-01-01",
                            gender = Gender.fromString(userObj["gender"]?.jsonPrimitive?.content),
                            spiritHardware = SpiritHardware.fromString(userObj["spirit_hardware"]?.jsonPrimitive?.content),
                            createdAtEpochMs = userObj["created_at_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 1713500000000L,
                        )
                        _currentUser.value = profile
                        _isLoggedIn.value = true
                        persistSession(profile)
                        return@withContext DomainResult.Success(profile)
                    } else {
                        return@withContext DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Kullanıcı veya şifre bulunamadı!")))
                    }
                }
            }
        } catch (_: Exception) {
            // Ağ hatası veya çevrimdışı durumda yerel kontrol
        }

        // 3. Yerel Kayıtlı Kullanıcı Hesabı Doğrulaması
        val account = _registeredAccounts[input.lowercase()]
        if (account != null) {
            if (account.password == password) {
                _currentUser.value = account.profile
                _isLoggedIn.value = true
                persistSession(account.profile)
                return@withContext DomainResult.Success(account.profile)
            } else {
                return@withContext DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Kullanıcı veya şifre bulunamadı!")))
            }
        }

        // 4. Bulunamayan Kullanıcı Hatası
        DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Kullanıcı veya şifre bulunamadı!")))
    }

    override suspend fun logout() = withContext(ioDispatcher) {
        _currentUser.value = null
        _isLoggedIn.value = false
        runCatching {
            hardwareDao?.clearUserSession()
        }
        Unit
    }

    override suspend fun updateProfile(profile: UserProfile): DomainResult<Unit> = withContext(ioDispatcher) {
        _currentUser.value = profile
        persistSession(profile)
        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/user_profiles?id=eq.${profile.id}"
            val body = buildJsonObject {
                put("birth_date_iso", profile.birthDateIso)
                put("gender", profile.gender.name)
                put("spirit_hardware", profile.spiritHardware.name)
            }
            httpClient.patch(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", SupabaseConfig.supabaseAnonKey)
                    append("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
                }
                setBody(body.toString())
            }
        }
        DomainResult.Success(Unit)
    }
}
