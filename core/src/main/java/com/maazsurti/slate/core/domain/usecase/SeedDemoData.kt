package com.maazsurti.slate.core.domain.usecase

import com.maazsurti.slate.core.domain.model.Category
import com.maazsurti.slate.core.domain.model.Customer
import com.maazsurti.slate.core.domain.model.LedgerEntry
import com.maazsurti.slate.core.domain.model.LedgerKind
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.model.Sale
import com.maazsurti.slate.core.domain.model.SaleLine
import com.maazsurti.slate.core.domain.repo.CustomerRepository
import com.maazsurti.slate.core.domain.repo.LedgerRepository
import com.maazsurti.slate.core.domain.repo.ProductRepository
import com.maazsurti.slate.core.domain.repo.SaleRepository
import java.time.Instant
import java.time.ZoneId

/**
 * Fills an empty database with a week of shop activity so the app has something to show on
 * first launch. It never touches a database that already holds products.
 */
class SeedDemoData(
    private val products: ProductRepository,
    private val customers: CustomerRepository,
    private val sales: SaleRepository,
    private val ledger: LedgerRepository,
) {
    private data class SeedProduct(
        val name: String,
        val rupees: Long,
        val stock: Int,
        val category: Category,
        val low: Int = 5,
    )
    private data class SeedCustomer(val name: String, val phone: String)

    private val catalog = listOf(
        SeedProduct("Basmati Rice 5kg", 850, 42, Category.Grains),
        SeedProduct("Sunflower Oil 1L", 210, 60, Category.Oil),
        SeedProduct("Tea 250g", 175, 36, Category.Tea),
        SeedProduct("Milk 500ml", 45, 84, Category.Dairy),
        SeedProduct("Sugar 1kg", 60, 55, Category.Grains),
        SeedProduct("Washing Powder 1kg", 230, 26, Category.Cleaning),
        SeedProduct("Biscuits 200g", 40, 96, Category.Snacks),
        SeedProduct("Toothpaste 150g", 120, 31, Category.Care),
        SeedProduct("Soap Bar", 55, 88, Category.Care),
        SeedProduct("Spice Pack", 95, 3, Category.Spices),
    )

    private val people = listOf(
        SeedCustomer("Ramesh Patel", "+91 98250 11223"),
        SeedCustomer("Kiran Shah", "+91 99785 44120"),
        SeedCustomer("Meena Desai", "+91 90990 78312"),
        SeedCustomer("Arjun Mehta", "+91 98240 66501"),
    )

    suspend fun seedIfEmpty(now: Long) {
        if (products.all().isNotEmpty()) return

        val productIds = catalog.map {
            products.save(
                Product(
                    name = it.name,
                    price = Money.of(it.rupees),
                    stock = it.stock,
                    lowStockThreshold = it.low,
                    category = it.category,
                )
            )
        }
        val customerIds = people.map { customers.save(Customer(name = it.name, phone = it.phone)) }

        record(at(now, daysAgo = 6, hour = 10), PaymentType.CASH, null, productIds, listOf(0 to 1, 3 to 4))
        record(at(now, daysAgo = 5, hour = 17), PaymentType.CREDIT, customerIds[0], productIds, listOf(1 to 2, 4 to 1))
        record(at(now, daysAgo = 4, hour = 12), PaymentType.CASH, null, productIds, listOf(2 to 1, 6 to 3, 8 to 2))
        record(at(now, daysAgo = 3, hour = 19), PaymentType.CREDIT, customerIds[1], productIds, listOf(5 to 1, 3 to 2))
        record(at(now, daysAgo = 2, hour = 11), PaymentType.CASH, null, productIds, listOf(0 to 2, 9 to 1))
        record(at(now, daysAgo = 1, hour = 16), PaymentType.CASH, null, productIds, listOf(7 to 1, 8 to 3, 6 to 2))
        record(at(now, daysAgo = 1, hour = 20), PaymentType.CREDIT, customerIds[2], productIds, listOf(1 to 1, 2 to 2))
        record(at(now, daysAgo = 0, hour = 9), PaymentType.CASH, null, productIds, listOf(3 to 6, 6 to 4))
        record(at(now, daysAgo = 0, hour = 13), PaymentType.CREDIT, customerIds[0], productIds, listOf(4 to 3, 8 to 2))

        ledger.record(
            LedgerEntry(
                customerId = customerIds[0],
                at = at(now, daysAgo = 1, hour = 18),
                kind = LedgerKind.PAYMENT,
                amount = Money.of(200),
                note = "Payment received",
            )
        )
    }

    private suspend fun record(
        atMillis: Long,
        payment: PaymentType,
        customerId: Long?,
        productIds: List<Long>,
        items: List<Pair<Int, Int>>,
    ) {
        val lines = items.map { (index, quantity) ->
            val product = catalog[index]
            SaleLine(
                productId = productIds[index],
                name = product.name,
                unitPrice = Money.of(product.rupees),
                quantity = quantity,
            )
        }
        sales.record(
            Sale(
                at = atMillis,
                payment = payment,
                total = lines.fold(Money.Zero) { sum, line -> sum + line.total },
                customerId = customerId,
                lines = lines,
            )
        )
    }

    private fun at(now: Long, daysAgo: Int, hour: Int): Long {
        val zone = ZoneId.systemDefault()
        val day = Instant.ofEpochMilli(now).atZone(zone).toLocalDate().minusDays(daysAgo.toLong())
        return day.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
    }
}
