package mg.caisse.data.repository

import kotlinx.coroutines.flow.Flow
import mg.caisse.data.local.AppDatabase
import mg.caisse.data.local.Seed
import mg.caisse.data.local.dao.NameTotal
import mg.caisse.data.local.dao.TuitionRow
import mg.caisse.data.local.entity.Category
import mg.caisse.data.local.entity.Student
import mg.caisse.data.local.entity.TransactionEntity
import mg.caisse.data.local.entity.TxType
import mg.caisse.util.monthLabel
import mg.caisse.util.monthRange
import java.time.YearMonth

data class ReportData(
    val periodLabel: String,
    val totalIn: Long,
    val totalOut: Long,
    val balance: Long,
    val byCategory: List<NameTotal>,
    val byStudent: List<NameTotal>
)

class CaisseRepository(private val db: AppDatabase) {

    private val txDao = db.transactionDao()
    private val studentDao = db.studentDao()
    private val categoryDao = db.categoryDao()

    // ---- Lectures ----
    fun observeBalance(): Flow<Long> = txDao.observeBalance()
    fun observeAllTransactions(): Flow<List<TransactionEntity>> = txDao.observeAll()
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>> = txDao.observeRecent(limit)
    fun observeByType(type: TxType): Flow<List<TransactionEntity>> = txDao.observeByType(type)
    fun observeActivities(): Flow<List<TransactionEntity>> = txDao.observeActivities()
    fun observeInvestments(): Flow<List<TransactionEntity>> = txDao.observeInvestments()
    fun observeSumByType(type: TxType, from: Long, to: Long): Flow<Long> = txDao.observeSumByType(type, from, to)
    fun observeOutByCategory(from: Long, to: Long): Flow<List<NameTotal>> = txDao.observeOutByCategory(from, to)
    fun observeOutByStudent(from: Long, to: Long): Flow<List<NameTotal>> = txDao.observeOutByStudent(from, to)
    fun observeTuitionTotal(from: Long, to: Long): Flow<Long> = txDao.observeTuitionTotal(from, to)
    fun observeTuitionMonth(from: Long, to: Long): Flow<List<TuitionRow>> = txDao.observeTuitionMonth(from, to)
    fun observeForStudent(studentId: Long): Flow<List<TransactionEntity>> = txDao.observeForStudent(studentId)
    fun observeTotalForStudent(studentId: Long): Flow<Long> = txDao.observeTotalForStudent(studentId)

    fun observeStudents(): Flow<List<Student>> = studentDao.observeActive()
    fun observeAllStudents(): Flow<List<Student>> = studentDao.observeAll()
    fun observeStudent(id: Long): Flow<Student?> = studentDao.observeById(id)
    fun observeCategories(): Flow<List<Category>> = categoryDao.observeAll()

    suspend fun transactionById(id: Long): TransactionEntity? = txDao.byId(id)
    suspend fun studentById(id: Long): Student? = studentDao.byId(id)
    suspend fun categoryByName(name: String): Category? = categoryDao.byName(name)
    suspend fun expenseCategories(): List<Category> = categoryDao.byKind(mg.caisse.data.local.entity.CategoryKind.EXPENSE)

    // ---- Écritures ----
    suspend fun addTransaction(transaction: TransactionEntity): Long = txDao.insert(transaction)
    suspend fun updateTransaction(transaction: TransactionEntity) = txDao.update(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = txDao.delete(transaction)

    suspend fun addStudent(student: Student): Long = studentDao.insert(student)
    suspend fun updateStudent(student: Student) = studentDao.update(student)
    suspend fun deleteStudent(student: Student) = studentDao.delete(student)

    suspend fun ensureSeed() = Seed.ensureSeeded(categoryDao)

    // ---- Rapport ----
    suspend fun buildReport(yearMonth: YearMonth): ReportData {
        val (from, to) = monthRange(yearMonth)
        val totalIn = txDao.sumByType(TxType.IN, from, to)
        val totalOut = txDao.sumByType(TxType.OUT, from, to)
        return ReportData(
            periodLabel = monthLabel(yearMonth),
            totalIn = totalIn,
            totalOut = totalOut,
            balance = totalIn - totalOut,
            byCategory = txDao.outByCategory(from, to),
            byStudent = txDao.outByStudent(from, to)
        )
    }
}
