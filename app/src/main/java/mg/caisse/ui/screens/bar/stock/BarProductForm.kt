package mg.caisse.ui.screens.bar.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mg.caisse.data.local.entity.bar.BarCategory
import mg.caisse.data.local.entity.bar.BarProduct
import mg.caisse.data.local.entity.bar.BarUnit
import mg.caisse.ui.components.rememberBarRepository
import mg.caisse.ui.screens.bar.label
import mg.caisse.ui.theme.Red
import mg.caisse.util.parseAmount

@Composable
fun BarProductFormScreen(
    productId: Long,
    onDone: () -> Unit
) {
    val repo = rememberBarRepository()
    val scope = rememberCoroutineScope()

    var existing by remember { mutableStateOf<BarProduct?>(null) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(BarCategory.BEER) }
    var unit by remember { mutableStateOf(BarUnit.BOTTLE) }
    var sizeCl by remember { mutableStateOf("") }
    var unitsPerPack by remember { mutableStateOf("") }
    var packLabel by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var alertQty by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId > 0) {
            val product = repo.productById(productId)
            if (product != null) {
                existing = product
                name = product.name
                category = product.category
                unit = product.unit
                sizeCl = product.sizeCl?.toString() ?: ""
                unitsPerPack = product.unitsPerPack?.toString() ?: ""
                packLabel = product.packLabel ?: ""
                salePrice = product.salePrice.toString()
                costPrice = product.costPrice?.toString() ?: ""
                alertQty = product.alertQty.toString()
                active = product.active
            }
        }
    }

    fun save() {
        val price = parseAmount(salePrice) ?: return
        if (name.isBlank() || price <= 0) return
        val entity = BarProduct(
            id = existing?.id ?: 0L,
            name = name.trim(),
            category = category,
            unit = unit,
            sizeCl = sizeCl.toIntOrNull(),
            unitsPerPack = unitsPerPack.toIntOrNull(),
            packLabel = packLabel.ifBlank { null },
            salePrice = price,
            costPrice = parseAmount(costPrice),
            stockQty = existing?.stockQty ?: 0,
            alertQty = alertQty.toIntOrNull() ?: 0,
            active = active,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        scope.launch {
            if (existing == null) repo.addProduct(entity) else repo.updateProduct(entity)
            onDone()
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
                if (existing == null) "Nouveau produit" else "Modifier le produit",
                style = MaterialTheme.typography.titleLarge
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom du produit") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownField(
            label = "Catégorie",
            selected = category,
            options = BarCategory.values().toList(),
            display = { it.label() },
            onSelect = { category = it }
        )

        DropdownField(
            label = "Unité",
            selected = unit,
            options = BarUnit.values().toList(),
            display = { it.label() },
            onSelect = { unit = it }
        )

        OutlinedTextField(
            value = sizeCl,
            onValueChange = { sizeCl = it.filter(Char::isDigit) },
            label = { Text("Contenance (cl, facultatif)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = unitsPerPack,
            onValueChange = { unitsPerPack = it.filter(Char::isDigit) },
            label = { Text("Unités par casier/pack (facultatif)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = packLabel,
            onValueChange = { packLabel = it },
            label = { Text("Nom du pack (casier, cageot…)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = salePrice,
            onValueChange = { salePrice = it },
            label = { Text("Prix de vente (Ar)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = costPrice,
            onValueChange = { costPrice = it },
            label = { Text("Coût d'achat (Ar, facultatif)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = alertQty,
            onValueChange = { alertQty = it.filter(Char::isDigit) },
            label = { Text("Seuil d'alerte (unités)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (existing != null) {
            Text(
                "Stock actuel : ${existing?.stockQty ?: 0}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = active, onCheckedChange = { active = it })
                Text("Produit actif", modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Annuler") }
            Button(onClick = { save() }, modifier = Modifier.weight(1f)) {
                Text(if (existing == null) "Ajouter" else "Enregistrer")
            }
        }

        if (existing != null) {
            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Supprimer / désactiver", color = Red)
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Supprimer le produit") },
            text = { Text("S'il a déjà des mouvements, il sera désactivé au lieu d'être supprimé.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        existing?.let { repo.removeProduct(it) }
                        showDelete = false
                        onDone()
                    }
                }) { Text("Confirmer") }
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
