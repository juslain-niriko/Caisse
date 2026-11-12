package mg.caisse.data.local

import mg.caisse.data.local.dao.CategoryDao
import mg.caisse.data.local.entity.Category
import mg.caisse.data.local.entity.CategoryKind

object Seed {

    val defaults: List<Category> = listOf(
        Category(name = "Financement", colorHex = "#0F766E", iconKey = "incoming", kind = CategoryKind.INCOME, builtIn = true),
        Category(name = "Écolage", colorHex = "#2563EB", iconKey = "school", kind = CategoryKind.EXPENSE, builtIn = true),
        Category(name = "Activité", colorHex = "#7C3AED", iconKey = "activity", kind = CategoryKind.EXPENSE, builtIn = true),
        Category(name = "Investissement", colorHex = "#16A34A", iconKey = "invest", kind = CategoryKind.EXPENSE, builtIn = true),
        Category(name = "Fournitures", colorHex = "#F59E0B", iconKey = "supplies", kind = CategoryKind.EXPENSE, builtIn = true),
        Category(name = "Transport", colorHex = "#0891B2", iconKey = "transport", kind = CategoryKind.EXPENSE, builtIn = true),
        Category(name = "Santé", colorHex = "#DC2626", iconKey = "health", kind = CategoryKind.EXPENSE, builtIn = true),
        Category(name = "Autre", colorHex = "#64748B", iconKey = "other", kind = CategoryKind.EXPENSE, builtIn = true)
    )

    suspend fun ensureSeeded(dao: CategoryDao) {
        if (dao.count() == 0) {
            defaults.forEach { dao.insert(it) }
        }
    }
}
