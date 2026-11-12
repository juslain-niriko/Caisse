package mg.caisse.ui.screens.students

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import mg.caisse.data.local.entity.TransactionEntity
import mg.caisse.ui.components.DeleteConfirmDialog
import mg.caisse.ui.components.TransactionRow
import mg.caisse.ui.components.rememberCategoryNames
import mg.caisse.ui.components.rememberRepository
import mg.caisse.util.formatAriary

@Composable
fun StudentDetailScreen(
    studentId: Long,
    onBack: () -> Unit,
    onPayTuition: () -> Unit
) {
    val repo = rememberRepository()
    val scope = rememberCoroutineScope()
    val categoryNames = rememberCategoryNames()

    val studentFlow = remember(studentId) { repo.observeStudent(studentId) }
    val student by studentFlow.collectAsStateWithLifecycle(initialValue = null)

    val totalFlow = remember(studentId) { repo.observeTotalForStudent(studentId) }
    val total by totalFlow.collectAsStateWithLifecycle(initialValue = 0L)

    val historyFlow = remember(studentId) { repo.observeForStudent(studentId) }
    val history by historyFlow.collectAsStateWithLifecycle(initialValue = emptyList<TransactionEntity>())

    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
            }
            Text(
                "Fiche élève",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showEdit = true }) {
                Icon(Icons.Filled.Edit, contentDescription = "Modifier")
            }
            IconButton(onClick = { showDelete = true }) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Supprimer")
            }
        }

        val current = student
        if (current == null) {
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(
                            current.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val subtitle = listOfNotNull(current.level, current.school).joinToString(" · ")
                        if (subtitle.isNotBlank()) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (!current.guardianContact.isNullOrBlank()) {
                            Text(
                                "Tuteur : ${current.guardianContact}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Total payé",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            formatAriary(total),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            item {
                FilledTonalButton(
                    onClick = onPayTuition,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.School, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Payer l'écolage")
                }
            }
            item {
                Text(
                    "Historique",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (history.isEmpty()) {
                item {
                    Text(
                        "Aucun paiement enregistré pour cet élève.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(history) { tx ->
                    TransactionRow(
                        transaction = tx,
                        categoryName = categoryNames[tx.categoryId] ?: "—",
                        onClick = {}
                    )
                }
            }
        }
    }

    val currentStudent = student
    if (showEdit && currentStudent != null) {
        StudentEditDialog(
            initial = currentStudent,
            onDismiss = { showEdit = false },
            onSave = { updated ->
                scope.launch { repo.updateStudent(updated) }
                showEdit = false
            }
        )
    }
    if (showDelete && currentStudent != null) {
        DeleteConfirmDialog(
            message = "Supprimer cet élève ? Ses paiements seront conservés mais ne seront plus rattachés.",
            onConfirm = {
                scope.launch {
                    repo.deleteStudent(currentStudent)
                    onBack()
                }
                showDelete = false
            },
            onDismiss = { showDelete = false }
        )
    }
}
