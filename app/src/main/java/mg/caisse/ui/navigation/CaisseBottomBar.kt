package mg.caisse.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem("dashboard", "Accueil", Icons.Filled.Home),
    BottomItem("students", "Élèves", Icons.Filled.People),
    BottomItem("tuition", "Écolage", Icons.Filled.School),
    BottomItem("reports", "Rapports", Icons.Filled.Assessment)
)

private val secondaryRoutes = setOf("entries", "activities", "investments", "settings")

@Composable
fun CaisseBottomBar(
    currentBase: String?,
    onNavigate: (String) -> Unit,
    onPlus: () -> Unit
) {
    NavigationBar {
        bottomItems.forEach { item ->
            NavigationBarItem(
                selected = currentBase == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
        NavigationBarItem(
            selected = currentBase in secondaryRoutes,
            onClick = onPlus,
            icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = "Plus") },
            label = { Text("Plus") }
        )
    }
}
