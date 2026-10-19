package com.panda.myapplication.data.repository

import com.panda.myapplication.data.remote.SupabaseConfig
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.Gender
import com.panda.myapplication.domain.model.SpiritHardware
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.domain.repository.UserRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class UserRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {

    // Başlangıçta aktif oturum (Varsayılan olarak dombikpanda veya misafir oturumu)
    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "usr-dombik-61",
            email = "yavuzyigitomer52@gmail.com",
            username = "dombikpanda",
            birthDateIso = "2000-05-14",
            gender = Gender.MALE,
            spiritHardware = SpiritHardware.OVERCLOCKED_CPU,
            createdAtEpochMs = 1713000000000L,
        )
    )
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

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

        _currentUser.value = profile
        _isLoggedIn.value = true

        DomainResult.Success(profile)
    }

    override suspend fun login(
        emailOrUsername: String,
        password: String,
    ): DomainResult<UserProfile> = withContext(ioDispatcher) {
        val input = emailOrUsername.trim().removePrefix("@")

        // 1. Master Yönetici Girişi Kontrolü
        val isMasterAdmin = (input.equals("dombikpanda", ignoreCase = true) || input.equals("yavuzyigitomer52@gmail.com", ignoreCase = true)) &&
                password == "TS61trabzonspor54"

        if (isMasterAdmin) {
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
            return@withContext DomainResult.Success(adminProfile)
        }

        // 2. Standart Kullanıcı Girişi
        if (password.length < 6) {
            return@withContext DomainResult.Failure(DomainError.Unknown(IllegalArgumentException("Hatalı şifre veya kullanıcı adı.")))
        }

        val userProfile = UserProfile(
            id = "usr-${input.hashCode().toString().takeLast(6)}",
            email = if (input.contains("@")) input else "$input@hardwarewizard.community",
            username = input,
            birthDateIso = "2002-06-20",
            gender = Gender.MALE,
            spiritHardware = SpiritHardware.FLAGSHIP_GPU,
            createdAtEpochMs = 1713500000000L,
        )

        _currentUser.value = userProfile
        _isLoggedIn.value = true
        DomainResult.Success(userProfile)
    }

    override suspend fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    override suspend fun updateProfile(profile: UserProfile): DomainResult<Unit> = withContext(ioDispatcher) {
        _currentUser.value = profile
        DomainResult.Success(Unit)
    }
}
