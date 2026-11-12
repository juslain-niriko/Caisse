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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlusSheet(onDismiss: () -> Unit, onNavigate: (String) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Plus", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            SheetItem(Icons.Filled.ArrowDownward, "Entrées", "Financements reçus") {
                onNavigate(Routes.ENTRIES)
            }
            SheetItem(Icons.Filled.LocalActivity, "Activités", "Dépenses d'activité") {
                onNavigate(Routes.ACTIVITIES)
            }
            SheetItem(Icons.AutoMirrored.Filled.TrendingUp, "Investissements", "Suivi des investissements") {
                onNavigate(Routes.INVESTMENTS)
            }
            SheetItem(Icons.Filled.Settings, "Réglages", "Thème, à propos") {
                onNavigate(Routes.SETTINGS)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSheet(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Ajouter une opération", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            SheetItem(Icons.Filled.ArrowDownward, "Entrée (financement)", "Argent reçu") { onPick("entry") }
            SheetItem(Icons.Filled.School, "Écolage", "Paiement pour un élève") { onPick("tuition") }
            SheetItem(Icons.Filled.LocalActivity, "Activité", "Autre dépense") { onPick("activity") }
            SheetItem(Icons.AutoMirrored.Filled.TrendingUp, "Investissement", "Dépense d'investissement") { onPick("investment") }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SheetItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
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
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
