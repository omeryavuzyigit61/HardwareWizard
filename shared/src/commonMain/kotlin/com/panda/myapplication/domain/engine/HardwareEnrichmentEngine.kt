package com.panda.myapplication.domain.engine

import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PrebuiltSpec
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

        // Benchmarks ONLY for CPU and GPU
        if (item.category == com.panda.myapplication.domain.model.HardwareCategory.CPU || item.category == com.panda.myapplication.domain.model.HardwareCategory.GPU) {
            val bench = item.benchmarkInfo
            if (bench != null) {
                val verifiedBadge = if (bench.isVerified) "Doğrulanmış Test" else "Simülasyon"
                badges.add(
                    EngineeringBadge(
                        label = "Performans Benchmark Puanı",
                        value = "${bench.score}/100 • ${bench.tier} ($verifiedBadge)",
                        isHighlighted = true,
                    )
                )
                badges.add(
                    EngineeringBadge(
                        label = "TechPowerUp Bağıl Güç Endeksi",
                        value = "%${bench.relativePerformanceIndex} Bağıl Performans",
                        isHighlighted = bench.relativePerformanceIndex >= 80.0,
                    )
                )
                bench.cpuSynthetic?.let { cb ->
                    val single = cb.cinebenchR23Single ?: cb.cinebench2024Single ?: cb.geekbench6Single
                    val multi = cb.cinebenchR23Multi ?: cb.cinebench2024Multi ?: cb.geekbench6Multi ?: cb.passmarkScore
                    if (single != null || multi != null) {
                        badges.add(
                            EngineeringBadge(
                                label = "CPU Sentetik Benchmark Skoru",
                                value = "Tek: ${single ?: "-"} Puan | Çoklu: ${multi ?: "-"} Puan",
                                isHighlighted = true,
                            )
                        )
                    }
                }
                bench.gpuSynthetic?.let { gpu ->
                    val pts = listOfNotNull(
                        gpu.timeSpyGraphics?.let { "Time Spy: $it pts" },
                        gpu.timeSpyExtreme?.let { "Extreme: $it pts" },
                        gpu.steelNomad?.let { "Steel Nomad: $it pts" },
                    ).joinToString(" | ")
                    if (pts.isNotBlank()) {
                        badges.add(
                            EngineeringBadge(
                                label = "3DMark Sentetik Grafik Puanı",
                                value = pts,
                                isHighlighted = true,
                            )
                        )
                    }
                }
                bench.gameFps?.let { games ->
                    val cp = games.cyberpunk2077?.let { "Cyberpunk: ${it.p1440.toInt()} FPS" }
                    val fh = games.forzaHorizon5?.let { "Forza: ${it.p1440.toInt()} FPS" }
                    val cs = games.counterStrike2?.let { "CS2: ${it.p1440.toInt()} FPS" }
                    val fpsSummary = listOfNotNull(cp, fh, cs).joinToString(" • ")
                    if (fpsSummary.isNotBlank()) {
                        badges.add(
                            EngineeringBadge(
                                label = "Referans Oyun Performansı (1440p Ultra)",
                                value = fpsSummary,
                                isHighlighted = true,
                            )
                        )
                    }
                }
            }
        }

        when (val s = item.spec) {
            is PsuSpec -> {
                badges.add(EngineeringBadge("Güç Değeri", "${s.wattage.watts}W"))
                badges.add(EngineeringBadge("Verimlilik", s.efficiency.displayName, isHighlighted = true))
                s.tierRating?.let { badges.add(EngineeringBadge("PSU Tier Derecesi", it, isHighlighted = true)) }
                s.capacitors?.let { badges.add(EngineeringBadge("Kapasitör Standardı", it, isHighlighted = true)) }
                val atxText = s.atxStandard ?: if (s.isAtx3Compliant || s.supportsPcie5Power) "ATX 3.0 / PCIe 5.0 Uyumlu" else null
                atxText?.let { badges.add(EngineeringBadge("Güç Standardı", it, isHighlighted = true)) }
                s.modularity?.let { badges.add(EngineeringBadge("Kablo Tasarımı", it)) }
                s.pcie12Vhpwr?.let { badges.add(EngineeringBadge("12VHPWR / 12V-2x6", it, isHighlighted = true)) }
            }

            is MotherboardSpec -> {
                s.vrmPhases?.let { badges.add(EngineeringBadge("VRM Faz Yapısı", it, isHighlighted = true)) }
                badges.add(EngineeringBadge("Yonga Seti", s.chipset))
                badges.add(EngineeringBadge("PCIe Veri Yolu", s.pcieGeneration.displayName))
                s.m2Slots?.let { badges.add(EngineeringBadge("M.2 NVMe Slot Sayısı", "${it}x Yuva")) }
                badges.add(EngineeringBadge("Bellek Desteği", "${s.ramSlots}x DIMM ${s.memoryGeneration.displayName}"))
                if (s.hasWifi == true) {
                    badges.add(EngineeringBadge("Kablosuz Ağ", "Wi-Fi Desteği Mevcut", isHighlighted = true))
                }
            }

            is RamSpec -> {
                badges.add(EngineeringBadge("Bellek Standardı", s.generation.displayName, isHighlighted = true))
                badges.add(EngineeringBadge("Frekans Değeri", "${s.speed.mhz} MT/s"))
                badges.add(EngineeringBadge("CAS Gecikmesi", "CL${s.casLatency}"))
                s.firstWordLatencyNs?.let { badges.add(EngineeringBadge("İlk Tepki Süresi", "$it ns")) }
                s.voltage?.let { badges.add(EngineeringBadge("Çalışma Voltajı", "$it V")) }
            }

            is CpuSpec -> {
                badges.add(EngineeringBadge("İşlemci Soketi", s.socket, isHighlighted = true))
                badges.add(EngineeringBadge("Çekirdek Yapısı", "${s.coreCount} Çekirdek / ${s.threadCount} İzlek"))
                s.architecture?.let { badges.add(EngineeringBadge("Üretim Mimarisi", it)) }
                s.l3CacheMb?.let { badges.add(EngineeringBadge("L3 Önbellek", "$it MB", isHighlighted = true)) }
                s.processNode?.let { badges.add(EngineeringBadge("Litografi", it)) }
                if (s.unlockedMultiplier == true) {
                    badges.add(EngineeringBadge("Çarpan Kilidi", "Açık (OC Destekli)", isHighlighted = true))
                }
            }

            is GpuSpec -> {
                badges.add(EngineeringBadge("GPU Grafik Yongası", s.chipset, isHighlighted = true))
                badges.add(EngineeringBadge("Bellek Kapasitesi", "${s.vram.megabytes / 1024} GB ${s.memoryType}"))
                s.busWidthBits?.let { badges.add(EngineeringBadge("Veri Yolu", "$it-bit")) }
                s.powerConnectors?.let { badges.add(EngineeringBadge("Güç Girişi", it)) }
                badges.add(EngineeringBadge("TDP", "${s.tdp.watts}W"))
                if (s.lengthMm != null && s.slotWidth != null) {
                    badges.add(EngineeringBadge("Boyut", "${s.slotWidth} • ${s.lengthMm} mm"))
                }
            }

            is StorageSpec -> {
                badges.add(EngineeringBadge("Depolama Tipi", "${s.mediaType.name} (${s.storageInterface.name})", isHighlighted = true))
                badges.add(EngineeringBadge("Kapasite", "${s.capacity.megabytes / 1024} GB"))
                badges.add(EngineeringBadge("Form Faktörü", s.formFactor.displayName))
                if (s.sequentialReadMbps > 0) {
                    badges.add(EngineeringBadge("Sıralı Okuma", "${s.sequentialReadMbps} MB/s"))
                }
                if (s.sequentialWriteMbps > 0) {
                    badges.add(EngineeringBadge("Sıralı Yazma", "${s.sequentialWriteMbps} MB/s"))
                }
                s.cacheMb?.let { badges.add(EngineeringBadge("DRAM / Önbellek", "$it MB")) }
                if (s.enduranceTbw != null && s.enduranceTbw > 0) {
                    badges.add(EngineeringBadge("Dayanıklılık", "${s.enduranceTbw} TBW"))
                }
            }

            is CoolerSpec -> {
                val coolerTypeName = if (s.type == CoolerType.LIQUID) "Sıvı Soğutma" else "Hava Soğutma"
                badges.add(EngineeringBadge("Soğutma Tipi", coolerTypeName, isHighlighted = true))
                badges.add(EngineeringBadge("Isı Dağıtma Kapasitesi", "${s.maxCoolingTdp.watts}W Termal Tasarım Gücü (TDP)"))
                if (s.type == CoolerType.LIQUID && s.radiatorSizeMm != null && s.radiatorSizeMm > 0) {
                    badges.add(EngineeringBadge("Radyatör Boyutu", "${s.radiatorSizeMm} mm"))
                } else if (s.heightMm != null) {
                    badges.add(EngineeringBadge("Soğutucu Yüksekliği", "${s.heightMm} mm"))
                }
                s.fanRpm?.let { badges.add(EngineeringBadge("Fan Devri", "$it RPM")) }
                s.noiseDba?.let { badges.add(EngineeringBadge("Akustik Seviye", "$it dBA")) }
            }

            is CaseSpec -> {
                badges.add(EngineeringBadge("Kasa Tipi & Form", s.type ?: "Veri Bulunamadı", isHighlighted = true))
                s.maxGpuLengthMm?.let { badges.add(EngineeringBadge("Maks. GPU Uzunluğu", "$it mm", isHighlighted = true)) }
                s.maxCpuCoolerHeightMm?.let { badges.add(EngineeringBadge("CPU Soğutucu Boşluğu", "$it mm")) }
                s.maxRadiatorSizeMm?.let { badges.add(EngineeringBadge("Maks. Radyatör Desteği", "$it mm")) }
                if (s.supportedMotherboardFormFactors.isNotEmpty()) {
                    badges.add(EngineeringBadge("Anakart Desteği", s.supportedMotherboardFormFactors.joinToString(", ") { it.name }))
                }
                s.sidePanel?.let { badges.add(EngineeringBadge("Yan Panel", it)) }
            }

            is PrebuiltSpec -> {
                badges.add(EngineeringBadge("Satıcı / Mağaza", s.storeName, isHighlighted = true))
                badges.add(EngineeringBadge("İşlemci", s.cpuModel))
                badges.add(EngineeringBadge("Ekran Kartı", s.gpuModel, isHighlighted = true))
                badges.add(EngineeringBadge("Anakart", s.motherboardModel))
                badges.add(EngineeringBadge("Bellek (RAM)", s.ramSpec))
                badges.add(EngineeringBadge("Depolama", s.storageSpec))
                badges.add(EngineeringBadge("Kasa & Güç Kaynağı", s.psuCaseSpec))
                if (s.priceAdvantageTry != null && s.priceAdvantageTry > 0.0) {
                    badges.add(EngineeringBadge("Kâr / Fiyat Avantajı", "+${s.priceAdvantageTry.toLong()} ₺ (%${s.advantagePercent.toInt()})", isHighlighted = true))
                }
            }

            is LaptopSpec -> {
                badges.add(EngineeringBadge("Ekran Kartı TGP Gücü", s.gpuWattTgp, isHighlighted = true))
                badges.add(EngineeringBadge("MUX & Optimus", s.muxSwitch, isHighlighted = true))
                badges.add(EngineeringBadge("İşlemci", s.cpuModel))
                badges.add(EngineeringBadge("Ekran Kartı", s.gpuModel))
                badges.add(EngineeringBadge("Panel & Ekran", s.displaySpec, isHighlighted = true))
                badges.add(EngineeringBadge("Bellek (RAM)", s.ramSpec))
                badges.add(EngineeringBadge("Depolama", s.storageSpec))
                badges.add(EngineeringBadge("Batarya", "${s.batteryWh} Wh"))
                badges.add(EngineeringBadge("Ağırlık", "${s.weightGOrKgText()}"))
                if (s.coolingTech.isNotBlank()) {
                    badges.add(EngineeringBadge("Soğutma Mimarisi", s.coolingTech))
                }
            }

            is com.panda.myapplication.domain.model.MouseSpec -> {
                badges.add(EngineeringBadge("Optik Sensör", s.sensor, isHighlighted = true))
                badges.add(EngineeringBadge("Raporlama Hızı", "${s.pollingRateHz} Hz", isHighlighted = s.pollingRateHz >= 4000))
                badges.add(EngineeringBadge("Ağırlık", "${s.weightG}g Ultra-Hafif", isHighlighted = s.weightG <= 60))
                badges.add(EngineeringBadge("Anahtar (Switch)", s.switchType))
                badges.add(EngineeringBadge("Maks. Çözünürlük", "${s.maxDpi} DPI"))
                badges.add(EngineeringBadge("Bağlantı", s.connectivity))
            }

            is com.panda.myapplication.domain.model.KeyboardSpec -> {
                badges.add(EngineeringBadge("Anahtar Mimarisi", s.switchType, isHighlighted = true))
                badges.add(EngineeringBadge("Rapid Trigger", if (s.rapidTriggerSupported) "Destekleniyor (${s.minActuationMm} mm)" else "Standart (${s.minActuationMm} mm)", isHighlighted = s.rapidTriggerSupported))
                badges.add(EngineeringBadge("Raporlama Hızı", "${s.pollingRateHz} Hz"))
                badges.add(EngineeringBadge("Form Faktörü", s.layout))
                badges.add(EngineeringBadge("Hot-Swap", if (s.hotSwap) "Var" else "Sabit"))
            }

            is com.panda.myapplication.domain.model.HeadsetSpec -> {
                badges.add(EngineeringBadge("Sürücü Mimarisi", "${s.driverSizeMm}mm ${s.driverType}", isHighlighted = true))
                badges.add(EngineeringBadge("Empedans", "${s.impedanceOhm} Ω", isHighlighted = s.impedanceOhm >= 80))
                badges.add(EngineeringBadge("DAC / Amfi", if (s.dacAmpRequired) "Harici DAC/Amfi Önerilir" else "Doğrudan Besleme", isHighlighted = s.dacAmpRequired))
                badges.add(EngineeringBadge("Frekans Aralığı", s.frequencyResponse))
                badges.add(EngineeringBadge("Ağırlık", "${s.weightG}g"))
            }

            is com.panda.myapplication.domain.model.MonitorSpec -> {
                badges.add(EngineeringBadge("Panel Teknolojisi", s.panelType, isHighlighted = true))
                badges.add(EngineeringBadge("Çözünürlük & Yenileme", "${s.resolution} @ ${s.refreshRateHz}Hz", isHighlighted = true))
                badges.add(EngineeringBadge("Tepki Süresi", "${s.responseSpeedGtGMs} ms GtG"))
                badges.add(EngineeringBadge("Piksel Yoğunluğu", "${s.ppi} PPI (${s.screenSizeInch}\")"))
                badges.add(EngineeringBadge("HDR Standardı", s.hdrCertification))
            }
        }

        return badges
    }

    private fun LaptopSpec.weightGOrKgText(): String = "${weightKg} kg"
}
