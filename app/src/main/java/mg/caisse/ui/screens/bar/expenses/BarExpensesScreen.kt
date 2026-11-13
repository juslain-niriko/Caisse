package mg.caisse.ui.screens.bar.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mg.caisse.data.local.entity.bar.BarExpense
import mg.caisse.ui.components.DeleteConfirmDialog
import mg.caisse.ui.components.EmptyState
import mg.caisse.ui.components.MonthSelector
import mg.caisse.ui.components.rememberBarRepository
import mg.caisse.ui.screens.bar.label
import mg.caisse.util.currentYearMonth
import mg.caisse.util.dateLabel
import mg.caisse.util.formatAriary
import mg.caisse.util.monthRange
import java.time.YearMonth

@Composable
fun BarExpensesScreen(
    onNewExpense: () -> Unit,
    onEditExpense: (Long) -> Unit
) {
    val repo = rememberBarRepository()
    val scope = rememberCoroutineScope()
    var yearMonth by remember { mutableStateOf(currentYearMonth()) }
    val range = remember(yearMonth) { monthRange(yearMonth) }

    val expensesFlow = remember(range) { repo.observeExpenses(range.first, range.second) }
    val expenses by expensesFlow.collectAsStateWithLifecycle(initialValue = emptyList<BarExpense>())
    val totalFlow = remember(range) { repo.observeExpensesTotal(range.first, range.second) }
    val total by totalFlow.collectAsStateWithLifecycle(initialValue = 0L)

    var pending by remember { mutableStateOf<BarExpense?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        MonthSelector(
            yearMonth = yearMonth,
            onPrevious = { yearMonth = yearMonth.minusMonths(1) },
            onNext = { yearMonth = yearMonth.plusMonths(1) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Dépenses du mois",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    formatAriary(total),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedButton(onClick = onNewExpense, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nouvelle dépense")
                }
            }
            if (expenses.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Payments,
                        title = "Aucune dépense",
                        subtitle = "Enregistrez le loyer, les salaires, la glace ou le transport."
                    )
                }
            } else {
                items(expenses, key = { it.id }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        onClick = { onEditExpense(expense.id) },
                        onDelete = { pending = expense }
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    pending?.let { expense ->
        DeleteConfirmDialog(
            message = "Supprimer cette dépense ?",
            onConfirm = {
                scope.launch { repo.deleteExpense(expense) }
                pending = null
            },
            onDismiss = { pending = null }
        )
    }
}

@Composable
private fun ExpenseRow(
    expense: BarExpense,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category.label(), style = MaterialTheme.typography.titleMedium)
                Text(
                    buildString {
                        append(dateLabel(expense.dateEpochDay))
                        expense.supplier?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!expense.note.isNullOrBlank()) {
                    Text(
                        expense.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                formatAriary(expense.amount),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Supprimer")
            }
        }
    }
}
