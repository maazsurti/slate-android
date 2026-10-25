package com.maazsurti.slate.core.data.repo

import androidx.room.withTransaction
import com.maazsurti.slate.core.data.db.LedgerDao
import com.maazsurti.slate.core.data.db.LedgerEntryEntity
import com.maazsurti.slate.core.data.db.ProductDao
import com.maazsurti.slate.core.data.db.SaleDao
import com.maazsurti.slate.core.data.db.SaleEntity
import com.maazsurti.slate.core.data.db.SlateDatabase
import com.maazsurti.slate.core.domain.model.LedgerKind
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Sale
import com.maazsurti.slate.core.domain.repo.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSaleRepository(
    private val db: SlateDatabase,
    private val sales: SaleDao,
    private val products: ProductDao,
    private val ledger: LedgerDao,
) : SaleRepository {

    override suspend fun record(sale: Sale): Long = db.withTransaction {
        val saleId = sales.insertSale(
            SaleEntity(
                at = sale.at,
                payment = sale.payment.name,
                totalCents = sale.total.cents,
                customerId = sale.customerId,
            )
        )
        val lines = sale.copy(id = saleId).toLineEntities()
        sales.insertLines(lines)
        lines.forEach { products.decrementStock(it.productId, it.quantity) }
        if (sale.payment == PaymentType.CREDIT && sale.customerId != null) {
            ledger.insert(
                LedgerEntryEntity(
                    customerId = sale.customerId,
                    at = sale.at,
                    kind = LedgerKind.CHARGE.name,
                    amountCents = sale.total.cents,
                    note = "Credit sale",
                )
            )
        }
        saleId
    }

    override fun observeSince(sinceMillis: Long): Flow<List<Sale>> =
        sales.observeSince(sinceMillis).map { rows -> rows.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<Sale>> =
        sales.observeRecent(limit).map { rows -> rows.map { it.toDomain() } }
}
