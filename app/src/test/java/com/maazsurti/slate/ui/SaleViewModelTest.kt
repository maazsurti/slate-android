package com.maazsurti.slate.ui

import com.maazsurti.slate.core.domain.model.Customer
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.usecase.CompleteSale
import com.maazsurti.slate.fakes.FakeCustomerRepository
import com.maazsurti.slate.fakes.FakeLedgerRepository
import com.maazsurti.slate.fakes.FakeProductRepository
import com.maazsurti.slate.fakes.FakeSaleRepository
import com.maazsurti.slate.testing.MainDispatcherRule
import com.maazsurti.slate.ui.sale.SaleViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SaleViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val rice = Product(id = 1, name = "Basmati Rice 5kg", price = Money.of(850), stock = 4)
    private val oil = Product(id = 2, name = "Sunflower Oil 1L", price = Money.of(210), stock = 9)
    private val products = FakeProductRepository(listOf(rice, oil))
    private val customers = FakeCustomerRepository(listOf(Customer(id = 3, name = "Ramesh Patel")))
    private val sales = FakeSaleRepository()

    private fun viewModel() = SaleViewModel(
        completeSale = CompleteSale(products, sales),
        products = products,
        customers = customers,
        sales = sales,
        ledger = FakeLedgerRepository(),
        clock = { 42L },
    )

    @Test
    fun `tapping products builds a cart with a running total`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect { } }
        advanceUntilIdle()

        vm.add(rice)
        vm.add(oil)
        vm.add(oil)
        advanceUntilIdle()

        assertEquals(3, vm.state.value.cart.itemCount)
        assertEquals(Money.of(1_270), vm.state.value.cart.total)
        assertEquals(2, vm.state.value.cart.quantityOf(oil.id))
    }

    @Test
    fun `completing a cash sale clears the cart and reports the total`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect { } }
        val notices = mutableListOf<String>()
        backgroundScope.launch { vm.messages.collect { notices += it } }
        advanceUntilIdle()

        vm.add(rice)
        vm.complete()
        advanceUntilIdle()

        assertTrue(vm.state.value.cart.isEmpty)
        assertEquals(1, sales.recorded.size)
        assertEquals("Sale recorded  ₹850.00", notices.last())
        assertEquals(42L, sales.recorded.single().at)
    }

    @Test
    fun `credit sale without a customer keeps the cart and asks for one`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect { } }
        val notices = mutableListOf<String>()
        backgroundScope.launch { vm.messages.collect { notices += it } }
        advanceUntilIdle()

        vm.add(oil)
        vm.selectPayment(PaymentType.CREDIT)
        vm.complete()
        advanceUntilIdle()

        assertEquals(1, vm.state.value.cart.itemCount)
        assertTrue(sales.recorded.isEmpty())
        assertEquals("Pick a customer for a credit sale", notices.last())
    }
}
