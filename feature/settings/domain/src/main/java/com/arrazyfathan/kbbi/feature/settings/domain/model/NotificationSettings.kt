package com.arrazyfathan.kbbi.feature.settings.domain.model

data class ReminderPreference(
    val enabled: Boolean,
    val time: ReminderTime,
)

data class NotificationSettings(
    val dailyWord: ReminderPreference = ReminderPreference(false, ReminderTime.DailyWord),
    val dailyProverb: ReminderPreference = ReminderPreference(false, ReminderTime.DailyProverb),
    val bookmarkReview: ReminderPreference = ReminderPreference(false, ReminderTime.BookmarkReview),
    val permissionGranted: Boolean = true,
    val permissionRequired: Boolean = false,
) {
    fun preference(type: ReminderType): ReminderPreference =
        when (type) {
            ReminderType.DAILY_WORD -> dailyWord
            ReminderType.DAILY_PROVERB -> dailyProverb
            ReminderType.BOOKMARK_REVIEW -> bookmarkReview
        }
}
