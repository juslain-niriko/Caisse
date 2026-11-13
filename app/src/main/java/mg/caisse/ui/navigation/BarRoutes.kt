package mg.caisse.ui.navigation

object BarRoutes {
    const val HOME = "bar/home"
    const val SALE = "bar/sale"
    const val STOCK = "bar/stock"
    const val EXPENSES = "bar/expenses"
    const val REPORTS = "bar/reports"
    const val SETTINGS = "bar/settings"

    const val PRODUCT_FORM = "bar/product/{productId}"
    const val STOCK_ENTRY = "bar/stockEntry/{productId}"
    const val EXPENSE_FORM = "bar/expense/{expenseId}"

    fun productForm(id: Long = 0L) = "bar/product/$id"

    fun stockEntry(id: Long) = "bar/stockEntry/$id"

    fun expenseForm(id: Long = 0L) = "bar/expense/$id"
}
