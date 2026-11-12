package mg.caisse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mg.caisse.data.local.entity.Category
import mg.caisse.data.local.entity.CategoryKind

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Query("SELECT * FROM categories ORDER BY kind ASC, name ASC")
    fun observeAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE kind = :kind ORDER BY name ASC")
    fun observeByKind(kind: CategoryKind): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE kind = :kind ORDER BY name ASC")
    suspend fun byKind(kind: CategoryKind): List<Category>

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun byName(name: String): Category?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
