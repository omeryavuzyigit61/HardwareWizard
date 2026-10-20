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
import androidx.compose.foundation.layout.imePadding
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
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.theme.SlateDarkBackground
import com.panda.myapplication.presentation.theme.TextSecondary
import com.panda.myapplication.presentation.tools.CoolingLabScreen
import com.panda.myapplication.presentation.tools.DeskSetupLabScreen
import com.panda.myapplication.presentation.tools.HardwareGlossaryScreen
import com.panda.myapplication.presentation.tools.RamLatencyLabScreen
import com.panda.myapplication.presentation.tools.UpsCalculatorScreen
import com.panda.myapplication.presentation.tools.UndervoltGuideScreen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.domain.repository.UserRepository
import com.panda.myapplication.presentation.profile.AuthSheet
import com.panda.myapplication.presentation.profile.UserProfileScreen
import com.panda.myapplication.presentation.tools.SecondHandValuationScreen
import com.panda.myapplication.presentation.tools.AdminModerationScreen
import com.panda.myapplication.presentation.vs_arena.VsArenaRoute
import com.panda.myapplication.presentation.wizard.WizardRoute
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.panda.myapplication.domain.repository.SettingsRepository
import com.panda.myapplication.presentation.settings.SettingsScreen
import com.panda.myapplication.presentation.settings.SettingsViewModel
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import com.panda.myapplication.presentation.util.getAppStrings
import androidx.compose.runtime.CompositionLocalProvider

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
    const val SECOND_HAND_VALUATOR = "second_hand_valuator"
    const val USER_PROFILE = "user_profile"
    const val SETTINGS = "settings"
}

private sealed class BottomNavTab(
    val route: String,
    val title: String,
    val iconType: FluidTabIconType,
) {
    class Catalog(title: String) : BottomNavTab(Destinations.HARDWARE_LIST, title, FluidTabIconType.CATALOG)
    class Wizard(title: String) : BottomNavTab(Destinations.WIZARD, title, FluidTabIconType.WIZARD)
    class VsArena(title: String) : BottomNavTab(Destinations.VS_ARENA, title, FluidTabIconType.VS_ARENA)
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
        val settingsRepository: SettingsRepository = koinInject()
        val appSettings by settingsRepository.settings.collectAsStateWithLifecycle()

        val appStrings = remember(appSettings.language) { getAppStrings(appSettings.language) }

        HardwareWizardTheme(themeMode = appSettings.themeMode) {
            CompositionLocalProvider(
                LocalAppStrings provides appStrings,
                LocalAppCurrency provides appSettings.currency,
            ) {
                val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val vsArenaRepository: VsArenaRepository = koinInject()
            val activeBuildRepository: ActiveBuildRepository = koinInject()
            val userRepository: UserRepository = koinInject()
            val vsSelection by vsArenaRepository.state.collectAsStateWithLifecycle()
            val activeBuild by activeBuildRepository.activeBuild.collectAsStateWithLifecycle()
            val currentUser by userRepository.currentUser.collectAsStateWithLifecycle()
            val isLoggedIn by userRepository.isLoggedIn.collectAsStateWithLifecycle()
            val vsFilledCount = (if (vsSelection.leftItem != null) 1 else 0) + (if (vsSelection.rightItem != null) 1 else 0)

            var showAuthSheet by remember { mutableStateOf(false) }
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
                        currentUser = currentUser,
                        isLoggedIn = isLoggedIn,
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
                        onOpenAuthSheet = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            showAuthSheet = true
                        },
                        onLogout = {
                            coroutineScope.launch {
                                userRepository.logout()
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
                            .padding(bottom = innerPadding.calculateBottomPadding())
                            .imePadding(),
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
                                onOpenAuthSheet = { showAuthSheet = true },
                            )
                        }

                        composable(route = Destinations.SECOND_HAND_VALUATOR) {
                            SecondHandValuationScreen(
                                activeBuildRepository = activeBuildRepository,
                                communityPriceRepository = koinInject(),
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToWizard = {
                                    navController.navigate(Destinations.WIZARD) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onOpenDrawer = openDrawer,
                            )
                        }

                        composable(route = Destinations.USER_PROFILE) {
                            UserProfileScreen(
                                userRepository = userRepository,
                                communityPriceRepository = koinInject(),
                                onNavigateBack = { navController.popBackStack() },
                                onOpenAuthSheet = { showAuthSheet = true },
                                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS) },
                            )
                        }

                        composable(route = Destinations.SETTINGS) {
                            val settingsViewModel: SettingsViewModel = koinViewModel()
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }
                    }
                }

                if (showAuthSheet) {
                    AuthSheet(
                        userRepository = userRepository,
                        onDismiss = { showAuthSheet = false },
                        onAuthSuccess = { showAuthSheet = false },
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun HardwareEngineeringDrawerContent(
    currentRoute: String?,
    currentUser: UserProfile?,
    isLoggedIn: Boolean,
    onNavigateToRoute: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenAuthSheet: () -> Unit,
    onLogout: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val engineeringTools = remember(strings) {
        listOf(
            EngineeringToolDrawerItem(
                route = Destinations.COOLING_LAB,
                icon = "❄️",
                title = strings.drawerCoolingTitle,
                subtitle = strings.drawerCoolingSubtitle,
                badge = "THERMAL LAB",
                accentColor = ElectricBlue,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.RAM_LATENCY_LAB,
                icon = "⚡",
                title = strings.drawerRamTitle,
                subtitle = strings.drawerRamSubtitle,
                badge = "MEMORY LAB",
                accentColor = NeonEmerald,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.UPS_CALCULATOR,
                icon = "🔋",
                title = strings.drawerUpsTitle,
                subtitle = strings.drawerUpsSubtitle,
                badge = "POWER LAB",
                accentColor = NeonAmber,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.DESK_SETUP_LAB,
                icon = "🎯",
                title = strings.drawerDeskTitle,
                subtitle = strings.drawerDeskSubtitle,
                badge = "ESPORTS LAB",
                accentColor = ElectricBlue,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.HARDWARE_GLOSSARY,
                icon = "📚",
                title = strings.drawerGlossaryTitle,
                subtitle = strings.drawerGlossarySubtitle,
                badge = "KNOWLEDGE",
                accentColor = NeonEmerald,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.UNDERVOLT_GUIDE,
                icon = "⚡",
                title = strings.drawerUndervoltTitle,
                subtitle = strings.drawerUndervoltSubtitle,
                badge = "TUNING LAB",
                accentColor = NeonCyan,
            ),
            EngineeringToolDrawerItem(
                route = Destinations.SECOND_HAND_VALUATOR,
                icon = "🔄",
                title = strings.drawerValuationTitle,
                subtitle = strings.drawerValuationSubtitle,
                badge = "2ND HAND VALUATION",
                accentColor = NeonEmerald,
            ),
        )
    }

    ModalDrawerSheet(
        modifier = Modifier
            .widthIn(min = 280.dp, max = 335.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
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
                color = MaterialTheme.colorScheme.surfaceVariant,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Surface(
                        onClick = onCloseDrawer,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Text(
                            text = "✕",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // User Profile Drawer Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (currentUser != null) Color(currentUser.spiritHardware.colorHex).copy(alpha = 0.5f) else GlassBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (currentUser != null && isLoggedIn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(currentUser.spiritHardware.colorHex).copy(alpha = 0.2f),
                                border = BorderStroke(1.5.dp, Color(currentUser.spiritHardware.colorHex)),
                                modifier = Modifier.size(44.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = currentUser.spiritHardware.emoji, fontSize = 22.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = currentUser.username,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonCyan.copy(alpha = 0.15f),
                                        border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.4f)),
                                    ) {
                                        Text(
                                            text = "Yaş: ${currentUser.age}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = currentUser.spiritHardware.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(currentUser.spiritHardware.colorHex),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = { onNavigateToRoute(Destinations.USER_PROFILE) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan.copy(alpha = 0.18f),
                                    contentColor = NeonCyan,
                                ),
                                border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Profili Gör", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onLogout,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, NeonRose.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRose),
                            ) {
                                Text("Çıkış", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👤", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Misafir Kullanıcı",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "Giriş yapılmadı",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Button(
                                onClick = onOpenAuthSheet,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            ) {
                                Text(
                                    text = "Giriş Yap",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                )
                            }
                        }
                    }
                }
            }

            // Quick Navigation Section
            Text(
                text = strings.drawerMainModules,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickDrawerTabChip(
                    title = strings.drawerQuickCatalog,
                    isSelected = currentRoute?.startsWith(Destinations.HARDWARE_LIST) == true,
                    onClick = { onNavigateToRoute(Destinations.HARDWARE_LIST) },
                    modifier = Modifier.weight(1f),
                )
                QuickDrawerTabChip(
                    title = strings.drawerQuickWizard,
                    isSelected = currentRoute == Destinations.WIZARD,
                    onClick = { onNavigateToRoute(Destinations.WIZARD) },
                    modifier = Modifier.weight(1f),
                )
                QuickDrawerTabChip(
                    title = strings.drawerQuickVs,
                    isSelected = currentRoute == Destinations.VS_ARENA,
                    onClick = { onNavigateToRoute(Destinations.VS_ARENA) },
                    modifier = Modifier.weight(1f),
                )
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.6f))

            Text(
                text = "${engineeringTools.size} ${strings.drawerToolsHeader}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonEmerald,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp),
            )

            engineeringTools.forEach { tool ->
                val isSelected = currentRoute == tool.route
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) tool.accentColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 15.sp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Settings Navigation Drawer Tile
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (currentRoute == Destinations.SETTINGS) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    width = if (currentRoute == Destinations.SETTINGS) 1.5.dp else 1.dp,
                    color = if (currentRoute == Destinations.SETTINGS) NeonCyan else GlassBorder.copy(alpha = 0.7f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigateToRoute(Destinations.SETTINGS) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NeonCyan.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "⚙️", fontSize = 18.sp)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "Uygulama Ayarları",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Tema, 7 Dil, Para Birimi, Uzman Modu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Text(
                        text = "›",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Footer Info Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
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
    val strings = LocalAppStrings.current
    val tabs = remember(strings) {
        listOf(
            BottomNavTab.Catalog(strings.tabCatalog),
            BottomNavTab.Wizard(strings.tabWizard),
            BottomNavTab.VsArena(strings.tabVsArena),
        )
    }

    val fluidTabs = remember(tabs) {
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