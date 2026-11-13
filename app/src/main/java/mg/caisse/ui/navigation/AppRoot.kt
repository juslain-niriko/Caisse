package mg.caisse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    fun backToHub() {
        if (!navController.popBackStack("hub", inclusive = false)) {
            navController.navigate("hub") { launchSingleTop = true }
        }
    }

    NavHost(navController = navController, startDestination = "hub") {
        composable("hub") {
            ModuleHubScreen(
                onOpenCaisse = {
                    navController.navigate("caisse") { launchSingleTop = true }
                },
                onOpenBar = {
                    navController.navigate("bar") { launchSingleTop = true }
                }
            )
        }
        composable("caisse") {
            CaisseRoot(onOpenModules = { backToHub() })
        }
        composable("bar") {
            BarRoot(onOpenModules = { backToHub() })
        }
    }
}
