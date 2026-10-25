package com.maazsurti.slate.core.data.repo

import com.maazsurti.slate.core.data.db.CustomerBalanceRow
import com.maazsurti.slate.core.data.db.CustomerEntity
import com.maazsurti.slate.core.data.db.LedgerEntryEntity
import com.maazsurti.slate.core.data.db.ProductEntity
import com.maazsurti.slate.core.data.db.SaleLineEntity
import com.maazsurti.slate.core.data.db.SaleRow
import com.maazsurti.slate.core.domain.model.Category
import com.maazsurti.slate.core.domain.model.Customer
import com.maazsurti.slate.core.domain.model.CustomerBalance
import com.maazsurti.slate.core.domain.model.LedgerEntry
import com.maazsurti.slate.core.domain.model.LedgerKind
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.model.Sale
import com.maazsurti.slate.core.domain.model.SaleLine

fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    price = Money(priceCents),
    stock = stock,
    lowStockThreshold = lowStockThreshold,
    category = Category.fromName(category),
)

fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    priceCents = price.cents,
    stock = stock,
    lowStockThreshold = lowStockThreshold,
    category = category.name,
)

fun CustomerEntity.toDomain() = Customer(id = id, name = name, phone = phone)

fun Customer.toEntity() = CustomerEntity(id = id, name = name, phone = phone)

fun CustomerBalanceRow.toDomain() = CustomerBalance(
    customerId = customerId,
    name = name,
    phone = phone,
    balance = Money(balanceCents),
)

fun LedgerEntryEntity.toDomain() = LedgerEntry(
    id = id,
    customerId = customerId,
    at = at,
    kind = if (kind == LedgerKind.PAYMENT.name) LedgerKind.PAYMENT else LedgerKind.CHARGE,
    amount = Money(amountCents),
    note = note,
)

fun LedgerEntry.toEntity() = LedgerEntryEntity(
    id = id,
    customerId = customerId,
    at = at,
    kind = kind.name,
    amountCents = amount.cents,
    note = note,
)

fun SaleRow.toDomain() = Sale(
    id = sale.id,
    at = sale.at,
    payment = if (sale.payment == PaymentType.CREDIT.name) PaymentType.CREDIT else PaymentType.CASH,
    total = Money(sale.totalCents),
    customerId = sale.customerId,
    lines = lines.map {
        SaleLine(
            productId = it.productId,
            name = it.name,
            unitPrice = Money(it.unitPriceCents),
            quantity = it.quantity,
        )
    },
)

fun Sale.toLineEntities(): List<SaleLineEntity> = lines.map {
    SaleLineEntity(
        saleId = id,
        productId = it.productId,
        name = it.name,
        unitPriceCents = it.unitPrice.cents,
        quantity = it.quantity,
    )
}
