package com.maazsurti.slate.ui.common

import com.maazsurti.slate.core.domain.model.Money
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Money.display(): String = "₹" + format()

private val zone: ZoneId get() = ZoneId.systemDefault()

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

fun Long.toClock(): String =
    Instant.ofEpochMilli(this).atZone(zone).format(DateTimeFormatter.ofPattern("HH:mm"))

fun dayLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH))
}
