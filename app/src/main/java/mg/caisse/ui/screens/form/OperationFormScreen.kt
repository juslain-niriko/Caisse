package mg.caisse.ui.screens.form

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mg.caisse.data.local.entity.Category
import mg.caisse.data.local.entity.CategoryKind
import mg.caisse.data.local.entity.Student
import mg.caisse.data.local.entity.TransactionEntity
import mg.caisse.data.local.entity.TxType
import mg.caisse.ui.components.ReceiptThumb
import mg.caisse.ui.components.rememberRepository
import mg.caisse.ui.theme.Red
import mg.caisse.util.copyImageToInternal
import mg.caisse.util.dateLabel
import mg.caisse.util.epochDayToDate
import mg.caisse.util.monthLabel
import mg.caisse.util.parseAmount
import mg.caisse.util.todayEpochDay
import java.time.YearMonth

private const val MILLIS_PER_DAY = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationFormScreen(
    mode: String,
    txId: Long,
    preselectedStudentId: Long,
    onDone: () -> Unit
) {
    val repo = rememberRepository()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val studentsFlow = remember { repo.observeStudents() }
    val students by studentsFlow.collectAsStateWithLifecycle(initialValue = emptyList<Student>())
    val categoriesFlow = remember { repo.observeCategories() }
    val categories by categoriesFlow.collectAsStateWithLifecycle(initialValue = emptyList<Category>())

    val isEditing = txId > 0L
    val txType = if (mode == "entry") TxType.IN else TxType.OUT

    var amountText by remember { mutableStateOf("") }
    var dateEpoch by remember { mutableStateOf(todayEpochDay()) }
    var note by remember { mutableStateOf("") }
    var projectLabel by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf(if (preselectedStudentId > 0L) preselectedStudentId else -1L) }
    var categoryId by remember { mutableStateOf(-1L) }
    var receiptPath by remember { mutableStateOf<String?>(null) }
    var existing by remember { mutableStateOf<TransactionEntity?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(txId) {
        if (isEditing) {
            val tx = repo.transactionById(txId)
            if (tx != null) {
                existing = tx
                amountText = tx.amount.toString()
                dateEpoch = tx.dateEpochDay
                note = tx.note ?: ""
                projectLabel = tx.label ?: ""
                studentId = tx.studentId ?: -1L
                categoryId = tx.categoryId
                receiptPath = tx.receiptUri
            }
        }
    }

    LaunchedEffect(categories, mode, isEditing) {
        if (!isEditing && categoryId < 0 && categories.isNotEmpty()) {
            categoryId = when (mode) {
                "entry" -> categories.firstOrNull { it.name == "Financement" }?.id ?: -1L
                "tuition" -> categories.firstOrNull { it.name == "Écolage" }?.id ?: -1L
                "investment" -> categories.firstOrNull { it.name == "Investissement" }?.id ?: -1L
                else -> categories.firstOrNull {
                    it.kind == CategoryKind.EXPENSE &&
                        it.name != "Écolage" && it.name != "Investissement"
                }?.id ?: -1L
            }
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            receiptPath = copyImageToInternal(context, uri) ?: uri.toString()
        }
    }

    val amount = parseAmount(amountText)
    val canSave = amount != null && amount > 0 &&
        categoryId >= 0 && (mode != "tuition" || studentId > 0)

    fun save() {
        val amt = parseAmount(amountText) ?: return
        if (categoryId < 0) return
        val yearMonth = YearMonth.from(epochDayToDate(dateEpoch))
        val finalLabel = when (mode) {
            "investment" -> projectLabel.ifBlank { null }
            "tuition" -> "Écolage ${monthLabel(yearMonth).lowercase()}"
            else -> existing?.label
        }
        val entity = TransactionEntity(
            id = existing?.id ?: 0L,
            type = txType,
            amount = amt,
            dateEpochDay = dateEpoch,
            categoryId = categoryId,
            studentId = if (mode == "tuition") studentId.takeIf { it > 0 } else existing?.studentId,
            label = finalLabel,
            note = note.ifBlank { null },
            receiptUri = receiptPath,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        scope.launch {
            if (isEditing) repo.updateTransaction(entity) else repo.addTransaction(entity)
            onDone()
        }
    }

    val title = when (mode) {
        "entry" -> if (isEditing) "Modifier l'entrée" else "Nouvelle entrée"
        "tuition" -> "Paiement d'écolage"
        "investment" -> if (isEditing) "Modifier l'investissement" else "Nouvel investissement"
        else -> if (isEditing) "Modifier l'activité" else "Nouvelle activité"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Montant (Ar)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(dateLabel(dateEpoch))
        }

        when (mode) {
            "tuition" -> {
                val selectedStudent = students.firstOrNull { it.id == studentId }
                LabeledDropdown(
                    label = "Élève",
                    selectedText = selectedStudent?.fullName ?: "Choisir un élève",
                    options = students.map { it.id to it.fullName },
                    onSelect = { studentId = it }
                )
            }

            "activity" -> {
                val options = categories
                    .filter { it.kind == CategoryKind.EXPENSE && it.name != "Écolage" && it.name != "Investissement" }
                    .map { it.id to it.name }
                val selected = options.firstOrNull { it.first == categoryId }?.second ?: "Choisir une catégorie"
                LabeledDropdown(
                    label = "Catégorie",
                    selectedText = selected,
                    options = options,
                    onSelect = { categoryId = it }
                )
            }

            "investment" -> {
                OutlinedTextField(
                    value = projectLabel,
                    onValueChange = { projectLabel = it },
                    label = { Text("Projet / description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (facultatif)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Justificatif (facultatif)", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (receiptPath == null) "Ajouter une photo" else "Changer la photo")
            }
            if (receiptPath != null) {
                Spacer(Modifier.width(12.dp))
                ReceiptThumb(path = receiptPath)
                TextButton(onClick = { receiptPath = null }) { Text("Retirer") }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.weight(1f)
            ) {
                Text("Annuler")
            }
            Button(
                onClick = { save() },
                enabled = canSave,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isEditing) "Enregistrer" else "Ajouter")
            }
        }

        if (isEditing) {
            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Supprimer", color = Red)
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateEpoch * MILLIS_PER_DAY)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dateEpoch = Math.floorDiv(millis, MILLIS_PER_DAY)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Supprimer") },
            text = { Text("Supprimer cette opération ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            existing?.let { repo.deleteTransaction(it) }
                            showDelete = false
                            onDone()
                        }
                    }
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun LabeledDropdown(
    label: String,
    selectedText: String,
    options: List<Pair<Long, String>>,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$label : $selectedText",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}
