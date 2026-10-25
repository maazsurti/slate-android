package com.maazsurti.slate.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE ASC")
    suspend fun all(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun get(id: Long): ProductEntity?

    @Upsert
    suspend fun upsert(product: ProductEntity): Long

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :id")
    suspend fun decrementStock(id: Long, quantity: Int)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun get(id: Long): CustomerEntity?

    @Upsert
    suspend fun upsert(customer: CustomerEntity): Long
}

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert
    suspend fun insertLines(lines: List<SaleLineEntity>)

    @Transaction
    @Query("SELECT * FROM sales WHERE at >= :sinceMillis ORDER BY at DESC")
    fun observeSince(sinceMillis: Long): Flow<List<SaleRow>>

    @Transaction
    @Query("SELECT * FROM sales ORDER BY at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SaleRow>>
}

@Dao
interface LedgerDao {
    @Insert
    suspend fun insert(entry: LedgerEntryEntity): Long

    @Query("SELECT * FROM ledger_entries WHERE customerId = :customerId ORDER BY at DESC")
    fun observeEntries(customerId: Long): Flow<List<LedgerEntryEntity>>

    @Query(
        """
        SELECT c.id AS customerId, c.name AS name, c.phone AS phone,
               COALESCE(SUM(CASE WHEN e.kind = 'CHARGE' THEN e.amountCents ELSE -e.amountCents END), 0) AS balanceCents
        FROM customers c
        LEFT JOIN ledger_entries e ON e.customerId = c.id
        GROUP BY c.id, c.name, c.phone
        ORDER BY balanceCents DESC, c.name COLLATE NOCASE ASC
        """
    )
    fun observeBalances(): Flow<List<CustomerBalanceRow>>
}
