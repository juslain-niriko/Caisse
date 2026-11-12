package mg.caisse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mg.caisse.data.local.entity.TransactionEntity
import mg.caisse.data.local.entity.TxType

data class NameTotal(val name: String, val total: Long)

data class TuitionRow(
    val studentId: Long,
    val name: String,
    val level: String?,
    val paid: Long
)

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun byId(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateEpochDay DESC, id DESC")
    fun observeByType(type: TxType): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'IN' THEN amount ELSE -amount END), 0) FROM transactions")
    fun observeBalance(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND dateEpochDay BETWEEN :from AND :to")
    fun observeSumByType(type: TxType, from: Long, to: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND dateEpochDay BETWEEN :from AND :to")
    suspend fun sumByType(type: TxType, from: Long, to: Long): Long

    @Query(
        """
        SELECT c.name AS name, COALESCE(SUM(t.amount), 0) AS total
        FROM transactions t JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'OUT' AND t.dateEpochDay BETWEEN :from AND :to
        GROUP BY t.categoryId ORDER BY total DESC
        """
    )
    fun observeOutByCategory(from: Long, to: Long): Flow<List<NameTotal>>

    @Query(
        """
        SELECT c.name AS name, COALESCE(SUM(t.amount), 0) AS total
        FROM transactions t JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'OUT' AND t.dateEpochDay BETWEEN :from AND :to
        GROUP BY t.categoryId ORDER BY total DESC
        """
    )
    suspend fun outByCategory(from: Long, to: Long): List<NameTotal>

    @Query(
        """
        SELECT s.fullName AS name, COALESCE(SUM(t.amount), 0) AS total
        FROM transactions t JOIN students s ON s.id = t.studentId
        WHERE t.type = 'OUT' AND t.studentId IS NOT NULL AND t.dateEpochDay BETWEEN :from AND :to
        GROUP BY t.studentId ORDER BY total DESC
        """
    )
    suspend fun outByStudent(from: Long, to: Long): List<NameTotal>

    @Query(
        """
        SELECT s.fullName AS name, COALESCE(SUM(t.amount), 0) AS total
        FROM transactions t JOIN students s ON s.id = t.studentId
        WHERE t.type = 'OUT' AND t.studentId IS NOT NULL AND t.dateEpochDay BETWEEN :from AND :to
        GROUP BY t.studentId ORDER BY total DESC
        """
    )
    fun observeOutByStudent(from: Long, to: Long): Flow<List<NameTotal>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'OUT' AND studentId IS NOT NULL AND dateEpochDay BETWEEN :from AND :to")
    fun observeTuitionTotal(from: Long, to: Long): Flow<Long>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY dateEpochDay DESC, id DESC")
    fun observeByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT t.* FROM transactions t JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'OUT' AND c.name NOT IN ('Écolage', 'Investissement')
        ORDER BY t.dateEpochDay DESC, t.id DESC
        """
    )
    fun observeActivities(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT t.* FROM transactions t JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'OUT' AND c.name = 'Investissement'
        ORDER BY t.dateEpochDay DESC, t.id DESC
        """
    )
    fun observeInvestments(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE studentId = :studentId ORDER BY dateEpochDay DESC, id DESC")
    fun observeForStudent(studentId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE studentId = :studentId AND type = 'OUT'")
    fun observeTotalForStudent(studentId: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE studentId = :studentId AND type = 'OUT' AND dateEpochDay BETWEEN :from AND :to")
    fun observeMonthForStudent(studentId: Long, from: Long, to: Long): Flow<Long>

    @Query(
        """
        SELECT s.id AS studentId, s.fullName AS name, s.level AS level,
          COALESCE((SELECT SUM(t.amount) FROM transactions t
                    WHERE t.studentId = s.id AND t.type = 'OUT'
                    AND t.dateEpochDay BETWEEN :from AND :to), 0) AS paid
        FROM students s WHERE s.active = 1
        ORDER BY s.fullName ASC
        """
    )
    fun observeTuitionMonth(from: Long, to: Long): Flow<List<TuitionRow>>
}
