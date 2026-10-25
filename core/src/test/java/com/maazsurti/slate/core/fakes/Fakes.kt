package com.maazsurti.slate.core.fakes

import com.maazsurti.slate.core.domain.model.Customer
import com.maazsurti.slate.core.domain.model.CustomerBalance
import com.maazsurti.slate.core.domain.model.LedgerEntry
import com.maazsurti.slate.core.domain.model.LedgerKind
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.model.Sale
import com.maazsurti.slate.core.domain.repo.CustomerRepository
import com.maazsurti.slate.core.domain.repo.LedgerRepository
import com.maazsurti.slate.core.domain.repo.ProductRepository
import com.maazsurti.slate.core.domain.repo.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeProductRepository(initial: List<Product> = emptyList()) : ProductRepository {
    private val items = initial.associateBy { it.id }.toMutableMap()
    private val state = MutableStateFlow(items.values.toList())

    override fun observeAll(): Flow<List<Product>> = state

    override suspend fun all(): List<Product> = items.values.toList()

    override suspend fun get(id: Long): Product? = items[id]

    override suspend fun save(product: Product): Long {
        val id = if (product.id == 0L) (items.keys.maxOrNull() ?: 0L) + 1 else product.id
        items[id] = product.copy(id = id)
        state.value = items.values.toList()
        return id
    }

    override suspend fun delete(id: Long) {
        items.remove(id)
        state.value = items.values.toList()
    }
}

class FakeSaleRepository : SaleRepository {
    val recorded = mutableListOf<Sale>()

    override suspend fun record(sale: Sale): Long {
        recorded += sale
        return recorded.size.toLong()
    }

    override fun observeSince(sinceMillis: Long): Flow<List<Sale>> = MutableStateFlow(recorded.toList())

    override fun observeRecent(limit: Int): Flow<List<Sale>> = MutableStateFlow(recorded.toList())
}

class FakeCustomerRepository(initial: List<Customer> = emptyList()) : CustomerRepository {
    private val items = initial.associateBy { it.id }.toMutableMap()
    private val state = MutableStateFlow(items.values.toList())

    override fun observeAll(): Flow<List<Customer>> = state

    override suspend fun get(id: Long): Customer? = items[id]

    override suspend fun save(customer: Customer): Long {
        val id = if (customer.id == 0L) (items.keys.maxOrNull() ?: 0L) + 1 else customer.id
        items[id] = customer.copy(id = id)
        state.value = items.values.toList()
        return id
    }
}

class FakeLedgerRepository : LedgerRepository {
    val entries = mutableListOf<LedgerEntry>()
    private val state = MutableStateFlow<List<LedgerEntry>>(emptyList())

    override fun observeBalances(): Flow<List<CustomerBalance>> =
        state.map { rows ->
            rows.groupBy { it.customerId }.map { (customerId, customerEntries) ->
                CustomerBalance(
                    customerId = customerId,
                    name = "Customer $customerId",
                    balance = customerEntries.fold(Money.Zero) { sum, entry ->
                        if (entry.kind == LedgerKind.CHARGE) sum + entry.amount else sum - entry.amount
                    },
                )
            }
        }

    override fun observeEntries(customerId: Long): Flow<List<LedgerEntry>> =
        state.map { rows -> rows.filter { it.customerId == customerId } }

    override suspend fun record(entry: LedgerEntry): Long {
        entries += entry
        state.value = entries.toList()
        return entries.size.toLong()
    }
}
