package com.maazsurti.slate.core.domain.usecase

import com.maazsurti.slate.core.domain.model.LedgerEntry
import com.maazsurti.slate.core.domain.model.LedgerKind
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.repo.CustomerRepository
import com.maazsurti.slate.core.domain.repo.LedgerRepository

sealed interface PaymentResult {
    data class Recorded(val amount: Money) : PaymentResult
    data object InvalidAmount : PaymentResult
    data object UnknownCustomer : PaymentResult
}

class RecordPayment(
    private val customers: CustomerRepository,
    private val ledger: LedgerRepository,
) {
    suspend operator fun invoke(customerId: Long, amount: Money, now: Long): PaymentResult {
        if (amount.cents <= 0) return PaymentResult.InvalidAmount
        customers.get(customerId) ?: return PaymentResult.UnknownCustomer
        ledger.record(
            LedgerEntry(
                customerId = customerId,
                at = now,
                kind = LedgerKind.PAYMENT,
                amount = amount,
                note = "Payment received",
            )
        )
        return PaymentResult.Recorded(amount)
    }
}
