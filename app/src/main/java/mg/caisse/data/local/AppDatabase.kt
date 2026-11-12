package mg.caisse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mg.caisse.data.local.dao.CategoryDao
import mg.caisse.data.local.dao.StudentDao
import mg.caisse.data.local.dao.TransactionDao
import mg.caisse.data.local.entity.Category
import mg.caisse.data.local.entity.Student
import mg.caisse.data.local.entity.TransactionEntity

@Database(
    entities = [Student::class, Category::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "caisse.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
