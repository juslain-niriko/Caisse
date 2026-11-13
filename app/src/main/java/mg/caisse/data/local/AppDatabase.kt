package mg.caisse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mg.caisse.data.local.dao.CategoryDao
import mg.caisse.data.local.dao.StudentDao
import mg.caisse.data.local.dao.TransactionDao
import mg.caisse.data.local.dao.bar.BarExpenseDao
import mg.caisse.data.local.dao.bar.BarProductDao
import mg.caisse.data.local.dao.bar.BarProfileDao
import mg.caisse.data.local.dao.bar.BarSaleDao
import mg.caisse.data.local.dao.bar.BarStockDao
import mg.caisse.data.local.entity.Category
import mg.caisse.data.local.entity.Student
import mg.caisse.data.local.entity.TransactionEntity
import mg.caisse.data.local.entity.bar.BarExpense
import mg.caisse.data.local.entity.bar.BarProduct
import mg.caisse.data.local.entity.bar.BarProfile
import mg.caisse.data.local.entity.bar.BarSale
import mg.caisse.data.local.entity.bar.BarSaleLine
import mg.caisse.data.local.entity.bar.BarStockMove

@Database(
    entities = [
        Student::class,
        Category::class,
        TransactionEntity::class,
        BarProfile::class,
        BarProduct::class,
        BarStockMove::class,
        BarSale::class,
        BarSaleLine::class,
        BarExpense::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    abstract fun barProfileDao(): BarProfileDao
    abstract fun barProductDao(): BarProductDao
    abstract fun barStockDao(): BarStockDao
    abstract fun barSaleDao(): BarSaleDao
    abstract fun barExpenseDao(): BarExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "caisse.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
