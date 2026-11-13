package mg.caisse.ui.screens.bar.expenses

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.launch
import mg.caisse.data.local.entity.bar.BarExpense
import mg.caisse.data.local.entity.bar.BarExpenseCategory
import mg.caisse.ui.components.ReceiptThumb
import mg.caisse.ui.components.rememberBarRepository
import mg.caisse.ui.screens.bar.BarDateButton
import mg.caisse.ui.screens.bar.label
import mg.caisse.ui.theme.Red
import mg.caisse.util.copyImageToInternal
import mg.caisse.util.parseAmount
import mg.caisse.util.todayEpochDay

@Composable
fun BarExpenseFormScreen(
    expenseId: Long,
    onDone: () -> Unit
) {
    val repo = rememberBarRepository()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var existing by remember { mutableStateOf<BarExpense?>(null) }
    var dateEpoch by remember { mutableStateOf(todayEpochDay()) }
    var category by remember { mutableStateOf(BarExpenseCategory.OTHER) }
    var amountText by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var receiptPath by remember { mutableStateOf<String?>(null) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) {
        if (expenseId > 0) {
            val expense = repo.expenseById(expenseId)
            if (expense != null) {
                existing = expense
                dateEpoch = expense.dateEpochDay
                category = expense.category
                amountText = expense.amount.toString()
                supplier = expense.supplier ?: ""
                note = expense.note ?: ""
                receiptPath = expense.receiptUri
            }
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            receiptPath = copyImageToInternal(context, uri) ?: uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDone) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
            }
            Text(
                if (existing == null) "Nouvelle dépense" else "Modifier la dépense",
                style = MaterialTheme.typography.titleLarge
            )
        }

        BarDateButton(epochDay = dateEpoch, onPick = { dateEpoch = it })

        DropdownField(
            label = "Catégorie",
            selected = category,
            options = BarExpenseCategory.values().toList(),
            display = { it.label() },
            onSelect = { category = it }
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Montant (Ar)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = supplier,
            onValueChange = { supplier = it },
            label = { Text("Fournisseur / bénéficiaire (facultatif)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Annuler") }
            Button(
                onClick = {
                    val amount = parseAmount(amountText) ?: return@Button
                    if (amount <= 0) return@Button
                    val entity = BarExpense(
                        id = existing?.id ?: 0L,
                        dateEpochDay = dateEpoch,
                        category = category,
                        amount = amount,
                        supplier = supplier.ifBlank { null },
                        note = note.ifBlank { null },
                        receiptUri = receiptPath,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    )
                    scope.launch {
                        if (existing == null) repo.addExpense(entity) else repo.updateExpense(entity)
                        onDone()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (existing == null) "Ajouter" else "Enregistrer")
            }
        }

        if (existing != null) {
            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Supprimer", color = Red)
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Supprimer") },
            text = { Text("Supprimer cette dépense ?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        existing?.let { repo.deleteExpense(it) }
                        showDelete = false
                        onDone()
                    }
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun <T> DropdownField(
    label: String,
    selected: T,
    options: List<T>,
    display: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                "$label : ${display(selected)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(display(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
