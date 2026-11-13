package mg.caisse.data.local.entity.bar

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BarPaymentMethod { CASH }

@Entity(tableName = "bar_sales", indices = [Index("dateEpochDay")])
data class BarSale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val total: Long,
    val paid: Long,
    val paymentMethod: BarPaymentMethod = BarPaymentMethod.CASH,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
