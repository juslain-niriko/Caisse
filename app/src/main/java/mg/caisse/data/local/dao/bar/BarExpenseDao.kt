package mg.caisse.data.local.dao.bar

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mg.caisse.data.local.entity.bar.BarExpense

@Dao
interface BarExpenseDao {
    @Insert
    suspend fun insert(expense: BarExpense): Long

    @Update
    suspend fun update(expense: BarExpense)

    @Delete
    suspend fun delete(expense: BarExpense)

    @Query("SELECT * FROM bar_expenses ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<BarExpense>>

    @Query("SELECT * FROM bar_expenses WHERE dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay DESC, id DESC")
    fun observeRange(from: Long, to: Long): Flow<List<BarExpense>>

    @Query("SELECT * FROM bar_expenses WHERE id = :id")
    suspend fun byId(id: Long): BarExpense?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM bar_expenses WHERE dateEpochDay BETWEEN :from AND :to")
    fun observeSum(from: Long, to: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM bar_expenses WHERE dateEpochDay BETWEEN :from AND :to")
    suspend fun sum(from: Long, to: Long): Long
}
