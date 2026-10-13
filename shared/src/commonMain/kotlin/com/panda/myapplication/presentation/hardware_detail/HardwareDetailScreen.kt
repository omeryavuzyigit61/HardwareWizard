package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageFormFactor
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.Wattage
import com.panda.myapplication.domain.engine.HardwareEnrichmentEngine
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.HardwareWizardTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HardwareDetailRoute(
    itemId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HardwareDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HardwareDetailEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    HardwareDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onAddToBuildClick = viewModel::onAddToBuild,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun HardwareDetailContent(
    uiState: HardwareDetailUiState,
    onNavigateBack: () -> Unit,
    onAddToBuildClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailTopBar(
                title = uiState.item?.brand ?: "Donanım Detayı",
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            if (uiState.item != null) {
                DetailBottomBar(
                    isAddedToBuild = uiState.isAddedToBuild,
                    priceText = formatPrice(uiState.item.price),
                    onAddToBuildClick = onAddToBuildClick,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                        )
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Text(
                            text = "Teknik özellikler yükleniyor...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(Spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Text("Listeye Dön", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                uiState.item != null -> {
                    val item = uiState.item
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.medium,
                            end = Spacing.medium,
                            top = Spacing.small,
                            bottom = Spacing.large,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                    ) {
                        // Hero Header Card
                        item {
                            HeroHeaderCard(item = item)
                        }

                        // Price History & Market Analysis (TL) Card
                        item {
                            PriceHistoryCard(
                                priceHistory = item.priceHistory,
                                currentPriceTry = item.currentPriceTry,
                                lowestPriceTry = item.lowestPriceTry,
                                highestPriceTry = item.highestPriceTry,
                            )
                        }

                        // Synthetic Multi-Benchmark (CPU-Monkey & 3DMark) Card
                        if (item.category == HardwareCategory.CPU || item.category == HardwareCategory.GPU || item.benchmarkInfo != null) {
                            item {
                                SyntheticBenchmarkCard(
                                    category = item.category,
                                    benchmarkInfo = item.benchmarkInfo,
                                )
                            }
                        }

                        // Performance & Power Progress Bars
                        item {
                            PerformancePowerSection(item = item)
                        }

                        // Engineering & Tier Specification Chips Card
                        item {
                            EngineeringDetailsSection(item = item)
                        }

                        // Characteristic 2-Column Grid Card
                        item {
                            CharacteristicTableSection(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.small, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(48.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    BackArrowCanvas(color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.width(Spacing.small))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun HeroHeaderCard(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DetailBadge(
                    text = item.category.displayName,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                DetailBadge(
                    text = item.brand.uppercase(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = item.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacing.extraSmall))

            val modelSubtitle = if (item.spec is GpuSpec && !item.spec.partNumber.isNullOrBlank()) {
                "Model: ${item.model} • Parça No (SKU): ${item.spec.partNumber}"
            } else {
                "Model: ${item.model}"
            }
            Text(
                text = modelSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PerformancePowerSection(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "Performans & Güç Göstergeleri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            when (val s = item.spec) {
                is CpuSpec -> {
                    MetricProgressBar(
                        title = "Çekirdek & İzlek Kapasitesi",
                        currentValue = "${s.coreCount} Çekirdek / ${s.threadCount} İzlek",
                        fraction = (s.coreCount / 32f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Maksimum 32 Çekirdek referansına göre",
                    )

                    val clockMhz = (s.boostClock ?: s.baseClock).mhz
                    MetricProgressBar(
                        title = "Maksimum İşlemci Frekansı",
                        currentValue = "${clockMhz / 1000f} GHz",
                        fraction = (clockMhz / 6000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "6.0 GHz tepe saat hızına oranla",
                    )

                    MetricProgressBar(
                        title = "TDP Güç Tüketimi",
                        currentValue = "${s.tdp.watts} W",
                        fraction = (s.tdp.watts / 300f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Soğutucu gereksinim düzeyi",
                    )

                    val multiCoreIndex = ((s.coreCount * 250f + clockMhz) / 14000f).coerceIn(0f, 1f)
                    MetricProgressBar(
                        title = "Sentetik Performans İndeksi",
                        currentValue = "${(multiCoreIndex * 100).toInt()}% Skoru",
                        fraction = multiCoreIndex,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Hesaplama ve render gücü tahmini",
                    )
                }

                is GpuSpec -> {
                    val vramGb = s.vram.megabytes / 1024
                    MetricProgressBar(
                        title = "Video Bellek (VRAM)",
                        currentValue = "$vramGb GB ${s.memoryType}",
                        fraction = (vramGb / 24f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "24 GB referans tepe VRAM",
                    )

                    val gpuClock = (s.boostClock ?: s.coreClock).mhz
                    MetricProgressBar(
                        title = "Grafik Çekirdek Frekansı",
                        currentValue = "$gpuClock MHz",
                        fraction = (gpuClock / 3000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "3000 MHz hedef frekansa göre",
                    )

                    MetricProgressBar(
                        title = "TDP Güç Çekimi",
                        currentValue = "${s.tdp.watts} W",
                        fraction = (s.tdp.watts / 450f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Kartın doğrudan çektiği güç",
                    )

                    MetricProgressBar(
                        title = "Önerilen Güç Kaynağı (PSU)",
                        currentValue = "${s.recommendedPsu.watts} W",
                        fraction = (s.recommendedPsu.watts / 1000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Sistem geneli tavsiye edilen besleme",
                    )
                }

                is RamSpec -> {
                    val capGb = s.capacity.megabytes / 1024
                    MetricProgressBar(
                        title = "Bellek Kapasitesi",
                        currentValue = "$capGb GB (${s.moduleCount}x Modül)",
                        fraction = (capGb / 128f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "128 GB maksimum masaüstü sınırına göre",
                    )

                    MetricProgressBar(
                        title = "Frekans Veri Hızı",
                        currentValue = "${s.speed.mhz} MT/s",
                        fraction = (s.speed.mhz / 8400f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "JEDEC & XMP/EXPO maksimum profili",
                    )

                    val latencyEfficiency = (1f - ((s.casLatency - 14).coerceIn(0, 30) / 30f)).coerceIn(0.1f, 1f)
                    MetricProgressBar(
                        title = "Gecikme Hızı (CAS Latency)",
                        currentValue = "CL${s.casLatency}",
                        fraction = latencyEfficiency,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Daha düşük gecikme = yüksek duyarlılık",
                    )
                }

                is StorageSpec -> {
                    MetricProgressBar(
                        title = "Sıralı Okuma Hızı",
                        currentValue = "${s.sequentialReadMbps} MB/sn",
                        fraction = (s.sequentialReadMbps / 14000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "PCIe Gen 5 sınırına göre (14.000 MB/s)",
                    )

                    MetricProgressBar(
                        title = "Sıralı Yazma Hızı",
                        currentValue = "${s.sequentialWriteMbps} MB/sn",
                        fraction = (s.sequentialWriteMbps / 12000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Dosya kaydetme ve kopyalama hızı",
                    )

                    val storageCapGb = s.capacity.megabytes / 1024
                    MetricProgressBar(
                        title = "Depolama Alanı",
                        currentValue = if (storageCapGb >= 1024) "${storageCapGb / 1024} TB" else "$storageCapGb GB",
                        fraction = (storageCapGb / 4096f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Maksimum 4 TB referans",
                    )
                }

                is CoolerSpec -> {
                    MetricProgressBar(
                        title = "Maksimum Isı Dağıtımı (TDP)",
                        currentValue = "${s.maxCoolingTdp.watts} W",
                        fraction = (s.maxCoolingTdp.watts / 400f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Masaüstü tepe soğutma katsayısı",
                    )

                    val radiatorMm = s.radiatorSizeMm ?: 120
                    MetricProgressBar(
                        title = "Radyatör / Fan Boyutu",
                        currentValue = s.radiatorSizeMm?.let { "${it} mm" } ?: "Hava Soğutma Kulesi",
                        fraction = (radiatorMm / 420f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = if (s.type == CoolerType.LIQUID) "Sıvı soğutma radyatör uzunluğu" else "Standart kule tipi montaj",
                    )
                }

                is PsuSpec -> {
                    MetricProgressBar(
                        title = "Çıkış Gücü Kapasitesi",
                        currentValue = "${s.wattage.watts} W",
                        fraction = (s.wattage.watts / 1600f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "1600 Watt tepe standardına göre",
                    )

                    val effFraction = when (s.efficiency) {
                        PsuEfficiencyRating.TITANIUM -> 0.96f
                        PsuEfficiencyRating.PLATINUM -> 0.92f
                        PsuEfficiencyRating.GOLD -> 0.90f
                        PsuEfficiencyRating.SILVER -> 0.85f
                        PsuEfficiencyRating.BRONZE -> 0.82f
                        PsuEfficiencyRating.STANDARD -> 0.80f
                    }
                    MetricProgressBar(
                        title = "Verimlilik Derecesi",
                        currentValue = s.efficiency.displayName,
                        fraction = effFraction,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "80 PLUS enerji verimlilik sertifikası",
                    )
                }

                is MotherboardSpec -> {
                    MetricProgressBar(
                        title = "Bellek Yuva Desteği",
                        currentValue = "${s.ramSlots} RAM Slotu",
                        fraction = (s.ramSlots / 8f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Maksimum genişletilebilirlik",
                    )

                    val pcieFraction = when (s.pcieGeneration) {
                        PcieGeneration.GEN5 -> 1.0f
                        PcieGeneration.GEN4 -> 0.8f
                        PcieGeneration.GEN3 -> 0.6f
                    }
                    MetricProgressBar(
                        title = "PCI Express Veri Yolu",
                        currentValue = s.pcieGeneration.displayName,
                        fraction = pcieFraction,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Ekran kartı ve NVMe bant genişliği standardı",
                    )
                }

                is CaseSpec -> {
                    MetricProgressBar(
                        title = "Maksimum GPU Uzunluğu",
                        currentValue = "${s.maxGpuLengthMm} mm",
                        fraction = (s.maxGpuLengthMm / 450f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "450 mm referans kasa derinliğine göre",
                    )
                    MetricProgressBar(
                        title = "Maksimum Radyatör Desteği",
                        currentValue = "${s.maxRadiatorSizeMm} mm",
                        fraction = (s.maxRadiatorSizeMm / 420f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Sıvı soğutma radyatör montaj kapasitesi",
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricProgressBar(
    title: String,
    currentValue: String,
    fraction: Float,
    accentColor: Color,
    subText: String,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 600),
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = currentValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.extraSmall))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.extraSmall))

        Text(
            text = subText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private data class EngineeringChipData(
    val label: String,
    val value: String,
    val isHighlighted: Boolean = false,
)

@Composable
private fun EngineeringDetailsSection(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val chips = buildEngineeringChips(item)
    if (chips.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "Teknik Özellikler & Mühendislik Detayları",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                chips.chunked(2).forEach { rowChips ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        rowChips.forEach { chip ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(Spacing.small),
                                color = if (chip.isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, if (chip.isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            ) {
                                Column(modifier = Modifier.padding(Spacing.small)) {
                                    Text(
                                        text = chip.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = chip.value,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (chip.isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                        if (rowChips.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private fun buildEngineeringChips(item: HardwareItem): List<EngineeringChipData> {
    return HardwareEnrichmentEngine.getEngineeringBadges(item).map { badge ->
        EngineeringChipData(
            label = badge.label,
            value = badge.value,
            isHighlighted = badge.isHighlighted,
        )
    }
}

@Composable
private fun CharacteristicTableSection(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val rows = buildCharacteristicRows(item)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "Teknik Karakteristik Tablosu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent)
                        .padding(horizontal = Spacing.small, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1.2f),
                    )
                }
            }
        }
    }
}

private fun buildCharacteristicRows(item: HardwareItem): List<Pair<String, String>> {
    val rows = mutableListOf<Pair<String, String>>()
    rows.add("Marka" to item.brand)
    rows.add("Model Kodu" to item.model)
    rows.add("Kategori" to item.category.displayName)

    when (val s = item.spec) {
        is CpuSpec -> {
            rows.add("Soket Mimarisi" to s.socket)
            rows.add("Çekirdek / İzlek" to "${s.coreCount} Çekirdek / ${s.threadCount} İzlek")
            rows.add("Temel Saat Hızı" to "${s.baseClock.mhz / 1000f} GHz")
            rows.add("Boost Saat Hızı" to (s.boostClock?.let { "${it.mhz / 1000f} GHz" } ?: "Belirtilmemiş"))
            rows.add("TDP Güç Tüketimi" to "${s.tdp.watts} W")
            rows.add("Dahili Grafik (iGPU)" to if (s.hasIntegratedGraphics) "Mevcut" else "Bulunmuyor")
        }
        is GpuSpec -> {
            rows.add("Grafik Yongası" to s.chipset)
            rows.add("VRAM Kapasitesi" to "${s.vram.megabytes / 1024} GB")
            rows.add("Bellek Türü" to s.memoryType)
            rows.add("Çekirdek Frekansı" to "${s.coreClock.mhz} MHz")
            rows.add("Boost Frekansı" to (s.boostClock?.let { "${it.mhz} MHz" } ?: "Belirtilmemiş"))
            rows.add("Güç Tüketimi (TDP)" to "${s.tdp.watts} W")
            rows.add("Önerilen PSU" to "${s.recommendedPsu.watts} W")
            if (!s.partNumber.isNullOrBlank()) {
                rows.add("Fabrika / Parça Kodu (SKU)" to s.partNumber)
            }
        }
        is RamSpec -> {
            rows.add("Bellek Standardı" to s.generation.displayName)
            rows.add("Frekans / Hız" to "${s.speed.mhz} MT/s")
            rows.add("CAS Gecikmesi" to "CL${s.casLatency}")
            rows.add("Toplam Kapasite" to "${s.capacity.megabytes / 1024} GB")
            rows.add("Kit Modül Sayısı" to "${s.moduleCount}x Modül")
        }
        is MotherboardSpec -> {
            rows.add("İşlemci Soketi" to s.socket)
            rows.add("Yonga Seti (Chipset)" to s.chipset)
            rows.add("Form Faktörü" to s.formFactor.displayName)
            rows.add("Bellek Uyumluluğu" to s.memoryGeneration.displayName)
            rows.add("RAM Yuvası Sayısı" to "${s.ramSlots} Slot")
            rows.add("PCIe Sürümü" to s.pcieGeneration.displayName)
        }
        is PsuSpec -> {
            rows.add("Nominal Güç" to "${s.wattage.watts} W")
            rows.add("Verimlilik Sınıfı" to s.efficiency.displayName)
            rows.add("ATX 3.0 Uyumlu" to if (s.isAtx3Compliant) "Evet" else "Hayır")
            rows.add("12VHPWR (PCIe 5.0)" to if (s.supportsPcie5Power) "Destekleniyor" else "Bulunmuyor")
        }
        is StorageSpec -> {
            rows.add("Medya Türü" to s.mediaType.displayName)
            rows.add("Arayüz Bağlantısı" to s.storageInterface.displayName)
            rows.add("Form Faktörü" to s.formFactor.displayName)
            rows.add("PCIe Sürümü" to (s.pcieGeneration?.displayName ?: "SATA"))
            rows.add("Okuma Hızı" to "${s.sequentialReadMbps} MB/sn")
            rows.add("Yazma Hızı" to "${s.sequentialWriteMbps} MB/sn")
            rows.add("Kapasite" to if (s.capacity.megabytes >= 1024) "${s.capacity.megabytes / 1024} GB" else "${s.capacity.megabytes} MB")
        }
        is CoolerSpec -> {
            rows.add("Soğutma Türü" to s.type.displayName)
            rows.add("Radyatör Boyutu" to (s.radiatorSizeMm?.let { "${it} mm" } ?: "Hava Bloğu"))
            rows.add("Maksimum Soğutma (TDP)" to "${s.maxCoolingTdp.watts} W")
            rows.add("Desteklenen Soketler" to s.supportedSockets.joinToString(", "))
        }
        is CaseSpec -> {
            rows.add("Kasa Tipi" to s.type)
            rows.add("Maks. GPU Uzunluğu" to "${s.maxGpuLengthMm} mm")
            rows.add("Maks. CPU Soğutucu Yüksekliği" to "${s.maxCpuCoolerHeightMm} mm")
            rows.add("Maks. Radyatör Boyutu" to "${s.maxRadiatorSizeMm} mm")
            rows.add("Desteklenen Anakartlar" to s.supportedMotherboardFormFactors.joinToString(", ") { it.displayName })
        }
    }
    return rows
}

@Composable
private fun DetailBottomBar(
    isAddedToBuild: Boolean,
    priceText: String,
    onAddToBuildClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Tahmini Fiyat",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            val buttonContainerColor by animateColorAsState(
                targetValue = if (isAddedToBuild) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            )

            Button(
                onClick = onAddToBuildClick,
                shape = RoundedCornerShape(Spacing.small),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = Spacing.small),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAddedToBuild) {
                        CheckMarkCanvas(color = MaterialTheme.colorScheme.onSecondary)
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text(
                            text = "Sihirbaza Eklendi ✓",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    } else {
                        PlusIconCanvas(color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text(
                            text = "Sihirbaza Ekle",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Spacing.extraSmall),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
        )
    }
}

@Composable
private fun BackArrowCanvas(color: Color, modifier: Modifier = Modifier.size(16.dp)) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.6f, size.height * 0.2f)
            lineTo(size.width * 0.3f, size.height * 0.5f)
            lineTo(size.width * 0.6f, size.height * 0.8f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun CheckMarkCanvas(color: Color, modifier: Modifier = Modifier.size(16.dp)) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.5f)
            lineTo(size.width * 0.45f, size.height * 0.75f)
            lineTo(size.width * 0.85f, size.height * 0.25f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun PlusIconCanvas(color: Color, modifier: Modifier = Modifier.size(16.dp)) {
    Canvas(modifier = modifier) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.2f),
            end = Offset(size.width * 0.5f, size.height * 0.8f),
            strokeWidth = 2.5f.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height * 0.5f),
            end = Offset(size.width * 0.8f, size.height * 0.5f),
            strokeWidth = 2.5f.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun formatPrice(price: Money?): String {
    if (price == null) return "Fiyat Belirtilmemiş"
    val major = price.amountMinor / 100
    val minor = (price.amountMinor % 100).toString().padStart(2, '0')
    return when (price.currencyCode) {
        "USD" -> "$$major.$minor"
        "EUR" -> "€$major.$minor"
        "TRY" -> "₺$major,$minor"
        else -> "$major.$minor ${price.currencyCode}"
    }
}

@Preview
@Composable
private fun HardwareDetailContentPreview() {
    HardwareWizardTheme {
        HardwareDetailContent(
            uiState = HardwareDetailUiState(
                isLoading = false,
                item = HardwareItem(
                    id = HardwareId("cpu-1"),
                    brand = "AMD",
                    model = "Ryzen 7 7800X3D",
                    price = Money(amountMinor = 44900, currencyCode = "USD"),
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
                isAddedToBuild = false,
            ),
            onNavigateBack = {},
            onAddToBuildClick = {},
        )
    }
}

private val HardwareCategory.displayName: String
    get() = when (this) {
        HardwareCategory.CPU -> "İşlemci (CPU)"
        HardwareCategory.GPU -> "Ekran Kartı (GPU)"
        HardwareCategory.RAM -> "Bellek (RAM)"
        HardwareCategory.MOTHERBOARD -> "Anakart"
        HardwareCategory.PSU -> "Güç Kaynağı (PSU)"
        HardwareCategory.STORAGE -> "Depolama (SSD/HDD)"
        HardwareCategory.COOLER -> "İşlemci Soğutucu"
        HardwareCategory.CASE -> "Kasa"
    }

private val PsuEfficiencyRating.displayName: String
    get() = when (this) {
        PsuEfficiencyRating.STANDARD -> "80 PLUS Standart"
        PsuEfficiencyRating.BRONZE -> "80 PLUS Bronze"
        PsuEfficiencyRating.SILVER -> "80 PLUS Silver"
        PsuEfficiencyRating.GOLD -> "80 PLUS Gold"
        PsuEfficiencyRating.PLATINUM -> "80 PLUS Platinum"
        PsuEfficiencyRating.TITANIUM -> "80 PLUS Titanium"
    }

private val PcieGeneration.displayName: String
    get() = when (this) {
        PcieGeneration.GEN3 -> "PCIe 3.0"
        PcieGeneration.GEN4 -> "PCIe 4.0"
        PcieGeneration.GEN5 -> "PCIe 5.0"
    }

private val DdrGeneration.displayName: String
    get() = when (this) {
        DdrGeneration.DDR4 -> "DDR4"
        DdrGeneration.DDR5 -> "DDR5"
    }

private val MotherboardFormFactor.displayName: String
    get() = when (this) {
        MotherboardFormFactor.E_ATX -> "E-ATX"
        MotherboardFormFactor.ATX -> "ATX"
        MotherboardFormFactor.MICRO_ATX -> "Micro-ATX"
        MotherboardFormFactor.MINI_ITX -> "Mini-ITX"
    }

private val StorageMediaType.displayName: String
    get() = when (this) {
        StorageMediaType.SSD -> "SSD (Katı Hal Sürücüsü)"
        StorageMediaType.HDD -> "HDD (Sabit Disk)"
    }

private val StorageInterface.displayName: String
    get() = when (this) {
        StorageInterface.NVME -> "NVMe / M.2"
        StorageInterface.SATA -> "SATA III"
    }

private val StorageFormFactor.displayName: String
    get() = when (this) {
        StorageFormFactor.M2_2230 -> "M.2 2230"
        StorageFormFactor.M2_2242 -> "M.2 2242"
        StorageFormFactor.M2_2280 -> "M.2 2280"
        StorageFormFactor.INCH_2_5 -> "2.5 inç"
        StorageFormFactor.INCH_3_5 -> "3.5 inç"
    }

private val CoolerType.displayName: String
    get() = when (this) {
        CoolerType.AIR -> "Kule Tipi Hava Soğutma"
        CoolerType.LIQUID -> "Sıvı Soğutma (AIO)"
    }
