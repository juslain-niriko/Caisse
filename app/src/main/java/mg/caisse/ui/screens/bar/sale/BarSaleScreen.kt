package mg.caisse.ui.screens.bar.sale

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mg.caisse.data.local.entity.bar.BarProduct
import mg.caisse.data.repository.SaleLineInput
import mg.caisse.ui.components.EmptyState
import mg.caisse.ui.components.rememberBarRepository
import mg.caisse.ui.screens.bar.label
import mg.caisse.util.formatAriary
import mg.caisse.util.todayEpochDay

@Composable
fun BarSaleScreen() {
    val repo = rememberBarRepository()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val productsFlow = remember { repo.observeProducts() }
    val products by productsFlow.collectAsStateWithLifecycle(initialValue = emptyList<BarProduct>())
    var cart by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }

    val count = cart.values.sum()
    val total = cart.entries.sumOf { (id, qty) ->
        (products.firstOrNull { it.id == id }?.salePrice ?: 0L) * qty
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (products.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.ShoppingCart,
                title = "Aucun produit",
                subtitle = "Ajoutez des boissons dans le stock avant de vendre.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    val quantity = cart[product.id] ?: 0
                    SaleProductRow(
                        product = product,
                        quantity = quantity,
                        onAdd = {
                            if (quantity < product.stockQty) {
                                cart = cart.toMutableMap().apply { this[product.id] = quantity + 1 }
                            }
                        },
                        onRemove = {
                            if (quantity > 0) {
                                val updated = cart.toMutableMap()
                                if (quantity - 1 == 0) updated.remove(product.id)
                                else updated[product.id] = quantity - 1
                                cart = updated
                            }
                        }
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Panier · $count article${if (count > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        formatAriary(total),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Button(
                    onClick = {
                        val inputs = cart.map { SaleLineInput(it.key, it.value) }
                        scope.launch {
                            try {
                                repo.createSale(inputs, todayEpochDay())
                                cart = emptyMap()
                                Toast.makeText(context, "Vente enregistrée", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    e.message ?: "Impossible d'enregistrer la vente",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    enabled = cart.isNotEmpty()
                ) {
                    Text("Encaisser")
                }
            }
        }
    }
}

@Composable
private fun SaleProductRow(
    product: BarProduct,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
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
                Text(
                    "${formatAriary(product.salePrice)} · ${product.category.label()} · stock ${product.stockQty}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove, enabled = quantity > 0) {
                Icon(Icons.Filled.Remove, contentDescription = "Retirer")
            }
            Text(
                quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(24.dp)
            )
            IconButton(onClick = onAdd, enabled = quantity < product.stockQty) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter")
            }
        }
    }
}
