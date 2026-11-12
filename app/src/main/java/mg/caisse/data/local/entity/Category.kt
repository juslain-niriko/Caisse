package mg.caisse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CategoryKind { INCOME, EXPENSE }

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val iconKey: String,
    val kind: CategoryKind,
    val builtIn: Boolean = false
)
