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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.CircularProgressIndicator
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.panda.myapplication.domain.model.StoreOffer
import com.panda.myapplication.presentation.util.formatPrice
import com.panda.myapplication.presentation.util.formatPriceWithCurrency
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
                    priceText = uiState.item.currentPriceTry.formatPriceWithCurrency("Stok / Fiyat Yok"),
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

                        // Store Offers Comparison (Canlı TR Piyasası) Card
                        item {
                            StoreOffersCard(
                                offers = item.offers,
                                currentPriceTry = item.currentPriceTry,
                                productName = item.displayName,
                            )
                        }

                        // Synthetic Multi-Benchmark Card - STRICTLY CpuSpec & GpuSpec
                        if ((item.category == HardwareCategory.CPU || item.category == HardwareCategory.GPU) && item.benchmarkInfo != null) {
                            item {
                                SyntheticBenchmarkCard(
                                    category = item.category,
                                    benchmarkInfo = item.benchmarkInfo,
                                )
                            }
                        }

                        // Performance & Power Progress Bars - STRICTLY CpuSpec & GpuSpec
                        if (item.category == HardwareCategory.CPU || item.category == HardwareCategory.GPU) {
                            item {
                                PerformancePowerSection(item = item)
                            }
                        }

                        // Category-specific Polymorphic Technical Specifications Section
                        item {
                            PolymorphicTechnicalSpecsSection(item = item)
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
            HardwareDetailHeroImage(
                imageUrl = item.effectiveImageUrl,
                category = item.category,
                displayName = item.displayName,
            )
            Spacer(modifier = Modifier.height(Spacing.medium))

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
                        PcieGeneration.GEN2 -> 0.4f
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
                    s.maxGpuLengthMm?.let { gpuLen ->
                        MetricProgressBar(
                            title = "Maksimum GPU Uzunluğu",
                            currentValue = "$gpuLen mm",
                            fraction = (gpuLen / 450f).coerceIn(0f, 1f),
                            accentColor = MaterialTheme.colorScheme.primary,
                            subText = "450 mm referans kasa derinliğine göre",
                        )
                    }
                    s.maxRadiatorSizeMm?.let { radSize ->
                        MetricProgressBar(
                            title = "Maksimum Radyatör Desteği",
                            currentValue = "$radSize mm",
                            fraction = (radSize / 420f).coerceIn(0f, 1f),
                            accentColor = MaterialTheme.colorScheme.secondary,
                            subText = "Sıvı soğutma radyatör montaj kapasitesi",
                        )
                    }
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
private fun PolymorphicTechnicalSpecsSection(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val rows = buildPolymorphicSpecRows(item)

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "Teknik Özellikler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                DetailBadge(
                    text = item.category.displayName,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
                        color = if (value == "Veri Bulunamadı" || value == "Belirtilmedi") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1.3f),
                    )
                }
            }
        }
    }
}

private fun buildPolymorphicSpecRows(item: HardwareItem): List<Pair<String, String>> {
    val rows = mutableListOf<Pair<String, String>>()
    rows.add("Marka" to item.brand)
    rows.add("Model Kodu" to item.model)

    when (val s = item.spec) {
        is CpuSpec -> {
            rows.add("Soket" to s.socket)
            rows.add("Çekirdek / İzlek" to "${s.coreCount} Çekirdek / ${s.threadCount} İzlek")
            rows.add("Temel Saat Hızı" to "${s.baseClock.mhz / 1000f} GHz")
            val boostText = s.boostClock?.let { "${it.mhz / 1000f} GHz" } ?: "Veri Bulunamadı"
            rows.add("Turbo / Boost Frekans" to boostText)
            rows.add("L2 Önbellek" to (s.l2CacheMb?.let { "${it} MB" } ?: "Veri Bulunamadı"))
            rows.add("L3 Önbellek" to (s.l3CacheMb?.let { "${it} MB" } ?: "Veri Bulunamadı"))
            rows.add("TDP Güç Tüketimi" to "${s.tdp.watts} W")
            rows.add("Üretim Teknolojisi (Litografi)" to (s.processNode ?: "Veri Bulunamadı"))
            rows.add("Mimari" to (s.architecture ?: "Veri Bulunamadı"))
            rows.add("PCIe Veri Yolu Sürümü" to (s.pcieVersion ?: "Belirtilmedi"))
            rows.add("Maks. Bellek Desteği" to (s.maxMemorySpeed ?: "Belirtilmedi"))
            rows.add("Dahili Grafik (iGPU)" to if (s.hasIntegratedGraphics) "Mevcut (Dahili Ekran Kartı)" else "Bulunmuyor (Harici GPU Gerekli)")
            rows.add("Çarpan Kilidi (Overclock)" to if (s.unlockedMultiplier == true) "Açık (Unlocked)" else if (s.unlockedMultiplier == false) "Kapalı (Locked)" else "Belirtilmedi")
        }

        is GpuSpec -> {
            rows.add("Grafik İşlemci (GPU Çipi)" to s.chipset)
            rows.add("VRAM Kapasitesi" to "${s.vram.megabytes / 1024} GB")
            rows.add("Bellek Tipi" to s.memoryType)
            rows.add("Bellek Veri Yolu (Bus Width)" to (s.busWidthBits?.let { "$it-bit" } ?: "Belirtilmedi"))
            rows.add("Temel Çekirdek Frekansı" to "${s.coreClock.mhz} MHz")
            val boostText = s.boostClock?.let { "${it.mhz} MHz" } ?: "Belirtilmedi"
            rows.add("Boost Çekirdek Frekansı" to boostText)
            rows.add("TDP Güç Tüketimi" to "${s.tdp.watts} W")
            rows.add("Önerilen Güç Kaynağı (PSU)" to "${s.recommendedPsu.watts} W")
            rows.add("Kart Uzunluğu" to (s.lengthMm?.let { "$it mm" } ?: "Belirtilmedi"))
            rows.add("Slot Genişliği" to (s.slotWidth ?: "Belirtilmedi"))
            rows.add("Güç Girişleri" to (s.powerConnectors ?: "Belirtilmedi"))
            rows.add("Görüntü Çıkışları" to (s.displayOutputs ?: "Belirtilmedi"))
            rows.add("Parça Numarası (Part Number)" to (s.partNumber ?: item.model))
        }

        is MotherboardSpec -> {
            rows.add("Soket Tipi" to s.socket)
            rows.add("Yonga Seti (Chipset)" to s.chipset)
            rows.add("Form Faktörü" to s.formFactor.displayName)
            rows.add("Bellek Nesli (DDR Tipi)" to s.memoryGeneration.displayName)
            rows.add("RAM Slot Sayısı" to "${s.ramSlots} Yuva")
            rows.add("Maksimum Bellek Desteği" to (s.maxMemoryGb?.let { "$it GB" } ?: "Belirtilmedi"))
            rows.add("PCIe Gen Standardı" to s.pcieGeneration.displayName)
            rows.add("PCIe Genişleme Yuvaları" to (s.pcieSlotCount?.let { "${it}x PCIe Slot" } ?: "Belirtilmedi"))
            rows.add("M.2 NVMe Yuva Sayısı" to (s.m2Slots?.let { "${it}x M.2 NVMe" } ?: "Belirtilmedi"))
            rows.add("SATA 6Gb/s Port Sayısı" to (s.sataPorts?.let { "${it}x SATA 3.0" } ?: "Belirtilmedi"))
            rows.add("VRM Güç Fazları" to (s.vrmPhases ?: "Belirtilmedi"))
            rows.add("Kablosuz Bağlantı (Wi-Fi)" to if (s.hasWifi == true) "Dahili Wi-Fi & Bluetooth" else if (s.hasWifi == false) "Bulunmuyor" else "Belirtilmedi")
            rows.add("Arka Panel I/O Çıkışları" to (s.interfacePorts ?: "Belirtilmedi"))
        }

        is RamSpec -> {
            rows.add("Bellek Standardı" to s.generation.displayName)
            val totalGb = s.capacity.megabytes / 1024
            val perModGb = totalGb / s.moduleCount
            rows.add("Kapasite & Kit Yapısı" to "${totalGb} GB (${s.moduleCount}x${perModGb} GB Kit)")
            rows.add("Frekans Hızı (MT/s)" to "${s.speed.mhz} MHz (MT/s)")
            rows.add("CAS Gecikme Süresi (CL)" to "CL${s.casLatency}")
            rows.add("İlk Tepki Süresi (True Latency)" to (s.firstWordLatencyNs?.let { "$it ns" } ?: "Belirtilmedi"))
            rows.add("Çalışma Gerilimi (Voltaj)" to (s.voltage?.let { "$it V" } ?: "Belirtilmedi"))
            val coolerRgb = listOfNotNull(
                s.heatSpreader?.let { if (it) "Alüminyum Soğutucu Blok" else "Soğutucusuz" },
                s.rgb?.let { if (it) "RGB Aydınlatma" else "RGB Yok" },
            ).joinToString(" • ")
            rows.add("Soğutucu Blok / RGB" to coolerRgb.ifBlank { "Belirtilmedi" })
            rows.add("Hız Aşırtma Profili" to if (s.generation.name.contains("DDR5")) "Intel XMP 3.0 & AMD EXPO" else "Intel XMP 2.0")
        }

        is StorageSpec -> {
            val totalGb = s.capacity.megabytes / 1024
            rows.add("Depolama Kapasitesi" to (if (totalGb >= 1024) "${totalGb / 1024} TB (${totalGb} GB)" else "${totalGb} GB"))
            rows.add("Bellek / Disk Tipi" to "${s.mediaType.displayName} (${if (s.storageInterface == StorageInterface.NVME) "NVMe M.2" else "SATA"})")
            rows.add("Arayüz & Protokol" to (s.pcieGeneration?.let { "PCIe Gen ${it.displayName} x4" } ?: s.storageInterface.displayName))
            rows.add("Form Faktörü" to s.formFactor.displayName)
            rows.add("Sıralı Okuma Hızı" to if (s.sequentialReadMbps > 0) "${s.sequentialReadMbps} MB/sn" else "Belirtilmedi")
            rows.add("Sıralı Yazma Hızı" to if (s.sequentialWriteMbps > 0) "${s.sequentialWriteMbps} MB/sn" else "Belirtilmedi")
            rows.add("NAND Flaş Tipi" to (s.nandType ?: "Belirtilmedi"))
            rows.add("DRAM Önbellek (Buffer)" to (s.cacheMb?.let { "$it MB DRAM Cache" } ?: "Belirtilmedi"))
            rows.add("Dayanıklılık Ömrü (TBW)" to (s.enduranceTbw?.let { "$it TBW" } ?: "Belirtilmedi"))
        }

        is PsuSpec -> {
            rows.add("Çıkış Gücü Kapasitesi" to "${s.wattage.watts} Watt")
            rows.add("PSU Tier List Sınıfı" to (s.tierRating ?: "Belirtilmedi"))
            rows.add("ATX Standardı & PCIe Desteği" to (s.atxStandard ?: if (s.supportsPcie5Power) "ATX 3.1 & PCIe 5.1" else "Belirtilmedi"))
            val effText = listOfNotNull(s.efficiency.displayName, s.cybeneticsCert).joinToString(" • ")
            rows.add("80+ & Cybenetics Sertifikası" to effText)
            rows.add("Modülerlik Yapısı" to (s.modularity ?: "Belirtilmedi"))
            rows.add("12VHPWR / 12V-2x6 (600W)" to (s.pcie12Vhpwr ?: if (s.supportsPcie5Power) "1x 16-Pin (600W Native)" else "Belirtilmedi"))
            rows.add("CPU (EPS 8-Pin) Güç Çıkışları" to (s.epsConnectors ?: "Belirtilmedi"))
            rows.add("PCIe (6+2 Pin) Çıkış Sayısı" to (s.pcieConnectors ?: "Belirtilmedi"))
            rows.add("SATA Güç Çıkış Sayısı" to (s.sataConnectors ?: "Belirtilmedi"))
            rows.add("Topoloji & Kapasitör Standardı" to (s.topology ?: s.capacitors ?: "Belirtilmedi"))
            rows.add("Koruma Devreleri" to (s.protections ?: "Belirtilmedi"))
            rows.add("Soğutma Fanı & Boyut" to (s.fanAndSize ?: "Belirtilmedi"))
            rows.add("Form Faktörü" to (s.formFactor ?: "ATX"))
        }

        is CoolerSpec -> {
            rows.add("Soğutma Tipi" to s.type.displayName)
            rows.add("Maksimum Soğutma Gücü (TDP)" to "${s.maxCoolingTdp.watts} W")
            val radOrHeight = if (s.type == CoolerType.LIQUID) {
                s.radiatorSizeMm?.let { "${it} mm Alüminyum Radyatör" } ?: "Belirtilmedi"
            } else {
                s.heightMm?.let { "${it} mm Kule Tipi Soğutucu" } ?: "Belirtilmedi"
            }
            rows.add("Radyatör / Kule Yüksekliği" to radOrHeight)
            rows.add("Fan Dönüş Hızı (RPM)" to (s.fanRpm ?: "Belirtilmedi"))
            rows.add("Ses Seviyesi (Gürültü)" to (s.noiseDba?.let { "$it dBA" } ?: "Belirtilmedi"))
            rows.add("Desteklenen Soketler" to (if (s.supportedSockets.isNotEmpty()) s.supportedSockets.joinToString(", ") else "Belirtilmedi"))
        }

        is CaseSpec -> {
            rows.add("Kasa Tipi / Form Faktörü" to (s.type ?: "Belirtilmedi"))
            val mbSupport = if (s.supportedMotherboardFormFactors.isNotEmpty()) {
                s.supportedMotherboardFormFactors.joinToString(", ") { it.displayName }
            } else {
                "Belirtilmedi"
            }
            rows.add("Desteklenen Anakart Tipleri" to mbSupport)
            rows.add("Maksimum GPU Uzunluğu" to (s.maxGpuLengthMm?.let { "$it mm" } ?: "Belirtilmedi"))
            rows.add("Maksimum CPU Soğutucu Yüksekliği" to (s.maxCpuCoolerHeightMm?.let { "$it mm" } ?: "Belirtilmedi"))
            rows.add("Maksimum Radyatör Desteği" to (s.maxRadiatorSizeMm?.let { "${it} mm" } ?: "Belirtilmedi"))
            rows.add("Yan Panel Malzemesi" to (s.sidePanel ?: "Belirtilmedi"))
            rows.add("Dahili Disk Yuvaları" to (s.driveBays ?: "Belirtilmedi"))
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
                    text = "Piyasa Fiyatı",
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
    if (price == null) return "Stok / Fiyat Yok"
    val major = price.amountMinor / 100
    val minor = (price.amountMinor % 100).toString().padStart(2, '0')
    return "₺$major,$minor"
}

private data class StoreSearchTarget(
    val name: String,
    val searchUrlTemplate: String,
)

private val TR_STORE_SEARCH_TARGETS = listOf(
    StoreSearchTarget("Akakçe", "https://www.akakce.com/arama/?q="),
    StoreSearchTarget("İncehesap", "https://www.incehesap.com/arama/?q="),
    StoreSearchTarget("Hepsiburada", "https://www.hepsiburada.com/ara?q="),
    StoreSearchTarget("Amazon TR", "https://www.amazon.com.tr/s?k="),
    StoreSearchTarget("Sinerji", "https://www.sinerji.gen.tr/arama?q="),
    StoreSearchTarget("Tebilon", "https://www.tebilon.com/arama/?q="),
    StoreSearchTarget("N11", "https://www.n11.com/arama?q="),
    StoreSearchTarget("PttAVM", "https://www.pttavm.com/arama?q="),
)

private fun encodeSearchQuery(query: String): String {
    val allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~"
    val sb = StringBuilder()
    for (ch in query) {
        if (ch in allowed) {
            sb.append(ch)
        } else if (ch == ' ') {
            sb.append('+')
        } else {
            val bytes = ch.toString().encodeToByteArray()
            for (b in bytes) {
                sb.append('%')
                val hex = (b.toInt() and 0xFF).toString(16).uppercase()
                if (hex.length == 1) sb.append('0')
                sb.append(hex)
            }
        }
    }
    return sb.toString()
}

@Composable
private fun StoreOffersCard(
    offers: List<StoreOffer>,
    currentPriceTry: Double?,
    productName: String,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "MAĞAZA FİYATLARI & KARŞILAŞTIRMA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                ) {
                    Text(
                        text = if (offers.isNotEmpty()) "${offers.size} Doğrulanmış" else "Canlı Arama",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            // Verified Scraped Offers List (if present)
            if (offers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.medium))

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    offers.forEachIndexed { index, offer ->
                        val isCheapest = index == 0
                        Surface(
                            onClick = {
                                val trimmedUrl = offer.url.trim()
                                if (trimmedUrl.startsWith("https://", ignoreCase = true) || trimmedUrl.startsWith("http://", ignoreCase = true)) {
                                    runCatching { uriHandler.openUri(trimmedUrl) }
                                }
                            },
                            shape = RoundedCornerShape(Spacing.small),
                            color = if (isCheapest) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(
                                1.dp,
                                if (isCheapest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text = offer.storeName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (isCheapest) {
                                        Spacer(modifier = Modifier.width(Spacing.small))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                                        ) {
                                            Text(
                                                text = "EN UCUZ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(Spacing.small))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₺${offer.priceTry.formatPrice()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCheapest) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.small))
                                    Text(
                                        text = "↗",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )
            Spacer(modifier = Modifier.height(Spacing.medium))

            // 1-Tap Live Multi-Store Search Comparison
            Text(
                text = "TÜRKİYE MAĞAZALARINDA CANLI FİYAT ARA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = "Tek tıkla mağazada anlık stok ve fiyat aratın:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(Spacing.small))

            val cleanQuery = encodeSearchQuery(productName.ifBlank { "hardware" })
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                TR_STORE_SEARCH_TARGETS.chunked(2).forEach { rowTargets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        rowTargets.forEach { target ->
                            Surface(
                                onClick = {
                                    val fullUrl = "${target.searchUrlTemplate}$cleanQuery"
                                    runCatching { uriHandler.openUri(fullUrl) }
                                },
                                shape = RoundedCornerShape(Spacing.small),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                                modifier = Modifier.weight(1f),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = target.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "Ara ↗",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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

@Composable
fun HardwareDetailHeroImage(
    imageUrl: String?,
    category: HardwareCategory,
    displayName: String,
    modifier: Modifier = Modifier,
) {
    val containerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val shape = RoundedCornerShape(Spacing.cardRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(shape)
            .background(containerBg)
            .border(1.dp, borderColor, shape)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        )
                    }
                },
                error = {
                    DetailPlaceholderHero(category = category)
                },
            )
        } else {
            DetailPlaceholderHero(category = category)
        }
    }
}

@Composable
fun DetailPlaceholderHero(
    category: HardwareCategory,
    modifier: Modifier = Modifier,
) {
    val (badgeBg, badgeText) = when (category) {
        HardwareCategory.CPU -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.GPU -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HardwareCategory.RAM -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        HardwareCategory.MOTHERBOARD -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.PSU -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HardwareCategory.STORAGE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        HardwareCategory.COOLER -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.CASE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(badgeBg.copy(alpha = 0.35f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = badgeText,
        )
    }
}


