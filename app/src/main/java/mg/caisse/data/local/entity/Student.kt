package mg.caisse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val level: String? = null,
    val school: String? = null,
    val guardianContact: String? = null,
    val note: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
