package com.panda.myapplication

import com.panda.myapplication.data.mapper.HardwareMapper
import com.panda.myapplication.data.model.dto.SmartphoneCatalogItemDto
import com.panda.myapplication.data.model.dto.TabletCatalogItemDto
import com.panda.myapplication.domain.engine.HardwareEnrichmentEngine
import com.panda.myapplication.domain.engine.VsArenaEngine
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.model.TabletSpec
import com.panda.myapplication.domain.repository.VsArenaMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class MobileAndTabletHardwareTest {

    private fun createSamplePhone(
        id: String = "phone-apple-iphone-16-pro-max-256gb",
        brand: String = "Apple",
        model: String = "iPhone 16 Pro Max",
        chipset: String = "Apple A18 Pro",
        antutu: Int = 1820000,
        refreshHz: Int = 120,
        batteryMah: Int = 4685,
        mainMp: Int = 48,
        hasOis: Boolean = true,
        hasStylus: Boolean = false,
    ): HardwareItem {
        return HardwareItem(
            id = HardwareId(id),
            brand = brand,
            model = model,
            price = Money(7999900L, "TRY"),
            images = listOf("https://example.com/front.png", "https://example.com/back.png"),
            imageUrl = "https://example.com/front.png",
            spec = PhoneSpec(
                chipset = chipset,
                antutuScore = antutu,
                refreshRateHz = refreshHz,
                batteryMah = batteryMah,
                mainCameraMp = mainMp,
                hasOis = hasOis,
                hasStylus = hasStylus,
            ),
        )
    }

    private fun createSampleTablet(
        id: String = "tablet-apple-ipad-pro-13-m4-256gb",
        brand: String = "Apple",
        model: String = "iPad Pro 13 (M4)",
        chipset: String = "Apple M4",
        antutu: Int = 2750000,
        sizeInch: Double = 13.0,
        stylus: String = "Apple Pencil Pro",
        speakerCount: Int = 4,
    ): HardwareItem {
        return HardwareItem(
            id = HardwareId(id),
            brand = brand,
            model = model,
            price = Money(6499900L, "TRY"),
            images = listOf("https://example.com/front.png", "https://example.com/back.png"),
            imageUrl = "https://example.com/front.png",
            spec = TabletSpec(
                chipset = chipset,
                antutuScore = antutu,
                displaySizeInch = sizeInch,
                stylusSupport = stylus,
                speakerCount = speakerCount,
            ),
        )
    }

    @Test
    fun testMobileDomainAndDtoMapping() {
        val now = Instant.fromEpochMilliseconds(1731150000000L)
        val phoneDto = SmartphoneCatalogItemDto(
            id = "phone-apple-iphone-16-pro-max-256gb",
            brand = "Apple",
            model = "iPhone 16 Pro Max",
            priceTry = 79999.0,
            imageUrl = "https://example.com/img.png",
            images = listOf("https://example.com/img1.png", "https://example.com/img2.png"),
            spec = PhoneSpec(
                chipset = "Apple A18 Pro",
                antutuScore = 1820000,
                refreshRateHz = 120,
                hasOis = true,
            ),
        )

        val pair = HardwareMapper.smartphoneDtoToEntity(phoneDto, now)
        assertNotNull(pair, "Phone DTO to Entity pair must not be null")
        val (entity, specEntity) = pair
        assertEquals("phone-apple-iphone-16-pro-max-256gb", entity.id)
        assertEquals(HardwareCategory.SMARTPHONE, entity.category)
        assertEquals("Apple A18 Pro", specEntity.chipset)
        assertEquals(1820000, specEntity.antutuScore)

        val domainItem = HardwareMapper.toDomain(
            com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                item = entity,
                phone = specEntity,
            )
        )
        assertNotNull(domainItem, "Mapped domain item must not be null")
        assertEquals(HardwareCategory.SMARTPHONE, domainItem.category)
        val phoneSpec = domainItem.spec as? PhoneSpec
        assertNotNull(phoneSpec)
        assertEquals("Apple A18 Pro", phoneSpec.chipset)
    }

    @Test
    fun testHardwareEnrichmentEngineForMobile() {
        val iphone = createSamplePhone()
        val badges = HardwareEnrichmentEngine.getEngineeringBadges(iphone)
        assertTrue(badges.isNotEmpty(), "Phone must generate enrichment badges")
        assertTrue(badges.any { it.label.contains("Yonga Seti") && it.value.contains("Apple A18 Pro") })
        assertTrue(badges.any { it.label.contains("AnTuTu") && it.value.contains("1820000") })
        assertTrue(badges.any { it.label.contains("Ekran") && it.value.contains("120Hz") })
        assertTrue(badges.any { it.label.contains("Batarya") && it.value.contains("4685") })

        val ipad = createSampleTablet()
        val tabletBadges = HardwareEnrichmentEngine.getEngineeringBadges(ipad)
        assertTrue(tabletBadges.any { it.label.contains("Yonga") && it.value.contains("Apple M4") })
        assertTrue(tabletBadges.any { it.label.contains("Stylus") && it.value.contains("Apple Pencil Pro") })
        assertTrue(tabletBadges.any { it.label.contains("Ses") && it.value.contains("4 Hoparlör") })
    }

    @Test
    fun testVsArenaSmartphoneComparison() {
        val iphone = createSamplePhone(
            id = "phone-apple-iphone-16-pro-max-256gb",
            brand = "Apple",
            model = "iPhone 16 Pro Max",
            chipset = "Apple A18 Pro",
            antutu = 1820000,
        )
        val s24Ultra = createSamplePhone(
            id = "phone-samsung-galaxy-s24-ultra-256gb",
            brand = "Samsung",
            model = "Galaxy S24 Ultra",
            chipset = "Snapdragon 8 Gen 3 for Galaxy",
            antutu = 1980000,
            batteryMah = 5000,
            mainMp = 200,
            hasStylus = true,
        )

        val report = VsArenaEngine.compare(
            left = iphone,
            right = s24Ultra,
            mode = VsArenaMode.COMPONENT_VS_COMPONENT,
        )

        assertNotNull(report.performanceChampion)
        assertNotNull(report.valueChampion)
        assertTrue(report.rows.isNotEmpty(), "Comparison rows must be generated")
        assertTrue(report.rows.any { it.label.contains("Yonga Seti") })
        assertTrue(report.rows.any { it.label.contains("AnTuTu v10") })
        assertTrue(report.rows.any { it.label.contains("Batarya Kapasitesi") })
    }

    @Test
    fun testVsArenaTabletComparison() {
        val ipadM4 = createSampleTablet(
            id = "tablet-apple-ipad-pro-13-m4-256gb",
            brand = "Apple",
            model = "iPad Pro 13 (M4)",
            chipset = "Apple M4",
            antutu = 2750000,
            sizeInch = 13.0,
            stylus = "Apple Pencil Pro",
        )
        val tabS10 = createSampleTablet(
            id = "tablet-samsung-galaxy-tab-s10-ultra-256gb",
            brand = "Samsung",
            model = "Galaxy Tab S10 Ultra",
            chipset = "Dimensity 9300+",
            antutu = 2100000,
            sizeInch = 14.6,
            stylus = "Dahili S-Pen",
        )

        val report = VsArenaEngine.compare(
            left = ipadM4,
            right = tabS10,
            mode = VsArenaMode.COMPONENT_VS_COMPONENT,
        )

        assertNotNull(report.performanceChampion)
        assertTrue(report.rows.isNotEmpty())
        assertTrue(report.rows.any { it.label.contains("Yonga Seti") })
        assertTrue(report.rows.any { it.label.contains("Stylus Kalem") })
        assertTrue(report.rows.any { it.label.contains("Ekran Boyutu") })
        assertTrue(report.rows.any { it.label.contains("Batarya") })
    }

    @Test
    fun testActiveBuildIsolatesMobileDevices() {
        val emptyBuild = ActiveBuild()
        val iphone = createSamplePhone()

        val buildWithPhone = emptyBuild.withComponent(iphone)
        assertTrue(buildWithPhone.items.isEmpty(), "Phone must never be added to PC slots")

        val withoutPhone = buildWithPhone.withoutCategory(HardwareCategory.SMARTPHONE)
        assertTrue(withoutPhone.items.isEmpty())
    }

    @Test
    fun testVsArenaUiStateComponentCategoriesIncludesMobile() {
        val categories = com.panda.myapplication.presentation.vs_arena.VsArenaUiState.COMPONENT_CATEGORIES
        assertTrue(categories.contains(HardwareCategory.SMARTPHONE), "VS Arena must include SMARTPHONE in selectable categories")
        assertTrue(categories.contains(HardwareCategory.TABLET), "VS Arena must include TABLET in selectable categories")
    }
}
