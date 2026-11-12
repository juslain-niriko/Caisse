package mg.caisse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mg.caisse.data.local.entity.Student

@Dao
interface StudentDao {
    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun observeAll(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE active = 1 ORDER BY fullName ASC")
    fun observeActive(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun byId(id: Long): Student?

    @Query("SELECT * FROM students WHERE id = :id")
    fun observeById(id: Long): Flow<Student?>
}
