package mg.caisse.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FRENCH = Locale.FRENCH
private val MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy", FRENCH)
private val DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy", FRENCH)

private fun capitalizeFirst(value: String): String =
    value.replaceFirstChar { if (it.isLowerCase()) it.titlecase(FRENCH) else it.toString() }

fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

fun epochDayToDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

fun dateLabel(epochDay: Long): String = capitalizeFirst(epochDayToDate(epochDay).format(DAY_MONTH_YEAR))

fun currentYearMonth(): YearMonth = YearMonth.now()

fun monthRange(yearMonth: YearMonth): Pair<Long, Long> {
    val start = yearMonth.atDay(1).toEpochDay()
    val end = yearMonth.atEndOfMonth().toEpochDay()
    return start to end
}

fun monthLabel(yearMonth: YearMonth): String =
    capitalizeFirst(yearMonth.atDay(1).format(MONTH_YEAR))
