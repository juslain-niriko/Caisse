package mg.caisse.data.local.dao.bar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mg.caisse.data.local.entity.bar.BarProfile

@Dao
interface BarProfileDao {
    @Query("SELECT * FROM bar_profile WHERE id = 1")
    fun observe(): Flow<BarProfile?>

    @Query("SELECT * FROM bar_profile WHERE id = 1")
    suspend fun get(): BarProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(profile: BarProfile)
}
