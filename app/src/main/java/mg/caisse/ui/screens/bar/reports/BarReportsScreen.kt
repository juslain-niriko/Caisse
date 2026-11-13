package mg.caisse.ui.screens.bar.reports

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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mg.caisse.data.local.dao.bar.BarProductTotal
import mg.caisse.data.local.entity.bar.BarProfile
import mg.caisse.ui.components.MonthSelector
import mg.caisse.ui.components.rememberBarRepository
import mg.caisse.util.createBarReportPdf
import mg.caisse.util.currentYearMonth
import mg.caisse.util.formatAriary
import mg.caisse.util.monthRange
import mg.caisse.util.sharePdf
import java.time.YearMonth

@Composable
fun BarReportsScreen() {
    val repo = rememberBarRepository()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var yearMonth by remember { mutableStateOf(currentYearMonth()) }
    val range = remember(yearMonth) { monthRange(yearMonth) }

    val revenueFlow = remember(range) { repo.observeSalesTotal(range.first, range.second) }
    val revenue by revenueFlow.collectAsStateWithLifecycle(initialValue = 0L)
    val purchasesFlow = remember(range) { repo.observePurchases(range.first, range.second) }
    val purchases by purchasesFlow.collectAsStateWithLifecycle(initialValue = 0L)
    val expensesFlow = remember(range) { repo.observeExpensesTotal(range.first, range.second) }
    val expenses by expensesFlow.collectAsStateWithLifecycle(initialValue = 0L)
    val stockValueFlow = remember { repo.observeStockValue() }
    val stockValue by stockValueFlow.collectAsStateWithLifecycle(initialValue = 0L)
    val lossesFlow = remember(range) { repo.observeLossQuantity(range.first, range.second) }
    val losses by lossesFlow.collectAsStateWithLifecycle(initialValue = 0L)
    val topFlow = remember(range) { repo.observeTopProducts(range.first, range.second, 10) }
    val topProducts by topFlow.collectAsStateWithLifecycle(initialValue = emptyList<BarProductTotal>())
    val profileFlow = remember { repo.observeProfile() }
    val profile by profileFlow.collectAsStateWithLifecycle(initialValue = null as BarProfile?)

    val balance = revenue - purchases - expenses

    Column(modifier = Modifier.fillMaxSize()) {
        MonthSelector(
            yearMonth = yearMonth,
            onPrevious = { yearMonth = yearMonth.minusMonths(1) },
            onNext = { yearMonth = yearMonth.plusMonths(1) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        ReportLineRow("Recettes", revenue)
                        ReportLineRow("Achats", purchases)
                        ReportLineRow("Dépenses", expenses)
                        Spacer(Modifier.height(6.dp))
                        ReportLineRow("Solde caisse", balance, bold = true)
                        ReportLineRow("Valeur du stock", stockValue)
                        ReportLineRow("Pertes (unités)", losses)
                    }
                }
            }

            item { ReportSectionTitle("Top produits") }
            if (topProducts.isEmpty()) {
                item { ReportEmpty() }
            } else {
                items(topProducts) { row ->
                    ReportLineRow("${row.name} × ${row.quantity}", row.total)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val data = repo.buildReport(yearMonth)
                            val file = withContext(Dispatchers.IO) {
                                createBarReportPdf(
                                    context = context,
                                    barName = profile?.name ?: "Mon bar",
                                    period = data.periodLabel,
                                    revenue = data.revenue,
                                    purchases = data.purchases,
                                    expenses = data.expenses,
                                    balance = data.balance,
                                    stockValue = data.stockValue,
                                    lossQuantity = data.lossQuantity,
                                    topProducts = data.topProducts
                                )
                            }
                            sharePdf(context, file)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Exporter en PDF")
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ReportLineRow(label: String, amount: Long, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            if (label.startsWith("Pertes")) amount.toString() else formatAriary(amount),
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ReportSectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ReportEmpty() {
    Text(
        "Aucune donnée pour cette période.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
