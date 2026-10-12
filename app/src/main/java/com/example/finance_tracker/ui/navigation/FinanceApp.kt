package com.example.finance_tracker.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.finance_tracker.ui.screens.addtransaction.AddTransactionScreen
import com.example.finance_tracker.ui.screens.home.HomeScreen
import com.example.finance_tracker.ui.screens.placeholder.PlaceholderScreen
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.DividerColor
import com.example.finance_tracker.ui.theme.FabDark
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.ScreenBg

object Routes {
    const val HOME = "home"
    const val ADD_TRANSACTION = "add_transaction"
    const val HISTORY = "history"
    const val STATS = "stats"
    const val JARS = "jars"
    const val MORE = "more"
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Главная", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.HISTORY, "История", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    BottomNavItem(Routes.STATS, "Статистика", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(Routes.JARS, "Копилки", Icons.Filled.Savings, Icons.Outlined.Savings),
    BottomNavItem(Routes.MORE, "Ещё", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
)

/**
 * Каркас приложения: нижнее меню из 5 разделов, тёмный FAB «+».
 * Пока реализована только Главная, остальные — заглушки.
 */
@Composable
fun FinanceApp() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = ScreenBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute != Routes.ADD_TRANSACTION) {
                Column {
                    HorizontalDivider(color = DividerColor)
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(text = item.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = InkPrimary,
                                selectedIconColor = InkPrimary,
                                unselectedTextColor = InkSecondary,
                                unselectedIconColor = InkSecondary,
                                indicatorColor = CardBg
                            )
                        )
                    }
                }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Routes.HOME) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Routes.ADD_TRANSACTION) {
                            launchSingleTop = true
                        }
                    },
                    containerColor = FabDark,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Добавить операцию"
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(Routes.HOME) { HomeScreen() }
            composable(Routes.ADD_TRANSACTION) {
                AddTransactionScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Routes.HISTORY) { PlaceholderScreen("История") }
            composable(Routes.STATS) { PlaceholderScreen("Статистика") }
            composable(Routes.JARS) { PlaceholderScreen("Копилки") }
            composable(Routes.MORE) { PlaceholderScreen("Ещё") }
        }
    }
}
