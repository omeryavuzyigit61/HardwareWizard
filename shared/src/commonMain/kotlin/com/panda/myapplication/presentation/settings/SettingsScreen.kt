package com.panda.myapplication.presentation.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.panda.myapplication.domain.config.FeatureFlags
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import com.panda.myapplication.domain.model.AppCurrency
import com.panda.myapplication.domain.model.AppLanguage
import com.panda.myapplication.domain.model.FpsTarget
import com.panda.myapplication.domain.model.LengthUnit
import com.panda.myapplication.domain.model.ResolutionTarget
import com.panda.myapplication.domain.model.TemperatureUnit
import com.panda.myapplication.domain.model.ThemeMode
import com.panda.myapplication.presentation.util.getAppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsState()
    val catalogStatus by viewModel.catalogStatus.collectAsState()
    val isSyncingCatalog by viewModel.isSyncingCatalog.collectAsState()
    val platformContext = LocalPlatformContext.current
    val strings = remember(settings.language) { getAppStrings(settings.language) }
    val isTr = settings.language == com.panda.myapplication.domain.model.AppLanguage.TR
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showSourcesDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SettingsTopBar(
                title = strings.settingsTitle,
                backText = strings.back,
                onNavigateBack = onNavigateBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // 1. Görünüm & Tema
            item {
                SettingsSectionCard(
                    title = strings.appearanceSection,
                    subtitle = strings.appearanceSubtitle,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Stüdyo & Klasik
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ThemeOptionChip(
                                label = strings.themeDark,
                                icon = "🌙",
                                isSelected = settings.themeMode == ThemeMode.DARK,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.DARK) },
                                modifier = Modifier.weight(1f),
                            )
                            ThemeOptionChip(
                                label = strings.themeLight,
                                icon = "☀️",
                                isSelected = settings.themeMode == ThemeMode.LIGHT,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.LIGHT) },
                                modifier = Modifier.weight(1f),
                            )
                            ThemeOptionChip(
                                label = strings.themeWood,
                                icon = "🪵",
                                isSelected = settings.themeMode == ThemeMode.WOOD,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.WOOD) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ThemeOptionChip(
                                label = strings.themeOled,
                                icon = "🖤",
                                isSelected = settings.themeMode == ThemeMode.OLED,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.OLED) },
                                modifier = Modifier.weight(1f),
                            )
                            ThemeOptionChip(
                                label = strings.themeCyberpunk,
                                icon = "⚡",
                                isSelected = settings.themeMode == ThemeMode.CYBERPUNK,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.CYBERPUNK) },
                                modifier = Modifier.weight(1f),
                            )
                            ThemeOptionChip(
                                label = strings.themeSystem,
                                icon = "⚙️",
                                isSelected = settings.themeMode == ThemeMode.SYSTEM,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.SYSTEM) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // 🎮 Popüler Oyun Temaları
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ThemeOptionChip(
                                label = strings.themeValorant,
                                icon = "🎯",
                                isSelected = settings.themeMode == ThemeMode.VALORANT,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.VALORANT) },
                                modifier = Modifier.weight(1f),
                            )
                            ThemeOptionChip(
                                label = strings.themeCs2,
                                icon = "💣",
                                isSelected = settings.themeMode == ThemeMode.CS2,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.CS2) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ThemeOptionChip(
                                label = strings.themeGtaVice,
                                icon = "🌴",
                                isSelected = settings.themeMode == ThemeMode.GTA_VICE,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.GTA_VICE) },
                                modifier = Modifier.weight(1f),
                            )
                            ThemeOptionChip(
                                label = strings.themeWitcher,
                                icon = "🐺",
                                isSelected = settings.themeMode == ThemeMode.WITCHER,
                                onClick = { viewModel.onThemeModeChanged(ThemeMode.WITCHER) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // 2. Dil & Bölge
            item {
                SettingsSectionCard(
                    title = strings.languageRegionSection,
                    subtitle = strings.languageRegionSubtitle,
                ) {
                    SettingsNavigationRow(
                        label = strings.languageLabel,
                        value = "${settings.language.flag} ${settings.language.displayName}",
                        onClick = { showLanguageDialog = true },
                    )
                    if (FeatureFlags.IS_PRICING_ENABLED) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                        SettingsNavigationRow(
                            label = strings.currencyLabel,
                            value = settings.currency.displayName,
                            onClick = { showCurrencyDialog = true },
                        )
                    }
                }
            }

            // 3. Donanım & Performans Tercihleri
            item {
                SettingsSectionCard(
                    title = strings.hardwarePrefsSection,
                    subtitle = strings.hardwarePrefsSubtitle,
                ) {
                    // Enthusiast Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.enthusiastModeLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.enthusiastModeDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = settings.enthusiastMode,
                            onCheckedChange = { viewModel.onEnthusiastModeToggled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    // Temperature Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.tempUnitLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TemperatureUnit.entries.forEach { unit ->
                                SegmentChip(
                                    label = unit.symbol,
                                    isSelected = settings.temperatureUnit == unit,
                                    onClick = { viewModel.onTemperatureUnitChanged(unit) },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Length Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.lengthUnitLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LengthUnit.entries.forEach { unit ->
                                SegmentChip(
                                    label = unit.displayName.split(" ").first(),
                                    isSelected = settings.lengthUnit == unit,
                                    onClick = { viewModel.onLengthUnitChanged(unit) },
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    // Target Resolution
                    Text(
                        text = strings.targetResolutionLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ResolutionTarget.entries.forEach { res ->
                            SegmentChip(
                                label = res.displayName,
                                isSelected = settings.defaultResolution == res,
                                onClick = { viewModel.onResolutionTargetChanged(res) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target FPS
                    Text(
                        text = strings.targetFpsLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FpsTarget.entries.forEach { fps ->
                            SegmentChip(
                                label = fps.displayName,
                                isSelected = settings.defaultFps == fps,
                                onClick = { viewModel.onFpsTargetChanged(fps) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // 4. Veri & Depolama
            item {
                SettingsSectionCard(
                    title = strings.dataStorageSection,
                    subtitle = strings.dataStorageSubtitle,
                ) {
                    // Data saver switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.dataSaverLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.dataSaverDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = settings.dataSaver,
                            onCheckedChange = { viewModel.onDataSaverToggled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    // Manual Supabase Delta Sync
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = tr(isTr, "Katalog Delta Senkronizasyonu", "Catalog Delta Sync"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (catalogStatus.itemCount > 0) {
                                    tr(
                                        isTr,
                                        "Yerel Room DB: ${catalogStatus.itemCount} ürün • Rev ${catalogStatus.revision}",
                                        "Local Room DB: ${catalogStatus.itemCount} items • Rev ${catalogStatus.revision}",
                                    )
                                } else {
                                    tr(isTr, "Yeni donanım kayıtlarını buluttan kontrol et", "Check cloud for newly added hardware items")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(
                            enabled = !isSyncingCatalog,
                            onClick = {
                                viewModel.onSyncCatalogNow { ok, status ->
                                    scope.launch {
                                        val msg = if (ok) {
                                            tr(
                                                isTr,
                                                "✓ Katalog güncel: ${status.itemCount} ürün (Rev ${status.revision})",
                                                "✓ Catalog up to date: ${status.itemCount} items (Rev ${status.revision})",
                                            )
                                        } else {
                                            tr(
                                                isTr,
                                                "⚠️ Buluta bağlanılamadı — çevrimdışı veritabanı aktif.",
                                                "⚠️ Could not reach cloud — offline database remains active.",
                                            )
                                        }
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                text = if (isSyncingCatalog) tr(isTr, "Kontrol...", "Syncing...") else tr(isTr, "Güncelle", "Sync Now"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    // Cache clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = strings.cacheLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.cacheDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    runCatching {
                                        val loader = SingletonImageLoader.get(platformContext)
                                        loader.memoryCache?.clear()
                                        withContext(Dispatchers.IO) {
                                            loader.diskCache?.clear()
                                        }
                                    }
                                    viewModel.onClearCache {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(strings.cacheClearedMessage)
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(strings.clearCacheButton, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 5. Kaynakça & Doğrulama
            item {
                SettingsSectionCard(
                    title = strings.sourcesSection,
                    subtitle = strings.sourcesSubtitle,
                ) {
                    SettingsNavigationRow(
                        label = tr(isTr, "Detaylı Kaynakça & Veritabanı Dökümü", "Detailed Sources & Database Index"),
                        value = tr(isTr, "Görüntüle", "View"),
                        onClick = { showSourcesDialog = true },
                    )
                }
            }

            // 6. Uygulama Hakkında
            item {
                SettingsSectionCard(
                    title = strings.aboutSection,
                    subtitle = strings.aboutSubtitle,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = strings.appVersion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "v5.0.0 (Offline-First 2026 Edition)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = strings.developer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = strings.developerName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tr(isTr, "Açık Kaynak: Compose Multiplatform, Coil 3, Koin, Room SQLite, Ktor, Supabase PostgREST & RLS.", "Open Source: Compose Multiplatform, Coil 3, Koin, Room SQLite, Ktor, Supabase PostgREST & RLS."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Modal Dialogs
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = settings.language,
            strings = strings,
            onSelect = {
                viewModel.onLanguageChanged(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = settings.currency,
            strings = strings,
            onSelect = {
                viewModel.onCurrencyChanged(it)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false },
        )
    }

    if (showSourcesDialog) {
        SourcesDetailsDialog(
            strings = strings,
            isTr = isTr,
            onDismiss = { showSourcesDialog = false },
        )
    }
}

@Composable
private fun SettingsTopBar(
    title: String,
    backText: String,
    onNavigateBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onNavigateBack,
                shape = CircleShape,
            ) {
                Text(
                    text = "← $backText",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
            )
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "›",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SegmentChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = bg,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
            )
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    strings: com.panda.myapplication.presentation.util.AppStrings,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.selectLanguageTitle,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppLanguage.entries.forEach { lang ->
                    val isSel = lang == currentLanguage
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(lang) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (isSel) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = lang.flag, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.back, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun CurrencySelectionDialog(
    currentCurrency: AppCurrency,
    strings: com.panda.myapplication.presentation.util.AppStrings,
    onSelect: (AppCurrency) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.selectCurrencyTitle,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppCurrency.entries.forEach { curr ->
                    val isSel = curr == currentCurrency
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(curr) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (isSel) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = curr.symbol,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = curr.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.back, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun SourcesDetailsDialog(
    strings: com.panda.myapplication.presentation.util.AppStrings,
    isTr: Boolean,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📚 " + strings.sourcesSection,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SourceItemTile("🎮 ${strings.catGpu}", strings.sourcesGpuDesc)
                SourceItemTile("⚡ ${strings.catCpu}", strings.sourcesCpuDesc)
                SourceItemTile(
                    "🔋 ${strings.catPsu}",
                    "${strings.sourcesPsuDesc} • IRONScale Labs",
                )
                SourceItemTile(
                    "🖥️ ${tr(isTr, "Monitör & Ekran Panelleri", "Monitors & Display Panels")}",
                    tr(
                        isTr,
                        "DisplaySpecifications, TFTCentral, RTINGS Ekran Laboratuvarı (Panel Bit Derinliği, DCI-P3, HDR/Nit)",
                        "DisplaySpecifications, TFTCentral, RTINGS Display Lab (Panel Bit Depth, DCI-P3, HDR/Nits)",
                    ),
                )
                SourceItemTile(
                    "🖱️ ${tr(isTr, "Çevre Birimleri & Akustik", "Peripherals & Acoustics")}",
                    tr(
                        isTr,
                        "EloShapes, RTINGS, Sensor.fyi, AutoEq (Sensör, MCU, Switch, Polling Rate, Empedans)",
                        "EloShapes, RTINGS, Sensor.fyi, AutoEq (Sensor, MCU, Switches, Polling Rate, Impedance)",
                    ),
                )
                SourceItemTile(
                    "💾 ${tr(isTr, "Anakart, Bellek (RAM) & Depolama (SSD)", "Motherboards, Memory (RAM) & Storage (SSD)")}",
                    tr(
                        isTr,
                        "NewMaxx SSD Veritabanı, TechPowerUp SSD DB, pc-part-dataset & Üretici Resmi QVL/VRM Sayfaları",
                        "NewMaxx SSD Spreadsheet, TechPowerUp SSD DB, pc-part-dataset & Official Vendor QVL/VRM Datasheets",
                    ),
                )
                SourceItemTile(
                    "📱 ${tr(isTr, "Mobil, Tablet, Laptop & Akıllı Saat", "Mobile, Tablet, Laptop & Smartwatches")}",
                    tr(
                        isTr,
                        "GSMArena, Notebookcheck, Geekbench 6 Resmi Mobil JSON, AnTuTu v10 ve DXOMARK",
                        "GSMArena, Notebookcheck, Geekbench 6 Official Mobile JSON, AnTuTu v10, and DXOMARK",
                    ),
                )
                SourceItemTile(
                    "📡 ${tr(isTr, "Ağ & Güç Yedekleme (UPS)", "Networking & UPS Power Backup")}",
                    tr(
                        isTr,
                        "OpenWrt Table of Hardware (ToH), NUT UPS Hardware Compatibility (DDL) & Üretici Veri Sayfaları",
                        "OpenWrt Table of Hardware (ToH), NUT UPS Hardware Compatibility (DDL) & Official Vendor Datasheets",
                    ),
                )
                SourceItemTile("📸 ${tr(isTr, "Ürün Stüdyo Görselleri", "Product Studio Imagery")}", strings.sourcesImagesDesc)
                SourceItemTile(
                    "☁️ ${tr(isTr, "Merkezi Bulut & Çevrimdışı Senkronizasyon", "Cloud Single-Source & Offline Delta Sync")}",
                    tr(
                        isTr,
                        "Supabase PostgreSQL (değişmez doğrulanmış özellikler) + Yerel Room SQLite. Uygulama internetsiz 0ms hızda çalışır; yeni donanım çıktığında yalnızca delta güncellemesi indirilir.",
                        "Supabase PostgreSQL (immutable verified specs) + local Room SQLite. Runs 100% offline at 0ms; downloads only newly released hardware deltas.",
                    ),
                )
                if (FeatureFlags.IS_PRICING_ENABLED) {
                    SourceItemTile("💰 ${tr(isTr, "Piyasa & Fiyat Listeleme", "Market & Price Aggregators")}", strings.sourcesPricesDesc)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.back, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun SourceItemTile(title: String, desc: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun tr(isTr: Boolean, trText: String, enText: String): String = if (isTr) trText else enText
