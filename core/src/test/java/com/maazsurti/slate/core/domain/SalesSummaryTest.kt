package com.maazsurti.slate.core.domain

import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Sale
import com.maazsurti.slate.core.domain.model.SaleLine
import com.maazsurti.slate.core.domain.model.SalesSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class SalesSummaryTest {

    private fun sale(payment: PaymentType, vararg lines: Pair<String, Pair<Long, Int>>) = Sale(
        at = 0,
        payment = payment,
        total = lines.fold(Money.Zero) { sum, (_, line) -> sum + Money.of(line.first) * line.second },
        customerId = null,
        lines = lines.map { (name, line) ->
            SaleLine(productId = name.hashCode().toLong(), name = name, unitPrice = Money.of(line.first), quantity = line.second)
        },
    )

    @Test
    fun `splits cash from credit`() {
        val summary = SalesSummary.from(
            listOf(
                sale(PaymentType.CASH, "Rice" to (850L to 1)),
                sale(PaymentType.CREDIT, "Oil" to (210L to 2)),
            )
        )

        assertEquals(2, summary.salesCount)
        assertEquals(3, summary.items)
        assertEquals(Money.of(850), summary.cash)
        assertEquals(Money.of(420), summary.credit)
        assertEquals(Money.of(1_270), summary.total)
    }

    @Test
    fun `top products rank by quantity`() {
        val summary = SalesSummary.from(
            listOf(
                sale(PaymentType.CASH, "Rice" to (850L to 1), "Soap" to (55L to 2)),
                sale(PaymentType.CASH, "Soap" to (55L to 3)),
            )
        )

        assertEquals("Soap", summary.topProducts.first().name)
        assertEquals(5, summary.topProducts.first().quantity)
        assertEquals(Money.of(275), summary.topProducts.first().revenue)
    }
}
