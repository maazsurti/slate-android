package com.maazsurti.slate.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        SaleEntity::class,
        SaleLineEntity::class,
        LedgerEntryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class SlateDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun ledgerDao(): LedgerDao

    companion object {
        fun build(context: Context): SlateDatabase =
            Room.databaseBuilder(context, SlateDatabase::class.java, "slate.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
