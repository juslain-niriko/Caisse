package mg.caisse.ui.screens.bar.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mg.caisse.data.local.dao.bar.BarProductTotal
import mg.caisse.data.local.entity.bar.BarProduct
import mg.caisse.data.local.entity.bar.BarProfile
import mg.caisse.ui.components.SectionTitle
import mg.caisse.ui.components.StatCard
import mg.caisse.ui.components.StatCardText
import mg.caisse.ui.components.rememberBarRepository
import mg.caisse.util.currentYearMonth
import mg.caisse.util.formatAriary
import mg.caisse.util.monthRange
import mg.caisse.util.todayEpochDay

@Composable
fun BarHomeScreen(
    onNewSale: () -> Unit,
    onOpenStock: () -> Unit,
    onOpenReports: () -> Unit
) {
    val repo = rememberBarRepository()
    val today = remember { todayEpochDay() }
    val month = remember { currentYearMonth() }
    val range = remember(month) { monthRange(month) }

    val profileFlow = remember { repo.observeProfile() }
    val profile by profileFlow.collectAsStateWithLifecycle(initialValue = null as BarProfile?)

    val todaySalesFlow = remember(today) { repo.observeSalesTotal(today, today) }
    val todaySales by todaySalesFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val monthSalesFlow = remember(range) { repo.observeSalesTotal(range.first, range.second) }
    val monthSales by monthSalesFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val purchasesFlow = remember(range) { repo.observePurchases(range.first, range.second) }
    val purchases by purchasesFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val expensesFlow = remember(range) { repo.observeExpensesTotal(range.first, range.second) }
    val expenses by expensesFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val lowStockFlow = remember { repo.observeLowStock() }
    val lowStock by lowStockFlow.collectAsStateWithLifecycle(initialValue = emptyList<BarProduct>())

    val topFlow = remember(range) { repo.observeTopProducts(range.first, range.second, 5) }
    val topProducts by topFlow.collectAsStateWithLifecycle(initialValue = emptyList<BarProductTotal>())

    val stockValueFlow = remember { repo.observeStockValue() }
    val stockValue by stockValueFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val balance = monthSales - purchases - expenses

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                profile?.name ?: "Mon bar",
                style = MaterialTheme.typography.headlineSmall
            )
            if (!profile?.location.isNullOrBlank()) {
                Text(
                    profile?.location ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(
                        "Recette du jour",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        formatAriary(todaySales),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Recette (mois)", monthSales, Modifier.weight(1f))
                StatCard("Dépenses (mois)", expenses, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Achats (mois)", purchases, Modifier.weight(1f))
                StatCard("Solde caisse", balance, Modifier.weight(1f))
            }
        }
        item {
            StatCard("Valeur du stock", stockValue, Modifier.fillMaxWidth())
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onNewSale, modifier = Modifier.weight(1f)) {
                    Text("Nouvelle vente")
                }
                FilledTonalButton(onClick = onOpenStock, modifier = Modifier.weight(1f)) {
                    Text("Stock")
                }
            }
        }
        item {
            FilledTonalButton(onClick = onOpenReports, modifier = Modifier.fillMaxWidth()) {
                Text("Voir les rapports")
            }
        }

        item { SectionTitle("Alertes de stock") }
        if (lowStock.isEmpty()) {
            item {
                Text(
                    "Stock suffisant pour tous les produits.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(lowStock, key = { it.id }) { product ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(product.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${product.stockQty} / seuil ${product.alertQty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        item { SectionTitle("Top ventes du mois") }
        if (topProducts.isEmpty()) {
            item {
                Text(
                    "Aucune vente enregistrée ce mois.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(topProducts) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(row.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "× ${row.quantity} · ${formatAriary(row.total)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
