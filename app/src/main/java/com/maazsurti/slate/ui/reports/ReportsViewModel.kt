package com.maazsurti.slate.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.model.SalesSummary
import com.maazsurti.slate.core.domain.repo.ProductRepository
import com.maazsurti.slate.core.domain.repo.SaleRepository
import com.maazsurti.slate.ui.common.toLocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

data class DayTotal(val date: LocalDate, val total: Money, val count: Int)

data class ReportsUiState(
    val summary: SalesSummary = SalesSummary(0, 0, Money.Zero, Money.Zero, Money.Zero, emptyList()),
    val days: List<DayTotal> = emptyList(),
    val lowStock: List<Product> = emptyList(),
) {
    val bestDay: Money get() = days.maxOfOrNull { it.total } ?: Money.Zero
}

class ReportsViewModel(
    sales: SaleRepository,
    products: ProductRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val windowDays = 7

    private val firstDay: LocalDate =
        java.time.Instant.ofEpochMilli(clock()).atZone(ZoneId.systemDefault()).toLocalDate()
            .minusDays((windowDays - 1).toLong())

    private val since: Long = firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val state: StateFlow<ReportsUiState> = combine(
        sales.observeSince(since),
        products.observeAll(),
    ) { sales, products ->
        val byDay = sales.groupBy { it.at.toLocalDate() }
        val days = (0 until windowDays).map { offset ->
            val date = firstDay.plusDays(offset.toLong())
            val daySales = byDay[date].orEmpty()
            DayTotal(
                date = date,
                total = daySales.fold(Money.Zero) { sum, sale -> sum + sale.total },
                count = daySales.size,
            )
        }
        ReportsUiState(
            summary = SalesSummary.from(sales),
            days = days,
            lowStock = products.filter { it.isLowStock }.sortedBy { it.stock },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ReportsUiState())
}
