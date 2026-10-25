package com.maazsurti.slate.core.domain.model

enum class PaymentType { CASH, CREDIT }

data class SaleLine(
    val productId: Long,
    val name: String,
    val unitPrice: Money,
    val quantity: Int,
) {
    val total: Money get() = unitPrice * quantity
}

data class Sale(
    val id: Long = 0,
    val at: Long,
    val payment: PaymentType,
    val total: Money,
    val customerId: Long?,
    val lines: List<SaleLine> = emptyList(),
)

data class TopProduct(val name: String, val quantity: Int, val revenue: Money)

/** Aggregates a list of sales. Kept pure so reports are unit tested without a database. */
data class SalesSummary(
    val salesCount: Int,
    val items: Int,
    val total: Money,
    val cash: Money,
    val credit: Money,
    val topProducts: List<TopProduct>,
) {
    companion object {
        fun from(sales: List<Sale>, topCount: Int = 5): SalesSummary {
            val cash = sales.filter { it.payment == PaymentType.CASH }.fold(Money.Zero) { sum, s -> sum + s.total }
            val credit = sales.filter { it.payment == PaymentType.CREDIT }.fold(Money.Zero) { sum, s -> sum + s.total }
            val top = sales.flatMap { it.lines }
                .groupBy { it.name }
                .map { (name, lines) ->
                    TopProduct(
                        name = name,
                        quantity = lines.sumOf { it.quantity },
                        revenue = lines.fold(Money.Zero) { sum, line -> sum + line.total },
                    )
                }
                .sortedWith(compareByDescending<TopProduct> { it.quantity }.thenByDescending { it.revenue })
                .take(topCount)
            return SalesSummary(
                salesCount = sales.size,
                items = sales.sumOf { sale -> sale.lines.sumOf { it.quantity } },
                total = cash + credit,
                cash = cash,
                credit = credit,
                topProducts = top,
            )
        }
    }
}
