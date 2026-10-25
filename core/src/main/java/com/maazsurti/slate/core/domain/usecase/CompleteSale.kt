package com.maazsurti.slate.core.domain.usecase

import com.maazsurti.slate.core.domain.model.Cart
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Sale
import com.maazsurti.slate.core.domain.model.SaleLine
import com.maazsurti.slate.core.domain.repo.ProductRepository
import com.maazsurti.slate.core.domain.repo.SaleRepository

sealed interface SaleResult {
    data class Recorded(val saleId: Long, val total: Money) : SaleResult
    data class Rejected(val reason: Rejection) : SaleResult
}

sealed interface Rejection {
    data object EmptyCart : Rejection
    data object CreditNeedsCustomer : Rejection
    data class UnknownProduct(val name: String) : Rejection
    data class NotEnoughStock(val name: String, val available: Int) : Rejection
}

/**
 * Checks the cart against live stock before anything is written. The write itself is one
 * transaction in [SaleRepository.record].
 */
class CompleteSale(
    private val products: ProductRepository,
    private val sales: SaleRepository,
) {
    suspend operator fun invoke(
        cart: Cart,
        payment: PaymentType,
        customerId: Long?,
        now: Long,
    ): SaleResult {
        if (cart.isEmpty) return SaleResult.Rejected(Rejection.EmptyCart)
        if (payment == PaymentType.CREDIT && customerId == null) {
            return SaleResult.Rejected(Rejection.CreditNeedsCustomer)
        }

        val lines = mutableListOf<SaleLine>()
        for (line in cart.lines) {
            val product = products.get(line.productId)
                ?: return SaleResult.Rejected(Rejection.UnknownProduct(line.name))
            if (line.quantity > product.stock) {
                return SaleResult.Rejected(Rejection.NotEnoughStock(product.name, product.stock))
            }
            lines += SaleLine(
                productId = product.id,
                name = product.name,
                unitPrice = product.price,
                quantity = line.quantity,
            )
        }

        val total = lines.fold(Money.Zero) { sum, line -> sum + line.total }
        val saleId = sales.record(
            Sale(
                at = now,
                payment = payment,
                total = total,
                customerId = customerId,
                lines = lines,
            )
        )
        return SaleResult.Recorded(saleId, total)
    }
}
