package mg.caisse.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import mg.caisse.data.local.AppDatabase
import mg.caisse.data.local.BarSeed
import mg.caisse.data.local.dao.bar.BarProductTotal
import mg.caisse.data.local.entity.bar.BarExpense
import mg.caisse.data.local.entity.bar.BarMoveType
import mg.caisse.data.local.entity.bar.BarPaymentMethod
import mg.caisse.data.local.entity.bar.BarProduct
import mg.caisse.data.local.entity.bar.BarProfile
import mg.caisse.data.local.entity.bar.BarSale
import mg.caisse.data.local.entity.bar.BarSaleLine
import mg.caisse.data.local.entity.bar.BarStockMove
import mg.caisse.util.monthLabel
import mg.caisse.util.monthRange
import java.time.YearMonth

data class SaleLineInput(val productId: Long, val quantity: Int)

data class BarReportData(
    val periodLabel: String,
    val revenue: Long,
    val purchases: Long,
    val expenses: Long,
    val balance: Long,
    val stockValue: Long,
    val topProducts: List<BarProductTotal>,
    val lossQuantity: Long
)

class BarRepository(private val db: AppDatabase) {

    private val profileDao = db.barProfileDao()
    private val productDao = db.barProductDao()
    private val stockDao = db.barStockDao()
    private val saleDao = db.barSaleDao()
    private val expenseDao = db.barExpenseDao()

    // ---- Profil ----
    fun observeProfile(): Flow<BarProfile?> = profileDao.observe()
    suspend fun saveProfile(profile: BarProfile) = profileDao.save(profile)

    // ---- Produits ----
    fun observeProducts(): Flow<List<BarProduct>> = productDao.observeActive()
    fun observeAllProducts(): Flow<List<BarProduct>> = productDao.observeAll()
    fun observeLowStock(): Flow<List<BarProduct>> = productDao.observeLowStock()
    fun observeStockValue(): Flow<Long> = productDao.observeStockValue()
    suspend fun productById(id: Long): BarProduct? = productDao.byId(id)
    suspend fun addProduct(product: BarProduct): Long = productDao.insert(product)
    suspend fun updateProduct(product: BarProduct) = productDao.update(product)

    suspend fun removeProduct(product: BarProduct) {
        if (productDao.movementCount(product.id) > 0) {
            productDao.update(product.copy(active = false))
        } else {
            productDao.delete(product)
        }
    }

    // ---- Stock ----
    fun observeMovesForProduct(productId: Long): Flow<List<BarStockMove>> =
        stockDao.observeForProduct(productId)

    fun observeRecentMoves(limit: Int): Flow<List<BarStockMove>> =
        stockDao.observeRecent(limit)

    fun observePurchases(from: Long, to: Long): Flow<Long> = stockDao.observePurchases(from, to)
    fun observeLossQuantity(from: Long, to: Long): Flow<Long> = stockDao.observeLossQuantity(from, to)

    suspend fun purchase(
        productId: Long,
        packCount: Int?,
        unitCount: Int,
        totalCost: Long?,
        supplier: String?,
        dateEpochDay: Long,
        note: String?
    ) {
        db.withTransaction {
            val product = productDao.byId(productId) ?: return@withTransaction
            val quantity = if (packCount != null && packCount > 0 && product.unitsPerPack != null) {
                packCount * product.unitsPerPack
            } else {
                unitCount
            }
            if (quantity <= 0) return@withTransaction
            stockDao.insert(
                BarStockMove(
                    productId = productId,
                    type = BarMoveType.PURCHASE,
                    quantity = quantity,
                    totalCost = totalCost,
                    supplier = supplier,
                    dateEpochDay = dateEpochDay,
                    note = note
                )
            )
            productDao.update(product.copy(stockQty = product.stockQty + quantity))
        }
    }

    suspend fun adjustStock(productId: Long, countedQty: Int, dateEpochDay: Long, note: String?) {
        db.withTransaction {
            val product = productDao.byId(productId) ?: return@withTransaction
            val delta = countedQty - product.stockQty
            if (delta == 0) return@withTransaction
            stockDao.insert(
                BarStockMove(
                    productId = productId,
                    type = BarMoveType.ADJUSTMENT,
                    quantity = delta,
                    dateEpochDay = dateEpochDay,
                    note = note
                )
            )
            productDao.update(product.copy(stockQty = countedQty))
        }
    }

    suspend fun recordLoss(
        productId: Long,
        quantity: Int,
        type: BarMoveType,
        dateEpochDay: Long,
        note: String?
    ) {
        if (quantity <= 0) return
        db.withTransaction {
            val product = productDao.byId(productId) ?: return@withTransaction
            if (quantity > product.stockQty) return@withTransaction
            stockDao.insert(
                BarStockMove(
                    productId = productId,
                    type = type,
                    quantity = -quantity,
                    dateEpochDay = dateEpochDay,
                    note = note
                )
            )
            productDao.update(product.copy(stockQty = product.stockQty - quantity))
        }
    }

    // ---- Ventes ----
    fun observeSales(): Flow<List<BarSale>> = saleDao.observeAll()
    fun observeSales(from: Long, to: Long): Flow<List<BarSale>> = saleDao.observeRange(from, to)
    fun observeSalesTotal(from: Long, to: Long): Flow<Long> = saleDao.observeSumTotal(from, to)
    fun observeTopProducts(from: Long, to: Long, limit: Int = 5): Flow<List<BarProductTotal>> =
        saleDao.observeTopProducts(from, to, limit)

    suspend fun createSale(
        lines: List<SaleLineInput>,
        dateEpochDay: Long,
        note: String? = null
    ): Long {
        require(lines.isNotEmpty()) { "La vente est vide" }
        return db.withTransaction {
            val resolved = lines.map { input ->
                val product = productDao.byId(input.productId)
                    ?: throw IllegalArgumentException("Produit introuvable")
                if (input.quantity <= 0) throw IllegalArgumentException("Quantité invalide")
                if (product.stockQty < input.quantity) {
                    throw IllegalArgumentException("Stock insuffisant pour ${product.name}")
                }
                Triple(product, input.quantity, product.salePrice * input.quantity)
            }
            val total = resolved.sumOf { it.third }
            val saleId = saleDao.insertSale(
                BarSale(
                    dateEpochDay = dateEpochDay,
                    total = total,
                    paid = total,
                    paymentMethod = BarPaymentMethod.CASH,
                    note = note
                )
            )
            resolved.forEach { (product, quantity, lineTotal) ->
                saleDao.insertLine(
                    BarSaleLine(
                        saleId = saleId,
                        productId = product.id,
                        quantity = quantity,
                        unitPrice = product.salePrice,
                        lineTotal = lineTotal
                    )
                )
                stockDao.insert(
                    BarStockMove(
                        productId = product.id,
                        type = BarMoveType.SALE,
                        quantity = -quantity,
                        dateEpochDay = dateEpochDay,
                        saleId = saleId
                    )
                )
                productDao.update(product.copy(stockQty = product.stockQty - quantity))
            }
            saleId
        }
    }

    // ---- Dépenses ----
    fun observeExpenses(): Flow<List<BarExpense>> = expenseDao.observeAll()
    fun observeExpenses(from: Long, to: Long): Flow<List<BarExpense>> = expenseDao.observeRange(from, to)
    fun observeExpensesTotal(from: Long, to: Long): Flow<Long> = expenseDao.observeSum(from, to)

    suspend fun expenseById(id: Long): BarExpense? = expenseDao.byId(id)
    suspend fun addExpense(expense: BarExpense): Long = expenseDao.insert(expense)
    suspend fun updateExpense(expense: BarExpense) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: BarExpense) = expenseDao.delete(expense)

    // ---- Rapport ----
    suspend fun buildReport(yearMonth: YearMonth): BarReportData {
        val (from, to) = monthRange(yearMonth)
        val revenue = saleDao.sumTotal(from, to)
        val purchases = stockDao.sumPurchases(from, to)
        val expenses = expenseDao.sum(from, to)
        return BarReportData(
            periodLabel = monthLabel(yearMonth),
            revenue = revenue,
            purchases = purchases,
            expenses = expenses,
            balance = revenue - purchases - expenses,
            stockValue = productDao.observeStockValue().first(),
            topProducts = saleDao.topProducts(from, to, 10),
            lossQuantity = stockDao.sumLossQuantity(from, to)
        )
    }

    suspend fun ensureSeed() = BarSeed.ensureSeeded(profileDao, productDao)
}
