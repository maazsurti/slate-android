package com.maazsurti.slate.core.domain.model

enum class LedgerKind { CHARGE, PAYMENT }

data class LedgerEntry(
    val id: Long = 0,
    val customerId: Long,
    val at: Long,
    val kind: LedgerKind,
    val amount: Money,
    val note: String = "",
)
