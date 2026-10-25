package com.maazsurti.slate.ui.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maazsurti.slate.core.domain.model.Cart
import com.maazsurti.slate.core.domain.model.Category
import com.maazsurti.slate.core.domain.model.Customer
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.repo.CustomerRepository
import com.maazsurti.slate.core.domain.repo.LedgerRepository
import com.maazsurti.slate.core.domain.repo.ProductRepository
import com.maazsurti.slate.core.domain.repo.SaleRepository
import com.maazsurti.slate.core.domain.usecase.CompleteSale
import com.maazsurti.slate.core.domain.usecase.Rejection
import com.maazsurti.slate.core.domain.usecase.SaleResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SaleUiState(
    val query: String = "",
    val products: List<Product> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val cart: Cart = Cart(),
    val payment: PaymentType = PaymentType.CASH,
    val customerId: Long? = null,
    val category: Category? = null,
    val todayTotal: Money = Money.Zero,
    val todayCount: Int = 0,
    val outstanding: Money = Money.Zero,
) {
    val visibleProducts: List<Product>
        get() = products
            .filter { category == null || it.category == category }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }

    /** Only the shelves that actually hold something. */
    val shelves: List<Category>
        get() = products.map { it.category }.distinct().sortedBy { it.ordinal }

    val selectedCustomer: Customer?
        get() = customers.firstOrNull { it.id == customerId }
}

class SaleViewModel(
    private val completeSale: CompleteSale,
    products: ProductRepository,
    customers: CustomerRepository,
    sales: SaleRepository,
    ledger: LedgerRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private data class Inputs(
        val query: String,
        val category: Category?,
        val cart: Cart,
        val payment: PaymentType,
        val customerId: Long?,
    )

    private val query = MutableStateFlow("")
    private val cart = MutableStateFlow(Cart())
    private val payment = MutableStateFlow(PaymentType.CASH)
    private val customerId = MutableStateFlow<Long?>(null)
    private val category = MutableStateFlow<Category?>(null)

    /** One-shot notices for the snackbar, so a cleared message never cancels the snackbar. */
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    private val filters = combine(query, category) { query, category -> query to category }

    private val inputs = combine(filters, cart, payment, customerId) { (query, category), cart, payment, customerId ->
        Inputs(query, category, cart, payment, customerId)
    }

    private val todayStart: Long =
        java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    val state: StateFlow<SaleUiState> = combine(
        products.observeAll(),
        customers.observeAll(),
        inputs,
        sales.observeSince(todayStart),
        ledger.observeBalances(),
    ) { products, customers, input, todaySales, balances ->
        SaleUiState(
            query = input.query,
            category = input.category,
            products = products,
            customers = customers,
            cart = input.cart,
            payment = input.payment,
            customerId = input.customerId,
            todayTotal = todaySales.fold(Money.Zero) { sum, sale -> sum + sale.total },
            todayCount = todaySales.size,
            outstanding = balances.fold(Money.Zero) { sum, balance -> sum + balance.balance },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SaleUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun selectCategory(value: Category?) {
        category.value = value
    }

    fun add(product: Product) = cart.update { it.add(product) }

    fun increase(productId: Long) = cart.update { it.setQuantity(productId, it.quantityOf(productId) + 1) }

    fun decrease(productId: Long) = cart.update { it.setQuantity(productId, it.quantityOf(productId) - 1) }

    fun remove(productId: Long) = cart.update { it.remove(productId) }

    fun selectPayment(type: PaymentType) {
        payment.value = type
        if (type == PaymentType.CASH) customerId.value = null
    }

    fun selectCustomer(id: Long?) {
        customerId.value = id
    }

    fun complete() {
        val current = cart.value
        viewModelScope.launch {
            _messages.send(when (
                val result = completeSale(current, payment.value, customerId.value, clock())
            ) {
                is SaleResult.Recorded -> {
                    cart.value = Cart()
                    customerId.value = null
                    payment.value = PaymentType.CASH
                    "Sale recorded  ₹${result.total.format()}"
                }
                is SaleResult.Rejected -> when (val reason = result.reason) {
                    Rejection.EmptyCart -> "Add something to the sale first"
                    Rejection.CreditNeedsCustomer -> "Pick a customer for a credit sale"
                    is Rejection.UnknownProduct -> "${reason.name} is no longer in the catalogue"
                    is Rejection.NotEnoughStock -> "Only ${reason.available} × ${reason.name} left"
                }
            })
        }
    }
}
