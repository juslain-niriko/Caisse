package mg.caisse.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `bar_profile` (" +
                "`id` INTEGER NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`location` TEXT, " +
                "`phone` TEXT, " +
                "`note` TEXT, " +
                "`createdAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `bar_products` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`category` TEXT NOT NULL, " +
                "`unit` TEXT NOT NULL, " +
                "`sizeCl` INTEGER, " +
                "`unitsPerPack` INTEGER, " +
                "`packLabel` TEXT, " +
                "`salePrice` INTEGER NOT NULL, " +
                "`costPrice` INTEGER, " +
                "`stockQty` INTEGER NOT NULL, " +
                "`alertQty` INTEGER NOT NULL, " +
                "`active` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bar_products_category` ON `bar_products` (`category`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `bar_stock_moves` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`productId` INTEGER NOT NULL, " +
                "`type` TEXT NOT NULL, " +
                "`quantity` INTEGER NOT NULL, " +
                "`totalCost` INTEGER, " +
                "`supplier` TEXT, " +
                "`dateEpochDay` INTEGER NOT NULL, " +
                "`saleId` INTEGER, " +
                "`note` TEXT, " +
                "`createdAt` INTEGER NOT NULL, " +
                "FOREIGN KEY(`productId`) REFERENCES `bar_products`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bar_stock_moves_productId` ON `bar_stock_moves` (`productId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bar_stock_moves_dateEpochDay` ON `bar_stock_moves` (`dateEpochDay`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bar_stock_moves_saleId` ON `bar_stock_moves` (`saleId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `bar_sales` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`dateEpochDay` INTEGER NOT NULL, " +
                "`total` INTEGER NOT NULL, " +
                "`paid` INTEGER NOT NULL, " +
                "`paymentMethod` TEXT NOT NULL, " +
                "`note` TEXT, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bar_sales_dateEpochDay` ON `bar_sales` (`dateEpochDay`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `bar_sale_lines` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`saleId` INTEGER NOT NULL, " +
                "`productId` INTEGER NOT NULL, " +
                "`quantity` INTEGER NOT NULL, " +
                "`unitPrice` INTEGER NOT NULL, " +
                "`lineTotal` INTEGER NOT NULL, " +
                "FOREIGN KEY(`saleId`) REFERENCES `bar_sales`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`productId`) REFERENCES `bar_products`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bar_sale_lines_saleId` ON `bar_sale_lines` (`saleId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bar_sale_lines_productId` ON `bar_sale_lines` (`productId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `bar_expenses` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`dateEpochDay` INTEGER NOT NULL, " +
                "`category` TEXT NOT NULL, " +
                "`amount` INTEGER NOT NULL, " +
                "`supplier` TEXT, " +
                "`note` TEXT, " +
                "`receiptUri` TEXT, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bar_expenses_dateEpochDay` ON `bar_expenses` (`dateEpochDay`)")
    }
}
