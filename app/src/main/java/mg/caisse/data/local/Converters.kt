package mg.caisse.data.local

import androidx.room.TypeConverter
import mg.caisse.data.local.entity.CategoryKind
import mg.caisse.data.local.entity.TxType

class Converters {
    @TypeConverter
    fun fromTxType(value: TxType): String = value.name

    @TypeConverter
    fun toTxType(value: String): TxType = TxType.valueOf(value)

    @TypeConverter
    fun fromCategoryKind(value: CategoryKind): String = value.name

    @TypeConverter
    fun toCategoryKind(value: String): CategoryKind = CategoryKind.valueOf(value)
}
