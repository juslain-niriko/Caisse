package mg.caisse.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mg.caisse.ui.screens.activities.ActivitiesScreen
import mg.caisse.ui.screens.dashboard.DashboardScreen
import mg.caisse.ui.screens.entries.EntriesScreen
import mg.caisse.ui.screens.form.OperationFormScreen
import mg.caisse.ui.screens.investments.InvestmentsScreen
import mg.caisse.ui.screens.reports.ReportsScreen
import mg.caisse.ui.screens.settings.SettingsScreen
import mg.caisse.ui.screens.students.StudentDetailScreen
import mg.caisse.ui.screens.students.StudentsScreen
import mg.caisse.ui.screens.tuition.TuitionScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaisseRoot(onOpenModules: () -> Unit = {}) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    var showPlusSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }

    val hideChrome = route?.startsWith("form/") == true || route?.startsWith("student/") == true
    val currentBase = route?.substringBefore("/")

    fun navigateTop(target: String) {
        if (target == Routes.DASHBOARD) {
            if (!navController.popBackStack(Routes.DASHBOARD, inclusive = false)) {
                navController.navigate(Routes.DASHBOARD) { launchSingleTop = true }
            }
            return
        }
        navController.navigate(target) {
            launchSingleTop = true
            popUpTo(Routes.DASHBOARD) { saveState = true }
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!hideChrome) {
                CenterAlignedTopAppBar(
                    title = { Text(titleFor(route)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (!hideChrome) {
                CaisseBottomBar(
                    currentBase = currentBase,
                    onNavigate = { navigateTop(it) },
                    onPlus = { showPlusSheet = true }
                )
            }
        },
        floatingActionButton = {
            if (!hideChrome) {
                FloatingActionButton(onClick = { showAddSheet = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(onNavigate = { target -> navController.navigate(target) })
            }
            composable(Routes.ENTRIES) {
                EntriesScreen(onEdit = { id -> navController.navigate(Routes.form("entry", id)) })
            }
            composable(Routes.STUDENTS) {
                StudentsScreen(onOpenStudent = { id -> navController.navigate(Routes.studentDetail(id)) })
            }
            composable(Routes.TUITION) {
                TuitionScreen(onPayForStudent = { id -> navController.navigate(Routes.form("tuition", 0L, id)) })
            }
            composable(Routes.ACTIVITIES) {
                ActivitiesScreen(onEdit = { id -> navController.navigate(Routes.form("activity", id)) })
            }
            composable(Routes.INVESTMENTS) {
                InvestmentsScreen(onEdit = { id -> navController.navigate(Routes.form("investment", id)) })
            }
            composable(Routes.REPORTS) { ReportsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }

            composable(
                Routes.STUDENT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                StudentDetailScreen(
                    studentId = id,
                    onBack = { navController.popBackStack() },
                    onPayTuition = { navController.navigate(Routes.form("tuition", 0L, id)) }
                )
            }

            composable(
                Routes.FORM,
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType },
                    navArgument("txId") { type = NavType.LongType },
                    navArgument("studentId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val mode = entry.arguments?.getString("mode") ?: "entry"
                val txId = entry.arguments?.getLong("txId") ?: 0L
                val studentId = entry.arguments?.getLong("studentId") ?: -1L
                OperationFormScreen(
                    mode = mode,
                    txId = txId,
                    preselectedStudentId = studentId,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }

    if (showPlusSheet) {
        PlusSheet(
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
    if (showAddSheet) {
        AddSheet(
            onDismiss = { showAddSheet = false },
            onPick = { mode ->
                showAddSheet = false
                navController.navigate(Routes.form(mode))
            }
        )
    }
}

private fun titleFor(route: String?): String = when {
    route == null -> "Caisse"
    route.startsWith("form/") -> "Opération"
    route.startsWith("student/") -> "Élève"
    else -> when (route) {
        Routes.DASHBOARD -> "Caisse"
        Routes.ENTRIES -> "Entrées"
        Routes.STUDENTS -> "Élèves"
        Routes.TUITION -> "Écolage"
        Routes.ACTIVITIES -> "Activités"
        Routes.INVESTMENTS -> "Investissements"
        Routes.REPORTS -> "Rapports"
        Routes.SETTINGS -> "Réglages"
        else -> "Caisse"
    }
}
