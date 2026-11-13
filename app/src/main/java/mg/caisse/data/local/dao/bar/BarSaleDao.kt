package mg.caisse.data.local.dao.bar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mg.caisse.data.local.entity.bar.BarSale
import mg.caisse.data.local.entity.bar.BarSaleLine

data class BarProductTotal(
    val name: String,
    val quantity: Long,
    val total: Long
)

@Dao
interface BarSaleDao {
    @Insert
    suspend fun insertSale(sale: BarSale): Long

    @Insert
    suspend fun insertLine(line: BarSaleLine): Long

    @Query("SELECT * FROM bar_sales ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<BarSale>>

    @Query("SELECT * FROM bar_sales WHERE dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay DESC, id DESC")
    fun observeRange(from: Long, to: Long): Flow<List<BarSale>>

    @Query("SELECT COALESCE(SUM(total), 0) FROM bar_sales WHERE dateEpochDay BETWEEN :from AND :to")
    fun observeSumTotal(from: Long, to: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(total), 0) FROM bar_sales WHERE dateEpochDay BETWEEN :from AND :to")
    suspend fun sumTotal(from: Long, to: Long): Long

    @Query(
        """
        SELECT p.name AS name, SUM(l.quantity) AS quantity, SUM(l.lineTotal) AS total
        FROM bar_sale_lines l
        JOIN bar_sales s ON s.id = l.saleId
        JOIN bar_products p ON p.id = l.productId
        WHERE s.dateEpochDay BETWEEN :from AND :to
        GROUP BY l.productId
        ORDER BY total DESC
        LIMIT :limit
        """
    )
    fun observeTopProducts(from: Long, to: Long, limit: Int): Flow<List<BarProductTotal>>

    @Query(
        """
        SELECT p.name AS name, SUM(l.quantity) AS quantity, SUM(l.lineTotal) AS total
        FROM bar_sale_lines l
        JOIN bar_sales s ON s.id = l.saleId
        JOIN bar_products p ON p.id = l.productId
        WHERE s.dateEpochDay BETWEEN :from AND :to
        GROUP BY l.productId
        ORDER BY total DESC
        LIMIT :limit
        """
    )
    suspend fun topProducts(from: Long, to: Long, limit: Int): List<BarProductTotal>
}
