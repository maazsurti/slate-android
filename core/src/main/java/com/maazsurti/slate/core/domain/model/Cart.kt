package com.maazsurti.slate.core.domain.model

data class CartLine(
    val productId: Long,
    val name: String,
    val unitPrice: Money,
    val quantity: Int,
) {
    val total: Money get() = unitPrice * quantity
}

/** Pure cart logic, kept out of the view model so it can be tested on its own. */
data class Cart(val lines: List<CartLine> = emptyList()) {

    val total: Money get() = lines.fold(Money.Zero) { sum, line -> sum + line.total }

    val itemCount: Int get() = lines.sumOf { it.quantity }

    val isEmpty: Boolean get() = lines.isEmpty()

    fun quantityOf(productId: Long): Int = lines.firstOrNull { it.productId == productId }?.quantity ?: 0

    fun add(product: Product): Cart {
        if (product.isOutOfStock) return this
        val existing = lines.firstOrNull { it.productId == product.id }
        val updated = if (existing == null) {
            lines + CartLine(product.id, product.name, product.price, 1)
        } else {
            lines.map {
                if (it.productId == product.id) it.copy(quantity = it.quantity + 1) else it
            }
        }
        return Cart(updated)
    }

    fun setQuantity(productId: Long, quantity: Int): Cart =
        if (quantity <= 0) {
            Cart(lines.filterNot { it.productId == productId })
        } else {
            Cart(lines.map { if (it.productId == productId) it.copy(quantity = quantity) else it })
        }

    fun remove(productId: Long): Cart = Cart(lines.filterNot { it.productId == productId })

    fun clear(): Cart = Cart()
}
