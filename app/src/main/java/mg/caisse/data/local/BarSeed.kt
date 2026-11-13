package mg.caisse.data.local

import mg.caisse.data.local.dao.bar.BarProductDao
import mg.caisse.data.local.dao.bar.BarProfileDao
import mg.caisse.data.local.entity.bar.BarCategory
import mg.caisse.data.local.entity.bar.BarProduct
import mg.caisse.data.local.entity.bar.BarProfile
import mg.caisse.data.local.entity.bar.BarUnit

object BarSeed {

    private val profile = BarProfile(name = "Mon bar")

    private val products = listOf(
        BarProduct(
            name = "THB 65 cl",
            category = BarCategory.BEER,
            unit = BarUnit.BOTTLE,
            sizeCl = 65,
            unitsPerPack = 20,
            packLabel = "casier",
            salePrice = 3500,
            alertQty = 12
        ),
        BarProduct(
            name = "THB 33 cl",
            category = BarCategory.BEER,
            unit = BarUnit.BOTTLE,
            sizeCl = 33,
            unitsPerPack = 24,
            packLabel = "casier",
            salePrice = 2500,
            alertQty = 12
        ),
        BarProduct(
            name = "THB Fresh 65 cl",
            category = BarCategory.BEER,
            unit = BarUnit.BOTTLE,
            sizeCl = 65,
            unitsPerPack = 20,
            packLabel = "casier",
            salePrice = 3500,
            alertQty = 12
        ),
        BarProduct(
            name = "Queen's 65 cl",
            category = BarCategory.BEER,
            unit = BarUnit.BOTTLE,
            sizeCl = 65,
            unitsPerPack = 20,
            packLabel = "casier",
            salePrice = 3000,
            alertQty = 12
        ),
        BarProduct(
            name = "Coca 33 cl",
            category = BarCategory.SODA,
            unit = BarUnit.BOTTLE,
            sizeCl = 33,
            unitsPerPack = 24,
            packLabel = "casier",
            salePrice = 3000,
            alertQty = 12
        ),
        BarProduct(
            name = "Eau Vive 1,5 L",
            category = BarCategory.WATER,
            unit = BarUnit.BOTTLE,
            sizeCl = 150,
            unitsPerPack = 6,
            packLabel = "pack",
            salePrice = 3000,
            alertQty = 6
        ),
        BarProduct(
            name = "Boisson énergisante 33 cl",
            category = BarCategory.ENERGY,
            unit = BarUnit.CAN,
            sizeCl = 33,
            unitsPerPack = 24,
            packLabel = "casier",
            salePrice = 5000,
            alertQty = 6
        )
    )

    suspend fun ensureSeeded(profileDao: BarProfileDao, productDao: BarProductDao) {
        if (profileDao.get() == null) {
            profileDao.save(profile)
        }
        if (productDao.count() == 0) {
            products.forEach { productDao.insert(it) }
        }
    }
}
