package mg.caisse.data.local.dao.bar

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mg.caisse.data.local.entity.bar.BarProduct

@Dao
interface BarProductDao {
    @Insert
    suspend fun insert(product: BarProduct): Long

    @Update
    suspend fun update(product: BarProduct)

    @Delete
    suspend fun delete(product: BarProduct)

    @Query("SELECT * FROM bar_products WHERE id = :id")
    suspend fun byId(id: Long): BarProduct?

    @Query("SELECT * FROM bar_products WHERE active = 1 ORDER BY category ASC, name ASC")
    fun observeActive(): Flow<List<BarProduct>>

    @Query("SELECT * FROM bar_products ORDER BY active DESC, category ASC, name ASC")
    fun observeAll(): Flow<List<BarProduct>>

    @Query("SELECT * FROM bar_products WHERE active = 1 AND stockQty <= alertQty ORDER BY stockQty ASC, name ASC")
    fun observeLowStock(): Flow<List<BarProduct>>

    @Query("SELECT COALESCE(SUM(stockQty * COALESCE(costPrice, 0)), 0) FROM bar_products WHERE active = 1")
    fun observeStockValue(): Flow<Long>

    @Query("SELECT COUNT(*) FROM bar_products")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM bar_stock_moves WHERE productId = :productId")
    suspend fun movementCount(productId: Long): Int
}
