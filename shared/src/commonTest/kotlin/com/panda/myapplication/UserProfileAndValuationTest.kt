package com.panda.myapplication

import com.panda.myapplication.data.repository.UserRepositoryImpl
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.Gender
import com.panda.myapplication.domain.model.SpiritHardware
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.domain.util.ProfanityFilter
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserProfileAndValuationTest {

    @Test
    fun testUserAgeCalculation() {
        val user1 = UserProfile(
            id = "u1",
            email = "test@domain.com",
            username = "dombikpanda",
            birthDateIso = "2000-05-14",
        )
        // 2026 - 2000 = 26
        assertEquals(26, user1.age)

        val user2 = UserProfile(
            id = "u2",
            email = "young@domain.com",
            username = "gamer06",
            birthDateIso = "2006-11-20", // Kasım > Ekim -> 2026 - 2006 - 1 = 19
        )
        assertEquals(19, user2.age)

        val user3 = UserProfile(
            id = "u3",
            email = "vet@domain.com",
            username = "hardcore90",
            birthDateIso = "1990-01-01",
        )
        assertEquals(36, user3.age)
    }

    @Test
    fun testSpiritHardwareAttributes() {
        assertEquals(6, SpiritHardware.entries.size)
        SpiritHardware.entries.forEach { spirit ->
            assertTrue(spirit.title.isNotBlank())
            assertTrue(spirit.emoji.isNotBlank())
            assertTrue(spirit.motto.isNotBlank())
            assertTrue(spirit.colorHex != 0L)
        }
    }

    @Test
    fun testMasterAdminLoginWithUsername() = runTest {
        val repo = UserRepositoryImpl(
            httpClient = HttpClient(),
            json = Json { ignoreUnknownKeys = true },
            ioDispatcher = Dispatchers.Unconfined,
        )

        val result = repo.login("dombikpanda", "TS61trabzonspor54")
        assertTrue(result is DomainResult.Success)
        val admin = result.data
        assertEquals("dombikpanda", admin.username)
        assertEquals("yavuzyigitomer52@gmail.com", admin.email)
        assertEquals(SpiritHardware.OVERCLOCKED_CPU, admin.spiritHardware)
        assertTrue(repo.isLoggedIn.value)
    }

    @Test
    fun testMasterAdminLoginWithEmail() = runTest {
        val repo = UserRepositoryImpl(
            httpClient = HttpClient(),
            json = Json { ignoreUnknownKeys = true },
            ioDispatcher = Dispatchers.Unconfined,
        )

        val result = repo.login("yavuzyigitomer52@gmail.com", "TS61trabzonspor54")
        assertTrue(result is DomainResult.Success)
        val admin = result.data
        assertEquals("dombikpanda", admin.username)
        assertEquals("yavuzyigitomer52@gmail.com", admin.email)
    }

    @Test
    fun testStandardUserRegistrationAndLogout() = runTest {
        val repo = UserRepositoryImpl(
            httpClient = HttpClient(),
            json = Json { ignoreUnknownKeys = true },
            ioDispatcher = Dispatchers.Unconfined,
        )

        val signUpResult = repo.signUp(
            email = "custom_member@gmail.com",
            password = "secretPassword123",
            username = "speeddemon",
            birthDateIso = "1998-08-15",
            gender = Gender.MALE,
            spiritHardware = SpiritHardware.FLAGSHIP_GPU,
        )

        assertTrue(signUpResult is DomainResult.Success)
        val user = signUpResult.data
        assertEquals("speeddemon", user.username)
        assertEquals("custom_member@gmail.com", user.email)
        assertEquals(SpiritHardware.FLAGSHIP_GPU, user.spiritHardware)
        assertEquals(28, user.age) // 2026 - 1998 = 28
        assertTrue(repo.isLoggedIn.value)

        // Logout
        repo.logout()
        assertFalse(repo.isLoggedIn.value)
        assertEquals(null, repo.currentUser.value)
    }

    @Test
    fun testLoginWithInvalidCredentialsReturnsFailure() = runTest {
        val repo = UserRepositoryImpl(
            httpClient = HttpClient(),
            json = Json { ignoreUnknownKeys = true },
            ioDispatcher = Dispatchers.Unconfined,
        )

        val resultWrongPass = repo.login("dombikpanda", "WrongPassword123")
        assertTrue(resultWrongPass is DomainResult.Failure)

        val resultUnknownUser = repo.login("non_existent_user_999", "somePassword")
        assertTrue(resultUnknownUser is DomainResult.Failure)
    }

    @Test
    fun testSecondHandValuationFormulaTiers() {
        val basePrice = 30000.0 // 30,000 TL sıfır GPU
        val retentionFactor = 0.74 // GPU retention
        val marketMedian = basePrice * retentionFactor // 22,200 TL
        val quickSell = marketMedian * 0.83 // 18,426 TL (~%17 indirimli)
        val showcaseMint = marketMedian * 1.13 // 25,086 TL (~%13 primli)

        assertEquals(22200.0, marketMedian)
        assertTrue(quickSell < marketMedian)
        assertTrue(showcaseMint > marketMedian)

        val retentionPercentage = ((marketMedian / basePrice) * 100).roundToInt()
        assertEquals(74, retentionPercentage)
    }

    @Test
    fun testProfanityFilterMasking() {
        val cleanInput = "Bu anakart ve soğutucu ile 9070 XT gayet stabil çalışıyor, hiçbir sıkıntı yok."
        assertEquals(cleanInput, ProfanityFilter.mask(cleanInput))
        assertFalse(ProfanityFilter.containsProfanity(cleanInput))

        val profaneInput1 = "Bu ayar çok sikik olmuş"
        val masked1 = ProfanityFilter.mask(profaneInput1)
        assertTrue(ProfanityFilter.containsProfanity(profaneInput1))
        assertTrue(masked1.contains("s***k"))
        assertFalse(masked1.contains("sikik"))

        val profaneInput2 = "amk böyle kartın"
        val masked2 = ProfanityFilter.mask(profaneInput2)
        assertTrue(masked2.contains("***"))
        assertFalse(masked2.contains("amk"))
    }

    @Test
    fun testUserSuspensionAndWarnings() {
        val normalUser = UserProfile(
            id = "u-clean",
            email = "clean@domain.com",
            username = "cleanUser",
            warningCount = 2,
            isBanned = false,
            bannedUntilEpochMs = 0L,
        )
        assertFalse(normalUser.isSuspended)
        assertEquals("", normalUser.remainingSuspensionText)
        assertEquals(2, normalUser.warningCount)

        val bannedUser = UserProfile(
            id = "u-banned",
            email = "banned@domain.com",
            username = "toxicUser",
            isBanned = true,
            banReason = "Aşırı küfür ve hakaret",
        )
        assertTrue(bannedUser.isSuspended)
        assertEquals("Süresiz", bannedUser.remainingSuspensionText)

        val futureTime = kotlin.time.Clock.System.now().toEpochMilliseconds() + (48 * 60 * 60 * 1000L)
        val tempBannedUser = UserProfile(
            id = "u-temp",
            email = "temp@domain.com",
            username = "warnedUser",
            bannedUntilEpochMs = futureTime,
            banReason = "6 uyarı limiti",
            banEscalationTier = 1,
        )
        assertTrue(tempBannedUser.isSuspended)
        assertTrue(tempBannedUser.remainingSuspensionText.contains("gün"))
        assertEquals(1, tempBannedUser.banEscalationTier)
    }
}
