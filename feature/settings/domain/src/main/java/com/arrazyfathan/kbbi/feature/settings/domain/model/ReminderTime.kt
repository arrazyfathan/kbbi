package com.arrazyfathan.kbbi.feature.settings.domain.model

data class ReminderTime(
    val hour: Int,
    val minute: Int,
) {
    init {
        require(hour in 0..23)
        require(minute in 0..59)
    }

    val totalMinutes: Int get() = hour * 60 + minute

    companion object {
        val DailyWord = ReminderTime(9, 0)
        val DailyProverb = ReminderTime(12, 0)
        val BookmarkReview = ReminderTime(19, 0)
    }
}
