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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finance_tracker.presentation.PrefillData
import com.example.finance_tracker.ui.screens.addtransaction.AddTransactionScreen
import com.example.finance_tracker.ui.screens.categories.CategoriesScreen
import com.example.finance_tracker.ui.screens.converter.ConverterScreen
import com.example.finance_tracker.ui.screens.history.HistoryScreen
import com.example.finance_tracker.ui.screens.home.HomeScreen
import com.example.finance_tracker.ui.screens.jars.JarsScreen
import com.example.finance_tracker.ui.screens.more.MoreScreen
import com.example.finance_tracker.ui.screens.stats.StatsScreen
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.CardSolid
import com.example.finance_tracker.ui.theme.DividerColor
import com.example.finance_tracker.ui.theme.FabDark
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.OnAccent
import com.example.finance_tracker.ui.theme.ScreenBg

object Routes {
    const val HOME = "home"
    const val ADD_TRANSACTION = "add_transaction"
    const val EDIT_TRANSACTION = "edit_transaction"
    const val CATEGORIES = "categories"
    const val HISTORY = "history"
    const val STATS = "stats"
    const val JARS = "jars"
    const val MORE = "more"
    const val CONVERTER = "converter"
    /** Маршрут редактирования операции с аргументом id. */
    fun editTransaction(id: Long): String = "$EDIT_TRANSACTION/$id"

    /** Маршрут добавления с предзаполнением из share-intenta. */
    fun addTransactionPrefilled(
        amount: Double? = null,
        dateMillis: Long? = null,
        keyword: String? = null
    ): String {
        val params = buildList {
            amount?.let { add("prefillAmount=$it") }
            dateMillis?.let { add("prefillDate=$it") }
            keyword?.let { add("prefillKeyword=${android.net.Uri.encode(it)}") }
        }
        return if (params.isEmpty()) ADD_TRANSACTION
        else "$ADD_TRANSACTION?" + params.joinToString("&")
    }
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
fun FinanceApp(consumePendingPrefill: () -> PrefillData? = { null }) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // Нижнее меню показываем только на пяти главных вкладках.
    val isTabRoute = currentRoute in setOf(
        Routes.HOME, Routes.HISTORY, Routes.STATS, Routes.JARS, Routes.MORE
    )

    // Предзаполнение из банковского уведомления: ждём построения навигации.
    LaunchedEffect(Unit) {
        consumePendingPrefill()?.let { prefill ->
            navController.navigate(
                Routes.addTransactionPrefilled(
                    prefill.amount,
                    prefill.dateMillis,
                    prefill.keyword
                )
            ) { launchSingleTop = true }
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isTabRoute) {
                Column {
                    HorizontalDivider(color = DividerColor)
                NavigationBar(
                    containerColor = CardSolid,
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
                    contentColor = OnAccent,
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
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenConverter = {
                        navController.navigate(Routes.CONVERTER) { launchSingleTop = true }
                    }
                )
            }
            composable(
                route = "${Routes.ADD_TRANSACTION}?prefillAmount={prefillAmount}&prefillDate={prefillDate}&prefillKeyword={prefillKeyword}",
                arguments = listOf(
                    navArgument("prefillAmount") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("prefillDate") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("prefillKeyword") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                AddTransactionScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    onTransactionClick = { id ->
                        navController.navigate(Routes.editTransaction(id)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = "${Routes.EDIT_TRANSACTION}/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) {
                AddTransactionScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() }
                )
            }
            composable(Routes.STATS) { StatsScreen() }
            composable(Routes.JARS) { JarsScreen() }
            composable(Routes.MORE) {
                MoreScreen(
                    onOpenCategories = {
                        navController.navigate(Routes.CATEGORIES) { launchSingleTop = true }
                    }
                )
            }
            composable(Routes.CONVERTER) {
                ConverterScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CATEGORIES) {
                CategoriesScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
