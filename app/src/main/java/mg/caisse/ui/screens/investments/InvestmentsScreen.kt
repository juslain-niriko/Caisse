package mg.caisse.ui.screens.investments

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mg.caisse.data.local.entity.TransactionEntity
import mg.caisse.ui.components.DeleteConfirmDialog
import mg.caisse.ui.components.TransactionList
import mg.caisse.ui.components.rememberCategoryNames
import mg.caisse.ui.components.rememberRepository

@Composable
fun InvestmentsScreen(onEdit: (Long) -> Unit) {
    val repo = rememberRepository()
    val categoryNames = rememberCategoryNames()
    val scope = rememberCoroutineScope()

    val flow = remember { repo.observeInvestments() }
    val items by flow.collectAsStateWithLifecycle(initialValue = emptyList<TransactionEntity>())

    var pending by remember { mutableStateOf<TransactionEntity?>(null) }

    TransactionList(
        transactions = items,
        categoryNames = categoryNames,
        emptyIcon = Icons.AutoMirrored.Filled.TrendingUp,
        emptyTitle = "Aucun investissement",
        emptySubtitle = "Enregistrez un investissement (projet, achat…).",
        onEdit = onEdit,
        onDelete = { pending = it }
    )

    pending?.let { tx ->
        DeleteConfirmDialog(
            message = "Supprimer cet investissement ?",
            onConfirm = {
                scope.launch { repo.deleteTransaction(tx) }
                pending = null
            },
            onDismiss = { pending = null }
        )
    }
}
