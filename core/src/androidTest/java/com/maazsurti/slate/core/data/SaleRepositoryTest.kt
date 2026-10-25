package com.maazsurti.slate.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maazsurti.slate.core.data.db.CustomerEntity
import com.maazsurti.slate.core.data.db.LedgerEntryEntity
import com.maazsurti.slate.core.data.db.ProductEntity
import com.maazsurti.slate.core.data.db.SlateDatabase
import com.maazsurti.slate.core.data.repo.RoomSaleRepository
import com.maazsurti.slate.core.domain.model.LedgerKind
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Sale
import com.maazsurti.slate.core.domain.model.SaleLine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaleRepositoryTest {

    private lateinit var db: SlateDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SlateDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun creditSaleDecrementsStockAndPostsALedgerCharge() = runTest {
        val productId = db.productDao().upsert(
            ProductEntity(name = "Basmati Rice 5kg", priceCents = 85_000, stock = 5, lowStockThreshold = 2, category = "Grains")
        )
        val customerId = db.customerDao().upsert(CustomerEntity(name = "Ramesh Patel", phone = "+91 98250 11223"))
        val repository = RoomSaleRepository(db, db.saleDao(), db.productDao(), db.ledgerDao())

        repository.record(
            Sale(
                at = 1_000,
                payment = PaymentType.CREDIT,
                total = Money(85_000),
                customerId = customerId,
                lines = listOf(SaleLine(productId, "Basmati Rice 5kg", Money(85_000), 1)),
            )
        )

        assertEquals(4, db.productDao().get(productId)?.stock)
        val balance = db.ledgerDao().observeBalances().first().first { it.customerId == customerId }
        assertEquals(85_000, balance.balanceCents)
    }

    @Test
    fun paymentBringsTheBalanceBackDown() = runTest {
        val customerId = db.customerDao().upsert(CustomerEntity(name = "Kiran Shah", phone = ""))
        db.ledgerDao().insert(
            LedgerEntryEntity(customerId = customerId, at = 1, kind = LedgerKind.CHARGE.name, amountCents = 50_000, note = "Credit sale")
        )
        db.ledgerDao().insert(
            LedgerEntryEntity(customerId = customerId, at = 2, kind = LedgerKind.PAYMENT.name, amountCents = 20_000, note = "Payment received")
        )

        val balance = db.ledgerDao().observeBalances().first().first { it.customerId == customerId }
        assertEquals(30_000, balance.balanceCents)
    }
}
