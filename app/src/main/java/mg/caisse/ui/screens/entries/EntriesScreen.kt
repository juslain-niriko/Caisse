package mg.caisse.ui.screens.entries

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mg.caisse.data.local.entity.TransactionEntity
import mg.caisse.data.local.entity.TxType
import mg.caisse.ui.components.DeleteConfirmDialog
import mg.caisse.ui.components.TransactionList
import mg.caisse.ui.components.rememberCategoryNames
import mg.caisse.ui.components.rememberRepository

@Composable
fun EntriesScreen(onEdit: (Long) -> Unit) {
    val repo = rememberRepository()
    val categoryNames = rememberCategoryNames()
    val scope = rememberCoroutineScope()

    val flow = remember { repo.observeByType(TxType.IN) }
    val entries by flow.collectAsStateWithLifecycle(initialValue = emptyList<TransactionEntity>())

    var pending by remember { mutableStateOf<TransactionEntity?>(null) }

    TransactionList(
        transactions = entries,
        categoryNames = categoryNames,
        emptyIcon = Icons.Filled.ArrowDownward,
        emptyTitle = "Aucune entrée",
        emptySubtitle = "Enregistrez un financement reçu pour démarrer.",
        onEdit = onEdit,
        onDelete = { pending = it }
    )

    pending?.let { tx ->
        DeleteConfirmDialog(
            message = "Supprimer cette entrée ?",
            onConfirm = {
                scope.launch { repo.deleteTransaction(tx) }
                pending = null
            },
            onDismiss = { pending = null }
        )
    }
}
