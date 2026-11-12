package mg.caisse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mg.caisse.data.local.entity.TransactionEntity

@Composable
fun rememberCategoryNames(): Map<Long, String> {
    val repo = rememberRepository()
    val categoriesFlow = remember { repo.observeCategories() }
    val categories by categoriesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    return remember(categories) { categories.associate { it.id to it.name } }
}

@Composable
fun TransactionList(
    transactions: List<TransactionEntity>,
    categoryNames: Map<Long, String>,
    emptyIcon: ImageVector,
    emptyTitle: String,
    emptySubtitle: String,
    onEdit: (Long) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (transactions.isEmpty()) {
            item { EmptyState(emptyIcon, emptyTitle, emptySubtitle) }
        } else {
            items(transactions) { tx ->
                TransactionRow(
                    transaction = tx,
                    categoryName = categoryNames[tx.categoryId] ?: "—",
                    onClick = { onEdit(tx.id) },
                    onDelete = { onDelete(tx) }
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Supprimer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
