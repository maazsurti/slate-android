package com.maazsurti.slate.core.domain

import com.maazsurti.slate.core.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun `formats with grouping and two decimals`() {
        assertEquals("1,250.75", Money(125_075).format())
        assertEquals("0.05", Money(5).format())
        assertEquals("-12.00", Money(-1_200).format())
    }

    @Test
    fun `parses what a shopkeeper types`() {
        assertEquals(Money(125_050), Money.parse("1250.5"))
        assertEquals(Money(99_900), Money.parse("999"))
        assertEquals(Money(125_075), Money.parse("1,250.75"))
        assertNull(Money.parse("twelve"))
        assertNull(Money.parse(""))
    }

    @Test
    fun `arithmetic stays in minor units`() {
        assertEquals(Money(300), Money(100) + Money(200))
        assertEquals(Money(600), Money(200) * 3)
        assertEquals(Money(0), Money(150) - Money(150))
    }
}
