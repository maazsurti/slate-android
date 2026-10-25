package com.maazsurti.slate.core.domain

import com.maazsurti.slate.core.domain.model.Cart
import com.maazsurti.slate.core.domain.model.Customer
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.usecase.CompleteSale
import com.maazsurti.slate.core.domain.usecase.Rejection
import com.maazsurti.slate.core.domain.usecase.SaleResult
import com.maazsurti.slate.core.fakes.FakeCustomerRepository
import com.maazsurti.slate.core.fakes.FakeProductRepository
import com.maazsurti.slate.core.fakes.FakeSaleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteSaleTest {

    private val rice = Product(id = 1, name = "Basmati Rice 5kg", price = Money.of(850), stock = 3)
    private val oil = Product(id = 2, name = "Sunflower Oil 1L", price = Money.of(210), stock = 10)
    private val products = FakeProductRepository(listOf(rice, oil))
    private val sales = FakeSaleRepository()
    private val customers = FakeCustomerRepository(listOf(Customer(id = 7, name = "Ramesh Patel")))
    private val completeSale = CompleteSale(products, sales)

    @Test
    fun `empty cart is rejected before anything is written`() = runTest {
        val result = completeSale(Cart(), PaymentType.CASH, null, now = 0)

        assertTrue(result is SaleResult.Rejected)
        assertTrue((result as SaleResult.Rejected).reason is Rejection.EmptyCart)
        assertTrue(sales.recorded.isEmpty())
    }

    @Test
    fun `credit sale needs a customer`() = runTest {
        val result = completeSale(Cart().add(rice), PaymentType.CREDIT, customerId = null, now = 0)

        assertEquals(Rejection.CreditNeedsCustomer, (result as SaleResult.Rejected).reason)
        assertTrue(sales.recorded.isEmpty())
    }

    @Test
    fun `cart bigger than stock is rejected with the available count`() = runTest {
        val cart = Cart().add(rice).add(rice).add(rice).add(rice)

        val result = completeSale(cart, PaymentType.CASH, null, now = 0) as SaleResult.Rejected

        assertEquals(Rejection.NotEnoughStock("Basmati Rice 5kg", 3), result.reason)
        assertTrue(sales.recorded.isEmpty())
    }

    @Test
    fun `cash sale records the total and keeps the customer empty`() = runTest {
        val result = completeSale(Cart().add(rice).add(oil).add(oil), PaymentType.CASH, null, now = 123) as SaleResult.Recorded

        assertEquals(Money.of(1_270), result.total)
        assertEquals(1, sales.recorded.size)
        assertEquals(PaymentType.CASH, sales.recorded.single().payment)
        assertEquals(123L, sales.recorded.single().at)
        assertEquals(null, sales.recorded.single().customerId)
    }

    @Test
    fun `credit sale keeps the customer on the sale`() = runTest {
        completeSale(Cart().add(oil), PaymentType.CREDIT, customerId = 7, now = 0)

        assertEquals(7L, sales.recorded.single().customerId)
        assertEquals(PaymentType.CREDIT, sales.recorded.single().payment)
    }
}
