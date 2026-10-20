package com.panda.myapplication.presentation.tools

data class GuideStepItem(
    val step: String,
    val title: String,
    val description: String,
)

data class GuideSectionData(
    val title: String,
    val subtitle: String,
    val description: String,
    val steps: List<GuideStepItem>,
)

data class GuideWarningData(
    val title: String,
    val description: String,
)

data class GuideProtocolData(
    val title: String,
    val points: List<String>,
)

data class GuideShortcutData(
    val title: String,
    val description: String,
    val keyCombo: String,
    val note: String,
)

object UndervoltGuideContent {

    fun getCpuWarning(isTurkish: Boolean): GuideWarningData = if (isTurkish) {
        GuideWarningData(
            title = "İşlemci Voltaj Güvenliği & Silikon Farklılığı (Silicon Lottery)",
            description = "Her silikon çip aynı voltajda çalışmaz. Aşırı negatif voltaj mavi ekrana (BSOD) yol açabilir. Asla test etmeden kalıcı yapmayın.",
        )
    } else {
        GuideWarningData(
            title = "CPU Voltage Safety & Silicon Lottery",
            description = "Not every silicon wafer operates identically at the same voltage. Overly aggressive undervolting can cause sudden blue screens (BSOD). Always stress-test thoroughly before saving permanently.",
        )
    }

    fun getGpuWarning(isTurkish: Boolean): GuideWarningData = if (isTurkish) {
        GuideWarningData(
            title = "Ekran Kartı Frekans Eğrisi & Transient Güç Dalgalanmaları",
            description = "Oyunlarda anlık voltaj sıçramalarında çökmemek için eğriyi stabil tutun. DLSS 3 Frame Gen aktifken voltaj gereksinimi %3-5 artabilir.",
        )
    } else {
        GuideWarningData(
            title = "GPU Voltage Curve & Transient Power Spikes",
            description = "Maintain a stable curve ceiling to prevent crashes during microsecond power excursions. With Frame Generation active, silicon voltage demand can rise by 3-5%.",
        )
    }

    fun getRamWarning(isTurkish: Boolean): GuideWarningData = if (isTurkish) {
        GuideWarningData(
            title = "⚠️ DİKKAT: RAM Bit-Rot & Sessiz Veri Bozulması Riski",
            description = "Kararsız bellek ayarları oyun çökmelerine ek olarak Windows sistem dosyalarını ve kayıt dosyalarınızı sessizce bozabilir (Bit-Rot). TM5 ile 0 error doğrulanmalıdır.",
        )
    } else {
        GuideWarningData(
            title = "⚠️ CAUTION: RAM Bit-Rot & Silent Data Corruption",
            description = "Unstable memory sub-timings cause silent corruption in Windows system files and save data (Bit-Rot) beyond simple game crashes. Always verify 0 errors with TM5.",
        )
    }

    fun getIntelSection(isTurkish: Boolean): GuideSectionData = if (isTurkish) {
        GuideSectionData(
            title = "Intel 13. & 14. Nesil (Raptor Lake & Refresh)",
            subtitle = "Vmin Shift & Yüksek Voltaj Aşınması (Degradation) Önleme",
            description = "13700K/14700K ve 13900K/14900K işlemcilerde anakartların varsayılan olarak 1.50V+ basması silikon kanallarında mikroskobik hasara yol açmaktadır. Bu adımları uygulayarak işlemcinizin ömrünü uzatıp 15-20°C daha serin çalışmasını sağlayın:",
            steps = listOf(
                GuideStepItem("1. Adım", "Microcode 0x12B BIOS Güncellemesi", "Anakart üreticinizin web sitesinden en güncel BIOS sürümünü (Microcode 0x12B içeren) kesinlikle yükleyin."),
                GuideStepItem("2. Adım", "Intel Default Settings Profilini Seçin", "BIOS'ta 'Intel Baseline / Default' profilini açın. ICCMax değerini 307A veya 400A limitinde tutun."),
                GuideStepItem("3. Adım", "CPU Lite Load (AC/DC Loadline) Düşürme", "MSI anakartlarda CPU Lite Load Mode 12'den Mode 6 veya Mode 7'ye çekin. ASUS'ta SVID Behavior 'Typical' yapın."),
                GuideStepItem("4. Adım", "Negatif Vcore Voltaj Offseti", "Core Voltage Mode: Adaptive + Offset seçin. Negatif (-) 0.050V (-50mV) ile başlayın, Cinebench stabilse -0.075V (-75mV)'a kadar inin."),
            ),
        )
    } else {
        GuideSectionData(
            title = "Intel 13th & 14th Gen (Raptor Lake & Refresh)",
            subtitle = "Vmin Shift & High-Voltage Degradation Prevention",
            description = "Default motherboard profiles often push 1.50V+ on 13700K/14700K and 13900K/14900K chips, causing microscopic degradation in silicon vias. Follow these steps to safeguard CPU longevity and run 15-20°C cooler:",
            steps = listOf(
                GuideStepItem("Step 1", "Microcode 0x12B BIOS Update", "Install the latest BIOS update containing Microcode 0x12B directly from your motherboard manufacturer's support portal."),
                GuideStepItem("Step 2", "Select Intel Default Settings Profile", "Enable 'Intel Baseline / Default' profile in BIOS. Enforce ICCMax limit at 307A or 400A."),
                GuideStepItem("Step 3", "Lower CPU Lite Load (AC/DC Loadline)", "On MSI motherboards, drop CPU Lite Load from Mode 12 down to Mode 6 or 7. On ASUS, configure SVID Behavior to 'Typical'."),
                GuideStepItem("Step 4", "Negative Vcore Voltage Offset", "Select Core Voltage Mode: Adaptive + Offset. Start at negative (-) 0.050V (-50mV), stepping down to -0.075V (-75mV) if Cinebench passes."),
            ),
        )
    }

    fun getAmdRyzenSection(isTurkish: Boolean): GuideSectionData = if (isTurkish) {
        GuideSectionData(
            title = "AMD Ryzen 7000 / 8000 / 9000 & 5000 Serisi",
            subtitle = "PBO2 (Precision Boost Overdrive) & Curve Optimizer (CO)",
            description = "Ryzen işlemciler sabit voltaj/çarpan yerine eğri optimizasyonu (Curve Optimizer) ile çok daha yüksek frekansa çıkar ve daha az ısınır:",
            steps = listOf(
                GuideStepItem("PBO Modu", "Advanced PBO Etkinleştirme", "BIOS -> AMD Overclocking -> Precision Boost Overdrive: 'Advanced' olarak seçin. PBO Limits: 'Motherboard' veya manuel PPT/TDC/EDC."),
                GuideStepItem("Curve Optimizer", "Negatif Değer Girişi (All Core veya Per Core)", "Curve Optimizer -> All Cores -> Sign: Negative (-) -> Magnitude: 15 ile başlayın. Kaliteli silikonda -25 veya -30 değerine ulaşabilirsiniz."),
                GuideStepItem("3D V-Cache Uyarısı", "7800X3D / 9800X3D Özel Voltaj Sınırı", "3D V-Cache katmanı 1.30V üzeri SoC voltajında fiziksel hasar görebilir! SoC Voltage değerini manuel 1.20V - 1.25V arasında sabitleyin."),
            ),
        )
    } else {
        GuideSectionData(
            title = "AMD Ryzen 7000 / 8000 / 9000 & 5000 Series",
            subtitle = "PBO2 (Precision Boost Overdrive) & Curve Optimizer (CO)",
            description = "Ryzen processors reach significantly higher boost clocks and run cooler using curve optimization rather than fixed manual voltages:",
            steps = listOf(
                GuideStepItem("PBO Mode", "Enable Advanced PBO", "Navigate to BIOS -> AMD Overclocking -> Precision Boost Overdrive: Select 'Advanced'. Set PBO Limits to 'Motherboard' or manual PPT/TDC/EDC."),
                GuideStepItem("Curve Optimizer", "Enter Negative Offset (All Core or Per Core)", "Curve Optimizer -> All Cores -> Sign: Negative (-) -> Magnitude: Start at 15. Higher quality silicon can sustain -25 to -30."),
                GuideStepItem("3D V-Cache Notice", "7800X3D / 9800X3D Safe SoC Limits", "3D V-Cache layers risk degradation above 1.30V SoC! Manually lock SoC Voltage between 1.20V and 1.25V."),
            ),
        )
    }

    fun getCpuStability(isTurkish: Boolean): GuideProtocolData = if (isTurkish) {
        GuideProtocolData(
            title = "🧪 İşlemci Kararlılık Testi Protokolü",
            points = listOf(
                "1. Cinebench R23 / 2024: 30 dakikalık 'Stability Test' döngüsü çalıştırın. Çökme, mavi ekran veya skor düşüşü olmamalıdır.",
                "2. OCCT CPU Testi (AVX2 & SSE): Düşük yükte (idle geçişlerinde) çekirdek çökmesini tespit etmek için Small Data Set testi yapın.",
                "3. HWInfo64: Test esnasında 'WHEA Errors' sayacını kontrol edin. 1 tane bile WHEA hatası varsa voltaj offsetini 5mV artırın.",
            ),
        )
    } else {
        GuideProtocolData(
            title = "🧪 CPU Stability Testing Protocol",
            points = listOf(
                "1. Cinebench R23 / 2024: Run a 30-minute stability test loop. Ensure zero crashes, BSODs, or score drops.",
                "2. OCCT CPU Test (AVX2 & SSE): Run Small Data Set test to verify stability during light-load and idle core transitions.",
                "3. HWInfo64: Monitor the 'WHEA Errors' counter during testing. If even a single WHEA error occurs, raise voltage offset by 5mV.",
            ),
        )
    }

    fun getGpuNvidiaSection(isTurkish: Boolean): GuideSectionData = if (isTurkish) {
        GuideSectionData(
            title = "NVIDIA GeForce RTX 4000 & 3000 (MSI Afterburner)",
            subtitle = "Voltaj / Frekans Eğrisi (V/F Curve Editor) Ayarı",
            description = "MSI Afterburner ile voltaj/frekans eğrisini optimize ederek aynı performansta %25-35 güç tasarrufu sağlayın:",
            steps = listOf(
                GuideStepItem("1. Adım", "MSI Afterburner Curve Editor Açılışı", "MSI Afterburner ana ekranında Core Clock kaydırıcısını -150MHz yapın. Ardından klavyeden 'Ctrl + F' tuşlarına basarak V/F Curve ekranını açın."),
                GuideStepItem("2. Adım", "Tatlı Noktayı Seçme (Sweet Spot)", "RTX 4070/4080/4090 için: 925mV veya 950mV sütunundaki noktayı bulun. Bu noktayı kartınızın boost frekansına (örn: 2650-2750 MHz) yukarı çekin."),
                GuideStepItem("3. Adım", "Eğriyi Düzleştirme (L Tuşu ile Kilitleme)", "Seçtiğiniz noktanın sağında kalan tüm voltaj noktalarını 'L' tuşu ile veya Shift ile seçerek düz bir yatay çizgi haline getirin. Ana ekranda Apply (✓) butonuna basın."),
                GuideStepItem("Sonuç", "Sıcaklıkta 10-15°C Düşüş, %25-35 Güç Tasarrufu", "Oyun performansınız (FPS) aynı kalırken veya artarken fan sesiniz neredeyse tamamen kesilir."),
            ),
        )
    } else {
        GuideSectionData(
            title = "NVIDIA GeForce RTX 4000 & 3000 (MSI Afterburner)",
            subtitle = "Voltage / Frequency Curve (V/F Curve Editor) Setup",
            description = "Optimize the V/F curve in MSI Afterburner to preserve full gaming framerates while cutting 25-35% power draw:",
            steps = listOf(
                GuideStepItem("Step 1", "Open MSI Afterburner Curve Editor", "Offset Core Clock by -150MHz on the main screen, then press 'Ctrl + F' to bring up the Voltage/Frequency curve graph."),
                GuideStepItem("Step 2", "Select the Sweet Spot Voltage", "For RTX 4070/4080/4090: Locate the 925mV or 950mV column. Drag this point up to your GPU target boost clock (e.g. 2650-2750 MHz)."),
                GuideStepItem("Step 3", "Flatten Plateau & Apply", "Select all voltage points to the right and flatten them into a horizontal ceiling. Click Apply (✓) on the main interface."),
                GuideStepItem("Result", "10-15°C Cooler with 25-35% Less Power", "FPS remains identical or slightly higher, while fan noise drops to whisper-quiet levels."),
            ),
        )
    }

    fun getGpuAmdSection(isTurkish: Boolean): GuideSectionData = if (isTurkish) {
        GuideSectionData(
            title = "AMD Radeon RX 7000 (RDNA3) & RX 6000 (RDNA2)",
            subtitle = "AMD Software Adrenalin Dahili Tuning Rehberi",
            description = "Adrenalin yazılımı üzerinden harici programa ihtiyaç duymadan GPU voltajını düşürün:",
            steps = listOf(
                GuideStepItem("Performans Sekmesi", "Performans -> Ayarlanıyor (Custom Tuning)", "Adrenalin yazılımını açın. 'Özel' (Custom) ayarı seçin ve GPU Ayarı ile Voltaj kaydırıcılarını açın."),
                GuideStepItem("Voltaj Kaydırıcısı", "Milivolt (mV) Düşürme", "Varsayılan 1150mV olan voltajı -50mV azaltarak 1100mV yapın. RX 7800 XT / 7900 GRE gibi modellerde 1025mV - 1050mV arası çok kararlıdır."),
                GuideStepItem("Min-Max Frekans Kuralı", "Takılmaları Önleme (Min Clock Gap)", "Minimum frekansı, maksimum frekansın 100MHz altına ayarlayın (Örn: Maks 2600 ise Min 2500). Bu ayar FPS droplarını engeller."),
            ),
        )
    } else {
        GuideSectionData(
            title = "AMD Radeon RX 7000 (RDNA3) & RX 6000 (RDNA2)",
            subtitle = "AMD Software Adrenalin Built-in Tuning Guide",
            description = "Tune GPU voltage directly within AMD Software without third-party utilities:",
            steps = listOf(
                GuideStepItem("Performance Tab", "Performance -> Tuning -> Custom", "Launch AMD Adrenalin. Select 'Custom' tuning and toggle GPU Tuning and Voltage sliders to Enabled."),
                GuideStepItem("Voltage Slider", "Lower Voltage (mV)", "Step down default 1150mV by -50mV increments to 1100mV. RX 7800 XT and 7900 GRE typically sustain 1025-1050mV rock-solid."),
                GuideStepItem("Min-Max Clock Rule", "Prevent Stutters (100MHz Gap)", "Set Minimum Frequency within 100MHz of Maximum (e.g. Max 2600MHz / Min 2500MHz) to eliminate frametime dips."),
            ),
        )
    }

    fun getGpuShortcut(isTurkish: Boolean): GuideShortcutData = if (isTurkish) {
        GuideShortcutData(
            title = "⚡ GPU Ekran Kartı Sürücü Sıfırlama Kısayolu",
            description = "Oyun esnasında aşırı undervolt sebebiyle ekran siyah kalır veya donarsa bilgisayarı yeniden başlatmak yerine:",
            keyCombo = "⌨️ Win + Ctrl + Shift + B",
            note = "Bu tuş kombinasyonu ekran kartı grafik sürücüsünü 1 saniyede yeniden başlatır ve sizi masaüstüne döndürür.",
        )
    } else {
        GuideShortcutData(
            title = "⚡ GPU Driver Instant Reset Shortcut",
            description = "If an aggressive undervolt freezes the display or leaves a black screen during gaming, instead of a hard reboot:",
            keyCombo = "⌨️ Win + Ctrl + Shift + B",
            note = "This hotkey restarts the Windows graphics driver stack in 1 second, safely recovering your desktop session.",
        )
    }

    fun getRamVoltagesSection(isTurkish: Boolean): GuideSectionData = if (isTurkish) {
        GuideSectionData(
            title = "DDR5 ve DDR4 Güvenli Voltaj Eşikleri & Standartlar",
            subtitle = "Bellek Kontrolcüsü (IMC) ve DRAM Voltaj Sınırları",
            description = "RAM modüllerine voltaj verirken aşırı ısınma ve silikon yıpranmasını önlemek için güvenli tavanlar:",
            steps = listOf(
                GuideStepItem("DDR5 VDD / VDDQ", "1.35V - 1.40V Tatlı Nokta (Sweet Spot)", "XMP/EXPO 6000-6400 MT/s profillerinde 1.35V-1.40V günlük kullanım için güvenlidir. 1.43V üzerine çıkıldığında RAM modülleri üzerine direkt fan üflemesi zorunludur (PMIC aşırı ısınır)."),
                GuideStepItem("AMD AM5 Oranı", "UCLK:MCLK 1:1 Altın Kuralı (6000 MT/s CL30)", "Ryzen 7000/9000 serisinde bellek kontrolcüsü 6000 MT/s hızında 1:1 senkron çalışır. 6400 MT/s üzerine çıktığınızda 1:2 moduna düşerek nanosaniye gecikmesi ciddi oranda artar!"),
                GuideStepItem("DDR4 DRAM Voltage", "1.35V - 1.45V Güvenli Aralık", "DDR4 belleklerde Samsung B-die çipler 1.45V-1.50V kaldırabilirken, Hynix/Micron çiplerde 1.38V üzerinde hata oranları artabilir."),
            ),
        )
    } else {
        GuideSectionData(
            title = "DDR5 & DDR4 Safe Voltage Thresholds & Standards",
            subtitle = "Integrated Memory Controller (IMC) & DRAM Safe Limits",
            description = "Recommended voltage ceilings to prevent thermal runaway and silicon degradation:",
            steps = listOf(
                GuideStepItem("DDR5 VDD / VDDQ", "1.35V - 1.40V Daily Sweet Spot", "For 6000-6400 MT/s EXPO/XMP profiles, 1.35V-1.40V is optimal. Above 1.43V, dedicated active fan airflow over the DIMMs is mandatory to avoid PMIC overheating."),
                GuideStepItem("AMD AM5 Ratio", "UCLK:MCLK 1:1 Golden Rule (6000 MT/s CL30)", "Ryzen 7000/9000 memory controllers run synchronously in 1:1 mode at 6000 MT/s. Exceeding 6400 MT/s drops into 1:2 divider mode, spiking nanosecond latency!"),
                GuideStepItem("DDR4 DRAM Voltage", "1.35V - 1.45V Safe Range", "Samsung B-die scales up to 1.45-1.50V comfortably, whereas Hynix and Micron ICs show error increases above 1.38V."),
            ),
        )
    }

    fun getRamSubtimingsSection(isTurkish: Boolean): GuideSectionData = if (isTurkish) {
        GuideSectionData(
            title = "Alt Zamanlamalar (Sub-timings) & Bit-Rot Uyarısı",
            subtitle = "Bellek Tazeleme Döngüleri ve Veri Bütünlüğü",
            description = "Oyunlarda %1 Low FPS değerini artıran en kritik ayarlar ikincil zamanlamalardır. Ancak aşırı zorlandığında sessiz veri bozulmasına (Bit-Rot) yol açar:",
            steps = listOf(
                GuideStepItem("tREFI (En Riskli)", "Refresh Interval (Tazeleme Aralığı)", "Gecikmeyi en çok düşüren ayardır (Standart 8000'den 32768 veya 65535'e yükseltilir). UYARI: RAM sıcaklığı 50°C'yi geçtiğinde hücreler şarjını kaybeder ve veriler bozulur!"),
                GuideStepItem("tRFC", "Refresh Cycle Time", "Hynix A-die / M-die için 480-500 döngü civarına düşürülebilir. Kararsızlık anında oyun aniden masaüstüne atar."),
                GuideStepItem("tFAW / tRRD", "Dörtlü Aktivasyon Penceresi", "tFAW = 16, tRRDS = 4, tRRDL = 4 kuralı bellek bant genişliğini en üst düzeye çıkarır."),
            ),
        )
    } else {
        GuideSectionData(
            title = "Secondary Sub-Timings & Bit-Rot Warning",
            subtitle = "Memory Refresh Cycles & Silent Data Integrity",
            description = "Sub-timings provide the largest uplift to 1% Low gaming FPS. However, over-tightening risks silent file corruption (Bit-Rot):",
            steps = listOf(
                GuideStepItem("tREFI (Highest Risk)", "Refresh Interval", "Delivers the biggest latency cut (pushed from 8,000 to 32,768 or 65,535). WARNING: If DIMMs exceed 50°C, capacitors bleed charge and corrupt OS files!"),
                GuideStepItem("tRFC", "Refresh Cycle Time", "Hynix A-die/M-die can drop to ~480-500 cycles. Instability will trigger abrupt desktop crashes."),
                GuideStepItem("tFAW / tRRD", "Four Activate Window", "Applying tFAW = 16, tRRDS = 4, tRRDL = 4 rule maximizes continuous burst bandwidth."),
            ),
        )
    }

    fun getRamStability(isTurkish: Boolean): GuideProtocolData = if (isTurkish) {
        GuideProtocolData(
            title = "🧪 RAM Kararlılık Doğrulama Araçları",
            points = listOf(
                "1. TestMem5 (TM5): Anta777 Extreme1 veya 1usmus yapılandırmasıyla en az 3 tam döngü çalıştırın. '0 Errors' almadan sistemi asla günlük kullanmayın.",
                "2. Karhu RAM Test: %10.000 kapsama oranı ile arka plan bellek yazma hatalarını yakalayın.",
                "3. Windows Onarımı: Kararsız RAM testlerinden sonra yönetici komut satırında 'sfc /scannow' çalıştırarak bozulan sistem dosyalarını onarın.",
            ),
        )
    } else {
        GuideProtocolData(
            title = "🧪 RAM Stability Verification Protocol",
            points = listOf(
                "1. TestMem5 (TM5): Run at least 3 full cycles using Anta777 Extreme1 or 1usmus config. Never daily-drive without '0 Errors'.",
                "2. Karhu RAM Test: Run up to 10,000% coverage to detect microscopic memory write bit errors.",
                "3. Windows SFC Repair: After unstable RAM stress testing, run 'sfc /scannow' in an Administrator command prompt to repair corrupted system files.",
            ),
        )
    }

    fun getClearCmosSection(isTurkish: Boolean): GuideSectionData = if (isTurkish) {
        GuideSectionData(
            title = "ACİL KURTARMA: Clear CMOS (BIOS Sıfırlama)",
            subtitle = "Siyah Ekran / No POST Durumunda Donanımı Kurtarma",
            description = "Hatalı RAM zamanlaması veya aşırı undervolt sonucunda bilgisayar hiç açılmazsa (Siyah Ekran / No POST / Anakart DRAM Işığı Yanarsa) korkmayın! Donanımınız yanmadı, sadece BIOS'u sıfırlamanız gerekiyor:",
            steps = listOf(
                GuideStepItem("1. Adım", "Elektrik Bağlantısını Kesin", "Kasanın arkasındaki güç kaynağı (PSU) anahtarını kapatın (0 konumu) ve fişi prizden çekin."),
                GuideStepItem("2. Adım", "Statik Elektriği Boşaltın", "Kasanın önündeki açma/kapama (Power) butonuna 10 saniye boyunca basılı tutarak anakart kondansatörlerindeki kalan tüm akımı boşaltın."),
                GuideStepItem("3. Adım (Yöntem A)", "JBAT1 / CLR_CMOS Jumper Kısa Devresi", "Anakart üzerinde pilin yanında bulunan 'JBAT1' veya 'CLR_CMOS' yazılı 2 adet iğne pine metal bir tornavidanın ucunu 10 saniye boyunca değdirin."),
                GuideStepItem("3. Adım (Yöntem B)", "CR2032 BIOS Pilini Çıkarma", "Eğer jumper pinlerini bulamazsanız, anakart üzerindeki yuvarlak gümüş CR2032 BIOS pilinin tırnağına bastırıp çıkarın. 5 dakika bekledikten sonra pili geri takın."),
                GuideStepItem("4. Adım", "Sistemi Yeniden Başlatın", "Güç kablosunu takıp açın. Anakart 1-2 kez yeniden başlayabilir (Memory Training süreci 1-2 dakika sürebilir). Ardından ekran açılacak ve BIOS varsayılan ayarlarla başlayacaktır!"),
            ),
        )
    } else {
        GuideSectionData(
            title = "EMERGENCY RECOVERY: Clear CMOS (BIOS Reset)",
            subtitle = "Restoring System POST from Black Screen Failures",
            description = "If unstable memory sub-timings or excessive undervolting prevent boot (Black Screen / No POST / Motherboard DRAM LED On), your hardware is completely safe! Only BIOS NVRAM needs a reset:",
            steps = listOf(
                GuideStepItem("Step 1", "Cut Main Power", "Flip the PSU rocker switch to 0 (Off) and unplug the wall power cord completely."),
                GuideStepItem("Step 2", "Discharge Residual Charge", "Hold down the chassis Power button for 10 seconds to drain remaining capacitor energy."),
                GuideStepItem("Step 3 (Method A)", "Short JBAT1 / CLR_CMOS Pins", "Bridge the 2 designated Clear CMOS metal header pins on the motherboard using a flathead screwdriver for 10 seconds (or press rear Clear CMOS button)."),
                GuideStepItem("Step 3 (Method B)", "Remove CR2032 Coin Battery", "If header pins are blocked, press the latch to pop out the silver CR2032 coin battery. Wait 5 minutes, then re-insert."),
                GuideStepItem("Step 4", "Power On & Memory Training", "Plug back in and boot. The system will power cycle 1-2 times to train memory, then boot successfully into default BIOS!"),
            ),
        )
    }
}
