package com.maazsurti.slate.core.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String = "",
)

/** A customer and what they currently owe the shop. */
data class CustomerBalance(
    val customerId: Long,
    val name: String,
    val phone: String = "",
    val balance: Money,
) {
    val owes: Boolean get() = balance.cents > 0
}
