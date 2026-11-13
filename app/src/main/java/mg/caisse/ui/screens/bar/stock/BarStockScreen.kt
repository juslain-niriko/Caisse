package mg.caisse.ui.screens.bar.stock

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mg.caisse.data.local.entity.bar.BarMoveType
import mg.caisse.data.local.entity.bar.BarProduct
import mg.caisse.ui.components.EmptyState
import mg.caisse.ui.components.rememberBarRepository
import mg.caisse.ui.screens.bar.label
import mg.caisse.ui.theme.Red
import mg.caisse.util.todayEpochDay

@Composable
fun BarStockScreen(
    onEditProduct: (Long) -> Unit,
    onNewProduct: () -> Unit,
    onStockEntry: (Long) -> Unit
) {
    val repo = rememberBarRepository()
    val scope = rememberCoroutineScope()
    val productsFlow = remember { repo.observeProducts() }
    val products by productsFlow.collectAsStateWithLifecycle(initialValue = emptyList<BarProduct>())
    var lossProduct by remember { mutableStateOf<BarProduct?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedButton(onClick = onNewProduct, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nouveau produit")
            }
        }
        if (products.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "Aucun produit",
                    subtitle = "Ajoutez une boisson pour commencer à suivre le stock."
                )
            }
        } else {
            items(products, key = { it.id }) { product ->
                BarProductCard(
                    product = product,
                    onClick = { onEditProduct(product.id) },
                    onStockEntry = { onStockEntry(product.id) },
                    onLoss = { lossProduct = product }
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }

    lossProduct?.let { product ->
        LossDialog(
            product = product,
            onDismiss = { lossProduct = null },
            onConfirm = { quantity, type, note ->
                scope.launch {
                    repo.recordLoss(product.id, quantity, type, todayEpochDay(), note)
                }
                lossProduct = null
            }
        )
    }
}

@Composable
private fun BarProductCard(
    product: BarProduct,
    onClick: () -> Unit,
    onStockEntry: () -> Unit,
    onLoss: () -> Unit
) {
    val low = product.stockQty <= product.alertQty
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val size = product.sizeCl?.let { " · $it cl" } ?: ""
                Text(
                    "${product.category.label()} · ${product.unit.label()}$size",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Stock : ${product.stockQty}${if (low) " (seuil ${product.alertQty})" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (low) Red else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onStockEntry) {
                Icon(Icons.Filled.AddCircle, contentDescription = "Entrée de stock")
            }
            IconButton(onClick = onLoss) {
                Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Perte ou offert")
            }
        }
    }
}

@Composable
private fun LossDialog(
    product: BarProduct,
    onDismiss: () -> Unit,
    onConfirm: (Int, BarMoveType, String?) -> Unit
) {
    var quantityText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(BarMoveType.LOSS) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == BarMoveType.LOSS) "Perte / casse" else "Boisson offerte") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == BarMoveType.LOSS,
                        onClick = { type = BarMoveType.LOSS },
                        label = { Text("Perte") }
                    )
                    FilterChip(
                        selected = type == BarMoveType.GIFT,
                        onClick = { type = BarMoveType.GIFT },
                        label = { Text("Offert") }
                    )
                }
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter(Char::isDigit) },
                    label = { Text("Quantité") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (facultatif)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = quantityText.toIntOrNull() ?: 0
                    if (quantity > 0) onConfirm(quantity, type, note.ifBlank { null })
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
