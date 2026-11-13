package mg.caisse.ui.screens.bar.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mg.caisse.data.local.entity.bar.BarProduct
import mg.caisse.ui.components.rememberBarRepository
import mg.caisse.ui.screens.bar.BarDateButton
import mg.caisse.util.parseAmount
import mg.caisse.util.todayEpochDay

@Composable
fun BarStockEntryScreen(
    productId: Long,
    onDone: () -> Unit
) {
    val repo = rememberBarRepository()
    val scope = rememberCoroutineScope()

    var product by remember { mutableStateOf<BarProduct?>(null) }
    var packCountText by remember { mutableStateOf("") }
    var unitCountText by remember { mutableStateOf("") }
    var totalCostText by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var dateEpoch by remember { mutableStateOf(todayEpochDay()) }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        product = repo.productById(productId)
    }

    val current = product
    if (current == null) {
        Column(Modifier.padding(20.dp)) {
            Text("Produit introuvable.")
        }
        return
    }

    val packCount = packCountText.toIntOrNull()
    val unitCount = unitCountText.toIntOrNull() ?: 0
    val computedQty = if (current.unitsPerPack != null && packCount != null && packCount > 0) {
        packCount * current.unitsPerPack
    } else {
        unitCount
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
            Text("Entrée de stock", style = MaterialTheme.typography.titleLarge)
        }

        Text(current.name, style = MaterialTheme.typography.titleMedium)
        Text(
            "Stock actuel : ${current.stockQty}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (current.unitsPerPack != null) {
            OutlinedTextField(
                value = packCountText,
                onValueChange = { packCountText = it.filter(Char::isDigit) },
                label = { Text("Nombre de ${current.packLabel ?: "casiers/packs"}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "1 ${current.packLabel ?: "pack"} = ${current.unitsPerPack} unités · reçu : $computedQty unités",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            OutlinedTextField(
                value = unitCountText,
                onValueChange = { unitCountText = it.filter(Char::isDigit) },
                label = { Text("Quantité reçue (unités)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = totalCostText,
            onValueChange = { totalCostText = it },
            label = { Text("Coût total de l'achat (Ar, facultatif)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = supplier,
            onValueChange = { supplier = it },
            label = { Text("Fournisseur (facultatif)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        BarDateButton(epochDay = dateEpoch, onPick = { dateEpoch = it })

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (facultatif)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Annuler") }
            Button(
                onClick = {
                    if (computedQty <= 0) return@Button
                    scope.launch {
                        repo.purchase(
                            productId = current.id,
                            packCount = packCount,
                            unitCount = unitCount,
                            totalCost = parseAmount(totalCostText),
                            supplier = supplier.ifBlank { null },
                            dateEpochDay = dateEpoch,
                            note = note.ifBlank { null }
                        )
                        onDone()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Ajouter")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
