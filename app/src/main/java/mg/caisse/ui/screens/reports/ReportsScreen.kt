package mg.caisse.ui.screens.reports

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
import mg.caisse.data.local.dao.NameTotal
import mg.caisse.data.local.entity.TxType
import mg.caisse.ui.components.MonthSelector
import mg.caisse.ui.components.rememberRepository
import mg.caisse.util.ReportLine
import mg.caisse.util.createReportPdf
import mg.caisse.util.currentYearMonth
import mg.caisse.util.formatAriary
import mg.caisse.util.monthRange
import mg.caisse.util.sharePdf
import java.time.YearMonth

@Composable
fun ReportsScreen() {
    val repo = rememberRepository()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var yearMonth by remember { mutableStateOf(currentYearMonth()) }
    val range = remember(yearMonth) { monthRange(yearMonth) }

    val inFlow = remember(range) { repo.observeSumByType(TxType.IN, range.first, range.second) }
    val totalIn by inFlow.collectAsStateWithLifecycle(initialValue = 0L)
    val outFlow = remember(range) { repo.observeSumByType(TxType.OUT, range.first, range.second) }
    val totalOut by outFlow.collectAsStateWithLifecycle(initialValue = 0L)
    val byCategoryFlow = remember(range) { repo.observeOutByCategory(range.first, range.second) }
    val byCategory by byCategoryFlow.collectAsStateWithLifecycle(initialValue = emptyList<NameTotal>())
    val byStudentFlow = remember(range) { repo.observeOutByStudent(range.first, range.second) }
    val byStudent by byStudentFlow.collectAsStateWithLifecycle(initialValue = emptyList<NameTotal>())

    val balance = totalIn - totalOut

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
                        ReportLineRow("Entrées", totalIn)
                        ReportLineRow("Sorties", totalOut)
                        Spacer(Modifier.height(6.dp))
                        ReportLineRow("Solde", balance, bold = true)
                    }
                }
            }

            item { ReportSectionTitle("Sorties par catégorie") }
            if (byCategory.isEmpty()) {
                item { ReportEmpty() }
            } else {
                items(byCategory) { row -> ReportLineRow(row.name, row.total) }
            }

            item { ReportSectionTitle("Écolage par élève") }
            if (byStudent.isEmpty()) {
                item { ReportEmpty() }
            } else {
                items(byStudent) { row -> ReportLineRow(row.name, row.total) }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val data = repo.buildReport(yearMonth)
                            val file = withContext(Dispatchers.IO) {
                                createReportPdf(
                                    context = context,
                                    title = "Caisse — Rapport",
                                    period = data.periodLabel,
                                    totalIn = data.totalIn,
                                    totalOut = data.totalOut,
                                    balance = data.balance,
                                    byCategory = data.byCategory.map { ReportLine(it.name, it.total) },
                                    byStudent = data.byStudent.map { ReportLine(it.name, it.total) }
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
            formatAriary(amount),
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
