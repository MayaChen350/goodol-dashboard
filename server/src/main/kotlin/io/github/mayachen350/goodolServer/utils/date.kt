package io.github.mayachen350.goodolServer.utils

import kotlinx.datetime.*
import kotlin.time.Clock

fun LocalDate.atStartOfWeek(): LocalDate = if (dayOfWeek.ordinal != 0)
    this.minus(dayOfWeek.ordinal, DateTimeUnit.DAY)
else this

fun LocalDate.atEndOfWeek(): LocalDate = if (dayOfWeek.ordinal != 6)
    this.plus(6 - dayOfWeek.ordinal, DateTimeUnit.DAY)
else this

fun LocalDate.Companion.today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date