package com.panda.myapplication.data.repository

import com.panda.myapplication.data.local.dao.HardwareDao
import com.panda.myapplication.data.local.entity.UserSessionEntity
import com.panda.myapplication.data.remote.SecurityUtils
import com.panda.myapplication.data.remote.SupabaseConfig
import com.panda.myapplication.data.remote.SupabaseConfig.applySupabaseHeaders
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.Gender
import com.panda.myapplication.domain.model.SpiritHardware
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.domain.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
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
        val passwordHash: String,
    )

    private val _registeredAccounts = mutableMapOf(
        "dombikpanda" to AccountCredential(
            profile = MASTER_ADMIN_PROFILE,
            passwordHash = MASTER_ADMIN_PASSWORD_SHA256,
        ),
        "yavuzyigitomer52@gmail.com" to AccountCredential(
            profile = MASTER_ADMIN_PROFILE,
            passwordHash = MASTER_ADMIN_PASSWORD_SHA256,
        ),
    )

    init {
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
                        syncUserStatsFromCloud(session.userId)
                    }
                }
            }
        }
    }

    private suspend fun syncUserStatsFromCloud(userId: String) {
        if (userId.isBlank() || userId.startsWith("guest")) return
        runCatching {
            val safeCols = "reputation_score,total_deals_count,total_tunings_count,total_tips_count,badge_title,warning_count,is_banned,banned_until_epoch_ms,ban_reason,ban_escalation_tier"
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_USER_PROFILES}?id=eq.$userId&select=$safeCols"
            val response = httpClient.get(url) {
                applySupabaseHeaders()
            }
            if (response.status == HttpStatusCode.OK) {
                val array = json.parseToJsonElement(response.bodyAsText()).jsonArray
                if (array.isNotEmpty()) {
                    val obj = array[0].jsonObject
                    val repScore = obj["reputation_score"]?.jsonPrimitive?.intOrNull ?: 0
                    val totalDeals = obj["total_deals_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val totalTunings = obj["total_tunings_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val totalTips = obj["total_tips_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val badge = obj["badge_title"]?.jsonPrimitive?.content ?: UserProfile.calculateBadgeTitle(repScore)
                    val warningCount = obj["warning_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val isBanned = obj["is_banned"]?.jsonPrimitive?.booleanOrNull ?: false
                    val bannedUntil = obj["banned_until_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 0L
                    val banReason = obj["ban_reason"]?.jsonPrimitive?.contentOrNull
                    val banEscalationTier = obj["ban_escalation_tier"]?.jsonPrimitive?.intOrNull ?: 0

                    _currentUser.update { current ->
                        current?.copy(
                            reputationScore = repScore,
                            totalDealsCount = totalDeals,
                            totalTuningsCount = totalTunings,
                            totalTipsCount = totalTips,
                            badgeTitle = badge,
                            warningCount = warningCount,
                            isBanned = isBanned,
                            bannedUntilEpochMs = bannedUntil,
                            banReason = banReason,
                            banEscalationTier = banEscalationTier,
                        )
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

        val hashedPassword = SecurityUtils.hashPassword(password)
        val profile = UserProfile(
            id = "usr-${kotlin.random.Random.nextInt(100000, 999999)}",
            email = trimmedEmail,
            username = trimmedUsername,
            birthDateIso = birthDateIso.ifBlank { "2001-01-01" },
            gender = gender,
            spiritHardware = spiritHardware,
            createdAtEpochMs = System.currentTimeMillis(),
        )

        runCatching {
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_USER_PROFILES}"
            val body = buildJsonObject {
                put("id", profile.id)
                put("username", profile.username)
                put("email", profile.email)
                put("password_hash", hashedPassword)
                put("birth_date_iso", profile.birthDateIso)
                put("gender", profile.gender.name)
                put("spirit_hardware", profile.spiritHardware.name)
                put("created_at_epoch_ms", profile.createdAtEpochMs)
            }
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                applySupabaseHeaders(preferMinimal = true)
                setBody(body.toString())
            }
        }

        val cred = AccountCredential(profile, hashedPassword)
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
        if (input.isBlank() || password.isBlank()) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Kullanıcı veya şifre bulunamadı!")))
        }

        val hashedPassword = SecurityUtils.hashPassword(password)

        // 1. Sunucu Tarafı Güvenli RPC Doğrulaması (password_hash sütunu istemciye asla dönmez)
        try {
            val rpcUrl = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.RPC_AUTHENTICATE_USER}"
            val rpcPayload = buildJsonObject {
                put("p_identifier", input)
                put("p_password_hash", hashedPassword)
            }
            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                applySupabaseHeaders()
                setBody(rpcPayload.toString())
            }
            if (response.status == HttpStatusCode.OK) {
                val array = json.parseToJsonElement(response.bodyAsText()).jsonArray
                if (array.isNotEmpty()) {
                    val userObj = array[0].jsonObject
                    val repScore = userObj["reputation_score"]?.jsonPrimitive?.intOrNull ?: 0
                    val warningCount = userObj["warning_count"]?.jsonPrimitive?.intOrNull ?: 0
                    val isBanned = userObj["is_banned"]?.jsonPrimitive?.booleanOrNull ?: false
                    val bannedUntil = userObj["banned_until_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 0L
                    val banReason = userObj["ban_reason"]?.jsonPrimitive?.contentOrNull
                    val banEscalationTier = userObj["ban_escalation_tier"]?.jsonPrimitive?.intOrNull ?: 0

                    val profile = UserProfile(
                        id = userObj["id"]?.jsonPrimitive?.content ?: "usr-0",
                        email = userObj["email"]?.jsonPrimitive?.content ?: "",
                        username = userObj["username"]?.jsonPrimitive?.content ?: "",
                        birthDateIso = userObj["birth_date_iso"]?.jsonPrimitive?.content ?: "2000-01-01",
                        gender = Gender.fromString(userObj["gender"]?.jsonPrimitive?.content),
                        spiritHardware = SpiritHardware.fromString(userObj["spirit_hardware"]?.jsonPrimitive?.content),
                        createdAtEpochMs = userObj["created_at_epoch_ms"]?.jsonPrimitive?.longOrNull ?: 1713500000000L,
                        reputationScore = repScore,
                        totalDealsCount = userObj["total_deals_count"]?.jsonPrimitive?.intOrNull ?: 0,
                        totalTuningsCount = userObj["total_tunings_count"]?.jsonPrimitive?.intOrNull ?: 0,
                        totalTipsCount = userObj["total_tips_count"]?.jsonPrimitive?.intOrNull ?: 0,
                        badgeTitle = userObj["badge_title"]?.jsonPrimitive?.content ?: UserProfile.calculateBadgeTitle(repScore),
                        warningCount = warningCount,
                        isBanned = isBanned,
                        bannedUntilEpochMs = bannedUntil,
                        banReason = banReason,
                        banEscalationTier = banEscalationTier,
                    )
                    _currentUser.value = profile
                    _isLoggedIn.value = true
                    persistSession(profile)
                    return@withContext DomainResult.Success(profile)
                } else {
                    return@withContext DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Kullanıcı veya şifre bulunamadı!")))
                }
            }
        } catch (_: Exception) {
            // Çevrimdışı durumda yerel önbellek kontrolü
        }

        // 2. Çevrimdışı / Yerel Kayıtlı Kullanıcı Hesabı Doğrulaması (Özetlenmiş Şifre ile)
        val account = _registeredAccounts[input.lowercase()]
        if (account != null && account.passwordHash == hashedPassword) {
            _currentUser.value = account.profile
            _isLoggedIn.value = true
            persistSession(account.profile)
            return@withContext DomainResult.Success(account.profile)
        }

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
            val url = "${SupabaseConfig.restApiUrl}/${SupabaseConfig.TABLE_USER_PROFILES}?id=eq.${profile.id}"
            val body = buildJsonObject {
                put("username", profile.username)
                put("email", profile.email)
                put("birth_date_iso", profile.birthDateIso)
                put("gender", profile.gender.name)
                put("spirit_hardware", profile.spiritHardware.name)
            }
            httpClient.patch(url) {
                contentType(ContentType.Application.Json)
                applySupabaseHeaders(preferMinimal = true)
                setBody(body.toString())
            }
        }

        DomainResult.Success(Unit)
    }

    override suspend fun refreshCurrentUserStats() = withContext(ioDispatcher) {
        val uid = _currentUser.value?.id ?: return@withContext
        syncUserStatsFromCloud(uid)
    }

    companion object {
        // Yalnızca tek yönlü tuzlanmış SHA-256 özeti saklanır; düz metin (plaintext) parola asla kodda yer almaz.
        private const val MASTER_ADMIN_PASSWORD_SHA256 = "610768c536f619515fce0de482af604d60efa48af177c2d5fc201cb6e5c64d6d"

        private val MASTER_ADMIN_PROFILE = UserProfile(
            id = "usr-master-admin-01",
            email = "yavuzyigitomer52@gmail.com",
            username = "dombikpanda",
            birthDateIso = "2002-05-14",
            gender = Gender.MALE,
            spiritHardware = SpiritHardware.OVERCLOCKED_CPU,
            createdAtEpochMs = 1713500000000L,
            reputationScore = 500,
            badgeTitle = "Silikon Efendisi",
        )
    }
}
