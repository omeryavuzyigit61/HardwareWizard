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
                s.capacitors?.let { badges.add(EngineeringBadge("Kapasitör Markası", it, isHighlighted = true)) }
                if (s.isAtx3Compliant || s.supportsPcie5Power) {
                    badges.add(EngineeringBadge("Güç Standardı", "ATX 3.0 / PCIe 5.0 Uyumlu", isHighlighted = true))
                }
                s.modularity?.let { badges.add(EngineeringBadge("Kablo Tasarımı", it)) }
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
                s.l3CacheMb?.let { badges.add(EngineeringBadge("L3 Önbellek", "$it MB")) }
            }

            is GpuSpec -> {
                badges.add(EngineeringBadge("GPU Grafik Yongası", s.chipset, isHighlighted = true))
                badges.add(EngineeringBadge("Bellek Kapasitesi", "${s.vram.megabytes / 1024} GB ${s.memoryType}"))
                s.busWidthBits?.let { badges.add(EngineeringBadge("Veri Yolu", "$it-bit")) }
                s.powerConnectors?.let { badges.add(EngineeringBadge("Güç Girişi", it)) }
                s.tdp?.let { badges.add(EngineeringBadge("TDP", "${it.watts}W")) }
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
        }

        return badges
    }
}
