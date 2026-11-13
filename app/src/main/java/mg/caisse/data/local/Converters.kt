package mg.caisse.data.local

import androidx.room.TypeConverter
import mg.caisse.data.local.entity.CategoryKind
import mg.caisse.data.local.entity.TxType
import mg.caisse.data.local.entity.bar.BarCategory
import mg.caisse.data.local.entity.bar.BarExpenseCategory
import mg.caisse.data.local.entity.bar.BarMoveType
import mg.caisse.data.local.entity.bar.BarPaymentMethod
import mg.caisse.data.local.entity.bar.BarUnit

class Converters {
    @TypeConverter
    fun fromTxType(value: TxType): String = value.name

    @TypeConverter
    fun toTxType(value: String): TxType = TxType.valueOf(value)

    @TypeConverter
    fun fromCategoryKind(value: CategoryKind): String = value.name

    @TypeConverter
    fun toCategoryKind(value: String): CategoryKind = CategoryKind.valueOf(value)

    @TypeConverter
    fun fromBarCategory(value: BarCategory): String = value.name

    @TypeConverter
    fun toBarCategory(value: String): BarCategory = BarCategory.valueOf(value)

    @TypeConverter
    fun fromBarUnit(value: BarUnit): String = value.name

    @TypeConverter
    fun toBarUnit(value: String): BarUnit = BarUnit.valueOf(value)

    @TypeConverter
    fun fromBarMoveType(value: BarMoveType): String = value.name

    @TypeConverter
    fun toBarMoveType(value: String): BarMoveType = BarMoveType.valueOf(value)

    @TypeConverter
    fun fromBarPaymentMethod(value: BarPaymentMethod): String = value.name

    @TypeConverter
    fun toBarPaymentMethod(value: String): BarPaymentMethod = BarPaymentMethod.valueOf(value)

    @TypeConverter
    fun fromBarExpenseCategory(value: BarExpenseCategory): String = value.name

    @TypeConverter
    fun toBarExpenseCategory(value: String): BarExpenseCategory = BarExpenseCategory.valueOf(value)
}
