package com.panda.myapplication.presentation.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.panda.myapplication.domain.model.AppCurrency
import com.panda.myapplication.domain.model.AppLanguage
import com.panda.myapplication.domain.model.HardwareCategory

interface AppStrings {
    val languageCode: String
    val isTurkish: Boolean get() = languageCode == "tr"

    // Navigation Tabs
    val tabCatalog: String
    val tabWizard: String
    val tabVsArena: String

    // Settings
    val settingsTitle: String
    val appearanceSection: String
    val appearanceSubtitle: String
    val themeDark: String
    val themeLight: String
    val themeWood: String
    val themeOled: String
    val themeCyberpunk: String
    val themeSystem: String

    val languageRegionSection: String
    val languageRegionSubtitle: String
    val languageLabel: String
    val currencyLabel: String

    val hardwarePrefsSection: String
    val hardwarePrefsSubtitle: String
    val enthusiastModeLabel: String
    val enthusiastModeDesc: String
    val tempUnitLabel: String
    val lengthUnitLabel: String
    val targetResolutionLabel: String
    val targetFpsLabel: String

    val dataStorageSection: String
    val dataStorageSubtitle: String
    val dataSaverLabel: String
    val dataSaverDesc: String
    val cacheLabel: String
    val cacheDesc: String
    val clearCacheButton: String
    val cacheClearedMessage: String

    val sourcesSection: String
    val sourcesSubtitle: String
    val sourcesGpuDesc: String
    val sourcesCpuDesc: String
    val sourcesPsuDesc: String
    val sourcesImagesDesc: String
    val sourcesPricesDesc: String

    val aboutSection: String
    val aboutSubtitle: String
    val appVersion: String
    val developer: String
    val developerName: String
    val licenses: String
    val back: String
    val selectLanguageTitle: String
    val selectCurrencyTitle: String

    // Catalog Screen
    val catalogTitle: String
    val searchPlaceholder: String
    val segmentIndividual: String
    val segmentPeripherals: String
    val segmentPrebuilt: String
    val segmentLaptop: String
    val filterButton: String
    val filterAll: String
    val marketPrice: String
    val addToVs: String
    val addToWizard: String
    val noPriceFallback: String

    // Detail Screen
    val addToVsArena: String
    val fpsMatrixTitle: String
    val fpsMatrixSubtitle: String
    val localPriceOverride: String
    val localPriceDesc: String
    val enterPrice: String

    // VS Arena Screen
    val vsArenaTitle: String
    val vsArenaSubtitle: String
    val corner1: String
    val corner2: String
    val selectPart: String
    val startSampleBattle: String
    val vsDescription: String
    val selectCorner1: String
    val selectCorner2: String

    // Wizard Screen
    val wizardTitle: String
    val wizardSubtitle: String
    val totalBudget: String
    val clearBuild: String
    // Extended Themes
    val themeValorant: String
    val themeCs2: String
    val themeGtaVice: String
    val themeWitcher: String

    // Drawer & Tool Navigation
    val drawerMainModules: String
    val drawerToolsHeader: String
    val drawerQuickCatalog: String
    val drawerQuickWizard: String
    val drawerQuickVs: String
    val closeButton: String

    // Cooling Lab
    val drawerCoolingTitle: String
    val drawerCoolingSubtitle: String
    val selectCpuCatalog: String
    val selectCoolerCatalog: String
    val coolingSafeStatus: String
    val coolingWarningStatus: String
    val coolingThrottleStatus: String

    // RAM Latency Lab
    val drawerRamTitle: String
    val drawerRamSubtitle: String

    // UPS Lab
    val drawerUpsTitle: String
    val drawerUpsSubtitle: String

    // Desk Setup Lab
    val drawerDeskTitle: String
    val drawerDeskSubtitle: String

    // Glossary
    val drawerGlossaryTitle: String
    val drawerGlossarySubtitle: String

    // Undervolt Guide
    val drawerUndervoltTitle: String
    val drawerUndervoltSubtitle: String

    // Second Hand Valuator
    val drawerValuationTitle: String
    val drawerValuationSubtitle: String

    // Detail Screen Sections
    val detailPerformancePower: String
    val detailMultiGameFps: String
    val detailTechSpecs: String
    val detailPrebuiltAnalysis: String
    val detailLaptopSpecs: String
    val detailStoreOffers: String
    val detailCommunityLedger: String

    // Game Benchmark Tabs & Reference Rig
    val gameBenchTabSimulated: String
    val gameBenchTabVerified: String
    val gameBenchReferenceRigCpu: String
    val gameBenchReferenceRigGpu: String
    val gameBenchAvgMinLabel: String
    val gameBenchCurrentHardware: String
    val gameBenchNoVerifiedYet: String
    val gameBenchSwitchToSim: String

    // Second Hand Valuation Screen
    val valuationHeroTitle: String
    val valuationHeroSubtitle: String
    val valuationNoSystem: String
    val valuationActiveParts: String
    val valuationTotalMarket: String
    val valuationNewTotal: String
    val valuationBreakdownTitle: String
    val valuationTipsTitle: String
    val valuationEmptyTitle: String
    val valuationEmptySubtitle: String
    val valuationGoToWizard: String

    // Undervolt Guide Tabs
    val uvTabCpu: String
    val uvTabGpu: String
    val uvTabRam: String
    val uvTabCmos: String

    // Extended Localization
    val activeStatusBadge: String
    val drawerSettingsSubtitle: String
    val drawerCustomPriceTitle: String
    val drawerCustomPriceDesc: String

    val valQuickCash: String
    val valMarketValue: String
    val valMintBoxed: String
    val valVerifiedDealsFormat: String
    val valTip1Title: String
    val valTip1Desc: String
    val valTip2Title: String
    val valTip2Desc: String
    val valTip3Title: String
    val valTip3Desc: String

    val glossarySearchPlaceholder: String
    val glossaryCategoryAll: String
    val glossaryCriticalMetrics: String
    val glossaryRelated: String
    val glossaryEmpty: String

    val deskMonitorTitle: String
    val deskScreenSuffix: String
    val deskPixelDensity: String
    val deskViewingDistance: String
    val deskGpuPixelLoad: String
    val deskMouseTitle: String
    val deskReportInterval: String
    val deskKeyboardTitle: String
    val deskKeyDepth: String
    val deskMechSwitch: String
    val deskHallSwitch: String
    val deskMechActuated: String
    val deskMechNotActuated: String
    val deskHallActuated: String
    val deskHallReady: String
    val deskAudioTitle: String

    val ramGuideTitle: String
    val ramLatencyWinner: String
    val ramBwWinner: String
    val ramZenRuleTitle: String
    val ramFormulaTitle: String
    val ramFreqLabel: String
    val ramCasLabel: String
    val ramTrueLatency: String
    val ramBandwidth: String

    val upsSectionWatts: String
    val upsPcWatts: String
    val upsMonitorWatts: String
    val upsRecommendedTier: String
    val upsTotalLoad: String
    val upsRawVa: String
    val upsSafetyMargin: String
    val upsRuntimeSection: String

    val coolingSection1: String
    val coolingCpuTdp: String
    val coolingCoolerArch: String
    val coolingSection2: String
    val coolingRoomTemp: String
    val coolingAirflow: String
    val coolingSection3: String
    val coolingSaturationCurve: String

    val drawerToolsSubtitle: String
    val userAgeLabel: String
    val userViewProfile: String
    val userLogout: String
    val userGuest: String
    val userNotLoggedIn: String
    val userLogin: String
    val valComponentsCountFormat: String
    val valQuickCashDesc: String
    val valMarketValueDesc: String
    val valMintBoxedDesc: String
    val valNewPriceLabel: String
    val deskPpiRetina: String
    val deskPpiBalanced: String
    val deskPpiLow: String
    val deskMouse1k: String
    val deskMouse2k: String
    val deskMouse4k: String
    val deskMouse8k: String
    val deskMechResetDelay: String
    val deskHallResetSpeed: String
    val deskAudioDacNeeded: String
    val deskAudioDacOk: String
    val deskAudioDacNeededDesc: String
    val deskAudioDacOkDesc: String
    val ramKitA: String
    val ramKitB: String
    val ramLatencyWinnerA: String
    val ramLatencyWinnerB: String
    val ramLatencyEqual: String
    val ramBwWinnerA: String
    val ramBwWinnerB: String
    val ramBwEqual: String
    val ramZenRuleDesc: String
    val upsRuntimeHeaderTier: String
    val upsRuntimeHeaderBattery: String
    val upsRuntimeHeaderTime: String
    val upsRuntimeFormat: String
    val coolingSelectedCpu: String
    val coolingCpuSpecsFormat: String
    val coolingReset: String
    val coolingRecommendedCpu: String
    val coolingSelectedCooler: String
    val coolingRecommendedCooler: String
    val coolingIdleLoad: String
    val coolingGamingLoad: String
    val coolingStressLoad: String
    val coolingWarningDesc: String
    val coolingSafeDesc: String
    val detailLoadingSpecs: String
    val detailReturnToList: String
    val detailAuthRequiredPrice: String
    val detailCustomPriceTag: String
    val detailEditPrice: String
    val detailEnterPrice: String
    val detailPrebuiltAdvantage: String
    val detailPrebuiltBreakdown: String
    val detailPrebuiltSeparatePrice: String
    val detailPrebuiltTransfer: String
    val detailGpuTgpTitle: String
    val detailAddedToWizard: String
    val detailImportedSystem: String
    val detailLiveStoreSearch: String
    val detailLiveStoreSearchSub: String
    val detailVerifiedStores: String
    val detailLiveSearch: String
    val detailImageVerifying: String
    // Hardware Categories
    val catCpu: String
    val catGpu: String
    val catRam: String
    val catMotherboard: String
    val catPsu: String
    val catStorage: String
    val catCooler: String
    val catCase: String
    val catPrebuilt: String
    val catLaptop: String
    val catMouse: String
    val catKeyboard: String
    val catHeadset: String
    val catMonitor: String

    // Custom Price Dialog
    val customPriceDialogTitle: String
    val customPriceStoreRef: String
    val customPriceOutOfStock: String
    val customPriceFieldLabel: String
    val customPriceFieldPlaceholder: String
    val customPriceErrorInvalid: String
    val customPriceNoteLabel: String
    val customPriceNotePlaceholder: String
    val customPricePresetOwn: String
    val customPricePresetUsed: String
    val customPricePresetSale: String
    val customPricePresetImport: String
    val customPriceNotice: String

    // Auth Sheet
    val authLoginTitle: String
    val authRegisterTitle: String
    val authTabLogin: String
    val authTabRegister: String
    val authEmailOrUsername: String
    val authPassword: String
    val authLoggingIn: String
    val authRegistering: String
    val authUsernameReq: String
    val authEmailReq: String
    val authPasswordReq: String
    val authPasswordMinError: String
    val authLoginFailed: String
    val authRegisterSuccess: String
    val authRegisterFailed: String

    // User Profile
    val profileNotLoggedIn: String
    val profileLoginBtn: String
    val profileLogoutBtn: String
    val profileTitle: String

    // Wizard
    val wizardSystemCardCopied: String
    val wizardTotalPrefix: String
    val wizardNoPartSelected: String
    val wizardAddPart: String
    val wizardSelect: String
    val wizardSelected: String
    val wizardInteractiveTitle: String
    val wizardNotSelected: String

    // VS Arena
    val vsCompMode: String
    val vsPrebuiltMode: String
    val vsLaptopMode: String
    val vsCorner1: String
    val vsCorner2: String
    val vsArenaTitleFormat: String

    // Community Price Ledger
    val ledgerRefresh: String
    val ledgerSubmitDealBtn: String
    val ledgerAdminLogout: String
    val ledgerReportedPrice: String
    val ledgerUserNotePrefix: String
    val ledgerProofLinkBtn: String
    val ledgerSubmitSheetTitle: String
    val ledgerPriceLabel: String
    val ledgerDealTypeSelect: String
    val ledgerProofUrlLabel: String
    val ledgerNoteFieldLabel: String
    val ledgerSubmitAction: String
}


private class AppStringsTR : AppStrings {
    override val languageCode = "tr"

    override val tabCatalog = "Keşfet & Katalog"
    override val tabWizard = "PC Sihirbazı"
    override val tabVsArena = "VS Arena"

    override val settingsTitle = "Ayarlar"
    override val appearanceSection = "Görünüm & Tema Paleti"
    override val appearanceSubtitle = "Koyu, açık, ahşap veya saf OLED stüdyo temaları"
    override val themeDark = "Obsidian"
    override val themeLight = "Slate"
    override val themeWood = "Ahşap"
    override val themeOled = "OLED"
    override val themeCyberpunk = "Cyberpunk"
    override val themeSystem = "Sistem"

    override val languageRegionSection = "Dil & Bölge"
    override val languageRegionSubtitle = "Uygulama dili ve yerel para birimi biçimlendirmesi"
    override val languageLabel = "Uygulama Dili"
    override val currencyLabel = "Para Birimi"

    override val hardwarePrefsSection = "Donanım & Sihirbaz Tercihleri"
    override val hardwarePrefsSubtitle = "Sistem toplama ve bileşen analizi kriterleri"
    override val enthusiastModeLabel = "Uzman (Geek) Modu"
    override val enthusiastModeDesc = "Detaylı VRM fazları, bellek die çipleri ve PCIe veri yollarını göster"
    override val tempUnitLabel = "Sıcaklık Birimi"
    override val lengthUnitLabel = "Ölçü & Uzunluk Birimi"
    override val targetResolutionLabel = "Hedef Çözünürlük"
    override val targetFpsLabel = "Hedef Kare Hızı (FPS)"

    override val dataStorageSection = "Veri & Depolama"
    override val dataStorageSubtitle = "İnternet bant genişliği ve yerel önbellek yönetimi"
    override val dataSaverLabel = "Mobil Veri Tasarrufu"
    override val dataSaverDesc = "Bant genişliğini korumak için ürün galerilerinde yalnızca birincil açıyı yükler"
    override val cacheLabel = "Görsel Önbelleği"
    override val cacheDesc = "Cihazda depolanan yüksek çözünürlüklü donanım görselleri"
    override val clearCacheButton = "Önbelleği Temizle"
    override val cacheClearedMessage = "Görsel önbelleği başarıyla temizlendi."

    override val sourcesSection = "Kaynakça & Doğrulama"
    override val sourcesSubtitle = "Donanım özellikleri, benchmark ve görsel veri kaynakları"
    override val sourcesGpuDesc = "TechPowerUp GPU Database, VideoCardz, GPU-Monkey, AMD & NVIDIA Beyaz Bültenleri"
    override val sourcesCpuDesc = "AMD & Intel Teknik Dokümantasyonu, Geekbench 6, Cinebench R23/2024, Puget Systems"
    override val sourcesPsuDesc = "Cybenetics Labs (ETA & LAMBDA Veritabanı), Cultists Network PSU Tier List, 80 PLUS"
    override val sourcesImagesDesc = "PCPartPicker 1600p CDN, LDLC Stüdyo Çekimleri, Üretici Resmi Basın Kitleri"
    override val sourcesPricesDesc = "PCPartPicker Global Multi-Vendor API, Akakçe, Cimri, Amazon Storefronts"

    override val aboutSection = "Uygulama Hakkında"
    override val aboutSubtitle = "Sürüm bilgisi, geliştirici ve açık kaynak lisansları"
    override val appVersion = "Sürüm"
    override val developer = "Geliştirici"
    override val developerName = "Ömer Yavuz Yiğit"
    override val licenses = "Açık Kaynak Kütüphaneleri & Lisanslar"
    override val back = "Geri"
    override val selectLanguageTitle = "Uygulama Dilini Seçin"
    override val selectCurrencyTitle = "Para Birimini Seçin"

    override val catalogTitle = "HARDWARE WIZARD"
    override val searchPlaceholder = "Parça adı, model, marka (AMD, RTX 4070, DDR5...)"
    override val segmentIndividual = "Bireysel"
    override val segmentPeripherals = "Çevre"
    override val segmentPrebuilt = "Hazır Sistem"
    override val segmentLaptop = "Laptop"
    override val filterButton = "Filtreler"
    override val filterAll = "Tümü"
    override val marketPrice = "Piyasa Fiyatı"
    override val addToVs = "+ VS"
    override val addToWizard = "+ Sihirbaza Ekle"
    override val noPriceFallback = "Stok / Fiyat Yok"

    override val addToVsArena = "VS Arenaya Ekle"
    override val fpsMatrixTitle = "12 Oyun Canlı FPS & %1 Low Matrisi"
    override val fpsMatrixSubtitle = "Rekabetçi E-Spor (5) ve Ağır AAA (7) Test Katsayıları"
    override val localPriceOverride = "Yerel Özel Fiyat Belirle"
    override val localPriceDesc = "Kupon, indirim veya 2. el fiyatınızı girerek F/P ve bütçe hesabına dahil edin"
    override val enterPrice = "Fiyat Gir"

    override val vsArenaTitle = "Kafa Kafaya Kıyaslama"
    override val vsArenaSubtitle = "CPU, GPU, Anakart, RAM, SSD, PSU, Soğutucu, Kasa"
    override val corner1 = "1. KÖŞE"
    override val corner2 = "2. KÖŞE"
    override val selectPart = "+ Parça Seç"
    override val startSampleBattle = "Örnek Kapışmayı Hemen Başlat"
    override val vsDescription = "Yukarıdaki kutulara dokunarak veya Katalogdan 'VS' butonuyla iki ürünü kafa kafaya kıyaslayabilirsiniz."
    override val selectCorner1 = "1. Köşeyi Seç"
    override val selectCorner2 = "2. Köşeyi Seç"

    override val wizardTitle = "PC Sihirbazı"
    override val wizardSubtitle = "Akıllı uyumluluk, darboğaz ve PSU güç analizi"
    override val totalBudget = "Toplam Sistem Tutarı"
    override val clearBuild = "Sistemi Sıfırla"
    override val themeValorant = "Valorant"
    override val themeCs2 = "CS2"
    override val themeGtaVice = "GTA Vice"
    override val themeWitcher = "The Witcher"

    override val drawerMainModules = "ANA MODÜLLER"
    override val drawerToolsHeader = "İNTERAKTİF MÜHENDİSLİK ARAÇLARI"
    override val drawerQuickCatalog = "Katalog"
    override val drawerQuickWizard = "Sihirbaz"
    override val drawerQuickVs = "VS Arena"
    override val closeButton = "Kapat"

    override val drawerCoolingTitle = "Termal & Soğutma Simülatörü"
    override val drawerCoolingSubtitle = "TDP yükü, ortam sıcaklığı, kasa hava akışı ve AIO/Kule doygunluk analizi"
    override val selectCpuCatalog = "Kataloğumdan İşlemci Seç"
    override val selectCoolerCatalog = "Kataloğumdan Soğutucu Seç"
    override val coolingSafeStatus = "❄️ TERMAL OLARAK GÜVENLİ & SESSİZ"
    override val coolingWarningStatus = "⚠️ SINIRDA SOĞUTMA (YÜKSEK FAN DEVRİ)"
    override val coolingThrottleStatus = "🔥 THERMAL THROTTLING RİSKİ (FREKANS KISMA)"

    override val drawerRamTitle = "RAM True Latency & Bant Genişliği"
    override val drawerRamSubtitle = "CL × 2000 / MT/s nanosaniye gecikmesi, 1% Low FPS ve X3D etkisi"

    override val drawerUpsTitle = "UPS & Akü Dayanma Boyutlandırıcı"
    override val drawerUpsSubtitle = "VA / Gerçek Watt kapasitesi, Saf Sinüs PFC uyumu ve kesintide çalışma süresi"

    override val drawerDeskTitle = "Masa & Çevre Birimleri Laboratuvarı"
    override val drawerDeskSubtitle = "Monitör PPI, 8KHz Mouse CPU yükü, Klavye Rapid Trigger & DAC eşleştirici"

    override val drawerGlossaryTitle = "Donanım & Mühendislik Kütüphanesi"
    override val drawerGlossarySubtitle = "TGP vs TDP, 3D V-Cache, ATX 3.1, Hall Effect, QD-OLED sözlüğü"

    override val drawerUndervoltTitle = "Undervolt & Optimizasyon Rehberi"
    override val drawerUndervoltSubtitle = "Intel Vmin, AMD Curve Optimizer, GPU Frekans Eğrisi & RAM Bit-Rot"

    override val drawerValuationTitle = "Sistemim 2. Elde Ne Kadar Eder?"
    override val drawerValuationSubtitle = "Doğrulanmış defter verileriyle hızlı nakit, gerçekçi pazar ve vitrin değeri"

    override val detailPerformancePower = "PERFORMANS & GÜÇ PROFİLİ"
    override val detailMultiGameFps = "12 OYUN CANLI FPS SİMÜLASYONU"
    override val detailTechSpecs = "DETAYLI TEKNİK ÖZELLİKLER"
    override val detailPrebuiltAnalysis = "HAZIR SİSTEM BİLEŞEN ANALİZİ"
    override val detailLaptopSpecs = "GAMING LAPTOP ÖZEL DETAYLARI"
    override val detailStoreOffers = "GÜVENLİ MAĞAZA VE FİYAT TEKLİFLERİ"
    override val detailCommunityLedger = "TOPLULUK DOĞRULANMIŞ FİYAT DEFTERİ"

    override val gameBenchTabSimulated = "📊 Simüle Test (Model)"
    override val gameBenchTabVerified = "🧪 Doğrulanmış Gerçek Test"
    override val gameBenchReferenceRigCpu = "🔬 Referans GPU: RTX 5090 32GB (Sıfır Darboğaz)"
    override val gameBenchReferenceRigGpu = "🔬 Referans CPU: Ryzen 7 9800X3D (Sıfır Darboğaz)"
    override val gameBenchAvgMinLabel = "Ortalama ve Minimum FPS"
    override val gameBenchCurrentHardware = "Mevcut Ürün"
    override val gameBenchNoVerifiedYet = "Bu model için henüz doğrulanmış laboratuvar testi bulunmuyor."
    override val gameBenchSwitchToSim = "Simüle Teste Geç"

    override val valuationHeroTitle = "SİSTEMİM 2. ELDE NE KADAR EDER?"
    override val valuationHeroSubtitle = "Aktif PC Sihirbazı sisteminizdeki donanımları, doğrulanmış topluluk fiyat defteri ve amortisman algoritmasıyla 3 farklı piyasa kademesinde otomatik değerler."
    override val valuationNoSystem = "Sistem Seçilmedi"
    override val valuationActiveParts = "%d Parça Aktif"
    override val valuationTotalMarket = "SİSTEM TOPLAM 2. EL PİYASA DEĞERİ"
    override val valuationNewTotal = "Sıfır Liste: %s"
    override val valuationBreakdownTitle = "PARÇA BAZLI 2. EL DEĞER DAĞILIMI"
    override val valuationTipsTitle = "2. EL SATIŞ TAKTİKLERİ & PÜF NOKTALARI"
    override val valuationEmptyTitle = "Aktif Sistem Listeniz Boş"
    override val valuationEmptySubtitle = "Sisteminizin 2. el değerini hesaplamak için PC Sihirbazı'ndan parçalarınızı seçin veya kataloğu gezin."
    override val valuationGoToWizard = "PC Sihirbazı'na Git ve Parça Ekle"
    override val uvTabCpu = "İşlemci (CPU)"
    override val uvTabGpu = "Ekran Kartı (GPU)"
    override val uvTabRam = "Bellek (RAM)"
    override val uvTabCmos = "Acil Kurtarma (CMOS)"

    override val activeStatusBadge = "AKTİF"
    override val drawerSettingsSubtitle = "Tema, 7 Dil, Para Birimi, Uzman Modu"
    override val drawerCustomPriceTitle = "💡 Özel Fiyat Motoru (Local Price Override)"
    override val drawerCustomPriceDesc = "Katalogdaki veya detay sayfasındaki herhangi bir ürünün fiyat rozetine dokunarak kendi bulduğunuz indirimli/ikinci el fiyatı girebilirsiniz."

    override val valQuickCash = "Hızlı Nakit"
    override val valMarketValue = "Piyasa Değeri"
    override val valMintBoxed = "Kutulu Vitrin"
    override val valVerifiedDealsFormat = "✓ %d doğrulanmış 2. el topluluk işlemi baz alındı"
    override val valTip1Title = "Kutu & Fatura Etkisi (+%10-15)"
    override val valTip1Desc = "Kutusu, faturası ve TR distribütör garantisi bulunan donanımlar hem çok daha hızlı satılır hem de vitrin değerinde alıcı bulur."
    override val valTip2Title = "Stres Testi Ekran Görüntüleri"
    override val valTip2Desc = "İlanınıza 15 dakikalık FurMark, Cinebench R23 ve CrystalDiskInfo SSD sağlık ekran görüntülerini eklemek alıcı güvenini 3 katına çıkarır."
    override val valTip3Title = "Temizlik ve Termal Macun"
    override val valTip3Desc = "Kasa içi kablolaması düzenli, tozları temizlenmiş ve yeni termal macun uygulanmış sistemler fotoğraflarda anında fark yaratır."

    override val glossarySearchPlaceholder = "Terim veya metrik ara (DrMOS, 12V-2x6, QD-OLED, Rapid Trigger, X3D...)"
    override val glossaryCategoryAll = "Tümü"
    override val glossaryCriticalMetrics = "Kritik Mühendislik Metrikleri"
    override val glossaryRelated = "İlgili:"
    override val glossaryEmpty = "Aradığınız kriterlere uygun mühendislik terimi bulunamadı."

    override val deskMonitorTitle = "1. MONİTÖR PPI KESKİNLİK & GPU YÜK ÇARPANI"
    override val deskScreenSuffix = "Ekran"
    override val deskPixelDensity = "Piksel Yoğunluğu"
    override val deskViewingDistance = "İdeal Bakış Mesafesi"
    override val deskGpuPixelLoad = "GPU Piksel Yükü"
    override val deskMouseTitle = "2. MOUSE POLLING RATE (1000Hz – 8000Hz) ANALİZÖRÜ"
    override val deskReportInterval = "Raporlama Aralığı: %s ms"
    override val deskKeyboardTitle = "3. KLAVYE RAPID TRIGGER (HALL EFFECT) SİMÜLATÖRÜ"
    override val deskKeyDepth = "Simüle Edilen Tuş Basım Derinliği: %s mm / 4.0 mm"
    override val deskMechSwitch = "Mekanik Switch"
    override val deskHallSwitch = "Manyetik Hall Effect"
    override val deskMechActuated = "BASILI (2.0mm Geçildi)"
    override val deskMechNotActuated = "TETİKLENMEDİ (<2.0mm)"
    override val deskHallActuated = "AKTİF (0.1mm Rapid Trigger)"
    override val deskHallReady = "HAZIR"
    override val deskAudioTitle = "4. KULAKLIK EMPEDANS (Ω) & SES KARTI EŞLEŞTİRİCİ"

    override val ramGuideTitle = "MÜHENDİSLİK KIYASLAMASI & PLATFORM REHBERİ"
    override val ramLatencyWinner = "Gecikme Galibi:"
    override val ramBwWinner = "Bant Genişliği Galibi:"
    override val ramZenRuleTitle = "AMD Zen 4 & Zen 5 (AM5) UCLK Kuralı"
    override val ramFormulaTitle = "Matematiksel Formül Referansı"
    override val ramFreqLabel = "Frekans: %d MT/s"
    override val ramCasLabel = "CAS Gecikmesi: CL%d"
    override val ramTrueLatency = "Gerçek Gecikme (True Latency)"
    override val ramBandwidth = "Çift Kanal Bant Genişliği"

    override val upsSectionWatts = "1. SİSTEM & MONİTÖR GÜÇ TÜKETİMİ (WATT)"
    override val upsPcWatts = "Kasa Oyun Yükü Güç Çekimi"
    override val upsMonitorWatts = "Monitör + Modem / Çevre Birimi Tüketimi"
    override val upsRecommendedTier = "ÖNERİLEN MİNİMUM UPS SINIFI"
    override val upsTotalLoad = "Toplam Tam Yük"
    override val upsRawVa = "Ham VA (W / 0.6)"
    override val upsSafetyMargin = "+%20 Güvenlik Paylı"
    override val upsRuntimeSection = "ELEKTRİK KESİNTİSİNDE TAHMİNİ AKÜ SÜRELERİ"

    override val coolingSection1 = "1. CPU TERMAL YÜK & SOĞUTUCU TİPİ"
    override val coolingCpuTdp = "CPU TDP & Boost Yükü"
    override val coolingCoolerArch = "Soğutucu Mimarisi & Boyutu"
    override val coolingSection2 = "2. ORTAM SICAKLIĞI & KASA HAVA AKIŞI"
    override val coolingRoomTemp = "Oda Sıcaklığı"
    override val coolingAirflow = "Kasa Fan Akışı & Pozitif Basınç"
    override val coolingSection3 = "3. TAHMİNİ SICAKLIK & TERMAL DOYGUNLUK"
    override val coolingSaturationCurve = "Soğutucu Isıl Doygunluk Grafiği"

    override val drawerToolsSubtitle = "Donanım fiziği, gecikme ve akustik simülatörleri"
    override val userAgeLabel = "Yaş"
    override val userViewProfile = "Profili Gör"
    override val userLogout = "Çıkış"
    override val userGuest = "Misafir Kullanıcı"
    override val userNotLoggedIn = "Giriş yapılmadı"
    override val userLogin = "Giriş Yap"
    override val valComponentsCountFormat = "%d Bileşen"
    override val valQuickCashDesc = "24-48 Saat İçi"
    override val valMarketValueDesc = "Ortalama 1-2 Hafta"
    override val valMintBoxedDesc = "Koleksiyoncu / Sabırlı"
    override val valNewPriceLabel = "Sıfır Fiyatı: %s"
    override val deskPpiRetina = "✅ Retina Seviyesi Keskinlik (%d PPI): Pikseller çıplak gözle seçilemez, ancak GPU üzerinde 1080p'ye kıyasla %%%d piksel işleme yükü oluşturur."
    override val deskPpiBalanced = "✅ Altın Oran (%d PPI): Hem metin netliği hem de oyun FPS verimliliği için en dengeli masaüstü yoğunluğu."
    override val deskPpiLow = "⚠️ Düşük Yoğunluk (%d PPI): %s boyutta %s yakından bakıldığında pikselli görünebilir (en az %d cm mesafeden bakılmalıdır)."
    override val deskMouse1k = "Düşük CPU Yükü (~%1) • Tüm işlemciler ve 144Hz+ monitörlerle tam uyumlu."
    override val deskMouse2k = "Dengeli E-Spor Modu (~%2.5 CPU IRQ yükü) • Kablosuz batarya ömrü ve akıcılık dengesi."
    override val deskMouse4k = "Yüksek CPU Kesme (IRQ) Yükü (~%5-8) • En az Ryzen 5 7500F / i5-13600K ve 240Hz+ monitör önerilir."
    override val deskMouse8k = "⚠️ Ekstrem 8000Hz (0.125ms): Saniyede 8.000 USB kesmesi üretir! Eski işlemcilerde FPS drop yapabilir; Ryzen 7 7800X3D/9800X3D ve 360Hz+ monitörle gerçek farkını gösterir."
    override val deskMechResetDelay = "Durmak için parmağınızı %s mm yukarı (1.8mm noktasına) kaldırmalısınız (~32ms gecikme)."
    override val deskHallResetSpeed = "Parmağınızı sadece 0.1 mm yukarı oynattığınız anda (~2ms) tuş sıfırlanır ve karakter anında durur!"
    override val deskAudioDacNeeded = "⚠️ Harici DAC / Amfi Şart! (%d Ω vs %s)"
    override val deskAudioDacOk = "✅ Doğrudan Tam Güçle Sürülebilir (%d Ω)"
    override val deskAudioDacNeededDesc = "%s (%s), %d Ω empedanslı kulaklıkta yeterli voltaj salınımı üretemez; baslar zayıflar ve maksimum ses seviyesi düşük kalır. Harici USB DAC/Amfi kullanılmalıdır."
    override val deskAudioDacOkDesc = "%s (%s), %d Ω kulaklığı harici amfiye ihtiyaç duymadan tam dinamik aralıkta besleyebilir."
    override val ramKitA = "1. BELLEK KİTİ (KİT A)"
    override val ramKitB = "2. KARŞILAŞTIRMA KİTİ (KİT B)"
    override val ramLatencyWinnerA = "Kit A (%s ns) ilk kelime tepki süresinde %s ns daha hızlı!"
    override val ramLatencyWinnerB = "Kit B (%s ns) ilk kelime tepki süresinde %s ns daha hızlı!"
    override val ramLatencyEqual = "Her iki kitin de True Latency tepki süresi birebir eşit (%s ns)!"
    override val ramBwWinnerA = "Kit A (%s GB/s) teorik bant genişliğinde +%s GB/s önde."
    override val ramBwWinnerB = "Kit B (%s GB/s) teorik bant genişliğinde +%s GB/s önde."
    override val ramBwEqual = "Her iki kit de eşit Dual-Channel bant genişliği (%s GB/s) sunuyor."
    override val ramZenRuleDesc = "AMD Ryzen 7000/9000 mimarisinde bellek denetleyici (UCLK) ve bellek saati (MCLK) 6000 MT/s hızında 1:1 senkron çalışır. 6400 MT/s ve üzerinde bellek kontrolcüsü 1:2 moduna düşerek gecikmeyi 8-10 ns artırabilir. Bu sebeple AM5 için tatlı nokta DDR5 6000 CL30'dur."
    override val upsRuntimeHeaderTier = "UPS Modeli"
    override val upsRuntimeHeaderBattery = "Akü Grubu"
    override val upsRuntimeHeaderTime = "Tahmini Süre"
    override val upsRuntimeFormat = "~%d dakika"
    override val coolingSelectedCpu = "ÖZEL SEÇİM (KATALOG): %s"
    override val coolingCpuSpecsFormat = "TDP: %dW • Gerçek PPT/MTP: %dW"
    override val coolingReset = "Sıfırla"
    override val coolingRecommendedCpu = "Önerilen Referans İşlemciler:"
    override val coolingSelectedCooler = "ÖZEL SOĞUTUCU: %s"
    override val coolingRecommendedCooler = "Önerilen Referans Soğutucular:"
    override val coolingIdleLoad = "Masaüstü Boşta (Idle)"
    override val coolingGamingLoad = "Oyun Yükü (Gaming ~%60 PPT)"
    override val coolingStressLoad = "Tam Yük Render / Cinebench (%100 PPT)"
    override val coolingWarningDesc = "❗ Mühendislik Uyarısı: %s tam yükte %dW ısı üretirken %s maksimum %dW dağıtabilir. İşlemci %d°C sınırına çarpıp saat hızını düşürecektir (Thermal Throttling)."
    override val coolingSafeDesc = "✅ Mühendislik Onayı: %s, %s işlemcisinin %dW PPT/MTP yükünü %dW ekstra güvenlik payı ile rahatça sönümler."
    override val detailLoadingSpecs = "Teknik özellikler yükleniyor..."
    override val detailReturnToList = "Listeye Dön"
    override val detailAuthRequiredPrice = "⚠️ Topluluk fiyatı veya fırsat bildirmek için lütfen önce giriş yapın."
    override val detailCustomPriceTag = "🏷️ ÖZEL FİYAT"
    override val detailEditPrice = "Düzenle ✎"
    override val detailEnterPrice = "Fiyat Gir ✎"
    override val detailPrebuiltAdvantage = "Hazır Sistem Avantaj Endeksi"
    override val detailPrebuiltBreakdown = "AKILLI KÂR / ZARAR & TOPLAMA ANALİZİ"
    override val detailPrebuiltSeparatePrice = "Parçaları Tek Tek Toplama Tutarı"
    override val detailPrebuiltTransfer = "Bu Sistemi Sihirbaza Aktar & Özelleştir"
    override val detailGpuTgpTitle = "GERÇEK GPU TGP GÜÇ DEĞERİ"
    override val detailAddedToWizard = "Sihirbaza Eklendi ✓"
    override val detailImportedSystem = "Sisteme Aktarıldı ✓"
    override val detailLiveStoreSearch = "CANLI MAĞAZA VE FİYAT ARAMASI"
    override val detailLiveStoreSearchSub = "Tek tıkla teknoloji mağazalarında anlık stok ve fiyat aratın:"
    override val detailVerifiedStores = "%d Doğrulanmış"
    override val detailLiveSearch = "Canlı Arama"
    override val detailImageVerifying = "Görsel Doğrulanıyor / Eşleştirilmedi"
    // Hardware Categories
    override val catCpu = "İşlemci (CPU)"
    override val catGpu = "Ekran Kartı (GPU)"
    override val catRam = "Bellek (RAM)"
    override val catMotherboard = "Anakart"
    override val catPsu = "Güç Kaynağı (PSU)"
    override val catStorage = "Depolama (SSD/HDD)"
    override val catCooler = "İşlemci Soğutucu"
    override val catCase = "Kasa"
    override val catPrebuilt = "OEM Hazır Sistem"
    override val catLaptop = "Gaming Laptop"
    override val catMouse = "Oyuncu Faresi"
    override val catKeyboard = "Oyuncu Klavyesi"
    override val catHeadset = "Oyuncu Kulaklığı"
    override val catMonitor = "Oyuncu Monitörü"

    // Custom Price Dialog
    override val customPriceDialogTitle = "🏷️ Yerel Özel Fiyat Belirle"
    override val customPriceStoreRef = "Mağaza Referans Fiyatı:"
    override val customPriceOutOfStock = "Stok / Fiyat Yok"
    override val customPriceFieldLabel = "Özel Fiyatınız (₺ TL)"
    override val customPriceFieldPlaceholder = "Örn: 14500"
    override val customPriceErrorInvalid = "Lütfen geçerli bir tutar girin (Örn: 5499)."
    override val customPriceNoteLabel = "Not / Kaynak (İsteğe Bağlı)"
    override val customPriceNotePlaceholder = "Örn: İkinci El, Kampanya..."
    override val customPricePresetOwn = "Kendi Aldığım Fiyat"
    override val customPricePresetUsed = "İkinci El"
    override val customPricePresetSale = "Kampanya / İndirim"
    override val customPricePresetImport = "Yurt Dışı"
    override val customPriceNotice = "Girilen özel fiyat Room veritabanına kaydedilir; hem Katalog hem VS Arena F/P hesabı hem de PC Sihirbazı toplam bütçesinde öncelikli olarak kullanılır."

    // Auth Sheet
    override val authLoginTitle = "⚡ Giriş Yap"
    override val authRegisterTitle = "🚀 Topluluğa Katıl & Kayıt Ol"
    override val authTabLogin = "Giriş Yap"
    override val authTabRegister = "Kayıt Ol"
    override val authEmailOrUsername = "E-posta veya Kullanıcı Adı"
    override val authPassword = "Şifre"
    override val authLoggingIn = "Giriş Yapılıyor..."
    override val authRegistering = "Hesap Oluşturuluyor..."
    override val authUsernameReq = "Kullanıcı Adı (Rumuz) *"
    override val authEmailReq = "E-posta Adresi *"
    override val authPasswordReq = "Şifre (En az 6 karakter) *"
    override val authPasswordMinError = "Şifre en az 6 karakter olmalıdır."
    override val authLoginFailed = "❌ Kullanıcı bulunamadı veya şifre hatalı! Lütfen bilgilerinizi kontrol edin."
    override val authRegisterSuccess = "Kayıt başarılı! Hoş geldiniz, %s!"
    override val authRegisterFailed = "Kayıt oluşturulamadı. Lütfen bilgilerinizi kontrol edin."

    // User Profile
    override val profileNotLoggedIn = "Henüz Giriş Yapmadınız"
    override val profileLoginBtn = "⚡ Giriş Yap / Kayıt Ol"
    override val profileLogoutBtn = "🚪 Hesaptan Çıkış Yap"
    override val profileTitle = "Kullanıcı Profili"

    // Wizard
    override val wizardSystemCardCopied = "⚡ Sistem Kartı panoya kopyalandı! Paylaşmaya hazır."
    override val wizardTotalPrefix = "Toplam"
    override val wizardNoPartSelected = "Parça seçilmedi"
    override val wizardAddPart = "+ Parça Ekle"
    override val wizardSelect = "Seç"
    override val wizardSelected = "Seçili"
    override val wizardInteractiveTitle = "İnteraktif PC Toplama"
    override val wizardNotSelected = "Seçilmedi"

    // VS Arena
    override val vsCompMode = "🧩 Bileşen vs Bileşen"
    override val vsPrebuiltMode = "🖥️ OEM Hazır Sistem"
    override val vsLaptopMode = "💻 Gaming Laptop"
    override val vsCorner1 = "1. Köşeyi Seç"
    override val vsCorner2 = "2. Köşeyi Seç"
    override val vsArenaTitleFormat = "%s Karşılaştırma Arenası"

    // Community Price Ledger
    override val ledgerRefresh = "🔄 Yenile"
    override val ledgerSubmitDealBtn = "+ Fiyat Bildir / Fırsat Paylaş"
    override val ledgerAdminLogout = "Admin Çıkışı"
    override val ledgerReportedPrice = "Bildirilen Fiyat"
    override val ledgerUserNotePrefix = "💬 Kullanıcı Notu:"
    override val ledgerProofLinkBtn = "🔗 Kanıt Linkini İncele / Aç"
    override val ledgerSubmitSheetTitle = "⚡ Topluluk Fiyatı / Fırsatı Bildir"
    override val ledgerPriceLabel = "Fiyat (TL) *"
    override val ledgerDealTypeSelect = "İşlem Türü Seçin *"
    override val ledgerProofUrlLabel = "Kanıt Linki (Zorunlu) *"
    override val ledgerNoteFieldLabel = "Açıklama / Deneyim Notu"
    override val ledgerSubmitAction = "🚀 Fiyatı Bildir (Yönetici Onayına Sun)"
}
val StringsTR: AppStrings = AppStringsTR()


private class AppStringsEN : AppStrings {
    override val languageCode = "en"

    override val tabCatalog = "Explore & Catalog"
    override val tabWizard = "PC Builder"
    override val tabVsArena = "VS Arena"

    override val settingsTitle = "Settings"
    override val appearanceSection = "Appearance & Theme Palette"
    override val appearanceSubtitle = "Dark studio, crisp slate, cyber-wood, or OLED midnight themes"
    override val themeDark = "Obsidian"
    override val themeLight = "Slate"
    override val themeWood = "Walnut"
    override val themeOled = "OLED"
    override val themeCyberpunk = "Cyberpunk"
    override val themeSystem = "System"

    override val languageRegionSection = "Language & Region"
    override val languageRegionSubtitle = "App interface language and localized currency formatting"
    override val languageLabel = "App Language"
    override val currencyLabel = "Currency"

    override val hardwarePrefsSection = "Hardware & Wizard Preferences"
    override val hardwarePrefsSubtitle = "PC building criteria and component analysis targets"
    override val enthusiastModeLabel = "Enthusiast (Geek) Mode"
    override val enthusiastModeDesc = "Display advanced VRM power phases, memory die types, and PCIe lane bifurcation"
    override val tempUnitLabel = "Temperature Unit"
    override val lengthUnitLabel = "Measurement Unit"
    override val targetResolutionLabel = "Target Resolution"
    override val targetFpsLabel = "Target Frame Rate (FPS)"

    override val dataStorageSection = "Data & Storage"
    override val dataStorageSubtitle = "Network bandwidth efficiency and local cache management"
    override val dataSaverLabel = "Mobile Data Saver"
    override val dataSaverDesc = "Conserves bandwidth by loading only the primary hero angle in product galleries"
    override val cacheLabel = "Image Cache"
    override val cacheDesc = "High-resolution hardware product photography cached locally"
    override val clearCacheButton = "Clear Cache"
    override val cacheClearedMessage = "Image cache cleared successfully."

    override val sourcesSection = "Sources & Verification"
    override val sourcesSubtitle = "Authoritative specifications, benchmarks, and image providers"
    override val sourcesGpuDesc = "TechPowerUp GPU Database, VideoCardz, GPU-Monkey, AMD & NVIDIA Technical Whitepapers"
    override val sourcesCpuDesc = "AMD & Intel Official Technical Specs, Geekbench 6, Cinebench R23/2024, Puget Systems"
    override val sourcesPsuDesc = "Cybenetics Labs (ETA & LAMBDA Certifications), Cultists Network Tier List, 80 PLUS"
    override val sourcesImagesDesc = "PCPartPicker 1600p Studio CDN, LDLC Studio Galleries, Manufacturer Press Kits"
    override val sourcesPricesDesc = "PCPartPicker Global Retailer API, Akakçe, Cimri, Amazon Storefronts"

    override val aboutSection = "About Hardware Wizard"
    override val aboutSubtitle = "Version details, creator info, and open-source licenses"
    override val appVersion = "Version"
    override val developer = "Developer"
    override val developerName = "Ömer Yavuz Yiğit"
    override val licenses = "Open Source Libraries & Licenses"
    override val back = "Back"
    override val selectLanguageTitle = "Select Application Language"
    override val selectCurrencyTitle = "Select Preferred Currency"

    override val catalogTitle = "HARDWARE WIZARD"
    override val searchPlaceholder = "Part name, model, brand (AMD, RTX 4070, DDR5...)"
    override val segmentIndividual = "Components"
    override val segmentPeripherals = "Peripherals"
    override val segmentPrebuilt = "Prebuilts"
    override val segmentLaptop = "Laptops"
    override val filterButton = "Filters"
    override val filterAll = "All"
    override val marketPrice = "Market Price"
    override val addToVs = "+ VS"
    override val addToWizard = "+ Add to Build"
    override val noPriceFallback = "Out of Stock / No Price"

    override val addToVsArena = "Add to VS Arena"
    override val fpsMatrixTitle = "12-Game Live FPS & 1% Low Matrix"
    override val fpsMatrixSubtitle = "Competitive E-Sports (5) & Heavy AAA (7) Benchmark Multipliers"
    override val localPriceOverride = "Set Custom Local Price"
    override val localPriceDesc = "Include coupons, discounts, or 2nd-hand pricing in value & budget analytics"
    override val enterPrice = "Enter Price"

    override val vsArenaTitle = "Head-to-Head Comparison"
    override val vsArenaSubtitle = "CPU, GPU, Motherboard, RAM, SSD, PSU, Cooler, Case"
    override val corner1 = "CORNER 1"
    override val corner2 = "CORNER 2"
    override val selectPart = "+ Select Part"
    override val startSampleBattle = "Launch Sample Matchup"
    override val vsDescription = "Tap the corners above or click 'VS' from the catalog to compare two products side-by-side."
    override val selectCorner1 = "Select Corner 1"
    override val selectCorner2 = "Select Corner 2"

    override val wizardTitle = "PC Builder"
    override val wizardSubtitle = "Intelligent compatibility, bottleneck & PSU wattage analytics"
    override val totalBudget = "Total System Cost"
    override val clearBuild = "Reset Build"
    override val themeValorant = "Valorant"
    override val themeCs2 = "CS2"
    override val themeGtaVice = "GTA Vice"
    override val themeWitcher = "The Witcher"

    override val drawerMainModules = "CORE MODULES"
    override val drawerToolsHeader = "INTERACTIVE ENGINEERING TOOLS"
    override val drawerQuickCatalog = "Catalog"
    override val drawerQuickWizard = "PC Builder"
    override val drawerQuickVs = "VS Arena"
    override val closeButton = "Close"

    override val drawerCoolingTitle = "Thermal & Cooling Simulator"
    override val drawerCoolingSubtitle = "TDP load, ambient temp, case airflow and AIO thermal saturation"
    override val selectCpuCatalog = "Select CPU from Catalog"
    override val selectCoolerCatalog = "Select Cooler from Catalog"
    override val coolingSafeStatus = "❄️ THERMALLY SAFE & SILENT"
    override val coolingWarningStatus = "⚠️ NEAR LIMIT (HIGH FAN RPM)"
    override val coolingThrottleStatus = "🔥 THERMAL THROTTLING RISK (CLOCK DROP)"

    override val drawerRamTitle = "RAM True Latency & Bandwidth"
    override val drawerRamSubtitle = "CL × 2000 / MT/s true nanosecond latency, 1% Low FPS and X3D impact"

    override val drawerUpsTitle = "UPS & Battery Sizing Calculator"
    override val drawerUpsSubtitle = "VA / True Watts rating, Pure Sine Wave active PFC and outage runtime"

    override val drawerDeskTitle = "Desk & Peripherals Lab"
    override val drawerDeskSubtitle = "Monitor PPI, 8KHz Mouse CPU IRQ load, Keyboard Rapid Trigger & DAC"

    override val drawerGlossaryTitle = "Hardware & Engineering Glossary"
    override val drawerGlossarySubtitle = "TGP vs TDP, 3D V-Cache, ATX 3.1, Hall Effect, QD-OLED technical terms"

    override val drawerUndervoltTitle = "Undervolt & Tuning Guide"
    override val drawerUndervoltSubtitle = "Intel Vmin, AMD Curve Optimizer, GPU Voltage/Freq Curve & RAM Bit-Rot"

    override val drawerValuationTitle = "Used PC Valuation Calculator"
    override val drawerValuationSubtitle = "Quick cash, fair market, and listing price based on verified sales"

    override val detailPerformancePower = "PERFORMANCE & POWER PROFILE"
    override val detailMultiGameFps = "12-GAME LIVE FPS SIMULATION"
    override val detailTechSpecs = "DETAILED TECHNICAL SPECIFICATIONS"
    override val detailPrebuiltAnalysis = "PREBUILT COMPONENT BREAKDOWN"
    override val detailLaptopSpecs = "GAMING LAPTOP SPECIFICATIONS"
    override val detailStoreOffers = "VERIFIED STORE OFFERS & DEALS"
    override val detailCommunityLedger = "COMMUNITY VERIFIED PRICE LEDGER"

    override val gameBenchTabSimulated = "📊 Simulated FPS (Model)"
    override val gameBenchTabVerified = "🧪 Verified Real Benchmark"
    override val gameBenchReferenceRigCpu = "🔬 Reference GPU: RTX 5090 32GB (Zero Bottleneck)"
    override val gameBenchReferenceRigGpu = "🔬 Reference CPU: Ryzen 7 9800X3D (Zero Bottleneck)"
    override val gameBenchAvgMinLabel = "Average & Minimum FPS"
    override val gameBenchCurrentHardware = "Current Hardware"
    override val gameBenchNoVerifiedYet = "No verified lab test available for this model yet."
    override val gameBenchSwitchToSim = "Switch to Simulation"

    override val valuationHeroTitle = "WHAT IS MY RIG WORTH USED?"
    override val valuationHeroSubtitle = "Automatically calculates your PC Wizard build across 3 market tiers using verified community ledger data and depreciation models."
    override val valuationNoSystem = "No System Selected"
    override val valuationActiveParts = "%d Active Parts"
    override val valuationTotalMarket = "TOTAL USED MARKET VALUE"
    override val valuationNewTotal = "MSRP New: %s"
    override val valuationBreakdownTitle = "COMPONENT VALUE BREAKDOWN"
    override val valuationTipsTitle = "USED MARKET SELLING TIPS & TRICKS"
    override val valuationEmptyTitle = "Your Active Build is Empty"
    override val valuationEmptySubtitle = "To estimate second-hand value, choose components in PC Wizard or explore the catalog."
    override val valuationGoToWizard = "Open PC Wizard & Add Parts"
    override val uvTabCpu = "Processor (CPU)"
    override val uvTabGpu = "Graphics Card (GPU)"
    override val uvTabRam = "Memory (RAM)"
    override val uvTabCmos = "Emergency Reset (CMOS)"

    override val activeStatusBadge = "ACTIVE"
    override val drawerSettingsSubtitle = "Themes, 7 Languages, Currency, Geek Mode"
    override val drawerCustomPriceTitle = "💡 Custom Price Engine (Local Override)"
    override val drawerCustomPriceDesc = "Tap the price badge of any product in the catalog or details to input your custom deal or used price."

    override val valQuickCash = "Quick Cash"
    override val valMarketValue = "Fair Market"
    override val valMintBoxed = "Mint Boxed"
    override val valVerifiedDealsFormat = "✓ Based on %d verified community sales"
    override val valTip1Title = "Original Box & Invoice (+10-15%)"
    override val valTip1Desc = "Hardware with original box, receipt, and active warranty sells much faster and reaches top listing price."
    override val valTip2Title = "Stress Test Benchmarks"
    override val valTip2Desc = "Adding 15-minute FurMark, Cinebench, and CrystalDiskInfo SMART health screenshots triples buyer confidence."
    override val valTip3Title = "Deep Cleaning & Fresh Paste"
    override val valTip3Desc = "Clean dust-free chassis, neat cable management, and fresh thermal compound stand out instantly in listing photos."

    override val glossarySearchPlaceholder = "Search terms or metrics (DrMOS, 12V-2x6, QD-OLED, Rapid Trigger, X3D...)"
    override val glossaryCategoryAll = "All"
    override val glossaryCriticalMetrics = "Critical Engineering Metrics"
    override val glossaryRelated = "Related:"
    override val glossaryEmpty = "No engineering terms matched your search criteria."

    override val deskMonitorTitle = "1. MONITOR PPI SHARPNESS & GPU LOAD FACTOR"
    override val deskScreenSuffix = "Display"
    override val deskPixelDensity = "Pixel Density"
    override val deskViewingDistance = "Ideal Viewing Distance"
    override val deskGpuPixelLoad = "GPU Pixel Load"
    override val deskMouseTitle = "2. MOUSE POLLING RATE (1000Hz – 8000Hz) ANALYZER"
    override val deskReportInterval = "Report Interval: %s ms"
    override val deskKeyboardTitle = "3. KEYBOARD RAPID TRIGGER (HALL EFFECT) SIMULATOR"
    override val deskKeyDepth = "Simulated Travel Depth: %s mm / 4.0 mm"
    override val deskMechSwitch = "Mechanical Switch"
    override val deskHallSwitch = "Magnetic Hall Effect"
    override val deskMechActuated = "ACTUATED (>2.0mm)"
    override val deskMechNotActuated = "NOT ACTUATED (<2.0mm)"
    override val deskHallActuated = "ACTIVE (0.1mm Rapid Trigger)"
    override val deskHallReady = "READY"
    override val deskAudioTitle = "4. HEADPHONE IMPEDANCE (Ω) & AUDIO DAC MATCHER"

    override val ramGuideTitle = "ENGINEERING COMPARISON & PLATFORM GUIDE"
    override val ramLatencyWinner = "Latency Winner:"
    override val ramBwWinner = "Bandwidth Winner:"
    override val ramZenRuleTitle = "AMD Zen 4 & Zen 5 (AM5) 1:1 UCLK Rule"
    override val ramFormulaTitle = "Mathematical Formula Reference"
    override val ramFreqLabel = "Frequency: %d MT/s"
    override val ramCasLabel = "CAS Latency: CL%d"
    override val ramTrueLatency = "True Latency"
    override val ramBandwidth = "Dual-Channel Bandwidth"

    override val upsSectionWatts = "1. SYSTEM & MONITOR POWER CONSUMPTION (WATTS)"
    override val upsPcWatts = "PC Gaming Load Power"
    override val upsMonitorWatts = "Monitor + Router / Peripheral Draw"
    override val upsRecommendedTier = "RECOMMENDED MINIMUM UPS TIER"
    override val upsTotalLoad = "Total Peak Load"
    override val upsRawVa = "Raw VA (W / 0.6)"
    override val upsSafetyMargin = "+20% Headroom Included"
    override val upsRuntimeSection = "ESTIMATED BATTERY RUNTIME IN OUTAGES"

    override val coolingSection1 = "1. CPU THERMAL LOAD & COOLER TYPE"
    override val coolingCpuTdp = "CPU TDP & Boost Load"
    override val coolingCoolerArch = "Cooler Architecture & Size"
    override val coolingSection2 = "2. AMBIENT TEMP & CASE AIRFLOW"
    override val coolingRoomTemp = "Room Temperature"
    override val coolingAirflow = "Case Fan Airflow & Positive Pressure"
    override val coolingSection3 = "3. ESTIMATED TEMPERATURE & SATURATION"
    override val coolingSaturationCurve = "Cooler Thermal Saturation Curve"

    override val drawerToolsSubtitle = "Hardware physics, latency, and acoustic simulators"
    override val userAgeLabel = "Age"
    override val userViewProfile = "View Profile"
    override val userLogout = "Sign Out"
    override val userGuest = "Guest User"
    override val userNotLoggedIn = "Not signed in"
    override val userLogin = "Sign In"
    override val valComponentsCountFormat = "%d Components"
    override val valQuickCashDesc = "Within 24-48 Hours"
    override val valMarketValueDesc = "Average 1-2 Weeks"
    override val valMintBoxedDesc = "Collector / Patient Sale"
    override val valNewPriceLabel = "MSRP / New: %s"
    override val deskPpiRetina = "✅ Retina Clarity (%d PPI): Pixels undetectable to naked eye, creates %%%d GPU pixel rendering load vs 1080p."
    override val deskPpiBalanced = "✅ Golden Ratio (%d PPI): The most balanced desktop density for crisp text and high-FPS gaming."
    override val deskPpiLow = "⚠️ Low Density (%d PPI): %s at %s may look pixelated up close (view from at least %d cm)."
    override val deskMouse1k = "Low CPU Load (~1%) • Fully compatible with all CPUs and 144Hz+ monitors."
    override val deskMouse2k = "Balanced Esports Mode (~2.5% CPU IRQ load) • Ideal balance between wireless battery life and fluid tracking."
    override val deskMouse4k = "High CPU IRQ Load (~5-8%) • Recommend at least Ryzen 5 7500F / i5-13600K and 240Hz+ display."
    override val deskMouse8k = "⚠️ Extreme 8000Hz (0.125ms): 8,000 USB interrupts/sec! May cause stutter on older CPUs; requires Ryzen 7 7800X3D/9800X3D and 360Hz+ display to shine."
    override val deskMechResetDelay = "Finger must lift %s mm back up (to 1.8mm point) to reset key (~32ms delay)."
    override val deskHallResetSpeed = "Key resets the instant you release by just 0.1mm (~2ms), halting character movement immediately!"
    override val deskAudioDacNeeded = "⚠️ Dedicated DAC / Amp Required! (%d Ω vs %s)"
    override val deskAudioDacOk = "✅ Fully Driven with Sufficient Power (%d Ω)"
    override val deskAudioDacNeededDesc = "%s (%s) cannot produce sufficient voltage swing for %d Ω headphones; bass response and volume will be degraded. An external USB DAC/Amp is recommended."
    override val deskAudioDacOkDesc = "%s (%s) can fully power %d Ω headphones across their complete dynamic range without an external amplifier."
    override val ramKitA = "1. MEMORY KIT (KIT A)"
    override val ramKitB = "2. COMPARISON KIT (KIT B)"
    override val ramLatencyWinnerA = "Kit A (%s ns) is %s ns faster in initial word access response time!"
    override val ramLatencyWinnerB = "Kit B (%s ns) is %s ns faster in initial word access response time!"
    override val ramLatencyEqual = "Both memory kits have identical True Latency response times (%s ns)!"
    override val ramBwWinnerA = "Kit A (%s GB/s) leads theoretical bandwidth by +%s GB/s."
    override val ramBwWinnerB = "Kit B (%s GB/s) leads theoretical bandwidth by +%s GB/s."
    override val ramBwEqual = "Both kits deliver identical Dual-Channel bandwidth (%s GB/s)."
    override val ramZenRuleDesc = "On AMD Ryzen 7000/9000 CPUs, memory controller (UCLK) and memory clock (MCLK) run synchronous 1:1 up to 6000 MT/s. At 6400+ MT/s, the controller drops to 1:2 divider, adding 8-10 ns of latency. Hence DDR5-6000 CL30 remains the ultimate AM5 sweet spot."
    override val upsRuntimeHeaderTier = "UPS Model"
    override val upsRuntimeHeaderBattery = "Battery Bank"
    override val upsRuntimeHeaderTime = "Est. Runtime"
    override val upsRuntimeFormat = "~%d min"
    override val coolingSelectedCpu = "CUSTOM SELECTED (CATALOG): %s"
    override val coolingCpuSpecsFormat = "TDP: %dW • Actual PPT/MTP: %dW"
    override val coolingReset = "Reset"
    override val coolingRecommendedCpu = "Recommended Reference CPUs:"
    override val coolingSelectedCooler = "CUSTOM COOLER: %s"
    override val coolingRecommendedCooler = "Recommended Reference Coolers:"
    override val coolingIdleLoad = "Desktop Idle"
    override val coolingGamingLoad = "Gaming Load (~60% PPT)"
    override val coolingStressLoad = "Full Load Render / Stress (100% PPT)"
    override val coolingWarningDesc = "❗ Engineering Alert: %s dissipates %dW at full load while %s is rated for max %dW. The CPU will hit its %d°C TjMax and throttle clock speeds."
    override val coolingSafeDesc = "✅ Engineering Verified: %s comfortably handles the %dW load of %s with a %dW thermal safety headroom."
    override val detailLoadingSpecs = "Loading technical specifications..."
    override val detailReturnToList = "Back to List"
    override val detailAuthRequiredPrice = "⚠️ Please sign in to submit community prices or deals."
    override val detailCustomPriceTag = "🏷️ CUSTOM PRICE"
    override val detailEditPrice = "Edit ✎"
    override val detailEnterPrice = "Enter Price ✎"
    override val detailPrebuiltAdvantage = "Prebuilt Value Advantage"
    override val detailPrebuiltBreakdown = "SMART VALUE & COMPONENT BREAKDOWN"
    override val detailPrebuiltSeparatePrice = "Individual Component Total"
    override val detailPrebuiltTransfer = "Import to PC Wizard & Customize"
    override val detailGpuTgpTitle = "TRUE GPU TGP WATTAGE"
    override val detailAddedToWizard = "Added to Wizard ✓"
    override val detailImportedSystem = "Imported to Build ✓"
    override val detailLiveStoreSearch = "LIVE STORE & PRICE SEARCH"
    override val detailLiveStoreSearchSub = "One-click live stock and price check across major stores:"
    override val detailVerifiedStores = "%d Verified"
    override val detailLiveSearch = "Live Search"
    override val detailImageVerifying = "Visual Verifying / Unmatched"
    // Hardware Categories
    override val catCpu = "Processor (CPU)"
    override val catGpu = "Graphics Card (GPU)"
    override val catRam = "Memory (RAM)"
    override val catMotherboard = "Motherboard"
    override val catPsu = "Power Supply (PSU)"
    override val catStorage = "Storage (SSD/HDD)"
    override val catCooler = "CPU Cooler"
    override val catCase = "Chassis / Case"
    override val catPrebuilt = "Prebuilt PC"
    override val catLaptop = "Gaming Laptop"
    override val catMouse = "Gaming Mouse"
    override val catKeyboard = "Gaming Keyboard"
    override val catHeadset = "Gaming Headset"
    override val catMonitor = "Gaming Monitor"

    // Custom Price Dialog
    override val customPriceDialogTitle = "🏷️ Set Local Custom Price"
    override val customPriceStoreRef = "Store Reference Price:"
    override val customPriceOutOfStock = "Out of Stock / No Price"
    override val customPriceFieldLabel = "Your Custom Price"
    override val customPriceFieldPlaceholder = "e.g. 14500"
    override val customPriceErrorInvalid = "Please enter a valid amount (e.g. 5499)."
    override val customPriceNoteLabel = "Note / Source (Optional)"
    override val customPriceNotePlaceholder = "e.g. Used, Amazon Discount..."
    override val customPricePresetOwn = "My Purchase Price"
    override val customPricePresetUsed = "Used / Pre-owned"
    override val customPricePresetSale = "Deal / Promotion"
    override val customPricePresetImport = "Imported"
    override val customPriceNotice = "Custom price is saved locally; prioritized across Catalog, VS Arena value scoring, and PC Wizard budget calculations."

    // Auth Sheet
    override val authLoginTitle = "⚡ Sign In"
    override val authRegisterTitle = "🚀 Join Community & Register"
    override val authTabLogin = "Sign In"
    override val authTabRegister = "Register"
    override val authEmailOrUsername = "Email or Username"
    override val authPassword = "Password"
    override val authLoggingIn = "Signing In..."
    override val authRegistering = "Creating Account..."
    override val authUsernameReq = "Username *"
    override val authEmailReq = "Email Address *"
    override val authPasswordReq = "Password (At least 6 chars) *"
    override val authPasswordMinError = "Password must be at least 6 characters."
    override val authLoginFailed = "❌ User not found or invalid credentials! Please verify your info."
    override val authRegisterSuccess = "Registration successful! Welcome, %s!"
    override val authRegisterFailed = "Registration failed. Please check your information."

    // User Profile
    override val profileNotLoggedIn = "You Are Not Signed In Yet"
    override val profileLoginBtn = "⚡ Sign In / Register"
    override val profileLogoutBtn = "🚪 Sign Out"
    override val profileTitle = "User Profile"

    // Wizard
    override val wizardSystemCardCopied = "⚡ System Card copied to clipboard! Ready to share."
    override val wizardTotalPrefix = "Total"
    override val wizardNoPartSelected = "No component selected"
    override val wizardAddPart = "+ Add Component"
    override val wizardSelect = "Select"
    override val wizardSelected = "Selected"
    override val wizardInteractiveTitle = "Interactive PC Builder"
    override val wizardNotSelected = "Not Selected"

    // VS Arena
    override val vsCompMode = "🧩 Component vs Component"
    override val vsPrebuiltMode = "🖥️ Prebuilt System"
    override val vsLaptopMode = "💻 Gaming Laptop"
    override val vsCorner1 = "Select Corner 1"
    override val vsCorner2 = "Select Corner 2"
    override val vsArenaTitleFormat = "%s Comparison Arena"

    // Community Price Ledger
    override val ledgerRefresh = "🔄 Refresh"
    override val ledgerSubmitDealBtn = "+ Submit Price / Share Deal"
    override val ledgerAdminLogout = "Admin Logout"
    override val ledgerReportedPrice = "Reported Price"
    override val ledgerUserNotePrefix = "💬 User Note:"
    override val ledgerProofLinkBtn = "🔗 View / Open Proof Link"
    override val ledgerSubmitSheetTitle = "⚡ Submit Community Deal"
    override val ledgerPriceLabel = "Price (TL) *"
    override val ledgerDealTypeSelect = "Select Transaction Type *"
    override val ledgerProofUrlLabel = "Proof Link (Required) *"
    override val ledgerNoteFieldLabel = "Description / Experience Note"
    override val ledgerSubmitAction = "🚀 Submit Deal (Pending Review)"
}
val StringsEN: AppStrings = AppStringsEN()


private class AppStringsDE : AppStrings {
    override val languageCode = "de"

    override val tabCatalog = "Katalog & Entdecken"
    override val tabWizard = "PC-Konfigurator"
    override val tabVsArena = "VS Arena"

    override val settingsTitle = "Einstellungen"
    override val appearanceSection = "Erscheinungsbild & Farbpalette"
    override val appearanceSubtitle = "Dunkles Studio, klares Slate, Cyber-Wood oder reines OLED"
    override val themeDark = "Obsidian"
    override val themeLight = "Slate"
    override val themeWood = "Walnuss"
    override val themeOled = "OLED"
    override val themeCyberpunk = "Cyberpunk"
    override val themeSystem = "System"

    override val languageRegionSection = "Sprache & Region"
    override val languageRegionSubtitle = "App-Sprache und regionale Währungsformatierung"
    override val languageLabel = "Sprache"
    override val currencyLabel = "Währung"

    override val hardwarePrefsSection = "Hardware & PC-Konfigurator"
    override val hardwarePrefsSubtitle = "Kriterien für Systembau und Komponentenanalyse"
    override val enthusiastModeLabel = "Enthusiasten-Modus"
    override val enthusiastModeDesc = "Detaillierte VRM-Phasen, Speicher-Die-Hersteller und PCIe-Lanes anzeigen"
    override val tempUnitLabel = "Temperatureinheit"
    override val lengthUnitLabel = "Maßeinheit"
    override val targetResolutionLabel = "Zielauflösung"
    override val targetFpsLabel = "Ziel-Bildwiederholrate (FPS)"

    override val dataStorageSection = "Daten & Speicher"
    override val dataStorageSubtitle = "Bandbreiteneffizienz und lokale Zwischenspeicherverwaltung"
    override val dataSaverLabel = "Datensparmodus"
    override val dataSaverDesc = "Lädt in Galerien nur das Hauptproduktfoto zur Schonung des Datenvolumens"
    override val cacheLabel = "Bilder-Cache"
    override val cacheDesc = "Lokal zwischengespeicherte hochauflösende Hardware-Produktfotos"
    override val clearCacheButton = "Cache leeren"
    override val cacheClearedMessage = "Bilder-Cache wurde erfolgreich geleert."

    override val sourcesSection = "Quellennachweis & Verifikation"
    override val sourcesSubtitle = "Technische Spezifikationen, Benchmark- und Bilddatenquellen"
    override val sourcesGpuDesc = "TechPowerUp GPU-Datenbank, VideoCardz, GPU-Monkey, AMD & NVIDIA Dokumentation"
    override val sourcesCpuDesc = "AMD & Intel Technische Spezifikationen, Geekbench 6, Cinebench, Puget Systems"
    override val sourcesPsuDesc = "Cybenetics Labs (ETA & LAMBDA), Cultists Network PSU-Rangliste, 80 PLUS"
    override val sourcesImagesDesc = "PCPartPicker 1600p CDN, LDLC Produktgalerien, Offizielle Hersteller-Pressekits"
    override val sourcesPricesDesc = "PCPartPicker Globale Händler-API, Akakçe, Cimri, Amazon"

    override val aboutSection = "Über Hardware Wizard"
    override val aboutSubtitle = "Versionshinweise, Entwickler und Open-Source-Lizenzen"
    override val appVersion = "Version"
    override val developer = "Entwickler"
    override val developerName = "Ömer Yavuz Yiğit"
    override val licenses = "Open-Source-Bibliotheken & Lizenzen"
    override val back = "Zurück"
    override val selectLanguageTitle = "Sprache auswählen"
    override val selectCurrencyTitle = "Währung auswählen"

    override val catalogTitle = "HARDWARE WIZARD"
    override val searchPlaceholder = "Bauteilname, Modell, Marke (AMD, RTX 4070...)"
    override val segmentIndividual = "Komponenten"
    override val segmentPeripherals = "Peripherie"
    override val segmentPrebuilt = "Komplett-PCs"
    override val segmentLaptop = "Laptops"
    override val filterButton = "Filter"
    override val filterAll = "Alle"
    override val marketPrice = "Marktpreis"
    override val addToVs = "+ VS"
    override val addToWizard = "+ Zum Build hinzufügen"
    override val noPriceFallback = "Nicht lieferbar / Kein Preis"

    override val addToVsArena = "In VS Arena vergleichen"
    override val fpsMatrixTitle = "12-Spiele Live FPS & 1% Low Matrix"
    override val fpsMatrixSubtitle = "E-Sports (5) & AAA-Blockbuster (7) Benchmark-Koeffizienten"
    override val localPriceOverride = "Eigenen Preis festlegen"
    override val localPriceDesc = "Rabatte oder Gebrauchtpreise in Preis-Leistungs-Analysen einbeziehen"
    override val enterPrice = "Preis eingeben"

    override val vsArenaTitle = "Direktvergleich"
    override val vsArenaSubtitle = "CPU, GPU, Mainboard, RAM, SSD, Netzteil, Kühler, Gehäuse"
    override val corner1 = "ECKE 1"
    override val corner2 = "ECKE 2"
    override val selectPart = "+ Teil wählen"
    override val startSampleBattle = "Beispielduell starten"
    override val vsDescription = "Tippe auf die Ecken oder klicke 'VS' im Katalog, um zwei Produkte zu vergleichen."
    override val selectCorner1 = "Ecke 1 wählen"
    override val selectCorner2 = "Ecke 2 wählen"

    override val wizardTitle = "PC-Konfigurator"
    override val wizardSubtitle = "Intelligente Kompatibilitäts- und Netzteil-Leistungsanalyse"
    override val totalBudget = "Gesamtpreis des Systems"
    override val clearBuild = "Konfiguration zurücksetzen"
    override val themeValorant = "Valorant"
    override val themeCs2 = "CS2"
    override val themeGtaVice = "GTA Vice"
    override val themeWitcher = "The Witcher"

    override val drawerMainModules = "HAUPTMODULE"
    override val drawerToolsHeader = "INTERAKTIVE INGENIEUR-TOOLS"
    override val drawerQuickCatalog = "Katalog"
    override val drawerQuickWizard = "PC-Konfigurator"
    override val drawerQuickVs = "VS-Arena"
    override val closeButton = "Schließen"

    override val drawerCoolingTitle = "Thermik- & Kühlungs-Simulator"
    override val drawerCoolingSubtitle = "TDP-Last, Umgebungstemperatur, Gehäuse-Airflow und AIO-Sättigung"
    override val selectCpuCatalog = "CPU aus Katalog wählen"
    override val selectCoolerCatalog = "Kühler aus Katalog wählen"
    override val coolingSafeStatus = "❄️ THERMISCH SICHER & LEISE"
    override val coolingWarningStatus = "⚠️ GRENZWERTIG (HOHE LÜFTERDREHZAHL)"
    override val coolingThrottleStatus = "🔥 THERMISCHES THROTTLING-RISIKO"

    override val drawerRamTitle = "RAM True Latency & Bandbreite"
    override val drawerRamSubtitle = "CL × 2000 / MT/s Nanosekunden-Latenz, 1% Low FPS und X3D-Einfluss"

    override val drawerUpsTitle = "USV- & Batterie-Laufzeitrechner"
    override val drawerUpsSubtitle = "VA / Echte Watt-Kapazität, Reiner Sinus PFC und Überbrückungszeit"

    override val drawerDeskTitle = "Schreibtisch- & Peripherie-Labor"
    override val drawerDeskSubtitle = "Monitor-PPI, 8KHz-Maus-CPU-Last, Rapid Trigger & DAC-Anpassung"

    override val drawerGlossaryTitle = "Hardware- & Ingenieur-Glossar"
    override val drawerGlossarySubtitle = "TGP vs TDP, 3D V-Cache, ATX 3.1, Hall Effect, QD-OLED Begriffe"

    override val drawerUndervoltTitle = "Undervolting & Tuning-Leitfaden"
    override val drawerUndervoltSubtitle = "Intel Vmin, AMD Curve Optimizer, GPU-Spannungskurve & RAM Bit-Rot"

    override val drawerValuationTitle = "Gebraucht-PC Wertermittlung"
    override val drawerValuationSubtitle = "Schneller Barwert, realistischer Marktwert und Angebotspreis"

    override val detailPerformancePower = "LEISTUNGS- & STROMPROFIL"
    override val detailMultiGameFps = "12-SPIELE LIVE-FPS-SIMULATION"
    override val detailTechSpecs = "DETAILLIERTE TECHNISCHE DATEN"
    override val detailPrebuiltAnalysis = "KOMPONENTEN-ANALYSE FERTIG-PC"
    override val detailLaptopSpecs = "GAMING-LAPTOP SPEZIFIKATIONEN"
    override val detailStoreOffers = "VERIFIZIERTE SHOP-ANGEBOTE"
    override val detailCommunityLedger = "COMMUNITY-VERIFIZIERTES PREISBUCH"

    override val gameBenchTabSimulated = "📊 Simulierte FPS (Modell)"
    override val gameBenchTabVerified = "🧪 Verifizierter Praxistest"
    override val gameBenchReferenceRigCpu = "🔬 Referenz-GPU: RTX 5090 32GB (Kein Bottleneck)"
    override val gameBenchReferenceRigGpu = "🔬 Referenz-CPU: Ryzen 7 9800X3D (Kein Bottleneck)"
    override val gameBenchAvgMinLabel = "Durchschnittliche und Minimale FPS"
    override val gameBenchCurrentHardware = "Aktuelles Modell"
    override val gameBenchNoVerifiedYet = "Für dieses Modell liegt noch kein verifizierter Labortest vor."
    override val gameBenchSwitchToSim = "Zur Simulation wechseln"

    override val valuationHeroTitle = "WAS IST MEIN PC GEBRAUCHT WERT?"
    override val valuationHeroSubtitle = "Ermittelt automatisch den Wert Ihres PC-Konfigurator-Builds in 3 Marktstufen basierend auf verifizierten Verkäufen."
    override val valuationNoSystem = "Kein System ausgewählt"
    override val valuationActiveParts = "%d Teile aktiv"
    override val valuationTotalMarket = "GESAMTER GEBRAUCHTMARKTWERT"
    override val valuationNewTotal = "Neupreis: %s"
    override val valuationBreakdownTitle = "KOMPONENTEN-PREISAUFTEILUNG"
    override val valuationTipsTitle = "TIPPS FÜR DEN GEBRAUCHTVERKAUF"
    override val valuationEmptyTitle = "Ihre Systemliste ist leer"
    override val valuationEmptySubtitle = "Wählen Sie Komponenten im PC-Konfigurator oder durchsuchen Sie den Katalog, um den Gebrauchtpreis zu ermitteln."
    override val valuationGoToWizard = "Zum PC-Konfigurator & Teile hinzufügen"
    override val uvTabCpu = "Prozessor (CPU)"
    override val uvTabGpu = "Grafikkarte (GPU)"
    override val uvTabRam = "Arbeitsspeicher (RAM)"
    override val uvTabCmos = "Notfall-Reset (CMOS)"

    override val activeStatusBadge = "AKTIV"
    override val drawerSettingsSubtitle = "Design, 7 Sprachen, Währung, Geek-Modus"
    override val drawerCustomPriceTitle = "💡 Eigener Preismotor (Local Override)"
    override val drawerCustomPriceDesc = "Tippen Sie auf das Preisschild eines Produkts, um Ihren individuellen Rabatt- oder Gebrauchtpreis einzugeben."

    override val valQuickCash = "Schneller Barwert"
    override val valMarketValue = "Marktwert"
    override val valMintBoxed = "OVP Vitrine"
    override val valVerifiedDealsFormat = "✓ Basiert auf %d verifizierten Community-Verkäufen"
    override val valTip1Title = "OVP & Rechnungsvorteil (+10-15%)"
    override val valTip1Desc = "Hardware mit OVP, Rechnung und Garantie verkauft sich schneller und erzielt Spitzenpreise."
    override val valTip2Title = "Stresstest-Nachweise"
    override val valTip2Desc = "15-minütige FurMark-, Cinebench- und CrystalDiskInfo-Screenshots verdreifachen das Käufervertrauen."
    override val valTip3Title = "Reinigung & Frische Wärmeleitpaste"
    override val valTip3Desc = "Staubfreie Gehäuse, sauberes Kabelmanagement und frische Wärmeleitpaste fallen in Fotos sofort positiv auf."

    override val glossarySearchPlaceholder = "Begriff oder Metrik suchen (DrMOS, 12V-2x6, QD-OLED, Rapid Trigger, X3D...)"
    override val glossaryCategoryAll = "Alle"
    override val glossaryCriticalMetrics = "Kritische Kennzahlen"
    override val glossaryRelated = "Verwandt:"
    override val glossaryEmpty = "Keine Fachbegriffe für Ihre Suchkriterien gefunden."

    override val deskMonitorTitle = "1. MONITOR-PPI-SCHÄRFE & GPU-LASTFAKTOR"
    override val deskScreenSuffix = "Display"
    override val deskPixelDensity = "Pixeldichte"
    override val deskViewingDistance = "Idealer Sehabstand"
    override val deskGpuPixelLoad = "GPU-Pixellast"
    override val deskMouseTitle = "2. MAUS-POLLING-RATE (1000Hz – 8000Hz) ANALYSATOR"
    override val deskReportInterval = "Berichtsintervall: %s ms"
    override val deskKeyboardTitle = "3. TASTATUR RAPID TRIGGER (HALL EFFECT) SIMULATOR"
    override val deskKeyDepth = "Simulierte Hubtiefe: %s mm / 4.0 mm"
    override val deskMechSwitch = "Mechanischer Schalter"
    override val deskHallSwitch = "Magnetischer Hall-Effekt"
    override val deskMechActuated = "BETÄTIGT (>2.0mm)"
    override val deskMechNotActuated = "NICHT BETÄTIGT (<2.0mm)"
    override val deskHallActuated = "AKTIV (0.1mm Rapid Trigger)"
    override val deskHallReady = "BEREIT"
    override val deskAudioTitle = "4. KOPFHÖRER-IMPEDANZ (Ω) & AUDIO-DAC-ABGLEICH"

    override val ramGuideTitle = "INGENIEUR-VERGLEICH & PLATTFORM-LEITFADEN"
    override val ramLatencyWinner = "Latenz-Sieger:"
    override val ramBwWinner = "Bandbreiten-Sieger:"
    override val ramZenRuleTitle = "AMD Zen 4 & Zen 5 (AM5) 1:1 UCLK Regel"
    override val ramFormulaTitle = "Mathematische Formel-Referenz"
    override val ramFreqLabel = "Frequenz: %d MT/s"
    override val ramCasLabel = "CAS-Latenz: CL%d"
    override val ramTrueLatency = "Echte Latenz (True Latency)"
    override val ramBandwidth = "Dual-Channel-Bandbreite"

    override val upsSectionWatts = "1. SYSTEM- & MONITOR-STROMVERBRAUCH (WATT)"
    override val upsPcWatts = "PC-Gaming-Lastleistung"
    override val upsMonitorWatts = "Monitor + Router / Peripherie-Last"
    override val upsRecommendedTier = "EMPFOHLENE MINDEST-USV-KLASSE"
    override val upsTotalLoad = "Gesamte Spitzenlast"
    override val upsRawVa = "Basis-VA (W / 0.6)"
    override val upsSafetyMargin = "+20% Sicherheitsreserve"
    override val upsRuntimeSection = "GESCHÄTZTE BATTERIELAUFZEIT BEI STROMAUSFALL"

    override val coolingSection1 = "1. CPU-WÄRMELAST & KÜHLER-TYP"
    override val coolingCpuTdp = "CPU TDP & Boost-Last"
    override val coolingCoolerArch = "Kühler-Architektur & Größe"
    override val coolingSection2 = "2. UMGEBUNGSTEMPERATUR & GEHÄUSE-LUFTSTROM"
    override val coolingRoomTemp = "Raumtemperatur"
    override val coolingAirflow = "Gehäuse-Luftstrom & Überdruck"
    override val coolingSection3 = "3. ERWARTETE TEMPERATUR & WÄRMESÄTTIGUNG"
    override val coolingSaturationCurve = "Kühler-Wärmesättigungskurve"

    override val drawerToolsSubtitle = "Hardware-Physik, Latenz und Akustik-Simulatoren"
    override val userAgeLabel = "Alter"
    override val userViewProfile = "Profil anzeigen"
    override val userLogout = "Abmelden"
    override val userGuest = "Gastbenutzer"
    override val userNotLoggedIn = "Nicht angemeldet"
    override val userLogin = "Anmelden"
    override val valComponentsCountFormat = "%d Komponenten"
    override val valQuickCashDesc = "Innerhalb 24-48 Stunden"
    override val valMarketValueDesc = "Durchschnittlich 1-2 Wochen"
    override val valMintBoxedDesc = "Sammler / Geduldiger Verkauf"
    override val valNewPriceLabel = "Neupreis: %s"
    override val deskPpiRetina = "✅ Retina-Schärfe (%d PPI): Einzelne Pixel sind mit bloßem Auge unsichtbar, erfordert %%%d GPU-Last im Vergleich zu 1080p."
    override val deskPpiBalanced = "✅ Goldener Schnitt (%d PPI): Ausgewogenste Desktop-Pixeldichte für scharfen Text und hohe FPS-Leistung."
    override val deskPpiLow = "⚠️ Geringe Dichte (%d PPI): %s bei %s kann aus der Nähe pixelig wirken (Mindestabstand %d cm)."
    override val deskMouse1k = "Geringe CPU-Last (~1%) • Vollständig kompatibel mit allen CPUs und 144Hz+ Monitoren."
    override val deskMouse2k = "Ausgewogener E-Sport-Modus (~2,5% CPU IRQ) • Perfekte Balance aus Akkulaufzeit und Abtastflüssigkeit."
    override val deskMouse4k = "Hohe CPU-Interrupt-Last (~5-8%) • Mindestens Ryzen 5 7500F / i5-13600K und 240Hz+ Monitor empfohlen."
    override val deskMouse8k = "⚠️ Extremes 8000Hz (0,125ms): 8.000 USB-Interrupts/Sekunde! Kann auf älteren CPUs Ruckler erzeugen; ideal mit Ryzen 7 7800X3D und 360Hz+ Monitor."
    override val deskMechResetDelay = "Finger muss %s mm nach oben angehoben werden (bis 1,8mm), um Taste zurückzusetzen (~32ms Verzögerung)."
    override val deskHallResetSpeed = "Die Taste wird sofort bei nur 0,1mm Aufwärtsbewegung zurückgesetzt (~2ms) – Spielfigur stoppt augenblicklich!"
    override val deskAudioDacNeeded = "⚠️ Dedizierter DAC / Verstärker erforderlich! (%d Ω vs %s)"
    override val deskAudioDacOk = "✅ Ausreichend Leistung bei direkter Ansteuerung (%d Ω)"
    override val deskAudioDacNeededDesc = "%s (%s) kann für %d-Ω-Kopfhörer keinen ausreichenden Spannungshub liefern; Bässe und Dynamik werden beeinträchtigt. Ein externer USB-DAC/Verstärker wird empfohlen."
    override val deskAudioDacOkDesc = "%s (%s) kann %d-Ω-Kopfhörer über den gesamten Dynamikbereich ohne zusätzlichen Verstärker versorgen."
    override val ramKitA = "1. SPEICHERKIT (KIT A)"
    override val ramKitB = "2. VERGLEICHSKIT (KIT B)"
    override val ramLatencyWinnerA = "Kit A (%s ns) ist um %s ns schneller bei der ersten Reaktionszeit!"
    override val ramLatencyWinnerB = "Kit B (%s ns) ist um %s ns schneller bei der ersten Reaktionszeit!"
    override val ramLatencyEqual = "Beide Speicherkits haben exakt identische Latenzzeiten (%s ns)!"
    override val ramBwWinnerA = "Kit A (%s GB/s) führt bei der Bandbreite um +%s GB/s."
    override val ramBwWinnerB = "Kit B (%s GB/s) führt bei der Bandbreite um +%s GB/s."
    override val ramBwEqual = "Beide Kits bieten dieselbe Dual-Channel-Bandbreite (%s GB/s)."
    override val ramZenRuleDesc = "Bei AMD Ryzen 7000/9000 laufen Speichercontroller (UCLK) und Takt (MCLK) bis 6000 MT/s im synchronen 1:1-Modus. Ab 6400 MT/s schaltet der Teiler auf 1:2 um (+8-10 ns Latenz). Daher ist DDR5-6000 CL30 der Sweet Spot für AM5."
    override val upsRuntimeHeaderTier = "UPS-Modell"
    override val upsRuntimeHeaderBattery = "Batteriepaket"
    override val upsRuntimeHeaderTime = "Geschätzte Laufzeit"
    override val upsRuntimeFormat = "~%d Min."
    override val coolingSelectedCpu = "BENUTZERDEFINIERT (KATALOG): %s"
    override val coolingCpuSpecsFormat = "TDP: %dW • Reale PPT/MTP: %dW"
    override val coolingReset = "Zurücksetzen"
    override val coolingRecommendedCpu = "Empfohlene Referenz-CPUs:"
    override val coolingSelectedCooler = "BENUTZERDEFINIERTER KÜHLER: %s"
    override val coolingRecommendedCooler = "Empfohlene Referenz-Kühler:"
    override val coolingIdleLoad = "Desktop Leerlauf (Idle)"
    override val coolingGamingLoad = "Gaming-Last (~60% PPT)"
    override val coolingStressLoad = "Volllast-Rendering / Stresstest (100% PPT)"
    override val coolingWarningDesc = "❗ Technischer Warnhinweis: %s erzeugt unter Volllast %dW, während %s maximal %dW abführen kann. Die CPU erreicht %d°C und drosselt den Takt."
    override val coolingSafeDesc = "✅ Technisch bestätigt: %s bewältigt die %dW-Last des %s mit einer Sicherheitsreserve von %dW problemlos."
    override val detailLoadingSpecs = "Technische Daten werden geladen..."
    override val detailReturnToList = "Zurück zur Liste"
    override val detailAuthRequiredPrice = "⚠️ Bitte melden Sie sich an, um Community-Preise oder Angebote zu melden."
    override val detailCustomPriceTag = "🏷️ EIGENER PREIS"
    override val detailEditPrice = "Bearbeiten ✎"
    override val detailEnterPrice = "Preis eingeben ✎"
    override val detailPrebuiltAdvantage = "Komplettsystem-Preisvorteil"
    override val detailPrebuiltBreakdown = "SMARTE KOSTEN- & KOMPONENTENANALYSE"
    override val detailPrebuiltSeparatePrice = "Einzelteil-Gesamtpreis"
    override val detailPrebuiltTransfer = "In PC-Konfigurator importieren & anpassen"
    override val detailGpuTgpTitle = "ECHTE GPU-TGP-LEISTUNG"
    override val detailAddedToWizard = "Zum Konfigurator hinzugefügt ✓"
    override val detailImportedSystem = "In System übernommen ✓"
    override val detailLiveStoreSearch = "LIVE-STORE & PREISVERGLEICH"
    override val detailLiveStoreSearchSub = "Ein-Klick-Suche nach Verfügbarkeit und Preisen in führenden Stores:"
    override val detailVerifiedStores = "%d Verifiziert"
    override val detailLiveSearch = "Live-Suche"
    override val detailImageVerifying = "Bild wird überprüft / Nicht zugeordnet"
    // Hardware Categories
    override val catCpu = "Prozessor (CPU)"
    override val catGpu = "Grafikkarte (GPU)"
    override val catRam = "Arbeitsspeicher (RAM)"
    override val catMotherboard = "Mainboard"
    override val catPsu = "Netzteil (PSU)"
    override val catStorage = "Speicher (SSD/HDD)"
    override val catCooler = "CPU-Kühler"
    override val catCase = "Gehäuse"
    override val catPrebuilt = "Komplett-PC"
    override val catLaptop = "Gaming-Laptop"
    override val catMouse = "Gaming-Maus"
    override val catKeyboard = "Gaming-Tastatur"
    override val catHeadset = "Gaming-Headset"
    override val catMonitor = "Gaming-Monitor"

    // Custom Price Dialog
    override val customPriceDialogTitle = "🏷️ Eigenen Wunschpreis festlegen"
    override val customPriceStoreRef = "Shop-Referenzpreis:"
    override val customPriceOutOfStock = "Nicht vorrätig / Kein Preis"
    override val customPriceFieldLabel = "Ihr Wunschpreis"
    override val customPriceFieldPlaceholder = "z.B. 14500"
    override val customPriceErrorInvalid = "Bitte einen gültigen Betrag eingeben (z.B. 5499)."
    override val customPriceNoteLabel = "Notiz / Quelle (Optional)"
    override val customPriceNotePlaceholder = "z.B. Gebraucht, Angebot..."
    override val customPricePresetOwn = "Eigener Kaufpreis"
    override val customPricePresetUsed = "Gebraucht"
    override val customPricePresetSale = "Aktion / Rabatt"
    override val customPricePresetImport = "Importware"
    override val customPriceNotice = "Der Wunschpreis wird lokal gespeichert und im Katalog, im Preis-Leistungs-Vergleich sowie im PC-Konfigurator priorisiert berücksichtigt."

    // Auth Sheet
    override val authLoginTitle = "⚡ Anmelden"
    override val authRegisterTitle = "🚀 Community beitreten & Registrieren"
    override val authTabLogin = "Anmelden"
    override val authTabRegister = "Registrieren"
    override val authEmailOrUsername = "E-Mail oder Benutzername"
    override val authPassword = "Passwort"
    override val authLoggingIn = "Wird angemeldet..."
    override val authRegistering = "Konto wird erstellt..."
    override val authUsernameReq = "Benutzername *"
    override val authEmailReq = "E-Mail-Adresse *"
    override val authPasswordReq = "Passwort (Mind. 6 Zeichen) *"
    override val authPasswordMinError = "Das Passwort muss mindestens 6 Zeichen lang sein."
    override val authLoginFailed = "❌ Benutzer nicht gefunden oder falsches Passwort! Bitte Daten prüfen."
    override val authRegisterSuccess = "Erfolgreich registriert! Willkommen, %s!"
    override val authRegisterFailed = "Registrierung fehlgeschlagen. Bitte Angaben überprüfen."

    // User Profile
    override val profileNotLoggedIn = "Sie sind noch nicht angemeldet"
    override val profileLoginBtn = "⚡ Anmelden / Registrieren"
    override val profileLogoutBtn = "🚪 Abmelden"
    override val profileTitle = "Benutzerprofil"

    // Wizard
    override val wizardSystemCardCopied = "⚡ System-Karte in die Zwischenablage kopiert! Bereit zum Teilen."
    override val wizardTotalPrefix = "Gesamt"
    override val wizardNoPartSelected = "Keine Komponente ausgewählt"
    override val wizardAddPart = "+ Komponente hinzufügen"
    override val wizardSelect = "Wählen"
    override val wizardSelected = "Ausgewählt"
    override val wizardInteractiveTitle = "Interaktiver PC-Konfigurator"
    override val wizardNotSelected = "Nicht gewählt"

    // VS Arena
    override val vsCompMode = "🧩 Komponente vs Komponente"
    override val vsPrebuiltMode = "🖥️ Komplett-PC"
    override val vsLaptopMode = "💻 Gaming-Laptop"
    override val vsCorner1 = "Ecke 1 wählen"
    override val vsCorner2 = "Ecke 2 wählen"
    override val vsArenaTitleFormat = "%s Vergleichs-Arena"

    // Community Price Ledger
    override val ledgerRefresh = "🔄 Aktualisieren"
    override val ledgerSubmitDealBtn = "+ Preis melden / Deal teilen"
    override val ledgerAdminLogout = "Admin Abmelden"
    override val ledgerReportedPrice = "Gemeldeter Preis"
    override val ledgerUserNotePrefix = "💬 Benutzer-Notiz:"
    override val ledgerProofLinkBtn = "🔗 Beleglink prüfen / öffnen"
    override val ledgerSubmitSheetTitle = "⚡ Community-Deal melden"
    override val ledgerPriceLabel = "Preis (TL) *"
    override val ledgerDealTypeSelect = "Transaktionsart wählen *"
    override val ledgerProofUrlLabel = "Nachweislink (Erforderlich) *"
    override val ledgerNoteFieldLabel = "Beschreibung / Erfahrungsbericht"
    override val ledgerSubmitAction = "🚀 Deal melden (Wird geprüft)"
}
val StringsDE: AppStrings = AppStringsDE()


private class AppStringsES : AppStrings {
    override val languageCode = "es"

    override val tabCatalog = "Catálogo"
    override val tabWizard = "Configurador"
    override val tabVsArena = "VS Arena"

    override val settingsTitle = "Ajustes"
    override val appearanceSection = "Apariencia y Paleta"
    override val appearanceSubtitle = "Temas oscuro estudio, slate claro, madera cyber o negro OLED puro"
    override val themeDark = "Obsidian"
    override val themeLight = "Slate"
    override val themeWood = "Nogal"
    override val themeOled = "OLED"
    override val themeCyberpunk = "Cyberpunk"
    override val themeSystem = "Sistema"

    override val languageRegionSection = "Idioma y Región"
    override val languageRegionSubtitle = "Idioma de la app y formato regional de moneda"
    override val languageLabel = "Idioma de la aplicación"
    override val currencyLabel = "Moneda"

    override val hardwarePrefsSection = "Preferencias de Hardware"
    override val hardwarePrefsSubtitle = "Objetivos de ensamblaje de PC y análisis de componentes"
    override val enthusiastModeLabel = "Modo Entusiasta (Geek)"
    override val enthusiastModeDesc = "Muestra fases VRM avanzadas, tipo de die de memoria y bifurcación PCIe"
    override val tempUnitLabel = "Unidad de Temperatura"
    override val lengthUnitLabel = "Unidad de Medida"
    override val targetResolutionLabel = "Resolución Objetivo"
    override val targetFpsLabel = "Tasa de Cuadros (FPS)"

    override val dataStorageSection = "Datos y Almacenamiento"
    override val dataStorageSubtitle = "Eficiencia de ancho de banda y gestión de caché local"
    override val dataSaverLabel = "Ahorro de Datos Móviles"
    override val dataSaverDesc = "Carga únicamente la foto principal para ahorrar tráfico de red"
    override val cacheLabel = "Caché de Imágenes"
    override val cacheDesc = "Fotografías de hardware de alta resolución guardadas en el dispositivo"
    override val clearCacheButton = "Borrar Caché"
    override val cacheClearedMessage = "Caché de imágenes borrada con éxito."

    override val sourcesSection = "Fuentes y Verificación"
    override val sourcesSubtitle = "Proveedores de especificaciones técnicas, pruebas y fotografías"
    override val sourcesGpuDesc = "Base de datos TechPowerUp, VideoCardz, GPU-Monkey, Documentación AMD y NVIDIA"
    override val sourcesCpuDesc = "Especificaciones oficiales AMD e Intel, Geekbench 6, Cinebench, Puget Systems"
    override val sourcesPsuDesc = "Cybenetics Labs (Certificaciones ETA y LAMBDA), Cultists Network Tier List, 80 PLUS"
    override val sourcesImagesDesc = "PCPartPicker 1600p CDN, Galerías LDLC, Kits de prensa de fabricantes"
    override val sourcesPricesDesc = "API global de tiendas PCPartPicker, Akakçe, Cimri, Amazon"

    override val aboutSection = "Acerca de la App"
    override val aboutSubtitle = "Detalles de versión, autor y licencias libres"
    override val appVersion = "Versión"
    override val developer = "Desarrollador"
    override val developerName = "Ömer Yavuz Yiğit"
    override val licenses = "Librerías de Código Abierto y Licencias"
    override val back = "Volver"
    override val selectLanguageTitle = "Seleccionar Idioma"
    override val selectCurrencyTitle = "Seleccionar Moneda"

    override val catalogTitle = "HARDWARE WIZARD"
    override val searchPlaceholder = "Nombre de pieza, modelo, marca (AMD, RTX 4070...)"
    override val segmentIndividual = "Componentes"
    override val segmentPeripherals = "Periféricos"
    override val segmentPrebuilt = "Equipos OEM"
    override val segmentLaptop = "Portátiles"
    override val filterButton = "Filtros"
    override val filterAll = "Todos"
    override val marketPrice = "Precio de Mercado"
    override val addToVs = "+ VS"
    override val addToWizard = "+ Añadir al Build"
    override val noPriceFallback = "Agotado / Sin Precio"

    override val addToVsArena = "Añadir a VS Arena"
    override val fpsMatrixTitle = "Matriz FPS y 1% Low en 12 Juegos"
    override val fpsMatrixSubtitle = "Multiplicadores E-Sports (5) y Títulos AAA Pesados (7)"
    override val localPriceOverride = "Definir Precio Local"
    override val localPriceDesc = "Introduce cupones o precios de 2ª mano para el cálculo de coste y calidad"
    override val enterPrice = "Introducir Precio"

    override val vsArenaTitle = "Comparación Cara a Cara"
    override val vsArenaSubtitle = "CPU, GPU, Placa Base, RAM, SSD, PSU, Disipador, Caja"
    override val corner1 = "ESQUINA 1"
    override val corner2 = "ESQUINA 2"
    override val selectPart = "+ Seleccionar"
    override val startSampleBattle = "Iniciar Duelo de Ejemplo"
    override val vsDescription = "Toca las esquinas o pulsa 'VS' en el catálogo para comparar dos productos."
    override val selectCorner1 = "Elegir Esquina 1"
    override val selectCorner2 = "Elegir Esquina 2"

    override val wizardTitle = "Configurador PC"
    override val wizardSubtitle = "Análisis inteligente de compatibilidad y fuente de alimentación"
    override val totalBudget = "Coste Total del Sistema"
    override val clearBuild = "Reiniciar Ensamble"
    override val themeValorant = "Valorant"
    override val themeCs2 = "CS2"
    override val themeGtaVice = "GTA Vice"
    override val themeWitcher = "The Witcher"

    override val drawerMainModules = "MÓDULOS PRINCIPALES"
    override val drawerToolsHeader = "HERRAMIENTAS DE INGENIERÍA"
    override val drawerQuickCatalog = "Catálogo"
    override val drawerQuickWizard = "Constructor PC"
    override val drawerQuickVs = "VS Arena"
    override val closeButton = "Cerrar"

    override val drawerCoolingTitle = "Simulador Térmico y Refrigeración"
    override val drawerCoolingSubtitle = "Carga TDP, temperatura ambiente, flujo de aire y análisis AIO"
    override val selectCpuCatalog = "Elegir CPU del catálogo"
    override val selectCoolerCatalog = "Elegir disipador del catálogo"
    override val coolingSafeStatus = "❄️ TÉRMICAMENTE SEGURO Y SILENCIOSO"
    override val coolingWarningStatus = "⚠️ AL LÍMITE (ALTAS RPM DE VENTILADOR)"
    override val coolingThrottleStatus = "🔥 RIESGO DE ESTRANGULAMIENTO TÉRMICO"

    override val drawerRamTitle = "Latencia Real y Ancho de Banda RAM"
    override val drawerRamSubtitle = "Latencia en nanosegundos CL × 2000 / MT/s, 1% Low FPS e impacto X3D"

    override val drawerUpsTitle = "Calculadora de Autonomía y SAI/UPS"
    override val drawerUpsSubtitle = "VA / Vatios reales, compatibilidad Onda Sinusoidal Pura PFC y autonomía"

    override val drawerDeskTitle = "Laboratorio de Periféricos y Escritorio"
    override val drawerDeskSubtitle = "PPI de monitor, carga CPU ratón 8KHz, Rapid Trigger de teclado y DAC"

    override val drawerGlossaryTitle = "Glosario de Hardware e Ingeniería"
    override val drawerGlossarySubtitle = "TGP vs TDP, 3D V-Cache, ATX 3.1, Hall Effect, QD-OLED términos técnicos"

    override val drawerUndervoltTitle = "Guía de Undervolt y Optimización"
    override val drawerUndervoltSubtitle = "Intel Vmin, AMD Curve Optimizer, curva de frecuencia GPU y Bit-Rot"

    override val drawerValuationTitle = "¿Cuánto Vale mi PC Usado?"
    override val drawerValuationSubtitle = "Efectivo rápido, mercado justo y precio de catálogo con datos verificados"

    override val detailPerformancePower = "PERFIL DE RENDIMIENTO Y POTENCIA"
    override val detailMultiGameFps = "SIMULACIÓN DE FPS EN 12 JUEGOS"
    override val detailTechSpecs = "ESPECIFICACIONES TÉCNICAS DETALLADAS"
    override val detailPrebuiltAnalysis = "ANÁLISIS DE PC PREMONTADO"
    override val detailLaptopSpecs = "DETALLES DE PORTÁTIL GAMING"
    override val detailStoreOffers = "OFERTAS DE TIENDAS VERIFICADAS"
    override val detailCommunityLedger = "LIBRO DE PRECIOS DE LA COMUNIDAD"

    override val gameBenchTabSimulated = "📊 FPS Simulados (Modelo)"
    override val gameBenchTabVerified = "🧪 Benchmark Real Verificado"
    override val gameBenchReferenceRigCpu = "🔬 GPU de Referencia: RTX 5090 32GB (Cero Cuello de Botella)"
    override val gameBenchReferenceRigGpu = "🔬 CPU de Referencia: Ryzen 7 9800X3D (Cero Cuello de Botella)"
    override val gameBenchAvgMinLabel = "FPS Promedio y Mínimo"
    override val gameBenchCurrentHardware = "Hardware Actual"
    override val gameBenchNoVerifiedYet = "Aún no hay pruebas de laboratorio verificadas para este modelo."
    override val gameBenchSwitchToSim = "Cambiar a Simulación"

    override val valuationHeroTitle = "¿CUÁNTO VALE MI PC USADO?"
    override val valuationHeroSubtitle = "Calcula automáticamente el valor de tu ensamble en 3 niveles de mercado usando transacciones verificadas y modelos de amortización."
    override val valuationNoSystem = "Ningún sistema seleccionado"
    override val valuationActiveParts = "%d piezas activas"
    override val valuationTotalMarket = "VALOR TOTAL EN EL MERCADO USADO"
    override val valuationNewTotal = "Precio nuevo: %s"
    override val valuationBreakdownTitle = "DESGLOSE DE VALOR POR COMPONENTE"
    override val valuationTipsTitle = "CONSEJOS PARA VENDER DE SEGUNDA MANO"
    override val valuationEmptyTitle = "Tu lista de sistema está vacía"
    override val valuationEmptySubtitle = "Para calcular el valor usado, selecciona componentes en el Asistente PC o explora el catálogo."
    override val valuationGoToWizard = "Ir al Asistente PC y Añadir Piezas"
    override val uvTabCpu = "Procesador (CPU)"
    override val uvTabGpu = "Tarjeta Gráfica (GPU)"
    override val uvTabRam = "Memoria (RAM)"
    override val uvTabCmos = "Rescate de Emergencia (CMOS)"

    override val activeStatusBadge = "ACTIVO"
    override val drawerSettingsSubtitle = "Temas, 7 idiomas, moneda, modo geek"
    override val drawerCustomPriceTitle = "💡 Motor de Precios Personalizados"
    override val drawerCustomPriceDesc = "Toca la etiqueta de precio de cualquier producto para introducir un precio rebajado o de segunda mano."

    override val valQuickCash = "Efectivo Rápido"
    override val valMarketValue = "Valor de Mercado"
    override val valMintBoxed = "Caja Impecable"
    override val valVerifiedDealsFormat = "✓ Basado en %d ventas comunitarias verificadas"
    override val valTip1Title = "Caja y Factura Original (+10-15%)"
    override val valTip1Desc = "Los componentes con caja original y factura se venden más rápido y al precio máximo de catálogo."
    override val valTip2Title = "Capturas de Test de Estrés"
    override val valTip2Desc = "Incluir capturas de FurMark, Cinebench y salud SSD en CrystalDiskInfo triplica la confianza del comprador."
    override val valTip3Title = "Limpieza y Pasta Térmica Fresca"
    override val valTip3Desc = "Un equipo sin polvo, con cables ordenados y pasta térmica nueva destaca de inmediato en las fotos del anuncio."

    override val glossarySearchPlaceholder = "Buscar término o métrica (DrMOS, 12V-2x6, QD-OLED, Rapid Trigger, X3D...)"
    override val glossaryCategoryAll = "Todos"
    override val glossaryCriticalMetrics = "Métricas Clave de Ingeniería"
    override val glossaryRelated = "Relacionado:"
    override val glossaryEmpty = "No se encontraron términos técnicos con ese criterio."

    override val deskMonitorTitle = "1. NITIDEZ PPI DEL MONITOR Y FACTOR DE CARGA GPU"
    override val deskScreenSuffix = "Pantalla"
    override val deskPixelDensity = "Densidad de Píxeles"
    override val deskViewingDistance = "Distancia de Visión Ideal"
    override val deskGpuPixelLoad = "Carga de Píxeles GPU"
    override val deskMouseTitle = "2. ANALIZADOR DE POLLING RATE DEL RATÓN (1000Hz – 8000Hz)"
    override val deskReportInterval = "Intervalo de Reporte: %s ms"
    override val deskKeyboardTitle = "3. SIMULADOR DE TECLADO RAPID TRIGGER (HALL EFFECT)"
    override val deskKeyDepth = "Profundidad Simulada: %s mm / 4.0 mm"
    override val deskMechSwitch = "Interruptor Mecánico"
    override val deskHallSwitch = "Efecto Hall Magnético"
    override val deskMechActuated = "ACCIONADO (>2.0mm)"
    override val deskMechNotActuated = "NO ACCIONADO (<2.0mm)"
    override val deskHallActuated = "ACTIVO (0.1mm Rapid Trigger)"
    override val deskHallReady = "LISTO"
    override val deskAudioTitle = "4. IMPEDANCIA DE AURICULARES (Ω) Y ACOPLAMIENTO DAC"

    override val ramGuideTitle = "COMPARATIVA DE INGENIERÍA Y GUÍA DE PLATAFORMA"
    override val ramLatencyWinner = "Ganador de Latencia:"
    override val ramBwWinner = "Ganador de Ancho de Banda:"
    override val ramZenRuleTitle = "Regla 1:1 UCLK AMD Zen 4 y Zen 5 (AM5)"
    override val ramFormulaTitle = "Referencia de Fórmula Matemática"
    override val ramFreqLabel = "Frecuencia: %d MT/s"
    override val ramCasLabel = "Latencia CAS: CL%d"
    override val ramTrueLatency = "Latencia Real (True Latency)"
    override val ramBandwidth = "Ancho de Banda Dual-Channel"

    override val upsSectionWatts = "1. CONSUMO DEL SISTEMA Y MONITOR (VATIOS)"
    override val upsPcWatts = "Consumo PC en Carga Gaming"
    override val upsMonitorWatts = "Consumo Monitor + Router / Periféricos"
    override val upsRecommendedTier = "GAMA DE SAI/UPS MÍNIMA RECOMENDADA"
    override val upsTotalLoad = "Carga Pico Total"
    override val upsRawVa = "VA Brutos (W / 0.6)"
    override val upsSafetyMargin = "+20% Margen de Seguridad"
    override val upsRuntimeSection = "AUTONOMÍA ESTIMADA EN CORTES DE LUZ"

    override val coolingSection1 = "1. CARGA TÉRMICA DE CPU Y TIPO DE DISIPADOR"
    override val coolingCpuTdp = "TDP de CPU y Carga Boost"
    override val coolingCoolerArch = "Arquitectura y Tamaño del Disipador"
    override val coolingSection2 = "2. TEMPERATURA AMBIENTE Y FLUJO DE AIRE"
    override val coolingRoomTemp = "Temperatura Ambiente"
    override val coolingAirflow = "Flujo de Ventiladores y Presión Positiva"
    override val coolingSection3 = "3. TEMPERATURA ESTIMADA Y SATURACIÓN"
    override val coolingSaturationCurve = "Curva de Saturación Térmica"

    override val drawerToolsSubtitle = "Física de hardware, latencia y simuladores acústicos"
    override val userAgeLabel = "Edad"
    override val userViewProfile = "Ver perfil"
    override val userLogout = "Cerrar sesión"
    override val userGuest = "Usuario invitado"
    override val userNotLoggedIn = "No autenticado"
    override val userLogin = "Iniciar sesión"
    override val valComponentsCountFormat = "%d Componentes"
    override val valQuickCashDesc = "En 24-48 horas"
    override val valMarketValueDesc = "Media 1-2 semanas"
    override val valMintBoxedDesc = "Coleccionista / Venta paciente"
    override val valNewPriceLabel = "Precio nuevo: %s"
    override val deskPpiRetina = "✅ Nitidez nivel Retina (%d PPI): Píxeles imperceptibles a simple vista, genera %%%d carga de renderizado respecto a 1080p."
    override val deskPpiBalanced = "✅ Proporción Áurea (%d PPI): Densidad óptima de escritorio para texto nítido y alto rendimiento en juegos."
    override val deskPpiLow = "⚠️ Baja densidad (%d PPI): %s en %s puede verse pixelado de cerca (mantener al menos %d cm de distancia)."
    override val deskMouse1k = "Baja carga de CPU (~1%) • Totalmente compatible con todas las CPUs y monitores de 144Hz+."
    override val deskMouse2k = "Modo Esports equilibrado (~2.5% IRQ CPU) • Gran equilibrio entre batería inalámbrica y fluidez."
    override val deskMouse4k = "Alta carga IRQ de CPU (~5-8%) • Se recomienda mínimo Ryzen 5 7500F / i5-13600K y monitor de 240Hz+."
    override val deskMouse8k = "⚠️ Extremo 8000Hz (0.125ms): ¡8.000 interrupciones USB por segundo! Requiere Ryzen 7 7800X3D y monitor de 360Hz+."
    override val deskMechResetDelay = "Debes levantar el dedo %s mm (hasta el punto de 1.8mm) para reiniciar la tecla (~32ms retardo)."
    override val deskHallResetSpeed = "¡La tecla se reinicia con levantar el dedo apenas 0.1 mm (~2ms), deteniendo al personaje al instante!"
    override val deskAudioDacNeeded = "⚠️ ¡DAC / Amplificador dedicado requerido! (%d Ω vs %s)"
    override val deskAudioDacOk = "✅ Alimentado directamente con suficiente potencia (%d Ω)"
    override val deskAudioDacNeededDesc = "%s (%s) no genera suficiente excursión de voltaje para auriculares de %d Ω; los graves se atenúan y el volumen será bajo. Se recomienda un DAC/amplificador USB externo."
    override val deskAudioDacOkDesc = "%s (%s) puede alimentar auriculares de %d Ω con rango dinámico completo sin amplificador externo."
    override val ramKitA = "1. KIT DE MEMORIA (KIT A)"
    override val ramKitB = "2. KIT DE COMPARACIÓN (KIT B)"
    override val ramLatencyWinnerA = "¡El Kit A (%s ns) es %s ns más rápido en tiempo de acceso inicial!"
    override val ramLatencyWinnerB = "¡El Kit B (%s ns) es %s ns más rápido en tiempo de acceso inicial!"
    override val ramLatencyEqual = "¡Ambos kits tienen un tiempo de respuesta de latencia idéntico (%s ns)!"
    override val ramBwWinnerA = "El Kit A (%s GB/s) lidera el ancho de banda teórico por +%s GB/s."
    override val ramBwWinnerB = "El Kit B (%s GB/s) lidera el ancho de banda teórico por +%s GB/s."
    override val ramBwEqual = "Ambos kits ofrecen el mismo ancho de banda Dual-Channel (%s GB/s)."
    override val ramZenRuleDesc = "En AMD Ryzen 7000/9000, el controlador de memoria (UCLK) y reloj (MCLK) corren síncronos 1:1 hasta 6000 MT/s. A 6400+ MT/s, pasa a 1:2 añadiendo 8-10 ns de latencia. Por ello, DDR5-6000 CL30 es el punto ideal para AM5."
    override val upsRuntimeHeaderTier = "Modelo SAI/UPS"
    override val upsRuntimeHeaderBattery = "Batería"
    override val upsRuntimeHeaderTime = "Autonomía estimada"
    override val upsRuntimeFormat = "~%d min"
    override val coolingSelectedCpu = "SELECCIÓN PERSONALIZADA (CATÁLOGO): %s"
    override val coolingCpuSpecsFormat = "TDP: %dW • PPT/MTP real: %dW"
    override val coolingReset = "Reiniciar"
    override val coolingRecommendedCpu = "CPUs de referencia recomendadas:"
    override val coolingSelectedCooler = "DISIPADOR PERSONALIZADO: %s"
    override val coolingRecommendedCooler = "Disipadores de referencia recomendados:"
    override val coolingIdleLoad = "Escritorio en reposo (Idle)"
    override val coolingGamingLoad = "Carga en juegos (~60% PPT)"
    override val coolingStressLoad = "Carga máxima render / Cinebench (100% PPT)"
    override val coolingWarningDesc = "❗ Advertencia técnica: %s genera %dW a plena carga mientras %s disipa máximo %dW. La CPU alcanzará el límite de %d°C y bajará su frecuencia."
    override val coolingSafeDesc = "✅ Certificación técnica: %s absorbe cómodamente la carga de %dW del %s con un margen térmico de seguridad de %dW."
    override val detailLoadingSpecs = "Cargando especificaciones técnicas..."
    override val detailReturnToList = "Volver a la lista"
    override val detailAuthRequiredPrice = "⚠️ Inicia sesión para compartir precios o chollos con la comunidad."
    override val detailCustomPriceTag = "🏷️ PRECIO PERSONALIZADO"
    override val detailEditPrice = "Editar ✎"
    override val detailEnterPrice = "Ingresar precio ✎"
    override val detailPrebuiltAdvantage = "Ventaja de precio PC premontado"
    override val detailPrebuiltBreakdown = "ANÁLISIS INTELIGENTE DE PRECIO Y COMPONENTES"
    override val detailPrebuiltSeparatePrice = "Coste comprando componentes por separado"
    override val detailPrebuiltTransfer = "Importar al Asistente PC y personalizar"
    override val detailGpuTgpTitle = "POTENCIA REAL TGP DE LA GPU"
    override val detailAddedToWizard = "Añadido al Asistente ✓"
    override val detailImportedSystem = "Importado al Sistema ✓"
    override val detailLiveStoreSearch = "BÚSQUEDA DE TIENDAS Y PRECIOS EN VIVO"
    override val detailLiveStoreSearchSub = "Comprobación de stock y precios con un clic en tiendas principales:"
    override val detailVerifiedStores = "%d Verificado"
    override val detailLiveSearch = "Búsqueda en vivo"
    override val detailImageVerifying = "Verificando imagen / No emparejado"
    // Hardware Categories
    override val catCpu = "Procesador (CPU)"
    override val catGpu = "Tarjeta Gráfica (GPU)"
    override val catRam = "Memoria (RAM)"
    override val catMotherboard = "Placa Base"
    override val catPsu = "Fuente de Alimentación (PSU)"
    override val catStorage = "Almacenamiento (SSD/HDD)"
    override val catCooler = "Disipador CPU"
    override val catCase = "Caja / Torre"
    override val catPrebuilt = "PC Premontado"
    override val catLaptop = "Portátil Gaming"
    override val catMouse = "Ratón Gaming"
    override val catKeyboard = "Teclado Gaming"
    override val catHeadset = "Auriculares Gaming"
    override val catMonitor = "Monitor Gaming"

    // Custom Price Dialog
    override val customPriceDialogTitle = "🏷️ Establecer Precio Personalizado"
    override val customPriceStoreRef = "Precio de Referencia en Tienda:"
    override val customPriceOutOfStock = "Sin stock / Sin precio"
    override val customPriceFieldLabel = "Tu Precio Personalizado"
    override val customPriceFieldPlaceholder = "ej. 14500"
    override val customPriceErrorInvalid = "Por favor ingrese un importe válido (ej. 5499)."
    override val customPriceNoteLabel = "Nota / Origen (Opcional)"
    override val customPriceNotePlaceholder = "ej. Segunda Mano, Oferta..."
    override val customPricePresetOwn = "Mi Precio de Compra"
    override val customPricePresetUsed = "Segunda Mano"
    override val customPricePresetSale = "Oferta / Descuento"
    override val customPricePresetImport = "Importación"
    override val customPriceNotice = "El precio personalizado se guarda localmente y se prioriza en el Catálogo, análisis de valor VS Arena y presupuesto del Asistente PC."

    // Auth Sheet
    override val authLoginTitle = "⚡ Iniciar Sesión"
    override val authRegisterTitle = "🚀 Unirse a la Comunidad y Registrarse"
    override val authTabLogin = "Iniciar Sesión"
    override val authTabRegister = "Registrarse"
    override val authEmailOrUsername = "Correo o Usuario"
    override val authPassword = "Contraseña"
    override val authLoggingIn = "Iniciando sesión..."
    override val authRegistering = "Creando cuenta..."
    override val authUsernameReq = "Nombre de Usuario *"
    override val authEmailReq = "Correo Electrónico *"
    override val authPasswordReq = "Contraseña (Mínimo 6 caracteres) *"
    override val authPasswordMinError = "La contraseña debe tener al menos 6 caracteres."
    override val authLoginFailed = "❌ ¡Usuario no encontrado o contraseña incorrecta! Por favor verifica tus datos."
    override val authRegisterSuccess = "¡Registro exitoso! ¡Bienvenido/a, %s!"
    override val authRegisterFailed = "No se pudo crear la cuenta. Por favor verifica tus datos."

    // User Profile
    override val profileNotLoggedIn = "Aún no has iniciado sesión"
    override val profileLoginBtn = "⚡ Iniciar Sesión / Registrarse"
    override val profileLogoutBtn = "🚪 Cerrar Sesión"
    override val profileTitle = "Perfil de Usuario"

    // Wizard
    override val wizardSystemCardCopied = "⚡ ¡Ficha del Sistema copiada al portapapeles! Lista para compartir."
    override val wizardTotalPrefix = "Total"
    override val wizardNoPartSelected = "Ningún componente seleccionado"
    override val wizardAddPart = "+ Añadir Componente"
    override val wizardSelect = "Elegir"
    override val wizardSelected = "Seleccionado"
    override val wizardInteractiveTitle = "Configurador Interactivo de PC"
    override val wizardNotSelected = "No seleccionado"

    // VS Arena
    override val vsCompMode = "🧩 Componente vs Componente"
    override val vsPrebuiltMode = "🖥️ PC Premontado"
    override val vsLaptopMode = "💻 Portátil Gaming"
    override val vsCorner1 = "Seleccionar Esquina 1"
    override val vsCorner2 = "Seleccionar Esquina 2"
    override val vsArenaTitleFormat = "Arena de Comparación de %s"

    // Community Price Ledger
    override val ledgerRefresh = "🔄 Actualizar"
    override val ledgerSubmitDealBtn = "+ Reportar Precio / Compartir Oferta"
    override val ledgerAdminLogout = "Cerrar Sesión Admin"
    override val ledgerReportedPrice = "Precio Notificado"
    override val ledgerUserNotePrefix = "💬 Nota del Usuario:"
    override val ledgerProofLinkBtn = "🔗 Revisar / Abrir Enlace de Prueba"
    override val ledgerSubmitSheetTitle = "⚡ Reportar Oferta Comunitaria"
    override val ledgerPriceLabel = "Precio (TL) *"
    override val ledgerDealTypeSelect = "Seleccionar Tipo de Transacción *"
    override val ledgerProofUrlLabel = "Enlace de Prueba (Obligatorio) *"
    override val ledgerNoteFieldLabel = "Descripción / Nota de Experiencia"
    override val ledgerSubmitAction = "🚀 Enviar Oferta (Revisión Pendiente)"
}
val StringsES: AppStrings = AppStringsES()


private class AppStringsFR : AppStrings {
    override val languageCode = "fr"

    override val tabCatalog = "Catalogue"
    override val tabWizard = "Configurateur"
    override val tabVsArena = "VS Arena"

    override val settingsTitle = "Paramètres"
    override val appearanceSection = "Apparence & Palette"
    override val appearanceSubtitle = "Thèmes studio sombre, slate clair, cyber-bois ou noir OLED pur"
    override val themeDark = "Obsidian"
    override val themeLight = "Slate"
    override val themeWood = "Noyer"
    override val themeOled = "OLED"
    override val themeCyberpunk = "Cyberpunk"
    override val themeSystem = "Système"

    override val languageRegionSection = "Langue & Région"
    override val languageRegionSubtitle = "Langue de l'application et formatage des devises"
    override val languageLabel = "Langue de l'application"
    override val currencyLabel = "Devise"

    override val hardwarePrefsSection = "Préférences Matériel & Config"
    override val hardwarePrefsSubtitle = "Critères d'assemblage et analyse technique des composants"
    override val enthusiastModeLabel = "Mode Passionné (Geek)"
    override val enthusiastModeDesc = "Affiche les étages d'alimentation VRM, types de puces mémoire et lignes PCIe"
    override val tempUnitLabel = "Unité de Température"
    override val lengthUnitLabel = "Unité de Mesure"
    override val targetResolutionLabel = "Résolution Cible"
    override val targetFpsLabel = "Taux de Rafraîchissement (FPS)"

    override val dataStorageSection = "Données & Stockage"
    override val dataStorageSubtitle = "Optimisation de la bande passante et gestion du cache"
    override val dataSaverLabel = "Économiseur de Données"
    override val dataSaverDesc = "Ne charge que la vue principale du produit pour réduire la consommation mobile"
    override val cacheLabel = "Cache d'Images"
    override val cacheDesc = "Photos de composants en haute définition stockées localement"
    override val clearCacheButton = "Vider le Cache"
    override val cacheClearedMessage = "Le cache d'images a été vidé avec succès."

    override val sourcesSection = "Sources & Vérification"
    override val sourcesSubtitle = "Bases de données officielles, bancs d'essai et galeries photos"
    override val sourcesGpuDesc = "Base TechPowerUp GPU, VideoCardz, GPU-Monkey, Livres blancs AMD & NVIDIA"
    override val sourcesCpuDesc = "Spécifications techniques AMD & Intel, Geekbench 6, Cinebench, Puget Systems"
    override val sourcesPsuDesc = "Cybenetics Labs (Certifications ETA & LAMBDA), Cultists Network Tier List, 80 PLUS"
    override val sourcesImagesDesc = "PCPartPicker CDN 1600p, Galeries LDLC, Dossiers de presse des constructeurs"
    override val sourcesPricesDesc = "API multi-vendeurs PCPartPicker, Akakçe, Cimri, Boutiques Amazon"

    override val aboutSection = "À Propos"
    override val aboutSubtitle = "Version, développeur et licences open source"
    override val appVersion = "Version"
    override val developer = "Développeur"
    override val developerName = "Ömer Yavuz Yiğit"
    override val licenses = "Bibliothèques Open Source & Licences"
    override val back = "Retour"
    override val selectLanguageTitle = "Choisir la Langue"
    override val selectCurrencyTitle = "Choisir la Devise"

    override val catalogTitle = "HARDWARE WIZARD"
    override val searchPlaceholder = "Composant, modèle, marque (AMD, RTX 4070...)"
    override val segmentIndividual = "Composants"
    override val segmentPeripherals = "Périphériques"
    override val segmentPrebuilt = "PC Fixes OEM"
    override val segmentLaptop = "Portables"
    override val filterButton = "Filtres"
    override val filterAll = "Tous"
    override val marketPrice = "Prix du Marché"
    override val addToVs = "+ VS"
    override val addToWizard = "+ Ajouter à la Config"
    override val noPriceFallback = "Épuisé / Pas de Prix"

    override val addToVsArena = "Ajouter à VS Arena"
    override val fpsMatrixTitle = "Matrice FPS en Direct sur 12 Jeux"
    override val fpsMatrixSubtitle = "Multiplicateurs E-Sports (5) et Gros Titres AAA (7)"
    override val localPriceOverride = "Définir Prix Personnalisé"
    override val localPriceDesc = "Inclure promotions ou prix d'occasion dans l'analyse de rapport qualité/prix"
    override val enterPrice = "Entrer le Prix"

    override val vsArenaTitle = "Face à Face"
    override val vsArenaSubtitle = "CPU, GPU, Carte Mère, RAM, SSD, Alimentation, Ventirad, Boîtier"
    override val corner1 = "COIN 1"
    override val corner2 = "COIN 2"
    override val selectPart = "+ Choisir"
    override val startSampleBattle = "Lancer un Duel Type"
    override val vsDescription = "Touchez les coins ou cliquez sur 'VS' dans le catalogue pour comparer deux produits."
    override val selectCorner1 = "Choisir Coin 1"
    override val selectCorner2 = "Choisir Coin 2"

    override val wizardTitle = "Configurateur PC"
    override val wizardSubtitle = "Analyse intelligente de compatibilité et de puissance requise"
    override val totalBudget = "Coût Total de la Config"
    override val clearBuild = "Réinitialiser la Config"
    override val themeValorant = "Valorant"
    override val themeCs2 = "CS2"
    override val themeGtaVice = "GTA Vice"
    override val themeWitcher = "The Witcher"

    override val drawerMainModules = "MODULES PRINCIPAUX"
    override val drawerToolsHeader = "OUTILS D'INGÉNIERIE INTERACTIFS"
    override val drawerQuickCatalog = "Catalogue"
    override val drawerQuickWizard = "Configurateur PC"
    override val drawerQuickVs = "Arène VS"
    override val closeButton = "Fermer"

    override val drawerCoolingTitle = "Simulateur Thermique & Refroidissement"
    override val drawerCoolingSubtitle = "Charge TDP, température ambiante, flux d'air et saturation AIO"
    override val selectCpuCatalog = "Choisir CPU du catalogue"
    override val selectCoolerCatalog = "Choisir AIO/Ventirad du catalogue"
    override val coolingSafeStatus = "❄️ THERMIQUEMENT SÛR ET SILENCIEUX"
    override val coolingWarningStatus = "⚠️ EN LIMITE (HAUTE VITESSE DU VENTILATEUR)"
    override val coolingThrottleStatus = "🔥 RISQUE D'ÉTRANGLEMENT THERMIQUE"

    override val drawerRamTitle = "Latence Réelle & Bande Passante RAM"
    override val drawerRamSubtitle = "Latence en nanosecondes CL × 2000 / MT/s, FPS 1% Low et impact X3D"

    override val drawerUpsTitle = "Dimensionnement Onduleur & Batterie"
    override val drawerUpsSubtitle = "Puissance VA / Watts réels, onde sinusoïdale pure PFC et autonomie"

    override val drawerDeskTitle = "Lab Bureau & Périphériques"
    override val drawerDeskSubtitle = "PPI d'écran, charge CPU souris 8KHz, Rapid Trigger clavier & DAC"

    override val drawerGlossaryTitle = "Glossaire Matériel & Ingénierie"
    override val drawerGlossarySubtitle = "TGP vs TDP, 3D V-Cache, ATX 3.1, Hall Effect, QD-OLED termes techniques"

    override val drawerUndervoltTitle = "Guide d'Undervolt & Optimisation"
    override val drawerUndervoltSubtitle = "Intel Vmin, AMD Curve Optimizer, courbe tension/fréq GPU & Bit-Rot"

    override val drawerValuationTitle = "Estimation PC d'Occasion"
    override val drawerValuationSubtitle = "Vente rapide, valeur marché et prix vitrine basés sur données vérifiées"

    override val detailPerformancePower = "PROFIL DE PERFORMANCE & CONSOMMATION"
    override val detailMultiGameFps = "SIMULATION FPS EN DIRECT SUR 12 JEUX"
    override val detailTechSpecs = "CARACTÉRISTIQUES TECHNIQUES DÉTAILLÉES"
    override val detailPrebuiltAnalysis = "ANALYSE DES COMPOSANTS PC PRÉMONTÉ"
    override val detailLaptopSpecs = "SPÉCIFICATIONS PC PORTABLE GAMER"
    override val detailStoreOffers = "OFFRES BOUTIQUES VÉRIFIÉES"
    override val detailCommunityLedger = "REGISTRE DES PRIX DE LA COMMUNAUTÉ"

    override val gameBenchTabSimulated = "📊 FPS Simulés (Modèle)"
    override val gameBenchTabVerified = "🧪 Benchmark Réel Vérifié"
    override val gameBenchReferenceRigCpu = "🔬 GPU de Référence : RTX 5090 32GB (Zéro Goulot d'Étranglement)"
    override val gameBenchReferenceRigGpu = "🔬 CPU de Référence : Ryzen 7 9800X3D (Zéro Goulot d'Étranglement)"
    override val gameBenchAvgMinLabel = "FPS Moyen et Minimum"
    override val gameBenchCurrentHardware = "Matériel Actuel"
    override val gameBenchNoVerifiedYet = "Aucun test de laboratoire vérifié disponible pour ce modèle pour l'instant."
    override val gameBenchSwitchToSim = "Passer à la Simulation"

    override val valuationHeroTitle = "COMBIEN VAUT MON PC D'OCCASION ?"
    override val valuationHeroSubtitle = "Évalue automatiquement votre configuration sur 3 niveaux du marché d'occasion grâce aux transactions communautaires vérifiées."
    override val valuationNoSystem = "Aucun système sélectionné"
    override val valuationActiveParts = "%d composants actifs"
    override val valuationTotalMarket = "VALEUR TOTALE D'OCCASION"
    override val valuationNewTotal = "Prix neuf : %s"
    override val valuationBreakdownTitle = "RÉPARTITION PAR COMPOSANT"
    override val valuationTipsTitle = "CONSEILS POUR LA VENTE D'OCCASION"
    override val valuationEmptyTitle = "Votre configuration est vide"
    override val valuationEmptySubtitle = "Sélectionnez des pièces dans l'Assistant PC ou explorez le catalogue pour évaluer votre configuration d'occasion."
    override val valuationGoToWizard = "Ouvrir l'Assistant PC & Ajouter des pièces"
    override val uvTabCpu = "Processeur (CPU)"
    override val uvTabGpu = "Carte Graphique (GPU)"
    override val uvTabRam = "Mémoire (RAM)"
    override val uvTabCmos = "Réinitialisation d'Urgence (CMOS)"

    override val activeStatusBadge = "ACTIF"
    override val drawerSettingsSubtitle = "Thèmes, 7 langues, devise, mode geek"
    override val drawerCustomPriceTitle = "💡 Moteur de Prix Personnalisés"
    override val drawerCustomPriceDesc = "Touchez le badge de prix de n'importe quel produit pour entrer votre prix réduit ou d'occasion."

    override val valQuickCash = "Vente Rapide"
    override val valMarketValue = "Valeur Marché"
    override val valMintBoxed = "Complet en Boîte"
    override val valVerifiedDealsFormat = "✓ Basé sur %d ventes communautaires vérifiées"
    override val valTip1Title = "Boîte & Facture d'Origine (+10-15%)"
    override val valTip1Desc = "Le matériel vendu avec sa boîte et facture d'origine se vend plus vite et au meilleur prix."
    override val valTip2Title = "Preuves de Tests de Stabilité"
    override val valTip2Desc = "Ajouter des captures de FurMark, Cinebench et de santé SSD CrystalDiskInfo triple la confiance des acheteurs."
    override val valTip3Title = "Nettoyage & Pâte Thermique Neuve"
    override val valTip3Desc = "Un boîtier dépoussiéré, un câblage propre et une pâte thermique neuve font toute la différence sur les photos."

    override val glossarySearchPlaceholder = "Rechercher terme ou métrique (DrMOS, 12V-2x6, QD-OLED, Rapid Trigger, X3D...)"
    override val glossaryCategoryAll = "Tous"
    override val glossaryCriticalMetrics = "Métriques d'Ingénierie Clés"
    override val glossaryRelated = "Lié à :"
    override val glossaryEmpty = "Aucun terme technique ne correspond à vos critères."

    override val deskMonitorTitle = "1. NETTETÉ PPI DU MONITEUR & FACTEUR DE CHARGE GPU"
    override val deskScreenSuffix = "Écran"
    override val deskPixelDensity = "Densité de Pixels"
    override val deskViewingDistance = "Distance de Recul Idéale"
    override val deskGpuPixelLoad = "Charge Pixel GPU"
    override val deskMouseTitle = "2. ANALYSEUR DE POLLING RATE SOURIS (1000Hz – 8000Hz)"
    override val deskReportInterval = "Intervalle de Rapport : %s ms"
    override val deskKeyboardTitle = "3. SIMULATEUR CLAVIER RAPID TRIGGER (EFFET HALL)"
    override val deskKeyDepth = "Course Simulée : %s mm / 4.0 mm"
    override val deskMechSwitch = "Interrupteur Mécanique"
    override val deskHallSwitch = "Effet Hall Magnétique"
    override val deskMechActuated = "ACTIONNÉ (>2.0mm)"
    override val deskMechNotActuated = "NON ACTIONNÉ (<2.0mm)"
    override val deskHallActuated = "ACTIF (0.1mm Rapid Trigger)"
    override val deskHallReady = "PRÊT"
    override val deskAudioTitle = "4. IMPÉDANCE CASQUE (Ω) & ADAPTATION DAC AUDIO"

    override val ramGuideTitle = "COMPARATIF INGÉNIERIE & GUIDE PLATEFORME"
    override val ramLatencyWinner = "Gagnant Latence :"
    override val ramBwWinner = "Gagnant Bande Passante :"
    override val ramZenRuleTitle = "Règle 1:1 UCLK AMD Zen 4 & Zen 5 (AM5)"
    override val ramFormulaTitle = "Référence Formule Mathématique"
    override val ramFreqLabel = "Fréquence : %d MT/s"
    override val ramCasLabel = "Latence CAS : CL%d"
    override val ramTrueLatency = "Latence Réelle (True Latency)"
    override val ramBandwidth = "Bande Passante Dual-Channel"

    override val upsSectionWatts = "1. CONSOMMATION SYSTÈME & MONITEUR (WATTS)"
    override val upsPcWatts = "Charge PC en Jeu"
    override val upsMonitorWatts = "Moniteur + Box / Périphériques"
    override val upsRecommendedTier = "GAMME ONDULEUR MINIMALE RECOMMANDÉE"
    override val upsTotalLoad = "Charge de Pointe Totale"
    override val upsRawVa = "VA Bruts (W / 0.6)"
    override val upsSafetyMargin = "+20% Marge de Sécurité"
    override val upsRuntimeSection = "AUTONOMIE ESTIMÉE LORS DES COUPURES"

    override val coolingSection1 = "1. CHARGE THERMIQUE CPU & TYPE DE REFROIDISSEUR"
    override val coolingCpuTdp = "TDP CPU & Charge Boost"
    override val coolingCoolerArch = "Architecture & Taille du Ventirad/AIO"
    override val coolingSection2 = "2. TEMPÉRATURE AMBIANTE & FLUX D'AIR BOÎTIER"
    override val coolingRoomTemp = "Température Ambiante"
    override val coolingAirflow = "Flux Ventilateurs & Pression Positive"
    override val coolingSection3 = "3. TEMPÉRATURE PRÉVUE & SATURATION"
    override val coolingSaturationCurve = "Courbe de Saturation Thermique"

    override val drawerToolsSubtitle = "Physique du matériel, latence et simulateurs acoustiques"
    override val userAgeLabel = "Âge"
    override val userViewProfile = "Voir le profil"
    override val userLogout = "Déconnexion"
    override val userGuest = "Utilisateur invité"
    override val userNotLoggedIn = "Non connecté"
    override val userLogin = "Se connecter"
    override val valComponentsCountFormat = "%d Composants"
    override val valQuickCashDesc = "Sous 24-48 heures"
    override val valMarketValueDesc = "Moyenne 1-2 semaines"
    override val valMintBoxedDesc = "Collectionneur / Vente patiente"
    override val valNewPriceLabel = "Prix neuf: %s"
    override val deskPpiRetina = "✅ Netteté Retina (%d PPI): Pixels invisibles à l'œil nu, génère %%%d de charge GPU par rapport au 1080p."
    override val deskPpiBalanced = "✅ Nombre d'Or (%d PPI): Densité de bureau la plus équilibrée pour la netteté du texte et le taux de FPS."
    override val deskPpiLow = "⚠️ Faible densité (%d PPI): %s en %s peut sembler pixélisé de près (recul recommandé: au moins %d cm)."
    override val deskMouse1k = "Faible charge processeur (~1%) • Entièrement compatible avec tous les CPU et moniteurs 144Hz+."
    override val deskMouse2k = "Mode Esport équilibré (~2,5% CPU IRQ) • Compromis idéal entre autonomie de batterie et fluidité."
    override val deskMouse4k = "Charge d'interruption CPU élevée (~5-8%) • Ryzen 5 7500F / i5-13600K et écran 240Hz+ minimum recommandés."
    override val deskMouse8k = "⚠️ Extrême 8000Hz (0,125ms): 8 000 interruptions USB/s! Peut causer des saccades sur anciens processeurs; nécessite Ryzen 7 7800X3D."
    override val deskMechResetDelay = "Le doigt doit remonter de %s mm (jusqu'à 1,8mm) pour relâcher la touche (~32ms de latence)."
    override val deskHallResetSpeed = "La touche se réinitialise dès que le doigt remonte de seulement 0,1 mm (~2ms), arrêt immédiat du joueur!"
    override val deskAudioDacNeeded = "⚠️ DAC / Ampli dédié requis ! (%d Ω vs %s)"
    override val deskAudioDacOk = "✅ Alimenté directement avec une puissance suffisante (%d Ω)"
    override val deskAudioDacNeededDesc = "%s (%s) ne fournit pas une excursion de tension suffisante pour un casque de %d Ω ; basses atténuées et volume insuffisant. Un DAC/ampli USB externe est recommandé."
    override val deskAudioDacOkDesc = "%s (%s) alimente les casques de %d Ω sur toute la plage dynamique sans amplificateur externe."
    override val ramKitA = "1. KIT MÉMOIRE (KIT A)"
    override val ramKitB = "2. KIT DE COMPARAISON (KIT B)"
    override val ramLatencyWinnerA = "Le Kit A (%s ns) est %s ns plus rapide en temps de réponse initial !"
    override val ramLatencyWinnerB = "Le Kit B (%s ns) est %s ns plus rapide en temps de réponse initial !"
    override val ramLatencyEqual = "Les deux kits ont un temps de latence réel parfaitement identique (%s ns) !"
    override val ramBwWinnerA = "Le Kit A (%s GB/s) mène en bande passante théorique de +%s GB/s."
    override val ramBwWinnerB = "Le Kit B (%s GB/s) mène en bande passante théorique de +%s GB/s."
    override val ramBwEqual = "Les deux kits offrent une bande passante double canal identique (%s GB/s)."
    override val ramZenRuleDesc = "Sur AMD Ryzen 7000/9000, le contrôleur mémoire (UCLK) et l'horloge (MCLK) fonctionnent en 1:1 synchrone jusqu'à 6000 MT/s. À 6400+ MT/s, le diviseur 1:2 ajoute 8-10 ns de latence. DDR5 6000 CL30 reste le sweet spot optimal."
    override val upsRuntimeHeaderTier = "Modèle onduleur"
    override val upsRuntimeHeaderBattery = "Batterie"
    override val upsRuntimeHeaderTime = "Autonomie estimée"
    override val upsRuntimeFormat = "~%d min"
    override val coolingSelectedCpu = "SÉLECTION PERSONNALISÉE (CATALOGUE): %s"
    override val coolingCpuSpecsFormat = "TDP: %dW • PPT/MTP réel: %dW"
    override val coolingReset = "Réinitialiser"
    override val coolingRecommendedCpu = "Processeurs de référence recommandés :"
    override val coolingSelectedCooler = "REFROIDISSEUR PERSONNALISÉ: %s"
    override val coolingRecommendedCooler = "Refroidisseurs de référence recommandés :"
    override val coolingIdleLoad = "Bureau au repos (Idle)"
    override val coolingGamingLoad = "Charge jeu (~60% PPT)"
    override val coolingStressLoad = "Charge maximale / Cinebench (100% PPT)"
    override val coolingWarningDesc = "❗ Alerte technique: %s produit %dW en pleine charge alors que %s dissipe au maximum %dW. Le processeur atteindra le seuil de %d°C et bridera sa fréquence."
    override val coolingSafeDesc = "✅ Validation technique: %s gère sans problème la charge de %dW du %s avec une marge de sécurité de %dW."
    override val detailLoadingSpecs = "Chargement des spécifications techniques..."
    override val detailReturnToList = "Retour à la liste"
    override val detailAuthRequiredPrice = "⚠️ Veuillez vous connecter pour proposer des prix communautaires."
    override val detailCustomPriceTag = "🏷️ PRIX PERSONNALISÉ"
    override val detailEditPrice = "Modifier ✎"
    override val detailEnterPrice = "Saisir prix ✎"
    override val detailPrebuiltAdvantage = "Avantage prix PC pré-assemblé"
    override val detailPrebuiltBreakdown = "ANALYSE RENTABILITÉ & COMPOSANTS SÉPARÉS"
    override val detailPrebuiltSeparatePrice = "Prix total des composants séparés"
    override val detailPrebuiltTransfer = "Importer dans l'Assistant PC et personnaliser"
    override val detailGpuTgpTitle = "PUISSANCE RÉELLE GPU TGP"
    override val detailAddedToWizard = "Ajouté à l'Assistant ✓"
    override val detailImportedSystem = "Importé dans la configuration ✓"
    override val detailLiveStoreSearch = "RECHERCHE EN DIRECT EN MAGASIN"
    override val detailLiveStoreSearchSub = "Vérifiez stock et prix en un clic dans les principales boutiques :"
    override val detailVerifiedStores = "%d Vérifié"
    override val detailLiveSearch = "Recherche en direct"
    override val detailImageVerifying = "Image en cours de vérification / Non associée"
    // Hardware Categories
    override val catCpu = "Processeur (CPU)"
    override val catGpu = "Carte Graphique (GPU)"
    override val catRam = "Mémoire (RAM)"
    override val catMotherboard = "Carte Mère"
    override val catPsu = "Alimentation (PSU)"
    override val catStorage = "Stockage (SSD/HDD)"
    override val catCooler = "Refroidissement CPU"
    override val catCase = "Boîtier"
    override val catPrebuilt = "PC Prémonté"
    override val catLaptop = "PC Portable Gaming"
    override val catMouse = "Souris Gaming"
    override val catKeyboard = "Clavier Gaming"
    override val catHeadset = "Casque Gaming"
    override val catMonitor = "Écran Gaming"

    // Custom Price Dialog
    override val customPriceDialogTitle = "🏷️ Définir un Prix Personnalisé"
    override val customPriceStoreRef = "Prix de Référence en Boutique :"
    override val customPriceOutOfStock = "Rupture / Aucun prix"
    override val customPriceFieldLabel = "Votre Prix Personnalisé"
    override val customPriceFieldPlaceholder = "ex. 14500"
    override val customPriceErrorInvalid = "Veuillez saisir un montant valide (ex. 5499)."
    override val customPriceNoteLabel = "Note / Source (Optionnel)"
    override val customPriceNotePlaceholder = "ex. Seconde Main, Promo..."
    override val customPricePresetOwn = "Mon Prix d'Achat"
    override val customPricePresetUsed = "Occasion"
    override val customPricePresetSale = "Promotion / Solde"
    override val customPricePresetImport = "Importé"
    override val customPriceNotice = "Le prix personnalisé est enregistré localement et pris en compte en priorité dans le Catalogue, le calcul de valeur VS Arena et le budget de l'Assistant PC."

    // Auth Sheet
    override val authLoginTitle = "⚡ Se Connecter"
    override val authRegisterTitle = "🚀 Rejoindre la Communauté & S'inscrire"
    override val authTabLogin = "Connexion"
    override val authTabRegister = "Inscription"
    override val authEmailOrUsername = "E-mail ou Nom d'utilisateur"
    override val authPassword = "Mot de passe"
    override val authLoggingIn = "Connexion en cours..."
    override val authRegistering = "Création du compte..."
    override val authUsernameReq = "Nom d'utilisateur *"
    override val authEmailReq = "Adresse E-mail *"
    override val authPasswordReq = "Mot de passe (Au moins 6 caractères) *"
    override val authPasswordMinError = "Le mot de passe doit comporter au moins 6 caractères."
    override val authLoginFailed = "❌ Utilisateur introuvable ou mot de passe incorrect ! Veuillez vérifier vos informations."
    override val authRegisterSuccess = "Inscription réussie ! Bienvenue, %s !"
    override val authRegisterFailed = "Échec de l'inscription. Veuillez vérifier vos coordonnées."

    // User Profile
    override val profileNotLoggedIn = "Vous n'êtes pas encore connecté"
    override val profileLoginBtn = "⚡ Se Connecter / S'inscrire"
    override val profileLogoutBtn = "🚪 Se Déconnecter"
    override val profileTitle = "Profil Utilisateur"

    // Wizard
    override val wizardSystemCardCopied = "⚡ Fiche Système copiée dans le presse-papiers ! Prête à être partagée."
    override val wizardTotalPrefix = "Total"
    override val wizardNoPartSelected = "Aucun composant sélectionné"
    override val wizardAddPart = "+ Ajouter un Composant"
    override val wizardSelect = "Choisir"
    override val wizardSelected = "Sélectionné"
    override val wizardInteractiveTitle = "Assistant Interactif d'Assemblage PC"
    override val wizardNotSelected = "Non sélectionné"

    // VS Arena
    override val vsCompMode = "🧩 Composant vs Composant"
    override val vsPrebuiltMode = "🖥️ PC Prémonté"
    override val vsLaptopMode = "💻 PC Portable Gaming"
    override val vsCorner1 = "Choisir le Coin 1"
    override val vsCorner2 = "Choisir le Coin 2"
    override val vsArenaTitleFormat = "Arène de Comparaison : %s"

    // Community Price Ledger
    override val ledgerRefresh = "🔄 Actualiser"
    override val ledgerSubmitDealBtn = "+ Signaler un Prix / Partager un Bon Plan"
    override val ledgerAdminLogout = "Déconnexion Admin"
    override val ledgerReportedPrice = "Prix Signalé"
    override val ledgerUserNotePrefix = "💬 Note de l'Utilisateur :"
    override val ledgerProofLinkBtn = "🔗 Vérifier / Ouvrir la Preuve"
    override val ledgerSubmitSheetTitle = "⚡ Signaler un Bon Plan Communautaire"
    override val ledgerPriceLabel = "Prix (TL) *"
    override val ledgerDealTypeSelect = "Sélectionner le Type de Transaction *"
    override val ledgerProofUrlLabel = "Lien de Preuve (Obligatoire) *"
    override val ledgerNoteFieldLabel = "Description / Avis d'Expérience"
    override val ledgerSubmitAction = "🚀 Soumettre (En attente de validation)"
}
val StringsFR: AppStrings = AppStringsFR()


private class AppStringsRU : AppStrings {
    override val languageCode = "ru"

    override val tabCatalog = "Каталог"
    override val tabWizard = "Конфигуратор"
    override val tabVsArena = "VS Арена"

    override val settingsTitle = "Настройки"
    override val appearanceSection = "Оформление и Палитра"
    override val appearanceSubtitle = "Тёмная студия, светлый slate, кибер-дерево или чистый OLED"
    override val themeDark = "Obsidian"
    override val themeLight = "Slate"
    override val themeWood = "Орех"
    override val themeOled = "OLED"
    override val themeCyberpunk = "Cyberpunk"
    override val themeSystem = "Системная"

    override val languageRegionSection = "Язык и Регион"
    override val languageRegionSubtitle = "Язык интерфейса и отображение валюты"
    override val languageLabel = "Язык приложения"
    override val currencyLabel = "Валюта"

    override val hardwarePrefsSection = "Параметры Сборки и Железа"
    override val hardwarePrefsSubtitle = "Критерии подбора комплектующих и цели конфигуратора"
    override val enthusiastModeLabel = "Режим Энтузиаста"
    override val enthusiastModeDesc = "Отображать подробные фазы питания VRM, чипы памяти (Die) и линии PCIe"
    override val tempUnitLabel = "Единица Температуры"
    override val lengthUnitLabel = "Единица Измерения"
    override val targetResolutionLabel = "Целевое Разрешение"
    override val targetFpsLabel = "Целевой FPS"

    override val dataStorageSection = "Данные и Хранилище"
    override val dataStorageSubtitle = "Экономия трафика и очистка локального кэша"
    override val dataSaverLabel = "Экономия Трафика"
    override val dataSaverDesc = "Загружать только главное фото товара для экономии мобильного интернета"
    override val cacheLabel = "Кэш Изображений"
    override val cacheDesc = "Сохранённые на устройстве фотографии компонентов в высоком разрешении"
    override val clearCacheButton = "Очистить Кэш"
    override val cacheClearedMessage = "Кэш изображений успешно очищен."

    override val sourcesSection = "Источники и Верификация"
    override val sourcesSubtitle = "Базы технических данных, бенчмарки и фотогалереи"
    override val sourcesGpuDesc = "База TechPowerUp GPU, VideoCardz, GPU-Monkey, Белые книги AMD и NVIDIA"
    override val sourcesCpuDesc = "Техническая документация AMD и Intel, Geekbench 6, Cinebench, Puget Systems"
    override val sourcesPsuDesc = "Cybenetics Labs (Сертификаты ETA и LAMBDA), Cultists Network Tier List, 80 PLUS"
    override val sourcesImagesDesc = "PCPartPicker 1600p CDN, Студийные фото LDLC, Пресс-киты производителей"
    override val sourcesPricesDesc = "Глобальный API PCPartPicker, Akakçe, Cimri, Магазины Amazon"

    override val aboutSection = "О Приложении"
    override val aboutSubtitle = "Версия, разработчик и открытые лицензии"
    override val appVersion = "Версия"
    override val developer = "Разработчик"
    override val developerName = "Ömer Yavuz Yiğit"
    override val licenses = "Библиотеки Open Source и Лицензии"
    override val back = "Назад"
    override val selectLanguageTitle = "Выберите Язык"
    override val selectCurrencyTitle = "Выберите Валюту"

    override val catalogTitle = "HARDWARE WIZARD"
    override val searchPlaceholder = "Деталь, модель, бренд (AMD, RTX 4070...)"
    override val segmentIndividual = "Комплектующие"
    override val segmentPeripherals = "Периферия"
    override val segmentPrebuilt = "Готовые ПК"
    override val segmentLaptop = "Ноутбуки"
    override val filterButton = "Фильтры"
    override val filterAll = "Все"
    override val marketPrice = "Рыночная Цена"
    override val addToVs = "+ VS"
    override val addToWizard = "+ В Конфигуратор"
    override val noPriceFallback = "Нет в наличии"

    override val addToVsArena = "Сравнить в VS Арене"
    override val fpsMatrixTitle = "Матрица 12 Игр: FPS и 1% Low"
    override val fpsMatrixSubtitle = "Киберспорт (5) и Тяжёлые AAA-Проекты (7)"
    override val localPriceOverride = "Указать Свою Цену"
    override val localPriceDesc = "Учитывайте скидки, купоны или б/у цены для точного расчёта выгоды"
    override val enterPrice = "Ввести Цену"

    override val vsArenaTitle = "Прямое Сравнение"
    override val vsArenaSubtitle = "Процессор, Видеокарта, Плата, ОЗУ, SSD, БП, Кулер, Корпус"
    override val corner1 = "УГОЛ 1"
    override val corner2 = "УГОЛ 2"
    override val selectPart = "+ Выбрать"
    override val startSampleBattle = "Запустить Тестовое Сравнение"
    override val vsDescription = "Нажмите на углы сверху или выберите 'VS' в каталоге для прямого сравнения."
    override val selectCorner1 = "Выбрать Угол 1"
    override val selectCorner2 = "Выбрать Угол 2"

    override val wizardTitle = "Конфигуратор ПК"
    override val wizardSubtitle = "Умная проверка совместимости и расчёт мощности БП"
    override val totalBudget = "Общая Стоимость Сборки"
    override val clearBuild = "Сбросить Сборку"
    override val themeValorant = "Valorant"
    override val themeCs2 = "CS2"
    override val themeGtaVice = "GTA Vice"
    override val themeWitcher = "Ведьмак"

    override val drawerMainModules = "ОСНОВНЫЕ МОДУЛИ"
    override val drawerToolsHeader = "ИНЖЕНЕРНЫЕ ИНСТРУМЕНТЫ"
    override val drawerQuickCatalog = "Каталог"
    override val drawerQuickWizard = "Конфигуратор"
    override val drawerQuickVs = "VS Арена"
    override val closeButton = "Закрыть"

    override val drawerCoolingTitle = "Симулятор охлаждения и температур"
    override val drawerCoolingSubtitle = "TDP нагрузка, температура среды, воздушный поток и насыщение СЖО"
    override val selectCpuCatalog = "Выбрать процессор из каталога"
    override val selectCoolerCatalog = "Выбрать кулер из каталога"
    override val coolingSafeStatus = "❄️ ТЕРМИЧЕСКИ БЕЗОПАСНО И БЕСШУМНО"
    override val coolingWarningStatus = "⚠️ НА ПРЕДЕЛЕ (ВЫСОКИЕ ОБОРОТЫ ВЕНТИЛЯТОРОВ)"
    override val coolingThrottleStatus = "🔥 РИСК ТЕРМИЧЕСКОГО ТРОТТЛИНГА"

    override val drawerRamTitle = "Истинная задержка и ПСП RAM"
    override val drawerRamSubtitle = "Задержка в нс CL × 2000 / MT/s, 1% Low FPS и влияние кэша X3D"

    override val drawerUpsTitle = "Калькулятор мощности и времени ИБП"
    override val drawerUpsSubtitle = "ВА / Реальные Ватты, чистый синус PFC и время работы при отключении"

    override val drawerDeskTitle = "Лаборатория периферии и рабочего места"
    override val drawerDeskSubtitle = "PPI монитора, нагрузка 8КГц мыши на CPU, Rapid Trigger клавиатуры и ЦАП"

    override val drawerGlossaryTitle = "Словарь аппаратного обеспечения и терминов"
    override val drawerGlossarySubtitle = "TGP против TDP, 3D V-Cache, ATX 3.1, Hall Effect, термины QD-OLED"

    override val drawerUndervoltTitle = "Руководство по андервольтингу и оптимизации"
    override val drawerUndervoltSubtitle = "Intel Vmin, AMD Curve Optimizer, кривая GPU и деградация RAM"

    override val drawerValuationTitle = "Оценка стоимости б/у ПК"
    override val drawerValuationSubtitle = "Быстрый выкуп, справедливый рынок и цена на витрине по реальным данным"

    override val detailPerformancePower = "ПРОФИЛЬ ПРОИЗВОДИТЕЛЬНОСТИ И ПИТАНИЯ"
    override val detailMultiGameFps = "ЖИВАЯ СИМУЛЯЦИЯ FPS В 12 ИГРАХ"
    override val detailTechSpecs = "ПОДРОБНЫЕ ТЕХНИЧЕСКИЕ ХАРАКТЕРИСТИКИ"
    override val detailPrebuiltAnalysis = "АНАЛИЗ КОМПЛЕКТУЮЩИХ ГОТОВОГО ПК"
    override val detailLaptopSpecs = "СПЕЦИФИКАЦИИ ИГРОВОГО НОУТБУКА"
    override val detailStoreOffers = "ПРОВЕРЕННЫЕ ПРЕДЛОЖЕНИЯ МАГАЗИНОВ"
    override val detailCommunityLedger = "ПРОВЕРЕННЫЙ РЕЕСТР ЦЕН СООБЩЕСТВА"

    override val gameBenchTabSimulated = "📊 Симулированный FPS (Модель)"
    override val gameBenchTabVerified = "🧪 Проверенный Реальный Тест"
    override val gameBenchReferenceRigCpu = "🔬 Эталонный GPU: RTX 5090 32GB (Без Узких Мест)"
    override val gameBenchReferenceRigGpu = "🔬 Эталонный CPU: Ryzen 7 9800X3D (Без Узких Мест)"
    override val gameBenchAvgMinLabel = "Средний и Минимальный FPS"
    override val gameBenchCurrentHardware = "Текущая Модель"
    override val gameBenchNoVerifiedYet = "Для этой модели пока нет подтвержденных лабораторных тестов."
    override val gameBenchSwitchToSim = "Перейти к Симуляции"

    override val valuationHeroTitle = "СКОЛЬКО СТОИТ МОЙ ПК Б/У?"
    override val valuationHeroSubtitle = "Автоматическая оценка сборки ПК по 3 рыночным уровням на основе подтвержденных сделок сообщества."
    override val valuationNoSystem = "Система не выбрана"
    override val valuationActiveParts = "%d дет. выбрано"
    override val valuationTotalMarket = "РЫНОЧНАЯ СТОИМОСТЬ Б/У СИСТЕМЫ"
    override val valuationNewTotal = "Новый: %s"
    override val valuationBreakdownTitle = "ОЦЕНКА ПО КОМПОНЕНТАМ"
    override val valuationTipsTitle = "СОВЕТЫ ПО ПРОДАЖЕ Б/У ЖЕЛЕЗА"
    override val valuationEmptyTitle = "Список сборки пуст"
    override val valuationEmptySubtitle = "Для оценки стоимости выберите детали в Мастере ПК или исследуйте каталог."
    override val valuationGoToWizard = "В Мастер ПК и добавить детали"
    override val uvTabCpu = "Процессор (CPU)"
    override val uvTabGpu = "Видеокарта (GPU)"
    override val uvTabRam = "Память (RAM)"
    override val uvTabCmos = "Сброс BIOS (CMOS)"

    override val activeStatusBadge = "АКТИВНО"
    override val drawerSettingsSubtitle = "Темы, 7 языков, валюта, режим гика"
    override val drawerCustomPriceTitle = "💡 Движок локальных цен (Price Override)"
    override val drawerCustomPriceDesc = "Нажмите на значок цены любого товара, чтобы ввести свою скидочную или б/у цену."

    override val valQuickCash = "Быстрый выкуп"
    override val valMarketValue = "Рыночная цена"
    override val valMintBoxed = "В коробке / Идеал"
    override val valVerifiedDealsFormat = "✓ На основе %d проверенных сделок сообщества"
    override val valTip1Title = "Коробка и чек (+10-15%)"
    override val valTip1Desc = "Железо с коробкой, чеком и гарантией продается в разы быстрее и по максимальной цене."
    override val valTip2Title = "Скриншоты стресс-тестов"
    override val valTip2Desc = "15 минут FurMark, Cinebench и смарт SSD из CrystalDiskInfo повышают доверие покупателей втрое."
    override val valTip3Title = "Чистка от пыли и свежая термопаста"
    override val valTip3Desc = "Очищенный от пыли корпус с аккуратным кабель-менеджментом и свежей пастой сразу привлекает внимание."

    override val glossarySearchPlaceholder = "Поиск термина или метрики (DrMOS, 12V-2x6, QD-OLED, Rapid Trigger, X3D...)"
    override val glossaryCategoryAll = "Все"
    override val glossaryCriticalMetrics = "Ключевые инженерные метрики"
    override val glossaryRelated = "Связано с:"
    override val glossaryEmpty = "Инженерные термины по вашему запросу не найдены."

    override val deskMonitorTitle = "1. ЧЕТКОСТЬ PPI МОНИТОРА И НАГРУЗКА НА GPU"
    override val deskScreenSuffix = "Экран"
    override val deskPixelDensity = "Плотность пикселей"
    override val deskViewingDistance = "Оптимальная дистанция"
    override val deskGpuPixelLoad = "Нагрузка пикселей GPU"
    override val deskMouseTitle = "2. АНАЛИЗАТОР ЧАСТОТЫ ОПРОСА МЫШИ (1000Гц – 8000Гц)"
    override val deskReportInterval = "Интервал опроса: %s мс"
    override val deskKeyboardTitle = "3. СИМУЛЯТОР RAPID TRIGGER (ЭФФЕКТ ХОЛЛА)"
    override val deskKeyDepth = "Ход клавиши: %s мм / 4.0 мм"
    override val deskMechSwitch = "Механический свитч"
    override val deskHallSwitch = "Магнитный эффект Холла"
    override val deskMechActuated = "НАЖАТО (>2.0мм)"
    override val deskMechNotActuated = "НЕ СРАБОТАЛО (<2.0мм)"
    override val deskHallActuated = "АКТИВНО (0.1мм Rapid Trigger)"
    override val deskHallReady = "ГОТОВО"
    override val deskAudioTitle = "4. ИМПЕДАНС НАУШНИКОВ (Ω) И ПОДБОР ЦАП"

    override val ramGuideTitle = "ИНЖЕНЕРНОЕ СРАВНЕНИЕ И ГАЙД ПО ПЛАТФОРМАМ"
    override val ramLatencyWinner = "Победитель по задержке:"
    override val ramBwWinner = "Победитель по пропускной способности:"
    override val ramZenRuleTitle = "Правило 1:1 UCLK для AMD Zen 4 и Zen 5 (AM5)"
    override val ramFormulaTitle = "Математическая формула расчета"
    override val ramFreqLabel = "Частота: %d MT/s"
    override val ramCasLabel = "CAS задержка: CL%d"
    override val ramTrueLatency = "Реальная задержка (True Latency)"
    override val ramBandwidth = "Пропускная способность Dual-Channel"

    override val upsSectionWatts = "1. ЭНЕРГОПОТРЕБЛЕНИЕ СИСТЕМЫ И МОНИТОРА (ВАТТ)"
    override val upsPcWatts = "Потребление ПК в играх"
    override val upsMonitorWatts = "Монитор + Роутер / Периферия"
    override val upsRecommendedTier = "РЕКОМЕНДУЕМЫЙ КЛАСС ИБП"
    override val upsTotalLoad = "Пиковая нагрузка"
    override val upsRawVa = "Расчетные VA (W / 0.6)"
    override val upsSafetyMargin = "+20% запас надежности"
    override val upsRuntimeSection = "ВРЕМЯ РАБОТЫ ОТ БАТАРЕИ ПРИ ОТКЛЮЧЕНИИ СЕТИ"

    override val coolingSection1 = "1. ТЕПЛОВЫДЕЛЕНИЕ CPU И ТИП КУЛЕРА"
    override val coolingCpuTdp = "TDP процессора и Boost нагрузка"
    override val coolingCoolerArch = "Тип и размер кулера"
    override val coolingSection2 = "2. ТЕМПЕРАТУРА В КОМНАТЕ И ВОЗДУШНЫЙ ПОТОК"
    override val coolingRoomTemp = "Температура в комнате"
    override val coolingAirflow = "Поток корпусных кулеров и давление"
    override val coolingSection3 = "3. ОЖИДАЕМАЯ ТЕМПЕРАТУРА И НАСЫЩЕНИЕ"
    override val coolingSaturationCurve = "Кривая теплового насыщения кулера"

    override val drawerToolsSubtitle = "Физика железа, задержка и акустические симуляторы"
    override val userAgeLabel = "Возраст"
    override val userViewProfile = "Профиль"
    override val userLogout = "Выйти"
    override val userGuest = "Гость"
    override val userNotLoggedIn = "Не авторизован"
    override val userLogin = "Войти"
    override val valComponentsCountFormat = "%d компонентов"
    override val valQuickCashDesc = "За 24-48 часов"
    override val valMarketValueDesc = "В среднем 1-2 недели"
    override val valMintBoxedDesc = "Коллекционер / Неспешная"
    override val valNewPriceLabel = "Цена нового: %s"
    override val deskPpiRetina = "✅ Четкость Retina (%d PPI): Пиксели неразличимы глазом, создает %%%d нагрузки на GPU по сравнению с 1080p."
    override val deskPpiBalanced = "✅ Золотое сечение (%d PPI): Идеальный баланс четкости текста и высокой частоты кадров в играх."
    override val deskPpiLow = "⚠️ Низкая плотность (%d PPI): %s на %s может пикселить вблизи (рекомендуется расстояние от %d см)."
    override val deskMouse1k = "Низкая нагрузка на процессор (~1%) • Полная совместимость со всеми CPU и мониторами от 144 Гц."
    override val deskMouse2k = "Сбалансированный киберспорт (~2.5% IRQ) • Оптимальный баланс автономности и плавности движения."
    override val deskMouse4k = "Высокая нагрузка CPU IRQ (~5-8%) • Рекомендуется от Ryzen 5 7500F / i5-13600K и монитор 240 Гц+."
    override val deskMouse8k = "⚠️ Экстремальный 8000 Гц (0.125мс): 8.000 прерываний в секунду! Требует Ryzen 7 7800X3D и монитор от 360 Гц."
    override val deskMechResetDelay = "Палец должен подняться на %s мм (до отметки 1.8мм) для сброса клавиши (~32мс задержка)."
    override val deskHallResetSpeed = "Клавиша сбрасывается при подъеме пальца всего на 0.1 мм (~2мс), персонаж останавливается мгновенно!"
    override val deskAudioDacNeeded = "⚠️ Требуется внешний ЦАП / усилитель! (%d Ω vs %s)"
    override val deskAudioDacOk = "✅ Достаточно мощности для прямого подключения (%d Ω)"
    override val deskAudioDacNeededDesc = "%s (%s) не обеспечивает достаточный размах напряжения для наушников %d Ω; басы ослабевают и громкость падает. Рекомендуется внешний USB-ЦАП/усилитель."
    override val deskAudioDacOkDesc = "%s (%s) обеспечивает полный динамический диапазон для наушников %d Ω без внешнего усилителя."
    override val ramKitA = "1. КОМПЛЕКТ ПАМЯТИ (КИТ А)"
    override val ramKitB = "2. КОМПЛЕКТ СРАВНЕНИЯ (КИТ B)"
    override val ramLatencyWinnerA = "Кит A (%s нс) быстрее на %s нс по времени первого доступа!"
    override val ramLatencyWinnerB = "Кит B (%s нс) быстрее на %s нс по времени первого доступа!"
    override val ramLatencyEqual = "Оба комплекта имеют абсолютно равную реальную задержку (%s нс)!"
    override val ramBwWinnerA = "Кит A (%s ГБ/с) опережает по пропускной способности на +%s ГБ/с."
    override val ramBwWinnerB = "Кит B (%s ГБ/с) опережает по пропускной способности на +%s ГБ/с."
    override val ramBwEqual = "Оба комплекта обеспечивают равную пропускную способность (%s ГБ/с)."
    override val ramZenRuleDesc = "На процессорах AMD Ryzen 7000/9000 контроллер памяти (UCLK) и память (MCLK) работают синхронно 1:1 до 6000 MT/s. При 6400+ делитель переключается в 1:2 (+8-10 нс). Поэтому DDR5 6000 CL30 — золотой стандарт для AM5."
    override val upsRuntimeHeaderTier = "Модель ИБП"
    override val upsRuntimeHeaderBattery = "Батарейный блок"
    override val upsRuntimeHeaderTime = "Расчетное время"
    override val upsRuntimeFormat = "~%d мин"
    override val coolingSelectedCpu = "ВЫБРАНО ИЗ КАТАЛОГА: %s"
    override val coolingCpuSpecsFormat = "TDP: %dW • Реальный PPT/MTP: %dW"
    override val coolingReset = "Сброс"
    override val coolingRecommendedCpu = "Рекомендуемые процессоры:"
    override val coolingSelectedCooler = "ВЫБРАННЫЙ КУЛЕР: %s"
    override val coolingRecommendedCooler = "Рекомендуемые кулеры:"
    override val coolingIdleLoad = "Рабочий стол (Простой)"
    override val coolingGamingLoad = "Игровая нагрузка (~60% PPT)"
    override val coolingStressLoad = "Максимальный рендер / стресс (100% PPT)"
    override val coolingWarningDesc = "❗ Предупреждение: %s выделяет %dW тепла при максимуме кулера %s в %dW. Процессор упрется в лимит %d°C и сбросит частоты (троттлинг)."
    override val coolingSafeDesc = "✅ Инженерное подтверждение: %s легко рассеивает тепловыделение %dW процессора %s с запасом в %dW."
    override val detailLoadingSpecs = "Загрузка технических характеристик..."
    override val detailReturnToList = "Назад к списку"
    override val detailAuthRequiredPrice = "⚠️ Войдите в аккаунт, чтобы отправлять цены или скидки."
    override val detailCustomPriceTag = "🏷️ СВОЯ ЦЕНА"
    override val detailEditPrice = "Изменить ✎"
    override val detailEnterPrice = "Указать цену ✎"
    override val detailPrebuiltAdvantage = "Выгода готовой сборки"
    override val detailPrebuiltBreakdown = "УМНЫЙ АНАЛИЗ СТОИМОСТИ И СБОРКИ"
    override val detailPrebuiltSeparatePrice = "Стоимость компонентов по отдельности"
    override val detailPrebuiltTransfer = "Перенести в конфигуратор и настроить"
    override val detailGpuTgpTitle = "РЕАЛЬНЫЙ TGP ВИДЕОКАРТЫ"
    override val detailAddedToWizard = "Добавлено в сборщик ✓"
    override val detailImportedSystem = "Перенесено в систему ✓"
    override val detailLiveStoreSearch = "ПОИСК В МАГАЗИНАХ В РЕАЛЬНОМ ВРЕМЕНИ"
    override val detailLiveStoreSearchSub = "Проверка цен и наличия в один клик по популярным магазинам:"
    override val detailVerifiedStores = "%d проверено"
    override val detailLiveSearch = "Живой поиск"
    override val detailImageVerifying = "Изображение проверяется / Не сопоставлено"
    // Hardware Categories
    override val catCpu = "Процессор (CPU)"
    override val catGpu = "Видеокарта (GPU)"
    override val catRam = "Оперативная память (RAM)"
    override val catMotherboard = "Материнская плата"
    override val catPsu = "Блок питания (PSU)"
    override val catStorage = "Накопитель (SSD/HDD)"
    override val catCooler = "Кулер для CPU"
    override val catCase = "Корпус"
    override val catPrebuilt = "Готовый ПК"
    override val catLaptop = "Игровой ноутбук"
    override val catMouse = "Игровая мышь"
    override val catKeyboard = "Игровая клавиатура"
    override val catHeadset = "Игровая гарнитура"
    override val catMonitor = "Игровой монитор"

    // Custom Price Dialog
    override val customPriceDialogTitle = "🏷️ Задать свою цену"
    override val customPriceStoreRef = "Базовая цена в магазинах:"
    override val customPriceOutOfStock = "Нет в наличии / Нет цены"
    override val customPriceFieldLabel = "Ваша цена"
    override val customPriceFieldPlaceholder = "напр. 14500"
    override val customPriceErrorInvalid = "Пожалуйста, введите корректную сумму (напр. 5499)."
    override val customPriceNoteLabel = "Примечание / Источник (Необязательно)"
    override val customPriceNotePlaceholder = "напр. Б/У, Скидка..."
    override val customPricePresetOwn = "Куплено мной"
    override val customPricePresetUsed = "Вторичный рынок (Б/У)"
    override val customPricePresetSale = "Акция / Скидка"
    override val customPricePresetImport = "Зарубежный заказ"
    override val customPriceNotice = "Установленная цена сохраняется локально и учитывается с наивысшим приоритетом в Каталоге, расчёте цена/качество в VS Arena и конфигураторе ПК."

    // Auth Sheet
    override val authLoginTitle = "⚡ Войти в аккаунт"
    override val authRegisterTitle = "🚀 Регистрация в сообществе"
    override val authTabLogin = "Вход"
    override val authTabRegister = "Регистрация"
    override val authEmailOrUsername = "Email или Имя пользователя"
    override val authPassword = "Пароль"
    override val authLoggingIn = "Вход в систему..."
    override val authRegistering = "Создание аккаунта..."
    override val authUsernameReq = "Имя пользователя *"
    override val authEmailReq = "Адрес эл. почты *"
    override val authPasswordReq = "Пароль (Минимум 6 символов) *"
    override val authPasswordMinError = "Пароль должен содержать не менее 6 символов."
    override val authLoginFailed = "❌ Пользователь не найден или неверный пароль! Проверьте введённые данные."
    override val authRegisterSuccess = "Регистрация успешна! Добро пожаловать, %s!"
    override val authRegisterFailed = "Не удалось создать аккаунт. Пожалуйста, проверьте данные."

    // User Profile
    override val profileNotLoggedIn = "Вы ещё не вошли в аккаунт"
    override val profileLoginBtn = "⚡ Войти / Регистрация"
    override val profileLogoutBtn = "🚪 Выйти из аккаунта"
    override val profileTitle = "Профиль пользователя"

    // Wizard
    override val wizardSystemCardCopied = "⚡ Карточка сборки скопирована в буфер обмена! Готова к отправке."
    override val wizardTotalPrefix = "Итого"
    override val wizardNoPartSelected = "Компонент не выбран"
    override val wizardAddPart = "+ Добавить компонент"
    override val wizardSelect = "Выбрать"
    override val wizardSelected = "Выбрано"
    override val wizardInteractiveTitle = "Интерактивный конфигуратор ПК"
    override val wizardNotSelected = "Не выбрано"

    // VS Arena
    override val vsCompMode = "🧩 Компонент против Компонента"
    override val vsPrebuiltMode = "🖥️ Готовая система"
    override val vsLaptopMode = "💻 Игровой ноутбук"
    override val vsCorner1 = "Выбрать угол 1"
    override val vsCorner2 = "Выбрать угол 2"
    override val vsArenaTitleFormat = "Арена сравнения: %s"

    // Community Price Ledger
    override val ledgerRefresh = "🔄 Обновить"
    override val ledgerSubmitDealBtn = "+ Сообщить цену / Поделиться скидкой"
    override val ledgerAdminLogout = "Выход администратора"
    override val ledgerReportedPrice = "Указанная цена"
    override val ledgerUserNotePrefix = "💬 Заметка пользователя:"
    override val ledgerProofLinkBtn = "🔗 Проверить / Открыть ссылку-доказательство"
    override val ledgerSubmitSheetTitle = "⚡ Сообщить цену сообщества"
    override val ledgerPriceLabel = "Цена (TL) *"
    override val ledgerDealTypeSelect = "Выберите тип сделки *"
    override val ledgerProofUrlLabel = "Ссылка-доказательство (Обязательно) *"
    override val ledgerNoteFieldLabel = "Описание / Примечание"
    override val ledgerSubmitAction = "🚀 Отправить (На модерацию)"
}
val StringsRU: AppStrings = AppStringsRU()


private class AppStringsJA : AppStrings {
    override val languageCode = "ja"

    override val tabCatalog = "カタログ"
    override val tabWizard = "PC構成ビルダー"
    override val tabVsArena = "VS アリーナ"

    override val settingsTitle = "設定"
    override val appearanceSection = "外観とテーマパレット"
    override val appearanceSubtitle = "ダークスタジオ、クリーンSlate、Cyber-Wood、または純粋OLED"
    override val themeDark = "Obsidian"
    override val themeLight = "Slate"
    override val themeWood = "Walnut"
    override val themeOled = "OLED"
    override val themeCyberpunk = "Cyberpunk"
    override val themeSystem = "システム"

    override val languageRegionSection = "言語と地域"
    override val languageRegionSubtitle = "アプリの言語と通貨表示設定"
    override val languageLabel = "言語設定"
    override val currencyLabel = "通貨設定"

    override val hardwarePrefsSection = "ハードウェア＆構成設定"
    override val hardwarePrefsSubtitle = "自作PCの構成基準とパーツ分析ターゲット"
    override val enthusiastModeLabel = "エンスージアスト（Geek）モード"
    override val enthusiastModeDesc = "VRM電源フェーズ、メモリーDieタイプ、PCIeレーン分岐などの詳細情報を表示"
    override val tempUnitLabel = "温度単位"
    override val lengthUnitLabel = "長さ・サイズの単位"
    override val targetResolutionLabel = "ターゲット解像度"
    override val targetFpsLabel = "目標フレームレート (FPS)"

    override val dataStorageSection = "データとストレージ"
    override val dataStorageSubtitle = "モバイルデータ通信の節約とキャッシュ管理"
    override val dataSaverLabel = "データセーバー"
    override val dataSaverDesc = "通信量を抑えるため、製品ギャラリーでメイン画像のみを読み込みます"
    override val cacheLabel = "画像キャッシュ"
    override val cacheDesc = "端末に一時保存された高解像度ハードウェア製品写真"
    override val clearCacheButton = "キャッシュを消去"
    override val cacheClearedMessage = "画像キャッシュを正常に消去しました。"

    override val sourcesSection = "参考文献とデータ出典"
    override val sourcesSubtitle = "仕様データベース、ベンチマーク、および公式製品写真のソース"
    override val sourcesGpuDesc = "TechPowerUp GPU Database、VideoCardz、GPU-Monkey、AMD＆NVIDIA技術白書"
    override val sourcesCpuDesc = "AMD＆Intel公式技術仕様書、Geekbench 6、Cinebench、Puget Systems"
    override val sourcesPsuDesc = "Cybenetics Labs（ETA＆LAMBDA認証）、Cultists Network PSU Tier List、80 PLUS"
    override val sourcesImagesDesc = "PCPartPicker 1600p CDN、LDLC製品ギャラリー、メーカー公式プレスキット"
    override val sourcesPricesDesc = "PCPartPickerグローバルAPI、Akakçe、Cimri、Amazonストアフロント"

    override val aboutSection = "アプリについて"
    override val aboutSubtitle = "バージョン情報、開発者、オープンソースライセンス"
    override val appVersion = "バージョン"
    override val developer = "開発者"
    override val developerName = "Ömer Yavuz Yiğit"
    override val licenses = "オープンソースライブラリとライセンス"
    override val back = "戻る"
    override val selectLanguageTitle = "言語を選択してください"
    override val selectCurrencyTitle = "通貨を選択してください"

    override val catalogTitle = "HARDWARE WIZARD"
    override val searchPlaceholder = "パーツ名、型番、ブランド（AMD、RTX 4070...）"
    override val segmentIndividual = "単体パーツ"
    override val segmentPeripherals = "周辺機器"
    override val segmentPrebuilt = "完成品PC"
    override val segmentLaptop = "ノートPC"
    override val filterButton = "フィルター"
    override val filterAll = "すべて"
    override val marketPrice = "市場価格"
    override val addToVs = "+ VS"
    override val addToWizard = "+ 構成に追加"
    override val noPriceFallback = "在庫なし / 価格未定"

    override val addToVsArena = "VSアリーナで比較"
    override val fpsMatrixTitle = "12タイトル リアルタイムFPS＆1% Lowマトリックス"
    override val fpsMatrixSubtitle = "競技Eスポーツ（5本）＆重量級AAA（7本）検証マルチプライヤー"
    override val localPriceOverride = "カスタム価格を設定"
    override val localPriceDesc = "クーポン、セール、または中古価格をコスパと予算計算に適用"
    override val enterPrice = "価格を入力"

    override val vsArenaTitle = "直接対決・比較"
    override val vsArenaSubtitle = "CPU、GPU、マザーボード、RAM、SSD、電源、クーラー、ケース"
    override val corner1 = "第1コーナー"
    override val corner2 = "第2コーナー"
    override val selectPart = "+ パーツを選択"
    override val startSampleBattle = "サンプルマッチを即座に開始"
    override val vsDescription = "上のコーナーをタップするか、カタログから「VS」を押して2製品を並べて比較します。"
    override val selectCorner1 = "コーナー1を選択"
    override val selectCorner2 = "コーナー2を選択"

    override val wizardTitle = "PC構成ビルダー"
    override val wizardSubtitle = "スマート互換性・ボトルネック・電源容量AI分析"
    override val totalBudget = "システム合計予算"
    override val clearBuild = "構成をリセット"
    override val themeValorant = "ヴァロラント"
    override val themeCs2 = "CS2"
    override val themeGtaVice = "GTA ヴァイス"
    override val themeWitcher = "ウィッチャー"

    override val drawerMainModules = "メインモジュール"
    override val drawerToolsHeader = "インタラクティブ・エンジニアリングツール"
    override val drawerQuickCatalog = "カタログ"
    override val drawerQuickWizard = "PCビルダー"
    override val drawerQuickVs = "VSアリーナ"
    override val closeButton = "閉じる"

    override val drawerCoolingTitle = "サーマル＆冷却シミュレーター"
    override val drawerCoolingSubtitle = "TDP負荷、室温、ケースエアフロー、AIO水冷熱飽和度分析"
    override val selectCpuCatalog = "カタログからCPUを選択"
    override val selectCoolerCatalog = "カタログからクーラーを選択"
    override val coolingSafeStatus = "❄️ サーマル的に安全＆静音"
    override val coolingWarningStatus = "⚠️ 限界付近（高ファン回転数）"
    override val coolingThrottleStatus = "🔥 サーマルスロットリングの危険"

    override val drawerRamTitle = "RAM実効レイテンシ＆帯域幅"
    override val drawerRamSubtitle = "CL×2000/MT/s実効ナノ秒遅延、1% Low FPS、X3Dキャッシュ効果"

    override val drawerUpsTitle = "UPS＆バッテリー持続時間計算機"
    override val drawerUpsSubtitle = "VA/実効ワット容量、正弦波PFC適合性、停電時動作時間"

    override val drawerDeskTitle = "デスク＆周辺機器ラボ"
    override val drawerDeskSubtitle = "モニターPPI、8KHzマウスCPU負荷、キーボードRapid Trigger＆DAC"

    override val drawerGlossaryTitle = "ハードウェア＆技術用語辞典"
    override val drawerGlossarySubtitle = "TGP vs TDP、3D V-Cache、ATX 3.1、Hall Effect、QD-OLED用語集"

    override val drawerUndervoltTitle = "アンダーボルト＆チューニングガイド"
    override val drawerUndervoltSubtitle = "Intel Vmin、AMD Curve Optimizer、GPU電圧周波数曲線、Bit-Rot"

    override val drawerValuationTitle = "中古PC査定シミュレーター"
    override val drawerValuationSubtitle = "実売データに基づく即現金化・適正相場・出品想定価格"

    override val detailPerformancePower = "性能＆消費電力プロファイル"
    override val detailMultiGameFps = "12大ゲーム実測FPSシミュレーション"
    override val detailTechSpecs = "詳細な技術仕様・スペック"
    override val detailPrebuiltAnalysis = "完成品PCコンポーネント詳細分析"
    override val detailLaptopSpecs = "ゲーミングノート特有スペック"
    override val detailStoreOffers = "検証済みストアの価格・在庫一覧"
    override val detailCommunityLedger = "コミュニティ検証済み実売台帳"

    override val gameBenchTabSimulated = "📊 推定シミュレーション (モデル)"
    override val gameBenchTabVerified = "🧪 検証済み実機ベンチマーク"
    override val gameBenchReferenceRigCpu = "🔬 基準GPU: RTX 5090 32GB (ボトルネックなし)"
    override val gameBenchReferenceRigGpu = "🔬 基準CPU: Ryzen 7 9800X3D (ボトルネックなし)"
    override val gameBenchAvgMinLabel = "平均および最小FPS"
    override val gameBenchCurrentHardware = "現在のハードウェア"
    override val gameBenchNoVerifiedYet = "このモデルの検証済み実機テストはまだありません。"
    override val gameBenchSwitchToSim = "シミュレーションに切り替え"

    override val valuationHeroTitle = "マイPCの中古相場は？"
    override val valuationHeroSubtitle = "検証済みのコミュニティ取引台帳と減価償却モデルに基づき、3つの市場ランクで構成パーツを自動査定します。"
    override val valuationNoSystem = "システム未選択"
    override val valuationActiveParts = "%d個のパーツ選択中"
    override val valuationTotalMarket = "総中古市場相場"
    override val valuationNewTotal = "新品定価: %s"
    override val valuationBreakdownTitle = "パーツ別相場内訳"
    override val valuationTipsTitle = "中古売却のコツと注意事項"
    override val valuationEmptyTitle = "構成パーツが選択されていません"
    override val valuationEmptySubtitle = "中古相場を算出するには、PCウィザードでパーツを選択するかカタログをご覧ください。"
    override val valuationGoToWizard = "PCウィザードを開いてパーツを追加"
    override val uvTabCpu = "プロセッサ (CPU)"
    override val uvTabGpu = "グラフィックボード (GPU)"
    override val uvTabRam = "メモリ (RAM)"
    override val uvTabCmos = "緊急復旧 (CMOS)"

    override val activeStatusBadge = "有効"
    override val drawerSettingsSubtitle = "テーマ・7言語・通貨・ギークモード"
    override val drawerCustomPriceTitle = "💡 カスタム価格エンジン（ローカル上書き）"
    override val drawerCustomPriceDesc = "カタログや製品詳細の価格バッジをタップして、独自の割引価格や中古価格を入力できます。"

    override val valQuickCash = "即現金化"
    override val valMarketValue = "適正相場"
    override val valMintBoxed = "元箱・美品"
    override val valVerifiedDealsFormat = "✓ %d件の検証済み取引実績に基づく"
    override val valTip1Title = "元箱・レシートの効果 (+10-15%)"
    override val valTip1Desc = "元箱・納品書・保証書が揃った製品は売却までの期間が短く、高値で取引されます。"
    override val valTip2Title = "ストレステスト結果の提示"
    override val valTip2Desc = "FurMarkやCinebench、CrystalDiskInfoの健康度スクリーンショットを掲載すると信頼性が大幅に向上します。"
    override val valTip3Title = "清掃とグリス塗り替え"
    override val valTip3Desc = "ホコリを除去し裏配線を整え、CPUグリスを塗り直したPCは写真写りが格段に良くなります。"

    override val glossarySearchPlaceholder = "用語またはメトリクスを検索 (DrMOS, 12V-2x6, QD-OLED, Rapid Trigger, X3D...)"
    override val glossaryCategoryAll = "すべて"
    override val glossaryCriticalMetrics = "重要技術メトリクス"
    override val glossaryRelated = "関連用語:"
    override val glossaryEmpty = "検索条件に一致する技術用語が見つかりませんでした。"

    override val deskMonitorTitle = "1. モニターPPI鮮明度＆GPU描画負荷係数"
    override val deskScreenSuffix = "画面"
    override val deskPixelDensity = "画素密度"
    override val deskViewingDistance = "最適視聴距離"
    override val deskGpuPixelLoad = "GPU描画負荷"
    override val deskMouseTitle = "2. マウスポーリングレート（1000Hz〜8000Hz）アナライザー"
    override val deskReportInterval = "ポーリング間隔: %s ms"
    override val deskKeyboardTitle = "3. キーボード RAPID TRIGGER（磁気ホール効果）シミュレーター"
    override val deskKeyDepth = "キーストローク深度: %s mm / 4.0 mm"
    override val deskMechSwitch = "メカニカルスイッチ"
    override val deskHallSwitch = "磁気ホール効果スイッチ"
    override val deskMechActuated = "押下検知 (>2.0mm)"
    override val deskMechNotActuated = "未検知 (<2.0mm)"
    override val deskHallActuated = "作動中 (0.1mm Rapid Trigger)"
    override val deskHallReady = "待機中"
    override val deskAudioTitle = "4. ヘッドホンインピーダンス（Ω）＆オーディオDAC適合診断"

    override val ramGuideTitle = "メモリ技術比較＆プラットフォーム最適化ガイド"
    override val ramLatencyWinner = "低遅延勝者:"
    override val ramBwWinner = "帯域幅勝者:"
    override val ramZenRuleTitle = "AMD Zen 4＆Zen 5（AM5）1:1 UCLK同期ルール"
    override val ramFormulaTitle = "実効遅延・帯域計算式リファレンス"
    override val ramFreqLabel = "動作クロック: %d MT/s"
    override val ramCasLabel = "CASレイテンシ: CL%d"
    override val ramTrueLatency = "実効ナノ秒遅延 (True Latency)"
    override val ramBandwidth = "デュアルチャネル帯域幅"

    override val upsSectionWatts = "1. PCシステム＆モニター消費電力（W）"
    override val upsPcWatts = "PC高負荷ゲーミング消費電力"
    override val upsMonitorWatts = "モニター＋ルーター／周辺機器消費電力"
    override val upsRecommendedTier = "推奨UPSクラス"
    override val upsTotalLoad = "システムピーク負荷"
    override val upsRawVa = "理論必要VA（W / 0.6）"
    override val upsSafetyMargin = "+20% 安全マージン込み"
    override val upsRuntimeSection = "停電時推定バッテリー駆動時間"

    override val coolingSection1 = "1. CPU発熱量（TDP）＆クーラータイプ"
    override val coolingCpuTdp = "CPU TDP＆ブースト発熱"
    override val coolingCoolerArch = "クーラー構造＆ラジエーターサイズ"
    override val coolingSection2 = "2. 室温環境＆PCケースエアフロー"
    override val coolingRoomTemp = "室内温度"
    override val coolingAirflow = "ケースファン風量＆正圧設計"
    override val coolingSection3 = "3. 推定温度＆熱飽和度分析"
    override val coolingSaturationCurve = "クーラー熱飽和推移グラフ"

    override val drawerToolsSubtitle = "ハードウェア物理・遅延・音響シミュレーター"
    override val userAgeLabel = "年齢"
    override val userViewProfile = "プロフィール"
    override val userLogout = "ログアウト"
    override val userGuest = "ゲストユーザー"
    override val userNotLoggedIn = "未ログイン"
    override val userLogin = "ログイン"
    override val valComponentsCountFormat = "%d個のコンポーネント"
    override val valQuickCashDesc = "24〜48時間以内"
    override val valMarketValueDesc = "平均1〜2週間"
    override val valMintBoxedDesc = "コレクター向け・じっくり売却"
    override val valNewPriceLabel = "新品定価: %s"
    override val deskPpiRetina = "✅ Retina級の鮮明さ (%d PPI): 肉眼で画素を識別不可、1080p比で%%%dのGPU描画負荷が発生します。"
    override val deskPpiBalanced = "✅ 黄金比 (%d PPI): テキストの鮮明さと高FPSゲーミングを両立する最もバランスの良い画素密度。"
    override val deskPpiLow = "⚠️ 低密度 (%d PPI): %s・%sでは近距離でドット感が見える場合があります (最低%d cm離れて視聴推奨)。"
    override val deskMouse1k = "低CPU負荷 (~1%) • 全てのCPUおよび144Hz以上のモニターと完全互換。"
    override val deskMouse2k = "バランスeスポーツモード (~2.5% CPU負荷) • バッテリー持続性と滑らかさの最適バランス。"
    override val deskMouse4k = "高CPU割り込み負荷 (~5-8%) • 最低Ryzen 5 7500F / i5-13600Kおよび240Hz以上のモニター推奨。"
    override val deskMouse8k = "⚠️ 極限 8000Hz (0.125ms): 毎秒8,000回のUSB割り込み！旧世代CPUではカクつきの恐れあり。Ryzen 7 7800X3Dおよび360Hz以上の画面で真価を発揮。"
    override val deskMechResetDelay = "入力を停止するには指を%s mm上（1.8mm地点）まで戻す必要があります（約32msの遅延）。"
    override val deskHallResetSpeed = "指をわずか0.1mm浮かせた瞬間にリセット（約2ms）、キャラクターが即座に静止します！"
    override val deskAudioDacNeeded = "⚠️ 外付けDAC・アンプ必須！ (%d Ω vs %s)"
    override val deskAudioDacOk = "✅ 直挿しで十分な音量・ダイナミックレンジを確保 (%d Ω)"
    override val deskAudioDacNeededDesc = "%s (%s) では %d Ωのヘッドホンに対して十分な電圧スイングを出力できず、低域や最大音量が不足します。外付けUSB DAC/アンプの導入を推奨します。"
    override val deskAudioDacOkDesc = "%s (%s) は外付けアンプなしで %d Ωのヘッドホンをフルダイナミックレンジで駆動できます。"
    override val ramKitA = "1. メモリキット (キットA)"
    override val ramKitB = "2. 比較メモリキット (キットB)"
    override val ramLatencyWinnerA = "キットA (%s ns) の方がアクセス応答時間で %s ns高速です！"
    override val ramLatencyWinnerB = "キットB (%s ns) の方がアクセス応答時間で %s ns高速です！"
    override val ramLatencyEqual = "両キットの実効ナノ秒レイテンシは完全に同等です (%s ns)！"
    override val ramBwWinnerA = "キットA (%s GB/s) が理論帯域幅で +%s GB/s リードしています。"
    override val ramBwWinnerB = "キットB (%s GB/s) が理論帯域幅で +%s GB/s リードしています。"
    override val ramBwEqual = "両キットともに同一のデュアルチャネル帯域幅 (%s GB/s) を発揮します。"
    override val ramZenRuleDesc = "AMD Ryzen 7000/9000ではメモリコントローラ（UCLK）とメモリクロック（MCLK）が6000 MT/sまで1:1同期します。6400 MT/s以上では1:2分周となり遅延が8〜10 ns悪化するため、AM5のスイートスポットはDDR5-6000 CL30です。"
    override val upsRuntimeHeaderTier = "UPSモデル"
    override val upsRuntimeHeaderBattery = "内蔵バッテリー"
    override val upsRuntimeHeaderTime = "推定駆動時間"
    override val upsRuntimeFormat = "約%d分"
    override val coolingSelectedCpu = "カタログ選択中: %s"
    override val coolingCpuSpecsFormat = "公称TDP: %dW • 実測PPT/MTP: %dW"
    override val coolingReset = "リセット"
    override val coolingRecommendedCpu = "推奨リファレンスCPU:"
    override val coolingSelectedCooler = "選択中のクーラー: %s"
    override val coolingRecommendedCooler = "推奨リファレンスクーラー:"
    override val coolingIdleLoad = "デスクトップアイドル"
    override val coolingGamingLoad = "ゲーミング負荷 (~60% PPT)"
    override val coolingStressLoad = "フル負荷レンダリング / Cinebench (100% PPT)"
    override val coolingWarningDesc = "❗ 技術警告: %sはフル負荷時に%dWの発熱を生じますが、%sの最大冷却能力は%dWです。CPU温度は%d°Cに達しサーマルスロットリングが発生します。"
    override val coolingSafeDesc = "✅ 適合確認済み: %sは%sの%dW負荷を%dWの余裕マージンを持って確実に冷却します。"
    override val detailLoadingSpecs = "スペック情報を読み込み中..."
    override val detailReturnToList = "一覧に戻る"
    override val detailAuthRequiredPrice = "⚠️ 価格情報やセール情報を投稿するにはログインしてください。"
    override val detailCustomPriceTag = "🏷️ カスタム価格"
    override val detailEditPrice = "編集 ✎"
    override val detailEnterPrice = "価格入力 ✎"
    override val detailPrebuiltAdvantage = "完成品PCお買い得指数"
    override val detailPrebuiltBreakdown = "パーツ個別購入 vs 完成品価格分析"
    override val detailPrebuiltSeparatePrice = "パーツ個別購入時の合計金額"
    override val detailPrebuiltTransfer = "この構成をPCウィザードに読み込んでカスタマイズ"
    override val detailGpuTgpTitle = "GPU実効TGPワット数"
    override val detailAddedToWizard = "ウィザードに追加済み ✓"
    override val detailImportedSystem = "構成に適用済み ✓"
    override val detailLiveStoreSearch = "オンラインショップリアルタイム価格検索"
    override val detailLiveStoreSearchSub = "ワンクリックで主要ショップの最新在庫と実売価格を検索:"
    override val detailVerifiedStores = "%d件の検証済み"
    override val detailLiveSearch = "リアルタイム検索"
    override val detailImageVerifying = "画像確認中 / 未一致"
    // Hardware Categories
    override val catCpu = "プロセッサー (CPU)"
    override val catGpu = "グラフィックボード (GPU)"
    override val catRam = "メモリ (RAM)"
    override val catMotherboard = "マザーボード"
    override val catPsu = "電源ユニット (PSU)"
    override val catStorage = "ストレージ (SSD/HDD)"
    override val catCooler = "CPUクーラー"
    override val catCase = "PCケース"
    override val catPrebuilt = "完成品PC"
    override val catLaptop = "ゲーミングノートPC"
    override val catMouse = "ゲーミングマウス"
    override val catKeyboard = "ゲーミングキーボード"
    override val catHeadset = "ゲーミングヘッドセット"
    override val catMonitor = "ゲーミングモニター"

    // Custom Price Dialog
    override val customPriceDialogTitle = "🏷️ カスタム価格を設定"
    override val customPriceStoreRef = "ショップ参考価格:"
    override val customPriceOutOfStock = "在庫なし / 価格未定"
    override val customPriceFieldLabel = "カスタム価格"
    override val customPriceFieldPlaceholder = "例: 14500"
    override val customPriceErrorInvalid = "有効な金額を入力してください (例: 5499)。"
    override val customPriceNoteLabel = "メモ / 購入元 (任意)"
    override val customPriceNotePlaceholder = "例: 中古、セール品..."
    override val customPricePresetOwn = "購入価格"
    override val customPricePresetUsed = "中古品"
    override val customPricePresetSale = "セール / 特価"
    override val customPricePresetImport = "海外輸入品"
    override val customPriceNotice = "カスタム価格は端末に保存され、カタログ、VS Arenaのコスパ判定、およびPC構成の合計予算計算で最優先適用されます。"

    // Auth Sheet
    override val authLoginTitle = "⚡ ログイン"
    override val authRegisterTitle = "🚀 コミュニティに参加・登録"
    override val authTabLogin = "ログイン"
    override val authTabRegister = "新規登録"
    override val authEmailOrUsername = "メールアドレスまたはユーザー名"
    override val authPassword = "パスワード"
    override val authLoggingIn = "ログイン中..."
    override val authRegistering = "アカウント作成中..."
    override val authUsernameReq = "ユーザー名 *"
    override val authEmailReq = "メールアドレス *"
    override val authPasswordReq = "パスワード (6文字以上) *"
    override val authPasswordMinError = "パスワードは6文字以上である必要があります。"
    override val authLoginFailed = "❌ ユーザーが見つからないかパスワードが違います。入力内容をご確認ください。"
    override val authRegisterSuccess = "登録が完了しました！ ようこそ、%sさん！"
    override val authRegisterFailed = "アカウント作成に失敗しました。入力内容をご確認ください。"

    // User Profile
    override val profileNotLoggedIn = "ログインしていません"
    override val profileLoginBtn = "⚡ ログイン / 新規登録"
    override val profileLogoutBtn = "🚪 ログアウト"
    override val profileTitle = "ユーザープロフィール"

    // Wizard
    override val wizardSystemCardCopied = "⚡ 構成カードをクリップボードにコピーしました！"
    override val wizardTotalPrefix = "合計"
    override val wizardNoPartSelected = "パーツが選択されていません"
    override val wizardAddPart = "+ パーツを追加"
    override val wizardSelect = "選択"
    override val wizardSelected = "選択中"
    override val wizardInteractiveTitle = "インタラクティブPC構成シミュレータ"
    override val wizardNotSelected = "未選択"

    // VS Arena
    override val vsCompMode = "🧩 単体パーツ比較"
    override val vsPrebuiltMode = "🖥️ 完成品PC比較"
    override val vsLaptopMode = "💻 ゲーミングノート比較"
    override val vsCorner1 = "第1コーナーを選択"
    override val vsCorner2 = "第2コーナーを選択"
    override val vsArenaTitleFormat = "%s 比較アリーナ"

    // Community Price Ledger
    override val ledgerRefresh = "🔄 更新"
    override val ledgerSubmitDealBtn = "+ 価格報告・特価を共有"
    override val ledgerAdminLogout = "管理者ログアウト"
    override val ledgerReportedPrice = "報告された価格"
    override val ledgerUserNotePrefix = "💬 投稿者のメモ:"
    override val ledgerProofLinkBtn = "🔗 証拠リンクを確認・開く"
    override val ledgerSubmitSheetTitle = "⚡ コミュニティ特価・実売価格を報告"
    override val ledgerPriceLabel = "価格 (TL) *"
    override val ledgerDealTypeSelect = "取引タイプを選択 *"
    override val ledgerProofUrlLabel = "購入証明リンク (必須) *"
    override val ledgerNoteFieldLabel = "説明 / 取引メモ"
    override val ledgerSubmitAction = "🚀 報告を送信 (審査待ち)"
}
val StringsJA: AppStrings = AppStringsJA()


fun getAppStrings(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.TR -> StringsTR
    AppLanguage.EN -> StringsEN
    AppLanguage.DE -> StringsDE
    AppLanguage.ES -> StringsES
    AppLanguage.FR -> StringsFR
    AppLanguage.RU -> StringsRU
    AppLanguage.JA -> StringsJA
}

val LocalAppStrings = staticCompositionLocalOf { StringsTR }
val LocalAppCurrency = staticCompositionLocalOf { AppCurrency.TRY }


fun HardwareCategory.localized(strings: AppStrings): String = when (this) {
    HardwareCategory.CPU -> strings.catCpu
    HardwareCategory.GPU -> strings.catGpu
    HardwareCategory.RAM -> strings.catRam
    HardwareCategory.MOTHERBOARD -> strings.catMotherboard
    HardwareCategory.PSU -> strings.catPsu
    HardwareCategory.STORAGE -> strings.catStorage
    HardwareCategory.COOLER -> strings.catCooler
    HardwareCategory.CASE -> strings.catCase
    HardwareCategory.PREBUILT_PC -> strings.catPrebuilt
    HardwareCategory.LAPTOP -> strings.catLaptop
    HardwareCategory.MOUSE -> strings.catMouse
    HardwareCategory.KEYBOARD -> strings.catKeyboard
    HardwareCategory.HEADSET -> strings.catHeadset
    HardwareCategory.MONITOR -> strings.catMonitor
}
