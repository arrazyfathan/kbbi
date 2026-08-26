package com.arrazyfathan.kbbi.widgets

import java.util.Calendar

internal object DailyItemSelector {
    fun <T> select(
        items: List<T>,
        year: Int,
        dayOfYear: Int,
    ): T? = items.takeIf { it.isNotEmpty() }?.get(dailyIndex(items.size, year, dayOfYear))

    fun <T> select(
        items: List<T>,
        calendar: Calendar = Calendar.getInstance(),
    ): T? =
        select(
            items = items,
            year = calendar.get(Calendar.YEAR),
            dayOfYear = calendar.get(Calendar.DAY_OF_YEAR),
        )

    fun selectSortedWords(
        words: List<String>,
        calendar: Calendar = Calendar.getInstance(),
    ): String? = select(words.filter(String::isNotBlank).sorted(), calendar)

    private fun dailyIndex(
        size: Int,
        year: Int,
        dayOfYear: Int,
    ): Int = (year * DAYS_PER_LEAP_YEAR + dayOfYear).mod(size)

    private const val DAYS_PER_LEAP_YEAR = 366
}
