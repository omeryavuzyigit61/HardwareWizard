package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.panda.myapplication.presentation.util.AppStrings
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose

private enum class UndervoltTab(val emoji: String) {
    CPU("🔥"),
    GPU("🎮"),
    RAM("⚡"),
    CLEAR_CMOS("🚨");

    fun getTitle(strings: AppStrings): String = when (this) {
        CPU -> strings.uvTabCpu
        GPU -> strings.uvTabGpu
        RAM -> strings.uvTabRam
        CLEAR_CMOS -> strings.uvTabCmos
    }
}

@Composable
fun UndervoltGuideScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(UndervoltTab.CPU) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = LocalAppStrings.current.drawerUndervoltTitle,
                subtitle = LocalAppStrings.current.drawerUndervoltSubtitle,
                icon = "⚡",
                onNavigateBack = onNavigateBack,
                onOpenDrawer = onOpenDrawer,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.medium),
        ) {
            Spacer(modifier = Modifier.height(Spacing.small))

            // Tab Selector Chips (Horizontally Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UndervoltTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = {
                            Text(
                                text = "${tab.emoji} ${tab.getTitle(LocalAppStrings.current)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (tab) {
                                UndervoltTab.CPU -> NeonAmber.copy(alpha = 0.2f)
                                UndervoltTab.GPU -> ElectricBlue.copy(alpha = 0.2f)
                                UndervoltTab.RAM -> NeonEmerald.copy(alpha = 0.2f)
                                UndervoltTab.CLEAR_CMOS -> NeonRose.copy(alpha = 0.2f)
                            },
                            selectedLabelColor = when (tab) {
                                UndervoltTab.CPU -> NeonAmber
                                UndervoltTab.GPU -> ElectricBlue
                                UndervoltTab.RAM -> NeonEmerald
                                UndervoltTab.CLEAR_CMOS -> NeonRose
                            },
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                when (selectedTab) {
                    UndervoltTab.CPU -> {
                        item(key = "cpu_banner") {
                            GuideWarningBanner(
                                title = "İşlemci Voltaj Güvenliği & Silikon Farklılığı (Silicon Lottery)",
                                description = "Her silikon çip aynı voltajda çalışmaz. Aşırı negatif voltaj mavi ekrana (BSOD) yol açabilir. Asla test etmeden kalıcı yapmayın.",
                                accentColor = NeonAmber,
                            )
                        }
                        item(key = "cpu_intel_13_14") {
                            CpuIntel1314Section()
                        }
                        item(key = "cpu_amd_ryzen") {
                            CpuAmdRyzenSection()
                        }
                        item(key = "cpu_stress_test") {
                            CpuStressTestSection()
                        }
                    }
                    UndervoltTab.GPU -> {
                        item(key = "gpu_banner") {
                            GuideWarningBanner(
                                title = "Ekran Kartı Frekans Eğrisi & Transient Güç Dalgalanmaları",
                                description = "Oyunlarda anlık voltaj sıçramalarında çökmemek için eğriyi stabil tutun. DLSS 3 Frame Gen aktifken voltaj gereksinimi %3-5 artabilir.",
                                accentColor = ElectricBlue,
                            )
                        }
                        item(key = "gpu_nvidia") {
                            GpuNvidiaSection()
                        }
                        item(key = "gpu_amd") {
                            GpuAmdSection()
                        }
                        item(key = "gpu_crash_recovery") {
                            GpuCrashRecoverySection()
                        }
                    }
                    UndervoltTab.RAM -> {
                        item(key = "ram_banner") {
                            GuideWarningBanner(
                                title = "⚠️ DİKKAT: RAM Bit-Rot & Sessiz Veri Bozulması Riski",
                                description = "Kararsız bellek ayarları oyun çökmelerine ek olarak Windows sistem dosyalarını ve kayıt dosyalarınızı sessizce bozabilir (Bit-Rot). TM5 ile 0 error doğrulanmalıdır.",
                                accentColor = NeonRose,
                            )
                        }
                        item(key = "ram_ddr5_vs_ddr4") {
                            RamDdr5Ddr4VoltagesSection()
                        }
                        item(key = "ram_subtimings") {
                            RamSubtimingsSection()
                        }
                        item(key = "ram_stability_test") {
                            RamStabilityTestSection()
                        }
                    }
                    UndervoltTab.CLEAR_CMOS -> {
                        item(key = "clear_cmos_guide") {
                            ClearCmosRescueSection()
                        }
                    }
                }
            }
        }
    }
}

// region CPU Sections

@Composable
private fun CpuIntel1314Section() {
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = NeonAmber,
        secondaryColor = ElectricBlue,
        contentPadding = Spacing.medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔵", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "Intel 13. & 14. Nesil (Raptor Lake & Refresh)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                )
            }

            Text(
                text = "Vmin Shift & Yüksek Voltaj Aşınması (Degradation) Önleme",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NeonAmber,
            )

            Text(
                text = "13700K/14700K ve 13900K/14900K işlemcilerde anakartların varsayılan olarak 1.50V+ basması silikon kanallarında mikroskobik hasara yol açmaktadır. Bu adımları uygulayarak işlemcinizin ömrünü uzatıp 15-20°C daha serin çalışmasını sağlayın:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GuideStepCard(
                step = "1. Adım",
                title = "Microcode 0x12B BIOS Güncellemesi",
                description = "Anakart üreticinizin web sitesinden en güncel BIOS sürümünü (Microcode 0x12B içeren) kesinlikle yükleyin.",
            )

            GuideStepCard(
                step = "2. Adım",
                title = "Intel Default Settings Profilini Seçin",
                description = "BIOS'ta 'Intel Baseline / Default' profilini açın. ICCMax değerini 307A veya 400A limitinde tutun.",
            )

            GuideStepCard(
                step = "3. Adım",
                title = "CPU Lite Load (AC/DC Loadline) Düşürme",
                description = "MSI anakartlarda CPU Lite Load Mode 12'den Mode 6 veya Mode 7'ye çekin. ASUS'ta SVID Behavior 'Typical' yapın.",
            )

            GuideStepCard(
                step = "4. Adım",
                title = "Negatif Vcore Voltaj Offseti",
                description = "Core Voltage Mode: Adaptive + Offset seçin. Negatif (-) 0.050V (-50mV) ile başlayın, Cinebench stabilse -0.075V (-75mV)'a kadar inin.",
            )
        }
    }
}

@Composable
private fun CpuAmdRyzenSection() {
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = NeonEmerald,
        secondaryColor = NeonAmber,
        contentPadding = Spacing.medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔴", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "AMD Ryzen 7000 / 8000 / 9000 & 5000 Serisi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonEmerald,
                )
            }

            Text(
                text = "PBO2 (Precision Boost Overdrive) & Curve Optimizer (CO)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
            )

            Text(
                text = "Ryzen işlemciler sabit voltaj/çarpan yerine eğri optimizasyonu (Curve Optimizer) ile çok daha yüksek frekansa çıkar ve daha az ısınır:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GuideStepCard(
                step = "PBO Modu",
                title = "Advanced PBO Etkinleştirme",
                description = "BIOS -> AMD Overclocking -> Precision Boost Overdrive: 'Advanced' olarak seçin. PBO Limits: 'Motherboard' veya manuel PPT/TDC/EDC.",
            )

            GuideStepCard(
                step = "Curve Optimizer",
                title = "Negatif Değer Girişi (All Core veya Per Core)",
                description = "Curve Optimizer -> All Cores -> Sign: Negative (-) -> Magnitude: 15 ile başlayın. Kaliteli silikonda -25 veya -30 değerine ulaşabilirsiniz.",
            )

            GuideStepCard(
                step = "3D V-Cache Uyarısı",
                title = "7800X3D / 9800X3D Özel Voltaj Sınırı",
                description = "3D V-Cache katmanı 1.30V üzeri SoC voltajında fiziksel hasar görebilir! SoC Voltage değerini manuel 1.20V - 1.25V arasında sabitleyin.",
            )
        }
    }
}

@Composable
private fun CpuStressTestSection() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "🧪 İşlemci Kararlılık Testi Protokolü",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
            )
            Text(
                text = "1. Cinebench R23 / 2024: 30 dakikalık 'Stability Test' döngüsü çalıştırın. Çökme, mavi ekran veya skor düşüşü olmamalıdır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "2. OCCT CPU Testi (AVX2 & SSE): Düşük yükte (idle geçişlerinde) çekirdek çökmesini tespit etmek için Small Data Set testi yapın.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "3. HWInfo64: Test esnasında 'WHEA Errors' sayacını kontrol edin. 1 tane bile WHEA hatası varsa voltaj offsetini 5mV artırın.",
                style = MaterialTheme.typography.bodySmall,
                color = NeonAmber,
            )
        }
    }
}

// endregion

// region GPU Sections

@Composable
private fun GpuNvidiaSection() {
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = ElectricBlue,
        secondaryColor = NeonEmerald,
        contentPadding = Spacing.medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🟢", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "NVIDIA GeForce RTX 4000 & 3000 (MSI Afterburner)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                )
            }

            Text(
                text = "Voltaj / Frekans Eğrisi (V/F Curve Editor) Ayarı",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
            )

            GuideStepCard(
                step = "1. Adım",
                title = "MSI Afterburner Curve Editor Açılışı",
                description = "MSI Afterburner ana ekranında Core Clock kaydırıcısını -150MHz yapın. Ardından klavyeden 'Ctrl + F' tuşlarına basarak V/F Curve ekranını açın.",
            )

            GuideStepCard(
                step = "2. Adım",
                title = "Tatlı Noktayı Seçme (Sweet Spot)",
                description = "RTX 4070/4080/4090 için: 925mV veya 950mV sütunundaki noktayı bulun. Bu noktayı kartınızın boost frekansına (örn: 2650-2750 MHz) yukarı çekin.",
            )

            GuideStepCard(
                step = "3. Adım",
                title = "Eğriyi Düzleştirme (L Tuşu ile Kilitleme)",
                description = "Seçtiğiniz noktanın sağında kalan tüm voltaj noktalarını 'L' tuşu ile veya Shift ile seçerek düz bir yatay çizgi haline getirin. Ana ekranda Apply (✓) butonuna basın.",
            )

            GuideStepCard(
                step = "Sonuç",
                title = "Sıcaklıkta 10-15°C Düşüş, %25-35 Güç Tasarrufu",
                description = "Oyun performansınız (FPS) aynı kalırken veya artarken fan sesiniz neredeyse tamamen kesilir.",
            )
        }
    }
}

@Composable
private fun GpuAmdSection() {
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = NeonRose,
        secondaryColor = NeonAmber,
        contentPadding = Spacing.medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔴", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "AMD Radeon RX 7000 (RDNA3) & RX 6000 (RDNA2)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonRose,
                )
            }

            Text(
                text = "AMD Software Adrenalin Dahili Tuning Rehberi",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NeonAmber,
            )

            GuideStepCard(
                step = "Performans Sekmesi",
                title = "Performans -> Ayarlanıyor (Custom Tuning)",
                description = "Adrenalin yazılımını açın. 'Özel' (Custom) ayarı seçin ve GPU Ayarı ile Voltaj kaydırıcılarını açın.",
            )

            GuideStepCard(
                step = "Voltaj Kaydırıcısı",
                title = "Milivolt (mV) Düşürme",
                description = "Varsayılan 1150mV olan voltajı -50mV azaltarak 1100mV yapın. RX 7800 XT / 7900 GRE gibi modellerde 1025mV - 1050mV arası çok kararlıdır.",
            )

            GuideStepCard(
                step = "Min-Max Frekans Kuralı",
                title = "Takılmaları Önleme (Min Clock Gap)",
                description = "Minimum frekansı, maksimum frekansın 100MHz altına ayarlayın (Örn: Maks 2600 ise Min 2500). Bu ayar FPS droplarını engeller.",
            )
        }
    }
}

@Composable
private fun GpuCrashRecoverySection() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "⚡ GPU Ekran Kartı Sürücü Sıfırlama Kısayolu",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
            )
            Text(
                text = "Oyun esnasında aşırı undervolt sebebiyle ekran siyah kalır veya donarsa bilgisayarı yeniden başlatmak yerine:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                Text(
                    text = "⌨️ Win + Ctrl + Shift + B",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonEmerald,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Text(
                text = "Bu tuş kombinasyonu ekran kartı grafik sürücüsünü 1 saniyede yeniden başlatır ve sizi masaüstüne döndürür.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// endregion

// region RAM Sections

@Composable
private fun RamDdr5Ddr4VoltagesSection() {
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = NeonEmerald,
        secondaryColor = ElectricBlue,
        contentPadding = Spacing.medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚡", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "DDR5 ve DDR4 Güvenli Voltaj Eşikleri & Standartlar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonEmerald,
                )
            }

            GuideStepCard(
                step = "DDR5 VDD / VDDQ",
                title = "1.35V - 1.40V Tatlı Nokta (Sweet Spot)",
                description = "XMP/EXPO 6000-6400 MT/s profillerinde 1.35V-1.40V günlük kullanım için güvenlidir. 1.43V üzerine çıkıldığında RAM modülleri üzerine direkt fan üflemesi zorunludur (PMIC aşırı ısınır).",
            )

            GuideStepCard(
                step = "AMD AM5 Oranı",
                title = "UCLK:MCLK 1:1 Altın Kuralı (6000 MT/s CL30)",
                description = "Ryzen 7000/9000 serisinde bellek kontrolcüsü 6000 MT/s hızında 1:1 senkron çalışır. 6400 MT/s üzerine çıktığınızda 1:2 moduna düşerek nanosaniye gecikmesi ciddi oranda artar!",
            )

            GuideStepCard(
                step = "DDR4 DRAM Voltage",
                title = "1.35V - 1.45V Güvenli Aralık",
                description = "DDR4 belleklerde Samsung B-die çipler 1.45V-1.50V kaldırabilirken, Hynix/Micron çiplerde 1.38V üzerinde hata oranları artabilir.",
            )
        }
    }
}

@Composable
private fun RamSubtimingsSection() {
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = NeonRose,
        secondaryColor = NeonAmber,
        contentPadding = Spacing.medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏱️", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "Alt Zamanlamalar (Sub-timings) & Bit-Rot Uyarısı",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonRose,
                )
            }

            Text(
                text = "Oyunlarda %1 Low FPS değerini artıran en kritik ayarlar ikincil zamanlamalardır. Ancak aşırı zorlandığında sessiz veri bozulmasına (Bit-Rot) yol açar:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GuideStepCard(
                step = "tREFI (En Riskli)",
                title = "Refresh Interval (Tazeleme Aralığı)",
                description = "Gecikmeyi en çok düşüren ayardır (Standart 8000'den 32768 veya 65535'e yükseltilir). UYARI: RAM sıcaklığı 50°C'yi geçtiğinde hücreler şarjını kaybeder ve veriler bozulur!",
            )

            GuideStepCard(
                step = "tRFC",
                title = "Refresh Cycle Time",
                description = "Hynix A-die / M-die için 480-500 döngü civarına düşürülebilir. Kararsızlık anında oyun aniden masaüstüne atar.",
            )

            GuideStepCard(
                step = "tFAW / tRRD",
                title = "Dörtlü Aktivasyon Penceresi",
                description = "tFAW = 16, tRRDS = 4, tRRDL = 4 kuralı bellek bant genişliğini en üst düzeye çıkarır.",
            )
        }
    }
}

@Composable
private fun RamStabilityTestSection() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "🧪 RAM Kararlılık Doğrulama Araçları",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
            )
            Text(
                text = "1. TestMem5 (TM5): Anta777 Extreme1 veya 1usmus yapılandırmasıyla en az 3 tam döngü çalıştırın. '0 Errors' almadan sistemi asla günlük kullanmayın.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "2. Karhu RAM Test: %10.000 kapsama oranı ile arka plan bellek yazma hatalarını yakalayın.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "3. Windows Onarımı: Kararsız RAM testlerinden sonra yönetici komut satırında 'sfc /scannow' çalıştırarak bozulan sistem dosyalarını onarın.",
                style = MaterialTheme.typography.bodySmall,
                color = NeonAmber,
            )
        }
    }
}

// endregion

// region Clear CMOS Section

@Composable
private fun ClearCmosRescueSection() {
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = NeonRose,
        secondaryColor = NeonAmber,
        contentPadding = Spacing.medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚨", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "ACİL KURTARMA: Clear CMOS (BIOS Sıfırlama)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonRose,
                )
            }

            Text(
                text = "Hatalı RAM zamanlaması veya aşırı undervolt sonucunda bilgisayar hiç açılmazsa (Siyah Ekran / No POST / Anakart DRAM Işığı Yanarsa) korkmayın! Donanımınız yanmadı, sadece BIOS'u sıfırlamanız gerekiyor:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GuideStepCard(
                step = "1. Adım",
                title = "Elektrik Bağlantısını Kesin",
                description = "Kasanın arkasındaki güç kaynağı (PSU) anahtarını kapatın (0 konumu) ve fişi prizden çekin.",
            )

            GuideStepCard(
                step = "2. Adım",
                title = "Statik Elektriği Boşaltın",
                description = "Kasanın önündeki açma/kapama (Power) butonuna 10 saniye boyunca basılı tutarak anakart kondansatörlerindeki kalan tüm akımı boşaltın.",
            )

            GuideStepCard(
                step = "3. Adım (Yöntem A)",
                title = "JBAT1 / CLR_CMOS Jumper Kısa Devresi",
                description = "Anakart üzerinde pilin yanında bulunan 'JBAT1' veya 'CLR_CMOS' yazılı 2 adet iğne pine metal bir tornavidanın ucunu 10 saniye boyunca değdirin.",
            )

            GuideStepCard(
                step = "3. Adım (Yöntem B)",
                title = "CR2032 BIOS Pilini Çıkarma",
                description = "Eğer jumper pinlerini bulamazsanız, anakart üzerindeki yuvarlak gümüş CR2032 BIOS pilinin tırnağına bastırıp çıkarın. 5 dakika bekledikten sonra pili geri takın.",
            )

            GuideStepCard(
                step = "4. Adım",
                title = "Sistemi Yeniden Başlatın",
                description = "Güç kablosunu takıp açın. Anakart 1-2 kez yeniden başlayabilir (Memory Training süreci 1-2 dakika sürebilir). Ardından ekran açılacak ve BIOS varsayılan ayarlarla başlayacaktır!",
            )
        }
    }
}

// endregion

@Composable
private fun GuideStepCard(
    step: String,
    title: String,
    description: String,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NeonCyan.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = step,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GuideWarningBanner(
    title: String,
    description: String,
    accentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text("⚠️", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(Spacing.small))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
