package mg.caisse.data.local.entity.bar

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BarExpenseCategory { RENT, SALARY, ELECTRICITY, ICE, TRANSPORT, MAINTENANCE, OTHER }

@Entity(tableName = "bar_expenses", indices = [Index("dateEpochDay")])
data class BarExpense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val category: BarExpenseCategory,
    val amount: Long,
    val supplier: String? = null,
    val note: String? = null,
    val receiptUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
