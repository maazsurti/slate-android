package com.maazsurti.slate.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maazsurti.slate.core.domain.model.CustomerBalance
import com.maazsurti.slate.core.domain.model.LedgerEntry
import com.maazsurti.slate.core.domain.model.LedgerKind
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.repo.CustomerRepository
import com.maazsurti.slate.core.domain.repo.LedgerRepository
import com.maazsurti.slate.core.domain.usecase.PaymentResult
import com.maazsurti.slate.core.domain.usecase.RecordPayment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LedgerUiState(
    val balances: List<CustomerBalance> = emptyList(),
    val totalOutstanding: Money = Money.Zero,
    val owingCount: Int = 0,
)

class LedgerViewModel(ledger: LedgerRepository) : ViewModel() {

    val state: StateFlow<LedgerUiState> = ledger.observeBalances()
        .map { balances ->
            LedgerUiState(
                balances = balances,
                totalOutstanding = balances.fold(Money.Zero) { sum, it -> sum + it.balance },
                owingCount = balances.count { it.owes },
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LedgerUiState())
}

data class CustomerDetailUiState(
    val name: String = "",
    val phone: String = "",
    val balance: Money = Money.Zero,
    val entries: List<LedgerEntry> = emptyList(),
)

class CustomerDetailViewModel(
    private val customerId: Long,
    customers: CustomerRepository,
    private val ledger: LedgerRepository,
    private val recordPayment: RecordPayment,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    val state: StateFlow<CustomerDetailUiState> = combine(
        customers.observeAll(),
        ledger.observeBalances(),
        ledger.observeEntries(customerId),
    ) { customers, balances, entries ->
        val customer = customers.firstOrNull { it.id == customerId }
        val balance = balances.firstOrNull { it.customerId == customerId }?.balance ?: Money.Zero
        CustomerDetailUiState(
            name = customer?.name.orEmpty(),
            phone = customer?.phone.orEmpty(),
            balance = balance,
            entries = entries,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CustomerDetailUiState())

    fun pay(amountText: String) {
        val amount = Money.parse(amountText)
        if (amount == null) {
            _messages.trySend("That is not an amount")
            return
        }
        viewModelScope.launch {
            _messages.send(when (val result = recordPayment(customerId, amount, clock())) {
                is PaymentResult.Recorded -> "Payment of ₹${result.amount.format()} recorded"
                PaymentResult.InvalidAmount -> "Enter an amount above zero"
                PaymentResult.UnknownCustomer -> "That customer is gone"
            })
        }
    }
}
