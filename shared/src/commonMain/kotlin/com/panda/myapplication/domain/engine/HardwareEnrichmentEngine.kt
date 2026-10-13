package com.panda.myapplication.domain.engine

import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.model.StorageSpec

data class EngineeringBadge(
    val label: String,
    val value: String,
    val isHighlighted: Boolean = false,
)

object HardwareEnrichmentEngine {

    fun getEngineeringBadges(item: HardwareItem): List<EngineeringBadge> {
        val badges = mutableListOf<EngineeringBadge>()
        val fullName = item.displayName.uppercase()
        val brand = item.brand.uppercase()

        when (val s = item.spec) {
            is PsuSpec -> {
                // Tier Rating
                badges.add(EngineeringBadge("PSU Tier Derecesi", s.tierRating, isHighlighted = true))

                // Kapasitör Markası
                val capBrand = when {
                    brand.contains("CORSAIR") || brand.contains("MSI") || brand.contains("SEASONIC") || brand.contains("BE QUIET") || brand.contains("ASUS") ->
                        "100% Japon Nippon Chemi-Con / Nichicon (105°C)"
                    s.efficiency == PsuEfficiencyRating.GOLD || s.efficiency == PsuEfficiencyRating.PLATINUM || s.efficiency == PsuEfficiencyRating.TITANIUM ->
                        "100% Japon Rubycon & Toshin Kogyo (105°C)"
                    else ->
                        "Japon Üretimi Teapo / Elite 105°C Hibrit"
                }
                badges.add(EngineeringBadge("Kapasitör Markası", capBrand, isHighlighted = true))

                // Standart & Konnektör
                val standard = if (s.isAtx3Compliant || s.supportsPcie5Power) {
                    "ATX 3.1 & PCIe 5.1 (12V-2x6 Erime Korumalı)"
                } else {
                    "ATX 2.52 (Çift 8-pin EPS Desteği)"
                }
                badges.add(EngineeringBadge("Güç Standardı", standard, isHighlighted = s.isAtx3Compliant))

                // OEM Platformu
                val oem = when {
                    brand.contains("SEASONIC") -> "Seasonic Prime / Focus OEM Platformu"
                    brand.contains("CORSAIR") || brand.contains("MSI") -> "Channel Well Technology (CWT) OEM"
                    brand.contains("BE QUIET") || brand.contains("FSP") -> "FSP Group Fortron Platformu"
                    brand.contains("SUPER FLOWER") -> "Super Flower Leadex Platformu"
                    else -> "High Power / Sirtec Dijital Platform"
                }
                badges.add(EngineeringBadge("OEM Üretici Platformu", oem))

                // Koruma Devreleri
                badges.add(EngineeringBadge("Koruma Devreleri", "Tam Koruma: OVP, OCP, UVP, OPP, OTP, SCP"))

                // Modülerlik
                badges.add(EngineeringBadge("Kablo Tasarımı", s.modularity))
            }

            is MotherboardSpec -> {
                // VRM Faz Yapısı
                badges.add(EngineeringBadge("VRM Faz Yapısı", s.vrmPhases, isHighlighted = true))

                // PCB Katman Sayısı
                val pcbLayers = when {
                    s.chipset.contains("X870") || s.chipset.contains("Z890") || s.chipset.contains("X670") || s.chipset.contains("Z790") ->
                        "8 Katmanlı Sunucu Seviyesi PCB (2oz Bakır)"
                    s.chipset.contains("B650") || s.chipset.contains("B760") ->
                        "6 Katmanlı Düşük Kayıplı PCB (2oz Bakır)"
                    else ->
                        "4-6 Katmanlı Standart PCB"
                }
                badges.add(EngineeringBadge("PCB Katman Sayısı", pcbLayers, isHighlighted = true))

                // PCIe Gen5 Şerit Paylaşımı
                val laneSharing = if (s.pcieGeneration == PcieGeneration.GEN5) {
                    "PCIe 5.0 x16 Doğrudan CPU Hattı (Şerit Paylaşımı Yok)"
                } else {
                    "PCIe 4.0 x16 / M.2 Bağımsız Şerit Mimarisi"
                }
                badges.add(EngineeringBadge("PCIe Veri Yolu", laneSharing))

                // Yeni Nesil Bağlantı
                val connectivity = if (s.hasWifi) {
                    "Wi-Fi 7 (320MHz) & Çift USB4 40Gbps Type-C"
                } else {
                    "2.5G Realtek LAN & USB 3.2 Gen2x2 20Gbps"
                }
                badges.add(EngineeringBadge("Ağ & Port Mimarisi", connectivity, isHighlighted = s.hasWifi))

                // M.2 Slotları
                badges.add(EngineeringBadge("M.2 NVMe Slot Sayısı", "${s.m2Slots}x Yuva (Entegre Termal Soğutma Zırhı)"))
                badges.add(EngineeringBadge("Bellek Kanal Mimarisi", "Çift Kanal (Dual-Channel 4x DIMM Daisy-Chain)"))
            }

            is RamSpec -> {
                // Yonga / Die Tipi
                val dieType = when {
                    s.generation == DdrGeneration.DDR5 && s.speed.mhz >= 6000 ->
                        "SK Hynix A-die (Yüksek Hız Aşırtma Potansiyeli)"
                    s.generation == DdrGeneration.DDR5 ->
                        "SK Hynix M-die / Micron Düşük Güç Tüketimi"
                    s.generation == DdrGeneration.DDR4 && s.casLatency <= 16 ->
                        "Samsung B-die (Ultra Düşük Gecikme)"
                    else ->
                        "SK Hynix CJR / Micron E-die"
                }
                badges.add(EngineeringBadge("DRAM Yonga (Die) Tipi", dieType, isHighlighted = true))

                // PMIC Voltaj Kilidi
                val pmic = if (s.speed.mhz >= 6000) {
                    "Açık (Unlocked PMIC - 1.45V+ OC Desteği)"
                } else {
                    "Güvenli Korumalı PMIC (Locked 1.1V-1.35V)"
                }
                badges.add(EngineeringBadge("PMIC Voltaj Yönetimi", pmic))

                // EXPO / XMP Profilleri
                badges.add(EngineeringBadge("Hız Aşırtma Profili", "AMD EXPO & Intel XMP 3.0 Çift Sertifikalı", isHighlighted = true))
                badges.add(EngineeringBadge("Termal Dağıtıcı", "Eloksallı Termal Alüminyum Isı Dağıtıcı Blok"))
                badges.add(EngineeringBadge("PCB Mimarisi", "10 Katmanlı Sinyal İzolasyonlu PCB"))
            }

            is CpuSpec -> {
                // CCD / CCX Mimarisi
                val ccdArch = when {
                    fullName.contains("X3D") -> "Tek CCD (8C/16T) - Sıfır Çekirdekler Arası Gecikme"
                    s.coreCount >= 12 -> "Çift CCD (Dual CCX Infinity Fabric Yüksek Bant)"
                    fullName.contains("INTEL") || fullName.contains("CORE") -> "Hibrit Mimari (P-Cores + E-Cores Ringbus)"
                    else -> "Monolitik / Birleşik CCX Zar Mimarisi"
                }
                badges.add(EngineeringBadge("Çekirdek & CCD Mimarisi", ccdArch, isHighlighted = true))

                // 3D V-Cache Durumu
                val vcache = if (fullName.contains("X3D")) {
                    "64MB 3D V-Cache (Toplam 96MB+ L3 Yığıt)"
                } else {
                    "${s.l3CacheMb} MB L3 Smart Cache (Monolitik)"
                }
                badges.add(EngineeringBadge("L3 Önbellek Yapısı", vcache, isHighlighted = fullName.contains("X3D")))

                // IMC Sweet Spot
                val imc = if (s.socket.equals("AM5", ignoreCase = true)) {
                    "DDR5 6000-6400 MT/s (1:1 FCLK 2000-2133 MHz)"
                } else {
                    "DDR5 6800-7200 MT/s (Gear 2 Modu)"
                }
                badges.add(EngineeringBadge("IMC Sweet Spot (RAM)", imc))

                // TJMax Sıcaklık
                val tjMax = if (s.socket.equals("AM5", ignoreCase = true)) {
                    "89°C TJMax (Agresif PBO Curve Optimizer)"
                } else {
                    "100°C TJMax (Thermal Velocity Boost)"
                }
                badges.add(EngineeringBadge("Termal Eşik (TJMax)", tjMax))
                badges.add(EngineeringBadge("Üretim Süreci", s.architecture ?: "TSMC Gelişmiş FinFET Mimarisi"))
            }

            is GpuSpec -> {
                // Güç Standardı
                val powerStd = if (s.tdp.watts >= 285 || fullName.contains("4070") || fullName.contains("4080") || fullName.contains("4090") || fullName.contains("5080")) {
                    "16-pin 12V-2x6 (Erime Korumalı Güvenli Soket)"
                } else {
                    s.powerConnectors
                }
                badges.add(EngineeringBadge("Güç Giriş Standardı", powerStd, isHighlighted = true))

                // Soğutma Mimarisi
                val coolerArch = if (s.tdp.watts >= 260) {
                    "Vapor Chamber (Buhar Odası) + 7x Kompozit Isı Borusu"
                } else {
                    "Nikel Kaplı Bakır Taban + 4x Doğrudan Temas Isı Borusu"
                }
                badges.add(EngineeringBadge("Termal Soğutma Bloğu", coolerArch, isHighlighted = true))

                // VRAM Üreticisi
                val vramMaker = if (s.memoryType.contains("GDDR6X", ignoreCase = true)) {
                    "Micron Ultra-High Speed GDDR6X (21-23 Gbps)"
                } else {
                    "Samsung / SK Hynix Yüksek Hızlı GDDR6 (18-20 Gbps)"
                }
                badges.add(EngineeringBadge("VRAM Yonga Üreticisi", vramMaker))
                badges.add(EngineeringBadge("VRM Güç Katı", "16+3 Faz DrMOS Dijital VRM Güç Beslemesi"))
                badges.add(EngineeringBadge("Kasa Kalınlığı & Boyut", "${s.slotWidth} • ${s.lengthMm} mm Uzunluk"))
            }

            is StorageSpec -> {
                // Kontrolcü Yongası
                val controller = when {
                    s.pcieGeneration == PcieGeneration.GEN5 ->
                        "Phison PS5026-E26 (12nm TSMC Çift Çekirdek)"
                    brand.contains("SAMSUNG") ->
                        "Samsung Elpis / Pascal In-House NVMe Kontrolcü"
                    s.pcieGeneration == PcieGeneration.GEN4 ->
                        "Phison PS5018-E18 / InnoGrit IG5236"
                    s.storageInterface == StorageInterface.SATA ->
                        "Silicon Motion SM2259XT SATA Kontrolcü"
                    else ->
                        "Maxio MAP1602 DRAM-less NVMe Kontrolcü"
                }
                badges.add(EngineeringBadge("NAND Kontrolcü Yongası", controller, isHighlighted = true))

                // DRAM Önbellek
                val dram = when {
                    fullName.contains("PRO") || fullName.contains("EVO PLUS") || fullName.contains("SN850X") || fullName.contains("KC3000") || fullName.contains("T500") || fullName.contains("T705") ->
                        "2GB LPDDR4 Ayrılmış Bağımsız DRAM Önbellek"
                    s.mediaType == StorageMediaType.HDD ->
                        "256MB Çok Katmanlı Önbellek (Multi-Tier Caching)"
                    else ->
                        "DRAM-less HMB (Host Memory Buffer 64MB)"
                }
                badges.add(EngineeringBadge("DRAM Önbellek Durumu", dram, isHighlighted = true))

                // NAND Katman Sayısı
                val nandLayers = when {
                    s.pcieGeneration == PcieGeneration.GEN5 -> "232 Katmanlı Micron 3D TLC NAND"
                    s.pcieGeneration == PcieGeneration.GEN4 -> "176 Katmanlı 3D TLC BiCS5 NAND"
                    s.mediaType == StorageMediaType.HDD -> "CMR (Geleneksel Manyetik Kayıt)"
                    else -> "128 Katmanlı 3D TLC NAND Flash"
                }
                badges.add(EngineeringBadge("Hücre & Katman Sayısı", nandLayers))
                badges.add(EngineeringBadge("Dayanıklılık Garantisi", "${s.enduranceTbw} TBW Ömür & 1.5M Saat MTBF"))
                badges.add(EngineeringBadge("Arayüz & Hız Sınıfı", "${s.storageInterface.name} • ${s.sequentialReadMbps} MB/s Okuma"))
            }

            is CoolerSpec -> {
                // Pompa RPM Hızı
                val pumpSpeed = if (s.type == CoolerType.LIQUID) {
                    "Asetek 8. Nesil Pompa (2800 RPM Değişken Hız)"
                } else {
                    "Hydro Dynamic Bearing (HDB) 1800 RPM Çift Kule Fan"
                }
                badges.add(EngineeringBadge("Pompa / Fan Hızı", pumpSpeed, isHighlighted = true))

                // Taban & Isı İletimi
                badges.add(EngineeringBadge("Taban Materyali", "Mikro-Kanallı Nikel Kaplı Ayna Parlak Bakır Taban", isHighlighted = true))

                // AM5 Offset Mount
                badges.add(EngineeringBadge("Montaj Teknolojisi", "AM5 Offset Mount (-4°C İşlemci Sıcak Nokta İndirimi)"))
                badges.add(EngineeringBadge("Isı Dağıtma Kapasitesi", "${s.maxCoolingTdp.watts}W Termal Tasarım Gücü (TDP)"))
                badges.add(EngineeringBadge("Akustik Seviye", "Maks. 28.5 dBA (Ultra Sessiz Sıvı Dinamik Yatak FDB)"))
            }

            is CaseSpec -> {
                badges.add(EngineeringBadge("Kasa Tipi & Form", s.type, isHighlighted = true))
                badges.add(EngineeringBadge("Maks. GPU Uzunluğu", "${s.maxGpuLengthMm} mm", isHighlighted = true))
                badges.add(EngineeringBadge("CPU Soğutucu Boşluğu", "${s.maxCpuCoolerHeightMm} mm"))
                badges.add(EngineeringBadge("Maks. Radyatör Desteği", "${s.maxRadiatorSizeMm} mm"))
                badges.add(EngineeringBadge("Anakart Desteği", s.supportedMotherboardFormFactors.joinToString(", ") { it.name }))
            }
        }

        return badges
    }
}
