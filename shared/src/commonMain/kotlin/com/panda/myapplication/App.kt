package com.panda.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.panda.myapplication.data.di.dataModule
import com.panda.myapplication.data.local.platformDatabaseModule
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.presentation.di.presentationModule
import com.panda.myapplication.presentation.hardware_detail.HardwareDetailRoute
import com.panda.myapplication.presentation.hardware_list.HardwareListRoute
import com.panda.myapplication.presentation.theme.HardwareWizardTheme
import com.panda.myapplication.presentation.wizard.WizardRoute
import org.koin.compose.KoinApplication

object Destinations {
    const val HARDWARE_LIST = "hardware_list"
    const val WIZARD = "wizard"
    const val HARDWARE_DETAIL = "hardware_detail"
}

private sealed class BottomNavTab(
    val route: String,
    val title: String,
) {
    data object Catalog : BottomNavTab(Destinations.HARDWARE_LIST, "Katalog")
    data object Wizard : BottomNavTab(Destinations.WIZARD, "PC Sihirbazı")
}

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

            val isBottomBarVisible = currentRoute != null && !currentRoute.startsWith(Destinations.HARDWARE_DETAIL)

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (isBottomBarVisible) {
                        HardwareBottomBar(
                            currentRoute = currentRoute,
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
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HardwareBottomBar(
    currentRoute: String?,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember { listOf(BottomNavTab.Catalog, BottomNavTab.Wizard) }

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
    ) {
        tabs.forEach { tab ->
            val isSelected = currentRoute?.startsWith(tab.route) == true
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    when (tab) {
                        BottomNavTab.Catalog -> CatalogTabIcon(
                            isSelected = isSelected,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        BottomNavTab.Wizard -> WizardTabIcon(
                            isSelected = isSelected,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                ),
            )
        }
    }
}

@Composable
private fun CatalogTabIcon(
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.32f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = if (isSelected) Stroke(strokeWidth) else Stroke(strokeWidth * 0.8f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.15f, h * 0.55f),
            size = Size(w * 0.7f, h * 0.32f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = if (isSelected) Stroke(strokeWidth) else Stroke(strokeWidth * 0.8f),
        )
        drawCircle(
            color = color,
            radius = 2.dp.toPx(),
            center = Offset(w * 0.32f, h * 0.31f),
        )
        drawLine(
            color = color,
            start = Offset(w * 0.3f, h * 0.71f),
            end = Offset(w * 0.7f, h * 0.71f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun WizardTabIcon(
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.28f, h * 0.28f),
            size = Size(w * 0.44f, h * 0.44f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = if (isSelected) Stroke(strokeWidth) else Stroke(strokeWidth * 0.8f),
        )
        if (isSelected) {
            drawCircle(
                color = color,
                radius = 2.5.dp.toPx(),
                center = Offset(w * 0.5f, h * 0.5f),
            )
        }
        drawLine(color = color, start = Offset(w * 0.38f, h * 0.14f), end = Offset(w * 0.38f, h * 0.28f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.62f, h * 0.14f), end = Offset(w * 0.62f, h * 0.28f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.38f, h * 0.72f), end = Offset(w * 0.38f, h * 0.86f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.62f, h * 0.72f), end = Offset(w * 0.62f, h * 0.86f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.14f, h * 0.38f), end = Offset(w * 0.28f, h * 0.38f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.14f, h * 0.62f), end = Offset(w * 0.28f, h * 0.62f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.72f, h * 0.38f), end = Offset(w * 0.86f, h * 0.38f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.72f, h * 0.62f), end = Offset(w * 0.86f, h * 0.62f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    }
}