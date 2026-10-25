package com.maazsurti.slate.core.domain.repo

import com.maazsurti.slate.core.domain.model.Customer
import com.maazsurti.slate.core.domain.model.CustomerBalance
import com.maazsurti.slate.core.domain.model.LedgerEntry
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAll(): Flow<List<Product>>
    suspend fun all(): List<Product>
    suspend fun get(id: Long): Product?
    suspend fun save(product: Product): Long
    suspend fun delete(id: Long)
}

interface CustomerRepository {
    fun observeAll(): Flow<List<Customer>>
    suspend fun get(id: Long): Customer?
    suspend fun save(customer: Customer): Long
}

interface SaleRepository {
    /**
     * Writes the sale, its lines and the stock decrement in one transaction, and posts a
     * ledger charge when the sale is on credit.
     */
    suspend fun record(sale: Sale): Long
    fun observeSince(sinceMillis: Long): Flow<List<Sale>>
    fun observeRecent(limit: Int): Flow<List<Sale>>
}

interface LedgerRepository {
    fun observeBalances(): Flow<List<CustomerBalance>>
    fun observeEntries(customerId: Long): Flow<List<LedgerEntry>>
    suspend fun record(entry: LedgerEntry): Long
}
