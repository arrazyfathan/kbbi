package com.arrazyfathan.kbbi.core.observability

sealed interface AnalyticsEvent {
    val name: String
    val parameters: Map<String, String>

    data class SearchCompleted(
        val source: EventSource,
        val inputMethod: InputMethod,
        val outcome: EventOutcome,
        val suggestionUsed: Boolean = false,
        val randomSelection: Boolean = false,
    ) : AnalyticsEvent {
        override val name = "search_completed"
        override val parameters =
            mapOf(
                "source" to source.value,
                "input_method" to inputMethod.value,
                "outcome" to outcome.value,
                "suggestion_used" to suggestionUsed.toString(),
                "random_selection" to randomSelection.toString(),
            )
    }

    data class ContentOpened(
        val contentType: ContentType,
        val source: EventSource,
    ) : AnalyticsEvent {
        override val name = "content_opened"
        override val parameters = mapOf("content_type" to contentType.value, "source" to source.value)
    }

    data class BookmarkChanged(
        val action: BookmarkAction,
        val surface: AnalyticsScreen,
    ) : AnalyticsEvent {
        override val name = "bookmark_changed"
        override val parameters = mapOf("action" to action.value, "surface" to surface.value)
    }

    data class TranslationChanged(
        val action: TranslationAction,
        val outcome: EventOutcome,
        val cacheHit: Boolean,
    ) : AnalyticsEvent {
        override val name = "translation_changed"
        override val parameters =
            mapOf(
                "action" to action.value,
                "outcome" to outcome.value,
                "cache_hit" to cacheHit.toString(),
            )
    }

    data class ProverbOpened(
        val outcome: EventOutcome,
    ) : AnalyticsEvent {
        override val name = "proverb_opened"
        override val parameters = mapOf("outcome" to outcome.value)
    }

    data class ReminderChanged(
        val reminderType: ReminderKind,
        val enabled: Boolean,
    ) : AnalyticsEvent {
        override val name = "reminder_changed"
        override val parameters = mapOf("reminder_type" to reminderType.value, "enabled" to enabled.toString())
    }

    data class NotificationOpened(
        val reminderType: ReminderKind,
    ) : AnalyticsEvent {
        override val name = "notification_opened"
        override val parameters = mapOf("reminder_type" to reminderType.value)
    }

    data class WidgetOpened(
        val widgetType: WidgetKind,
    ) : AnalyticsEvent {
        override val name = "widget_opened"
        override val parameters = mapOf("widget_type" to widgetType.value)
    }

    data class AppUpdateInteraction(
        val action: UpdateAction,
        val outcome: EventOutcome,
    ) : AnalyticsEvent {
        override val name = "app_update_action"
        override val parameters = mapOf("action" to action.value, "outcome" to outcome.value)
    }

    data object AppShared : AnalyticsEvent {
        override val name = "app_shared"
        override val parameters = emptyMap<String, String>()
    }
}

enum class AnalyticsScreen(val value: String) {
    Home("home"),
    Words("words"),
    Proverbs("proverbs"),
    Bookmarks("bookmarks"),
    Settings("settings"),
    WordDetail("word_detail"),
    PrivacyPolicy("privacy_policy"),
    TermsConditions("terms_conditions"),
    OpenSourceLicenses("open_source_licenses"),
}

enum class EventSource(val value: String) {
    BottomNavigation("bottom_navigation"),
    TypedSearch("typed_search"),
    VoiceSearch("voice_search"),
    Suggestion("suggestion"),
    RandomWord("random_word"),
    WordList("word_list"),
    ProverbList("proverb_list"),
    Bookmarks("bookmarks"),
    ExternalIntent("external_intent"),
    Shortcut("shortcut"),
    Notification("notification"),
    WidgetQuickSearch("widget_quick_search"),
    WidgetWordOfDay("widget_word_of_day"),
    WidgetSavedWord("widget_saved_word"),
}

enum class InputMethod(val value: String) {
    Text("text"),
    Voice("voice"),
    System("system"),
}

enum class EventOutcome(val value: String) {
    Success("success"),
    NotFound("not_found"),
    Error("error"),
    Shown("shown"),
    Dismissed("dismissed"),
    Started("started"),
}

enum class ContentType(val value: String) {
    Word("word"),
    Proverb("proverb"),
}

enum class BookmarkAction(val value: String) {
    Added("added"),
    Removed("removed"),
}

enum class TranslationAction(val value: String) {
    Enabled("enabled"),
    Disabled("disabled"),
}

enum class ReminderKind(val value: String) {
    DailyWord("daily_word"),
    DailyProverb("daily_proverb"),
    BookmarkReview("bookmark_review"),
}

enum class WidgetKind(val value: String) {
    QuickSearch("quick_search"),
    WordOfDay("word_of_day"),
    SavedWord("saved_word"),
}

enum class UpdateAction(val value: String) {
    Prompt("prompt"),
    Download("download"),
    Install("install"),
}
