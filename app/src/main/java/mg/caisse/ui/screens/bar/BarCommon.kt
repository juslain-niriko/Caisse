package mg.caisse.ui.screens.bar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.caisse.data.local.entity.bar.BarCategory
import mg.caisse.data.local.entity.bar.BarExpenseCategory
import mg.caisse.data.local.entity.bar.BarMoveType
import mg.caisse.data.local.entity.bar.BarPaymentMethod
import mg.caisse.data.local.entity.bar.BarUnit
import mg.caisse.util.dateLabel

private const val MILLIS_PER_DAY = 86_400_000L

fun BarCategory.label(): String = when (this) {
    BarCategory.BEER -> "Bière"
    BarCategory.SODA -> "Soda"
    BarCategory.WATER -> "Eau"
    BarCategory.ENERGY -> "Énergisant"
    BarCategory.ALCOHOL -> "Alcool"
    BarCategory.SNACK -> "Snack"
    BarCategory.OTHER -> "Autre"
}

fun BarUnit.label(): String = when (this) {
    BarUnit.BOTTLE -> "Bouteille"
    BarUnit.CAN -> "Canette"
    BarUnit.UNIT -> "Unité"
}

fun BarExpenseCategory.label(): String = when (this) {
    BarExpenseCategory.RENT -> "Loyer"
    BarExpenseCategory.SALARY -> "Salaire"
    BarExpenseCategory.ELECTRICITY -> "Électricité"
    BarExpenseCategory.ICE -> "Glace"
    BarExpenseCategory.TRANSPORT -> "Transport"
    BarExpenseCategory.MAINTENANCE -> "Entretien"
    BarExpenseCategory.OTHER -> "Autre"
}

fun BarMoveType.label(): String = when (this) {
    BarMoveType.PURCHASE -> "Achat"
    BarMoveType.SALE -> "Vente"
    BarMoveType.LOSS -> "Perte"
    BarMoveType.GIFT -> "Offert"
    BarMoveType.ADJUSTMENT -> "Ajustement"
}

@Suppress("unused")
fun BarPaymentMethod.label(): String = when (this) {
    BarPaymentMethod.CASH -> "Espèces"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarDateButton(
    epochDay: Long,
    onPick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { showPicker = true }, modifier = modifier.fillMaxWidth()) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(dateLabel(epochDay))
    }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = epochDay * MILLIS_PER_DAY)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onPick(Math.floorDiv(millis, MILLIS_PER_DAY))
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
