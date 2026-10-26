package com.panda.myapplication

import com.panda.myapplication.data.local.seed.MobileSeedCatalog
import com.panda.myapplication.domain.engine.HardwareEnrichmentEngine
import com.panda.myapplication.domain.engine.VsArenaEngine
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.model.TabletSpec
import com.panda.myapplication.domain.repository.VsArenaMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MobileAndTabletHardwareTest {

    @Test
    fun testMobileSeedCatalogPopulated() {
        val phones = MobileSeedCatalog.getSmartphones()
        val tablets = MobileSeedCatalog.getTablets()

        assertTrue(phones.isNotEmpty(), "Smartphones catalog must not be empty")
        assertTrue(tablets.isNotEmpty(), "Tablets catalog must not be empty")

        val iphone = phones.find { it.model.contains("iPhone 16 Pro Max") }
        assertNotNull(iphone, "iPhone 16 Pro Max must exist in catalog")
        assertEquals(HardwareCategory.SMARTPHONE, iphone.category)
        assertTrue(iphone.spec is PhoneSpec)
        val iphoneSpec = iphone.spec
        assertEquals("Apple A18 Pro", iphoneSpec.chipset)
        assertEquals(1820000, iphoneSpec.antutuScore)
        assertEquals(120, iphoneSpec.refreshRateHz)
        assertTrue(iphoneSpec.hasOis)

        val s24Ultra = phones.find { it.model.contains("Galaxy S24 Ultra") }
        assertNotNull(s24Ultra, "Galaxy S24 Ultra must exist in catalog")
        val s24Spec = s24Ultra.spec as PhoneSpec
        assertEquals(200, s24Spec.mainCameraMp)
        assertEquals(5000, s24Spec.batteryMah)
        assertTrue(s24Spec.hasStylus)

        val ipadM4 = tablets.find { it.model.contains("iPad Pro 13 (M4)") }
        assertNotNull(ipadM4, "iPad Pro M4 must exist in catalog")
        assertEquals(HardwareCategory.TABLET, ipadM4.category)
        val ipadSpec = ipadM4.spec as TabletSpec
        assertEquals("Apple M4", ipadSpec.chipset)
        assertEquals(2750000, ipadSpec.antutuScore)
        assertEquals(13.0, ipadSpec.displaySizeInch)
    }

    @Test
    fun testHardwareEnrichmentEngineForMobile() {
        val iphone = MobileSeedCatalog.findById("phone-apple-iphone-16-pro-max-256gb")
        assertNotNull(iphone)

        val badges = HardwareEnrichmentEngine.getEngineeringBadges(iphone)
        assertTrue(badges.isNotEmpty(), "Phone must generate enrichment badges")
        assertTrue(badges.any { it.label.contains("Yonga Seti") && it.value.contains("Apple A18 Pro") })
        assertTrue(badges.any { it.label.contains("AnTuTu") && it.value.contains("1820000") })
        assertTrue(badges.any { it.label.contains("Ekran") && it.value.contains("120Hz") })
        assertTrue(badges.any { it.label.contains("Batarya") && it.value.contains("4685") })

        val ipad = MobileSeedCatalog.findById("tablet-apple-ipad-pro-13-m4-256gb")
        assertNotNull(ipad)
        val tabletBadges = HardwareEnrichmentEngine.getEngineeringBadges(ipad)
        assertTrue(tabletBadges.any { it.label.contains("Yonga") && it.value.contains("Apple M4") })
        assertTrue(tabletBadges.any { it.label.contains("Stylus") && it.value.contains("Apple Pencil Pro") })
        assertTrue(tabletBadges.any { it.label.contains("Ses") && it.value.contains("4 Hoparlör") })
    }

    @Test
    fun testVsArenaSmartphoneComparison() {
        val iphone = MobileSeedCatalog.findById("phone-apple-iphone-16-pro-max-256gb")!!
        val s24Ultra = MobileSeedCatalog.findById("phone-samsung-galaxy-s24-ultra-256gb")!!

        val report = VsArenaEngine.compare(
            left = iphone,
            right = s24Ultra,
            mode = VsArenaMode.COMPONENT_VS_COMPONENT,
        )

        assertNotNull(report.performanceChampion)
        assertNotNull(report.valueChampion)
        assertTrue(report.rows.isNotEmpty(), "Comparison rows must be generated")

        // Check key comparison metrics
        assertTrue(report.rows.any { it.label.contains("Yonga Seti") })
        assertTrue(report.rows.any { it.label.contains("AnTuTu v10") })
        assertTrue(report.rows.any { it.label.contains("Geekbench 6") })
        assertTrue(report.rows.any { it.label.contains("3DMark Termal Kararlılık") })
        assertTrue(report.rows.any { it.label.contains("Ana Kamera") })
        assertTrue(report.rows.any { it.label.contains("Batarya Kapasitesi") })
        assertTrue(report.rows.any { it.label.contains("Tepe Parlaklık") })
    }

    @Test
    fun testVsArenaTabletComparison() {
        val ipadM4 = MobileSeedCatalog.findById("tablet-apple-ipad-pro-13-m4-256gb")!!
        val tabS10 = MobileSeedCatalog.findById("tablet-samsung-galaxy-tab-s10-ultra-256gb")!!

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
        val iphone = MobileSeedCatalog.findById("phone-apple-iphone-16-pro-max-256gb")!!

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

    @Test
    fun testExpandedMobileCatalogScaleAndMultiImages() {
        val phones = MobileSeedCatalog.getSmartphones()
        val tablets = MobileSeedCatalog.getTablets()

        assertTrue(phones.size >= 50, "Smartphones catalog should contain at least 50 models, found: ${phones.size}")
        assertTrue(tablets.size >= 15, "Tablets catalog should contain at least 15 models, found: ${tablets.size}")

        // Test brand diversity
        val phoneBrands = phones.map { it.brand }.distinct()
        assertTrue(phoneBrands.contains("Apple"))
        assertTrue(phoneBrands.contains("Samsung"))
        assertTrue(phoneBrands.contains("Xiaomi"))
        assertTrue(phoneBrands.contains("POCO"))
        assertTrue(phoneBrands.contains("Vivo"))
        assertTrue(phoneBrands.contains("Google"))
        assertTrue(phoneBrands.contains("Nothing"))
        assertTrue(phoneBrands.contains("Honor"))
        assertTrue(phoneBrands.contains("OnePlus"))
        assertTrue(phoneBrands.contains("Realme"))
        assertTrue(phoneBrands.contains("Huawei"))
        assertTrue(phoneBrands.contains("Asus"))
        assertTrue(phoneBrands.contains("Sony"))

        // Multi-image validation
        val allItems = phones + tablets
        allItems.forEach { item ->
            assertTrue(
                item.images.size >= 2,
                "Device ${item.displayName} (${item.id.value}) must have at least 2 image angles, found: ${item.images.size}"
            )
            assertTrue(
                item.effectiveImages.isNotEmpty(),
                "Device ${item.displayName} must have non-empty effectiveImages"
            )
        }
    }
}
