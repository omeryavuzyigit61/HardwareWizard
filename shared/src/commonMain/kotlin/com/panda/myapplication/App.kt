package com.panda.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.panda.myapplication.data.di.dataModule
import com.panda.myapplication.data.local.platformDatabaseModule
import com.panda.myapplication.domain.engine.BuildAnalysisEngine
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.VsArenaRepository
import com.panda.myapplication.presentation.components.FluidBottomBar
import com.panda.myapplication.presentation.components.FluidBottomNavTabItem
import com.panda.myapplication.presentation.components.FluidTabIconType
import com.panda.myapplication.presentation.di.presentationModule
import com.panda.myapplication.presentation.hardware_detail.HardwareDetailRoute
import com.panda.myapplication.presentation.hardware_list.HardwareListRoute
import com.panda.myapplication.presentation.theme.CardSurface
import com.panda.myapplication.presentation.theme.CardSurfaceElevated
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GlassBorder
import com.panda.myapplication.presentation.theme.HardwareWizardTheme
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.SlateDarkBackground
import com.panda.myapplication.presentation.theme.TextSecondary
import com.panda.myapplication.presentation.tools.CoolingLabScreen
import com.panda.myapplication.presentation.tools.DeskSetupLabScreen
import com.panda.myapplication.presentation.tools.HardwareGlossaryScreen
import com.panda.myapplication.presentation.tools.RamLatencyLabScreen
import com.panda.myapplication.presentation.tools.UpsCalculatorScreen
import com.panda.myapplication.presentation.tools.UndervoltGuideScreen
import com.panda.myapplication.presentation.tools.AdminModerationScreen
import com.panda.myapplication.presentation.vs_arena.VsArenaRoute
import com.panda.myapplication.presentation.wizard.WizardRoute
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

object Destinations {
    const val HARDWARE_LIST = "hardware_list"
    const val WIZARD = "wizard"
    const val VS_ARENA = "vs_arena"
    const val HARDWARE_DETAIL = "hardware_detail"
    const val COOLING_LAB = "cooling_lab"
    const val RAM_LATENCY_LAB = "ram_latency_lab"
    const val UPS_CALCULATOR = "ups_calculator"
    const val DESK_SETUP_LAB = "desk_setup_lab"
    const val HARDWARE_GLOSSARY = "hardware_glossary"
    const val UNDERVOLT_GUIDE = "undervolt_guide"
    const val ADMIN_MODERATION = "admin_moderation"
}

private sealed class BottomNavTab(
    val route: String,
    val title: String,
    val iconType: FluidTabIconType,
) {
    data object Catalog : BottomNavTab(Destinations.HARDWARE_LIST, "Keşfet & Katalog", FluidTabIconType.CATALOG)
    data object Wizard : BottomNavTab(Destinations.WIZARD, "PC Sihirbazı", FluidTabIconType.WIZARD)
    data object VsArena : BottomNavTab(Destinations.VS_ARENA, "VS Arena", FluidTabIconType.VS_ARENA)
}

private data class EngineeringToolDrawerItem(
    val route: String,
    val icon: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val accentColor: Color,
)

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

    KoinApplication(
        application = {
            modules(dataModule, platformDatabaseModule, presentationModule)
        },
    ) {
        HardwareWizardTheme {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val vsArenaRepository: VsArenaRepository = koinInject()
            val activeBuildRepository: ActiveBuildRepository = koinInject()
            val vsSelection by vsArenaRepository.state.collectAsStateWithLifecycle()
            val activeBuild by activeBuildRepository.activeBuild.collectAsStateWithLifecycle()
            val vsFilledCount = (if (vsSelection.leftItem != null) 1 else 0) + (if (vsSelection.rightItem != null) 1 else 0)

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()

            val openDrawer: () -> Unit = {
                coroutineScope.launch {
                    drawerState.open()
                }
            }

            val isBottomBarVisible = currentRoute != null && !currentRoute.startsWith(Destinations.HARDWARE_DETAIL)

            val navigateToVsArenaTab: () -> Unit = {
                navController.navigate(Destinations.VS_ARENA) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    HardwareEngineeringDrawerContent(
                        currentRoute = currentRoute,
                        onNavigateToRoute = { route ->
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onCloseDrawer = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        },
                    )
                },
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (isBottomBarVisible) {
                            HardwareBottomBar(
                                currentRoute = currentRoute,
                                vsFilledCount = vsFilledCount,
                                onTabSelected = { tab ->
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    },
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "${Destinations.HARDWARE_LIST}?category={category}",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                    ) {
                        composable(
                            route = "${Destinations.HARDWARE_LIST}?category={category}",
                            arguments = listOf(
                                navArgument("category") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                            ),
                        ) { backStackEntry ->
                            val categoryName = backStackEntry.arguments?.read { getStringOrNull("category") }
                            val initialCategory = categoryName?.let {
                                runCatching { HardwareCategory.valueOf(it) }.getOrNull()
                            }

                            HardwareListRoute(
                                initialCategory = initialCategory,
                                onItemClick = { item ->
                                    navController.navigate("${Destinations.HARDWARE_DETAIL}/${item.id.value}")
                                },
                                onNavigateToVsArena = navigateToVsArenaTab,
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.WIZARD) {
                            WizardRoute(
                                onNavigateToCatalogWithCategory = { category ->
                                    navController.navigate("${Destinations.HARDWARE_LIST}?category=${category.name}") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onNavigateToItemDetail = { item ->
                                    navController.navigate("${Destinations.HARDWARE_DETAIL}/${item.id.value}")
                                },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.VS_ARENA) {
                            VsArenaRoute(
                                onNavigateToDetail = { itemId ->
                                    navController.navigate("${Destinations.HARDWARE_DETAIL}/${itemId.value}")
                                },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.COOLING_LAB) {
                            CoolingLabScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.RAM_LATENCY_LAB) {
                            RamLatencyLabScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.UPS_CALCULATOR) {
                            val estimatedWatts = remember(activeBuild) {
                                val cockpit = BuildAnalysisEngine.calculateWattageCockpit(activeBuild)
                                if (cockpit.peakTdpWithTolerance > 80) cockpit.peakTdpWithTolerance else 450
                            }
                            UpsCalculatorScreen(
                                initialBuildWatts = estimatedWatts,
                                onNavigateBack = { navController.popBackStack() },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.DESK_SETUP_LAB) {
                            DeskSetupLabScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.HARDWARE_GLOSSARY) {
                            HardwareGlossaryScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.UNDERVOLT_GUIDE) {
                            UndervoltGuideScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.ADMIN_MODERATION) {
                            AdminModerationScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(
                            route = "${Destinations.HARDWARE_DETAIL}/{itemId}",
                            arguments = listOf(
                                navArgument("itemId") {
                                    type = NavType.StringType
                                },
                            ),
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.read { getStringOrNull("itemId") }.orEmpty()
                            HardwareDetailRoute(
                                itemId = itemId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToVsArena = navigateToVsArenaTab,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HardwareEngineeringDrawerContent(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    onCloseDrawer: () -> Unit,
) {
    val engineeringTools = remember {
        listOf(
            EngineeringToolDrawerItem(
                route = Destinations.COOLING_LAB,
                icon = "❄️",
                title = "Termal & Soğutma Simülatörü",
                subtitle = "TDP yükü, ortam sıcaklığı, kasa hava akışı ve AIO/Kule fan doygunluk analizi",
                badge = "THERMAL LAB",
                accentColor = ElectricBlue,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.RAM_LATENCY_LAB,
                icon = "⚡",
                title = "RAM True Latency & Bant Genişliği",
                subtitle = "CL × 2000 / MT/s gerçek nanosaniye gecikmesi, 1% Low FPS ve X3D önbellek etkisi",
                badge = "MEMORY LAB",
                accentColor = NeonEmerald,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.UPS_CALCULATOR,
                icon = "🔋",
                title = "UPS & Akü Dayanma Boyutlandırıcı",
                subtitle = "VA / Gerçek Watt kapasitesi, Saf Sinüs PFC uyumu ve kesintide çalışma süresi (dk)",
                badge = "POWER LAB",
                accentColor = NeonAmber,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.DESK_SETUP_LAB,
                icon = "🎯",
                title = "Masa & Çevre Birimleri Laboratuvarı",
                subtitle = "Monitör PPI, 8KHz Mouse CPU yükü, Klavye Rapid Trigger & Kulaklık Ohm/DAC eşleştirici",
                badge = "ESPORTS LAB",
                accentColor = ElectricBlue,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.HARDWARE_GLOSSARY,
                icon = "📚",
                title = "Donanım & Mühendislik Kütüphanesi",
                subtitle = "TGP vs TDP, 3D V-Cache, ATX 3.1, Hall Effect, QD-OLED, Planar Manyetik sözlüğü",
                badge = "KNOWLEDGE",
                accentColor = NeonEmerald,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.UNDERVOLT_GUIDE,
                icon = "⚡",
                title = "Undervolt & Optimizasyon Rehberi",
                subtitle = "Intel 13/14. Nesil Vmin, AMD PBO2 Curve Optimizer, GPU Frekans Eğrisi & RAM Bit-Rot",
                badge = "TUNING LAB",
                accentColor = NeonCyan,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.ADMIN_MODERATION,
                icon = "🛡️",
                title = "Yönetici Moderasyon Paneli",
                subtitle = "Topluluk fiyat bildirimlerini incele, doğrula veya reddet (Admin PIN korumalı)",
                badge = "ADMIN CONSOLE",
                accentColor = NeonAmber,
            ),
        )
    }

    ModalDrawerSheet(
        modifier = Modifier
            .widthIn(min = 280.dp, max = 335.dp)
            .fillMaxHeight(),
        drawerContainerColor = SlateDarkBackground,
        drawerContentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Drawer Header
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElectricBlue.copy(alpha = 0.16f),
                            ) {
                                Text(
                                    text = "HARDWARE WIZARD PRO",
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricBlue,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        }
                        Text(
                            text = "Mühendislik Araçları & Lab",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Donanım fiziği, gecikme ve akustik simülatörleri",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Surface(
                        onClick = onCloseDrawer,
                        shape = RoundedCornerShape(10.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, GlassBorder),
                    ) {
                        Text(
                            text = "✕",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                        )
                    }
                }
            }

            // Quick Navigation Section
            Text(
                text = "ANA MODÜLLER",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickDrawerTabChip(
                    title = "Katalog",
                    isSelected = currentRoute?.startsWith(Destinations.HARDWARE_LIST) == true,
                    onClick = { onNavigateToRoute(Destinations.HARDWARE_LIST) },
                    modifier = Modifier.weight(1f),
                )
                QuickDrawerTabChip(
                    title = "Sihirbaz",
                    isSelected = currentRoute == Destinations.WIZARD,
                    onClick = { onNavigateToRoute(Destinations.WIZARD) },
                    modifier = Modifier.weight(1f),
                )
                QuickDrawerTabChip(
                    title = "VS Arena",
                    isSelected = currentRoute == Destinations.VS_ARENA,
                    onClick = { onNavigateToRoute(Destinations.VS_ARENA) },
                    modifier = Modifier.weight(1f),
                )
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.6f))

            Text(
                text = "5 İNTERAKTİF MÜHENDİSLİK ARACI",
                style = MaterialTheme.typography.labelSmall,
                color = NeonEmerald,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp),
            )

            engineeringTools.forEach { tool ->
                val isSelected = currentRoute == tool.route
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) tool.accentColor.copy(alpha = 0.14f) else CardSurface,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) tool.accentColor else GlassBorder.copy(alpha = 0.7f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigateToRoute(tool.route) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = tool.accentColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, tool.accentColor.copy(alpha = 0.4f)),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = tool.icon,
                                    fontSize = 18.sp,
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = tool.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tool.accentColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                )
                                if (isSelected) {
                                    Text(
                                        text = "AKTİF",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonEmerald,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                            Text(
                                text = tool.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = tool.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 15.sp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Footer Info Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardSurfaceElevated.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "💡 Özel Fiyat Motoru (Local Price Override)",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonAmber,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Katalogdaki veya detay sayfasındaki herhangi bir ürünün fiyat rozetine dokunarak kendi bulduğunuz indirimli/ikinci el fiyatı girebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickDrawerTabChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) ElectricBlue.copy(alpha = 0.18f) else CardSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) ElectricBlue else GlassBorder.copy(alpha = 0.7f),
        ),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun HardwareBottomBar(
    currentRoute: String?,
    vsFilledCount: Int,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember {
        listOf(
            BottomNavTab.Catalog,
            BottomNavTab.Wizard,
            BottomNavTab.VsArena,
        )
    }

    val fluidTabs = remember {
        tabs.map {
            FluidBottomNavTabItem(
                route = it.route,
                title = it.title,
                iconType = it.iconType,
            )
        }
    }

    FluidBottomBar(
        tabs = fluidTabs,
        currentRoute = currentRoute,
        vsFilledCount = vsFilledCount,
        onTabSelected = { fluidItem ->
            val tab = tabs.first { it.route == fluidItem.route }
            onTabSelected(tab)
        },
        modifier = modifier,
    )
}