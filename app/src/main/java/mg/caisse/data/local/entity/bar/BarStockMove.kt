package mg.caisse.data.local.entity.bar

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BarMoveType { PURCHASE, SALE, LOSS, GIFT, ADJUSTMENT }

@Entity(
    tableName = "bar_stock_moves",
    foreignKeys = [
        ForeignKey(
            entity = BarProduct::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("productId"), Index("dateEpochDay"), Index("saleId")]
)
data class BarStockMove(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val type: BarMoveType,
    val quantity: Int,
    val totalCost: Long? = null,
    val supplier: String? = null,
    val dateEpochDay: Long,
    val saleId: Long? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
