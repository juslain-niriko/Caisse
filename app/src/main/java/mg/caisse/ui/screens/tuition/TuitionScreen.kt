package mg.caisse.ui.screens.tuition

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mg.caisse.data.local.dao.TuitionRow
import mg.caisse.ui.components.MonthSelector
import mg.caisse.ui.components.rememberRepository
import mg.caisse.ui.theme.Green
import mg.caisse.util.currentYearMonth
import mg.caisse.util.formatAriary
import mg.caisse.util.monthRange
import java.time.YearMonth

@Composable
fun TuitionScreen(onPayForStudent: (Long) -> Unit) {
    val repo = rememberRepository()
    var yearMonth by remember { mutableStateOf(currentYearMonth()) }
    val range = remember(yearMonth) { monthRange(yearMonth) }

    val rowsFlow = remember(range) { repo.observeTuitionMonth(range.first, range.second) }
    val rows by rowsFlow.collectAsStateWithLifecycle(initialValue = emptyList<TuitionRow>())

    val totalFlow = remember(range) { repo.observeTuitionTotal(range.first, range.second) }
    val total by totalFlow.collectAsStateWithLifecycle(initialValue = 0L)

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
                    "Total écolage du mois",
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
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (rows.isEmpty()) {
                item {
                    Text(
                        "Aucun élève actif. Ajoutez un élève depuis l'onglet Élèves.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(rows) { row ->
                    TuitionCard(row = row, onClick = { onPayForStudent(row.studentId) })
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun TuitionCard(row: TuitionRow, onClick: () -> Unit) {
    val paid = row.paid > 0
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(row.name, style = MaterialTheme.typography.titleMedium)
                if (!row.level.isNullOrBlank()) {
                    Text(
                        row.level,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = if (paid) formatAriary(row.paid) else "Non payé",
                style = MaterialTheme.typography.titleMedium,
                color = if (paid) Green else Color(0xFFB45309)
            )
        }
    }
}
