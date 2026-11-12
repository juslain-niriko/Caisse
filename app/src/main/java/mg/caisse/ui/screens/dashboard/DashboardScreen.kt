package mg.caisse.ui.screens.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mg.caisse.data.local.dao.NameTotal
import mg.caisse.data.local.entity.TransactionEntity
import mg.caisse.ui.components.SectionTitle
import mg.caisse.ui.components.StatCard
import mg.caisse.ui.components.StatCardText
import mg.caisse.ui.components.TransactionRow
import mg.caisse.ui.components.rememberRepository
import mg.caisse.ui.navigation.Routes
import mg.caisse.ui.theme.Green
import mg.caisse.ui.theme.Red
import mg.caisse.ui.theme.Teal
import mg.caisse.util.currentYearMonth
import mg.caisse.util.formatAriary
import mg.caisse.util.monthRange

private fun editRoute(tx: TransactionEntity, categoryName: String): String {
    val mode = when {
        tx.type == mg.caisse.data.local.entity.TxType.IN -> "entry"
        categoryName == "Écolage" -> "tuition"
        categoryName == "Investissement" -> "investment"
        else -> "activity"
    }
    return Routes.form(mode, tx.id, tx.studentId ?: -1L)
}

@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    val repo = rememberRepository()
    val yearMonth = remember { currentYearMonth() }
    val range = remember(yearMonth) { monthRange(yearMonth) }

    val balanceFlow = remember { repo.observeBalance() }
    val balance by balanceFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val monthInFlow = remember(range) { repo.observeSumByType(mg.caisse.data.local.entity.TxType.IN, range.first, range.second) }
    val monthIn by monthInFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val monthOutFlow = remember(range) { repo.observeSumByType(mg.caisse.data.local.entity.TxType.OUT, range.first, range.second) }
    val monthOut by monthOutFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val tuitionFlow = remember(range) { repo.observeTuitionTotal(range.first, range.second) }
    val tuition by tuitionFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val byCategoryFlow = remember(range) { repo.observeOutByCategory(range.first, range.second) }
    val byCategory by byCategoryFlow.collectAsStateWithLifecycle(initialValue = emptyList<NameTotal>())

    val recentFlow = remember { repo.observeRecent(6) }
    val recent by recentFlow.collectAsStateWithLifecycle(initialValue = emptyList<TransactionEntity>())

    val studentsFlow = remember { repo.observeStudents() }
    val students by studentsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val categoriesFlow = remember { repo.observeCategories() }
    val categories by categoriesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val categoryNames = remember(categories) { categories.associate { it.id to it.name } }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(
                        "Solde de la caisse",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        formatAriary(balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "Mise à jour selon les opérations enregistrées",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Reçu (mois)", monthIn, Modifier.weight(1f))
                StatCard("Dépensé (mois)", monthOut, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Écolage (mois)", tuition, Modifier.weight(1f))
                StatCardText("Élèves actifs", students.size.toString(), Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(onClick = { onNavigate(Routes.form("entry")) }) { Text("+ Entrée") }
                FilledTonalButton(onClick = { onNavigate(Routes.form("tuition")) }) { Text("+ Écolage") }
                FilledTonalButton(onClick = { onNavigate(Routes.form("activity")) }) { Text("+ Activité") }
                FilledTonalButton(onClick = { onNavigate(Routes.form("investment")) }) { Text("+ Investissement") }
            }
        }

        item { SectionTitle("Répartition des sorties") }
        if (byCategory.isEmpty()) {
            item {
                Text(
                    "Aucune sortie enregistrée ce mois.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val max = byCategory.maxOf { it.total }.coerceAtLeast(1L)
            items(byCategory) { row ->
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(row.name, style = MaterialTheme.typography.bodyMedium)
                        Text(formatAriary(row.total), style = MaterialTheme.typography.bodyMedium)
                    }
                    LinearProgressIndicator(
                        progress = { row.total.toFloat() / max.toFloat() },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        color = Teal
                    )
                }
            }
        }

        item { SectionTitle("Dernières opérations") }
        if (recent.isEmpty()) {
            item {
                Text(
                    "Aucune opération pour l'instant.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recent) { tx ->
                TransactionRow(
                    transaction = tx,
                    categoryName = categoryNames[tx.categoryId] ?: "—",
                    onClick = { onNavigate(editRoute(tx, categoryNames[tx.categoryId] ?: "")) }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
