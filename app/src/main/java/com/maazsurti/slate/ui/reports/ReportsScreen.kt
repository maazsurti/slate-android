package com.maazsurti.slate.ui.reports

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.ui.common.Hairline
import com.maazsurti.slate.ui.common.MoneyText
import com.maazsurti.slate.ui.common.Overline
import com.maazsurti.slate.ui.common.Panel
import com.maazsurti.slate.ui.common.ScreenHeader
import com.maazsurti.slate.ui.common.Tag
import com.maazsurti.slate.ui.common.Tone
import com.maazsurti.slate.ui.common.display
import com.maazsurti.slate.ui.slateContainer
import com.maazsurti.slate.ui.theme.Clay
import com.maazsurti.slate.ui.theme.Green
import com.maazsurti.slate.ui.theme.Hairline as HairlineColor
import com.maazsurti.slate.ui.theme.Ink
import com.maazsurti.slate.ui.theme.MoneyStyle
import com.maazsurti.slate.ui.vmFactory
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ReportsScreen() {
    val container = slateContainer()
    val viewModel: ReportsViewModel = viewModel(
        factory = vmFactory { ReportsViewModel(container.sales, container.products) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val summary = state.summary

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                ScreenHeader(
                    overline = "Last 7 days",
                    title = "Takings",
                    meta = "${summary.salesCount} sales · ${summary.items} items out of the shop",
                )
            }
            item {
                Panel(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Overline("Revenue")
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(summary.total.display(), style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.weight(1f))
                        CashDonut(cash = summary.cash, credit = summary.credit)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row {
                        LegendDot(Green, "Cash ${summary.cash.display()}")
                        Spacer(Modifier.width(16.dp))
                        LegendDot(Clay, "Credit ${summary.credit.display()}")
                    }
                }
            }
            item {
                Panel(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Overline("Daily takings")
                        Spacer(Modifier.weight(1f))
                        Overline("best ${state.bestDay.display()}")
                    }
                    Spacer(Modifier.height(18.dp))
                    WeekChart(state)
                }
            }
            item {
                Panel(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Overline("Top products")
                    Spacer(Modifier.height(12.dp))
                    if (summary.topProducts.isEmpty()) {
                        Text(
                            "No sales in this window.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    summary.topProducts.forEachIndexed { index, product ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                (index + 1).toString().padStart(2, '0'),
                                style = MoneyStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(28.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${product.quantity} sold",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            MoneyText(product.revenue)
                        }
                        if (index < summary.topProducts.lastIndex) Hairline()
                    }
                }
            }
            item {
                Panel(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Overline("Restock soon")
                        Spacer(Modifier.weight(1f))
                        if (state.lowStock.isNotEmpty()) Tag("${state.lowStock.size} items", Tone.Warning)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (state.lowStock.isEmpty()) {
                        Text(
                            "Everything is above its low-stock line.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    state.lowStock.take(4).forEach { product ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(product.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text(
                                "${product.stock} left",
                                style = MoneyStyle,
                                color = Clay,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.clip(RoundedCornerShape(2.dp)).width(8.dp).height(8.dp).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Cash against credit, as a ring. Cash share sits in the middle. */
@Composable
private fun CashDonut(cash: Money, credit: Money) {
    val total = (cash.cents + credit.cents).coerceAtLeast(1)
    val cashFraction = cash.cents.toFloat() / total
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 9.dp.toPx()
                drawArc(
                    color = Green,
                    startAngle = -90f,
                    sweepAngle = 360f * cashFraction,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                )
                drawArc(
                    color = Clay,
                    startAngle = -90f + 360f * cashFraction,
                    sweepAngle = 360f * (1f - cashFraction),
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                )
            }
            Text(
                "${(cashFraction * 100).toInt()}%",
                style = MoneyStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun WeekChart(state: ReportsUiState) {
    val max = state.bestDay.cents.coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().height(132.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        state.days.forEachIndexed { index, day ->
            val fraction = day.total.cents.toFloat() / max
            val height = remember(day.date) { Animatable(0f) }
            LaunchedEffect(day.date, fraction) {
                height.animateTo(fraction, tween(520 + index * 40, easing = FastOutSlowInEasing))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .height((96f * height.value).dp.coerceAtLeast(2.dp))
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(if (day.total.isZero) HairlineColor else if (index == state.days.lastIndex) Ink else Green)
                )
                Spacer(Modifier.height(8.dp))
                Hairline()
                Spacer(Modifier.height(6.dp))
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.ENGLISH),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
