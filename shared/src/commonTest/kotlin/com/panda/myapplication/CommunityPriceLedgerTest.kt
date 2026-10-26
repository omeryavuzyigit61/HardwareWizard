package com.panda.myapplication

import com.panda.myapplication.data.repository.CommunityPriceRepositoryImpl
import com.panda.myapplication.domain.engine.BuildAnalysisEngine
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.domain.model.CommunityDealType
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.DealStatus
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.Wattage
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CommunityPriceLedgerTest {

    private val httpClient = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    private fun createRepository(): CommunityPriceRepositoryImpl {
        return CommunityPriceRepositoryImpl(
            httpClient = httpClient,
            json = json,
            ioDispatcher = Dispatchers.Default,
            autoFetchRemote = false,
        )
    }

    @Test
    fun testInitialSeedDealsExistAndContainApprovedDeals() = runTest {
        val repository = createRepository()
        val allDeals = repository.observeAllDeals().first()
        assertTrue(allDeals.isNotEmpty(), "Seed deals must be initialized")

        val approvedDeals = allDeals.filter { it.status == DealStatus.APPROVED }
        assertTrue(approvedDeals.size >= 4, "Should have at least 4 initial approved deals")

        val rtxDeal = allDeals.find { it.hardwareId == "gpu_rtx_4070_super" }
        assertNotNull(rtxDeal)
        assertEquals(CommunityDealType.DISCOUNT_CAMPAIGN, rtxDeal.dealType)
        assertEquals(23499.0, rtxDeal.priceTry)
        assertNotNull(rtxDeal.proofUrl)
    }

    @Test
    fun testSubmitDealAddsPendingRecord() = runTest {
        val repository = createRepository()
        val newDeal = CommunityDeal(
            id = "test-deal-999",
            hardwareId = "test_gpu_custom",
            hardwareName = "NVIDIA GeForce RTX 4070 Ti Super",
            priceTry = 29500.0,
            dealType = CommunityDealType.OWN_PURCHASE,
            note = "İncehesap cuma gecesinden alındı.",
            proofUrl = "https://www.incehesap.com",
            userDisplayName = "Test Oyuncu",
            createdAtEpochMs = 1713500000000L,
            status = DealStatus.PENDING,
        )

        repository.submitDeal(newDeal)

        val hardwareDeals = repository.observeDealsForHardware("test_gpu_custom").first()
        assertEquals(1, hardwareDeals.size)
        val saved = hardwareDeals.first()
        assertEquals("test-deal-999", saved.id)
        assertEquals(DealStatus.PENDING, saved.status)
        assertEquals(29500.0, saved.priceTry)
    }

    @Test
    fun testAdminAuthenticationWithPins() {
        val repository = createRepository()
        assertFalse(repository.isAdmin())

        // Wrong pin fails
        assertFalse(repository.loginAdmin("0000"))
        assertFalse(repository.isAdmin())

        // Default admin pin succeeds
        assertTrue(repository.loginAdmin("1923"))
        assertTrue(repository.isAdmin())

        // Logout works
        repository.logoutAdmin()
        assertFalse(repository.isAdmin())
    }

    @Test
    fun testAdminApproveAndRejectWorkflow() = runTest {
        val repository = createRepository()
        val pendingDealsBefore = repository.observeAllPendingDeals().first()
        val targetPending = pendingDealsBefore.first()

        // 1. Approve
        repository.approveDeal(targetPending.id, adminName = "Ömer Admin")
        val allDealsAfterApprove = repository.observeAllDeals().first()
        val approvedDeal = allDealsAfterApprove.find { it.id == targetPending.id }
        assertNotNull(approvedDeal)
        assertEquals(DealStatus.APPROVED, approvedDeal.status)
        assertEquals("Ömer Admin", approvedDeal.reviewedBy)

        // 2. Reject another deal
        val anotherDeal = CommunityDeal(
            id = "deal-fake-price",
            hardwareId = "gpu_fake",
            hardwareName = "Fake GPU",
            priceTry = 100.0,
            dealType = CommunityDealType.SECOND_HAND,
            createdAtEpochMs = 1713500000000L,
            status = DealStatus.PENDING,
        )
        repository.submitDeal(anotherDeal)
        repository.rejectDeal(anotherDeal.id, reason = "Piyasa dışı afaki fiyat")

        val allDealsAfterReject = repository.observeAllDeals().first()
        val rejectedDeal = allDealsAfterReject.find { it.id == anotherDeal.id }
        assertNotNull(rejectedDeal)
        assertEquals(DealStatus.REJECTED, rejectedDeal.status)
        assertEquals("Piyasa dışı afaki fiyat", rejectedDeal.reviewNote)
    }

    @Test
    fun testPrecisionClearanceRadarWithRamAndRadiatorDetails() {
        val build = ActiveBuild(
            cpu = HardwareItem(
                id = HardwareId("cpu_7800x3d"),
                brand = "AMD",
                model = "Ryzen 7 7800X3D",
                price = Money(1650000L, "TRY"),
                spec = CpuSpec(
                    socket = "AM5",
                    coreCount = 8,
                    threadCount = 16,
                    baseClock = Frequency(4200),
                    boostClock = Frequency(5000),
                    tdp = Wattage(120),
                    hasIntegratedGraphics = true,
                ),
            ),
            gpu = HardwareItem(
                id = HardwareId("gpu_4080s"),
                brand = "ASUS",
                model = "ROG Strix RTX 4080 Super",
                price = Money(3800000L, "TRY"),
                spec = GpuSpec(
                    chipset = "AD103",
                    vram = MemorySize(16384),
                    memoryType = "GDDR6X",
                    coreClock = Frequency(2295),
                    boostClock = Frequency(2550),
                    lengthMm = 357,
                    tdp = Wattage(320),
                    recommendedPsu = Wattage(750),
                ),
            ),
            ram = HardwareItem(
                id = HardwareId("ram_corsair_rgb"),
                brand = "Corsair",
                model = "Vengeance RGB DDR5",
                price = Money(520000L, "TRY"),
                spec = RamSpec(
                    generation = DdrGeneration.DDR5,
                    speed = Frequency(6000),
                    casLatency = 30,
                    capacity = MemorySize(32768),
                    moduleCount = 2,
                ),
            ),
            cooler = HardwareItem(
                id = HardwareId("cooler_arctic_360"),
                brand = "Arctic",
                model = "Liquid Freezer III 360",
                price = Money(420000L, "TRY"),
                spec = CoolerSpec(
                    type = CoolerType.LIQUID,
                    radiatorSizeMm = 360,
                    maxCoolingTdp = Wattage(300),
                    supportedSockets = listOf("AM5", "LGA1700"),
                ),
            ),
            case = HardwareItem(
                id = HardwareId("case_lian_li_o11"),
                brand = "Lian Li",
                model = "O11 Vision",
                price = Money(650000L, "TRY"),
                spec = CaseSpec(
                    maxGpuLengthMm = 455,
                    maxCpuCoolerHeightMm = 167,
                    maxRadiatorSizeMm = 360,
                    radiatorSupport = "Side: 360mm, Bottom: 360mm",
                    includedFans = "Fansız (Custom Airflow)",
                ),
            ),
        )

        val clearance = BuildAnalysisEngine.calculateClearanceRadar(build)
        assertFalse(clearance.hasClearanceIssue, "Build should fit cleanly")
        assertEquals(98, clearance.gpuClearanceMm) // 455 - 357 = 98mm
        assertEquals(0, clearance.radiatorClearanceMm) // 360 - 360 = 0mm

        // Verify enhanced precision fields
        assertNotNull(clearance.radiatorSupportSummary)
        assertTrue(clearance.radiatorSupportSummary.contains("360mm"))

        assertNotNull(clearance.ramClearanceStatus)
        assertTrue(clearance.ramClearanceStatus.contains("Sıvı Soğutma"))

        assertNotNull(clearance.fanCapacitySummary)
        assertTrue(clearance.fanCapacitySummary.contains("Fansız"))
    }
}
