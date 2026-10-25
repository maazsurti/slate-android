package com.maazsurti.slate.core.domain

import com.maazsurti.slate.core.domain.model.Cart
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartTest {

    private val rice = Product(id = 1, name = "Rice", price = Money.of(850), stock = 4)
    private val milk = Product(id = 2, name = "Milk", price = Money.of(45), stock = 0)

    @Test
    fun `adding the same product raises its quantity`() {
        val cart = Cart().add(rice).add(rice)

        assertEquals(2, cart.itemCount)
        assertEquals(1, cart.lines.size)
        assertEquals(Money.of(1_700), cart.total)
    }

    @Test
    fun `an out-of-stock product never enters the cart`() {
        assertTrue(Cart().add(milk).isEmpty)
    }

    @Test
    fun `dropping to zero removes the line`() {
        val cart = Cart().add(rice).add(rice).setQuantity(rice.id, 0)

        assertTrue(cart.isEmpty)
        assertEquals(Money.Zero, cart.total)
    }

    @Test
    fun `total adds lines of different products`() {
        val cart = Cart().add(rice).add(milk.copy(stock = 5)).add(milk.copy(stock = 5))

        assertEquals(3, cart.itemCount)
        assertEquals(Money.of(940), cart.total)
    }
}
