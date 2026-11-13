package mg.caisse.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mg.caisse.ui.screens.bar.expenses.BarExpenseFormScreen
import mg.caisse.ui.screens.bar.expenses.BarExpensesScreen
import mg.caisse.ui.screens.bar.home.BarHomeScreen
import mg.caisse.ui.screens.bar.reports.BarReportsScreen
import mg.caisse.ui.screens.bar.sale.BarSaleScreen
import mg.caisse.ui.screens.bar.settings.BarSettingsScreen
import mg.caisse.ui.screens.bar.stock.BarProductFormScreen
import mg.caisse.ui.screens.bar.stock.BarStockEntryScreen
import mg.caisse.ui.screens.bar.stock.BarStockScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarRoot(onOpenModules: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val section = route?.removePrefix("bar/")?.substringBefore("/")

    var showPlusSheet by remember { mutableStateOf(false) }

    val hideChrome = section == "product" || section == "stockEntry" || section == "expense"
    val selected = when (section) {
        "home" -> "home"
        "sale" -> "sale"
        "stock", "product", "stockEntry" -> "stock"
        "expenses", "expense" -> "expenses"
        else -> ""
    }

    fun navigateTop(target: String) {
        if (target == BarRoutes.HOME) {
            if (!navController.popBackStack(BarRoutes.HOME, inclusive = false)) {
                navController.navigate(BarRoutes.HOME) { launchSingleTop = true }
            }
            return
        }
        navController.navigate(target) {
            launchSingleTop = true
            popUpTo(BarRoutes.HOME) { saveState = true }
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!hideChrome) {
                CenterAlignedTopAppBar(
                    title = { Text(barTitle(section)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (!hideChrome) {
                BarBottomBar(
                    selected = selected,
                    onNavigate = { navigateTop(it) },
                    onPlus = { showPlusSheet = true }
                )
            }
        },
        floatingActionButton = {
            if (!hideChrome && section != "sale") {
                FloatingActionButton(onClick = { navController.navigate(BarRoutes.SALE) { launchSingleTop = true } }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nouvelle vente")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BarRoutes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(BarRoutes.HOME) {
                BarHomeScreen(
                    onNewSale = { navController.navigate(BarRoutes.SALE) },
                    onOpenStock = { navController.navigate(BarRoutes.STOCK) },
                    onOpenReports = { navController.navigate(BarRoutes.REPORTS) }
                )
            }
            composable(BarRoutes.SALE) {
                BarSaleScreen()
            }
            composable(BarRoutes.STOCK) {
                BarStockScreen(
                    onEditProduct = { id -> navController.navigate(BarRoutes.productForm(id)) },
                    onNewProduct = { navController.navigate(BarRoutes.productForm()) },
                    onStockEntry = { id -> navController.navigate(BarRoutes.stockEntry(id)) }
                )
            }
            composable(
                BarRoutes.PRODUCT_FORM,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("productId") ?: 0L
                BarProductFormScreen(productId = id, onDone = { navController.popBackStack() })
            }
            composable(
                BarRoutes.STOCK_ENTRY,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("productId") ?: 0L
                BarStockEntryScreen(productId = id, onDone = { navController.popBackStack() })
            }
            composable(BarRoutes.EXPENSES) {
                BarExpensesScreen(
                    onNewExpense = { navController.navigate(BarRoutes.expenseForm()) },
                    onEditExpense = { id -> navController.navigate(BarRoutes.expenseForm(id)) }
                )
            }
            composable(
                BarRoutes.EXPENSE_FORM,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("expenseId") ?: 0L
                BarExpenseFormScreen(expenseId = id, onDone = { navController.popBackStack() })
            }
            composable(BarRoutes.REPORTS) { BarReportsScreen() }
            composable(BarRoutes.SETTINGS) { BarSettingsScreen() }
        }
    }

    if (showPlusSheet) {
        BarPlusSheet(
            onDismiss = { showPlusSheet = false },
            onNavigate = { target ->
                showPlusSheet = false
                navController.navigate(target) { launchSingleTop = true }
            },
            onOpenModules = {
                showPlusSheet = false
                onOpenModules()
            }
        )
    }
}

private data class BarBottomItem(val key: String, val route: String, val label: String, val icon: ImageVector)

private val barBottomItems = listOf(
    BarBottomItem("home", BarRoutes.HOME, "Accueil", Icons.Filled.Home),
    BarBottomItem("sale", BarRoutes.SALE, "Vente", Icons.Filled.ShoppingCart),
    BarBottomItem("stock", BarRoutes.STOCK, "Stock", Icons.Filled.Inventory2),
    BarBottomItem("expenses", BarRoutes.EXPENSES, "Dépenses", Icons.Filled.Payments)
)

@Composable
private fun BarBottomBar(
    selected: String,
    onNavigate: (String) -> Unit,
    onPlus: () -> Unit
) {
    NavigationBar {
        barBottomItems.forEach { item ->
            NavigationBarItem(
                selected = selected == item.key,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
        NavigationBarItem(
            selected = false,
            onClick = onPlus,
            icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = "Plus") },
            label = { Text("Plus") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarPlusSheet(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenModules: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Plus", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            BarSheetItem(Icons.Filled.Assessment, "Rapports", "Recettes, dépenses, stock") {
                onNavigate(BarRoutes.REPORTS)
            }
            BarSheetItem(Icons.Filled.Settings, "Réglages", "Nom du bar, lieu") {
                onNavigate(BarRoutes.SETTINGS)
            }
            BarSheetItem(Icons.Filled.Apps, "Modules", "Revenir au choix Caisse / BAR") {
                onOpenModules()
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BarSheetItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun barTitle(section: String?): String = when (section) {
    "home" -> "BAR"
    "sale" -> "Vente"
    "stock" -> "Stock"
    "expenses" -> "Dépenses"
    "reports" -> "Rapports"
    "settings" -> "Réglages"
    "product" -> "Produit"
    "stockEntry" -> "Entrée de stock"
    "expense" -> "Dépense"
    else -> "BAR"
}
