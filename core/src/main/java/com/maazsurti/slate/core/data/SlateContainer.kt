package com.maazsurti.slate.core.data

import android.content.Context
import com.maazsurti.slate.core.data.db.SlateDatabase
import com.maazsurti.slate.core.data.repo.RoomCustomerRepository
import com.maazsurti.slate.core.data.repo.RoomLedgerRepository
import com.maazsurti.slate.core.data.repo.RoomProductRepository
import com.maazsurti.slate.core.data.repo.RoomSaleRepository
import com.maazsurti.slate.core.domain.repo.CustomerRepository
import com.maazsurti.slate.core.domain.repo.LedgerRepository
import com.maazsurti.slate.core.domain.repo.ProductRepository
import com.maazsurti.slate.core.domain.repo.SaleRepository
import com.maazsurti.slate.core.domain.usecase.CompleteSale
import com.maazsurti.slate.core.domain.usecase.RecordPayment
import com.maazsurti.slate.core.domain.usecase.SaveProduct
import com.maazsurti.slate.core.domain.usecase.SeedDemoData

/**
 * Manual dependency graph. One place to swap Room for something else, and no annotation
 * processor for a project this size.
 */
class SlateContainer(context: Context) {

    private val database = SlateDatabase.build(context)

    val products: ProductRepository = RoomProductRepository(database.productDao())
    val customers: CustomerRepository = RoomCustomerRepository(database.customerDao())
    val ledger: LedgerRepository = RoomLedgerRepository(database.ledgerDao())
    val sales: SaleRepository = RoomSaleRepository(
        db = database,
        sales = database.saleDao(),
        products = database.productDao(),
        ledger = database.ledgerDao(),
    )

    val completeSale = CompleteSale(products, sales)
    val saveProduct = SaveProduct(products)
    val recordPayment = RecordPayment(customers, ledger)
    val seedDemoData = SeedDemoData(products, customers, sales, ledger)
}
