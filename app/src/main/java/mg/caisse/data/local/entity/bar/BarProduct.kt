package mg.caisse.data.local.entity.bar

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BarCategory { BEER, SODA, WATER, ENERGY, ALCOHOL, SNACK, OTHER }

enum class BarUnit { BOTTLE, CAN, UNIT }

@Entity(tableName = "bar_products", indices = [Index("category")])
data class BarProduct(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: BarCategory,
    val unit: BarUnit = BarUnit.BOTTLE,
    val sizeCl: Int? = null,
    val unitsPerPack: Int? = null,
    val packLabel: String? = null,
    val salePrice: Long,
    val costPrice: Long? = null,
    val stockQty: Int = 0,
    val alertQty: Int = 0,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
