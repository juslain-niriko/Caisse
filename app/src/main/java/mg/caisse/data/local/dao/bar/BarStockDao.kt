package mg.caisse.data.local.dao.bar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mg.caisse.data.local.entity.bar.BarStockMove

@Dao
interface BarStockDao {
    @Insert
    suspend fun insert(move: BarStockMove): Long

    @Query("SELECT * FROM bar_stock_moves WHERE productId = :productId ORDER BY dateEpochDay DESC, id DESC")
    fun observeForProduct(productId: Long): Flow<List<BarStockMove>>

    @Query("SELECT * FROM bar_stock_moves ORDER BY dateEpochDay DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<BarStockMove>>

    @Query("SELECT COALESCE(SUM(totalCost), 0) FROM bar_stock_moves WHERE type = 'PURCHASE' AND dateEpochDay BETWEEN :from AND :to")
    fun observePurchases(from: Long, to: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(totalCost), 0) FROM bar_stock_moves WHERE type = 'PURCHASE' AND dateEpochDay BETWEEN :from AND :to")
    suspend fun sumPurchases(from: Long, to: Long): Long

    @Query("SELECT COALESCE(SUM(-quantity), 0) FROM bar_stock_moves WHERE type IN ('LOSS', 'GIFT') AND dateEpochDay BETWEEN :from AND :to")
    fun observeLossQuantity(from: Long, to: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(-quantity), 0) FROM bar_stock_moves WHERE type IN ('LOSS', 'GIFT') AND dateEpochDay BETWEEN :from AND :to")
    suspend fun sumLossQuantity(from: Long, to: Long): Long
}
