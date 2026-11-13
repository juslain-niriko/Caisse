package mg.caisse.data.local.entity.bar

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bar_profile")
data class BarProfile(
    @PrimaryKey val id: Long = 1,
    val name: String,
    val location: String? = null,
    val phone: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
