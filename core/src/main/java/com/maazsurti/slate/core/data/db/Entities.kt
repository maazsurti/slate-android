package com.maazsurti.slate.core.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val priceCents: Long,
    val stock: Int,
    val lowStockThreshold: Int,
    val category: String,
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long,
    val payment: String,
    val totalCents: Long,
    val customerId: Long?,
)

@Entity(
    tableName = "sale_lines",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("saleId")],
)
data class SaleLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val name: String,
    val unitPriceCents: Long,
    val quantity: Int,
)

@Entity(
    tableName = "ledger_entries",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("customerId")],
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val at: Long,
    val kind: String,
    val amountCents: Long,
    val note: String,
)

/** A sale with its lines, loaded in one transaction. */
data class SaleRow(
    @Embedded val sale: SaleEntity,
    @Relation(parentColumn = "id", entityColumn = "saleId")
    val lines: List<SaleLineEntity>,
)

/** Projection for the ledger screen: what each customer owes right now. */
data class CustomerBalanceRow(
    val customerId: Long,
    val name: String,
    val phone: String,
    val balanceCents: Long,
)
