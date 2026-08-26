package com.arrazyfathan.kbbi.widgets

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext
import androidx.glance.color.ColorProvider as DayNightColorProvider
import com.arrazyfathan.kbbi.core.R as CoreR

private val COMPACT_SIZE = DpSize(110.dp, 56.dp)
private val EXPANDED_SIZE = DpSize(220.dp, 56.dp)
private val RESPONSIVE_SIZES = setOf(COMPACT_SIZE, EXPANDED_SIZE)

private data class WidgetPaletteResources(
    @ColorRes val background: Int,
    @ColorRes val iconBackground: Int,
    @ColorRes val iconTint: Int,
    @ColorRes val primaryText: Int,
    @ColorRes val supportingText: Int,
    @ColorRes val actionBackground: Int,
    @ColorRes val actionTint: Int,
)

private data class WidgetPalette(
    val background: ColorProvider,
    val iconBackground: ColorProvider,
    val iconTint: ColorProvider,
    val primaryText: ColorProvider,
    val supportingText: ColorProvider,
    val actionBackground: ColorProvider,
    val actionTint: ColorProvider,
)

private val wordOfDayPalette =
    WidgetPaletteResources(
        background = R.color.widget_daily_background,
        iconBackground = R.color.widget_daily_icon_background,
        iconTint = R.color.widget_daily_icon,
        primaryText = R.color.widget_daily_text_primary,
        supportingText = R.color.widget_daily_text_secondary,
        actionBackground = R.color.widget_daily_action_background,
        actionTint = R.color.widget_daily_action,
    )

private val searchPalette =
    WidgetPaletteResources(
        background = R.color.widget_search_background,
        iconBackground = R.color.widget_search_icon_background,
        iconTint = R.color.widget_search_icon,
        primaryText = R.color.widget_search_text_primary,
        supportingText = R.color.widget_search_text_secondary,
        actionBackground = R.color.widget_search_action_background,
        actionTint = R.color.widget_search_action,
    )

private val savedPalette =
    WidgetPaletteResources(
        background = R.color.widget_saved_background,
        iconBackground = R.color.widget_saved_icon_background,
        iconTint = R.color.widget_saved_icon,
        primaryText = R.color.widget_saved_text_primary,
        supportingText = R.color.widget_saved_text_secondary,
        actionBackground = R.color.widget_saved_action_background,
        actionTint = R.color.widget_saved_action,
    )

private fun WidgetPaletteResources.resolve(context: Context): WidgetPalette {
    val dayContext = context.withNightMode(enabled = false)
    val nightContext = context.withNightMode(enabled = true)

    fun provider(
        @ColorRes colorRes: Int,
    ): ColorProvider =
        DayNightColorProvider(
            day = Color(ContextCompat.getColor(dayContext, colorRes)),
            night = Color(ContextCompat.getColor(nightContext, colorRes)),
        )

    return WidgetPalette(
        background = provider(background),
        iconBackground = provider(iconBackground),
        iconTint = provider(iconTint),
        primaryText = provider(primaryText),
        supportingText = provider(supportingText),
        actionBackground = provider(actionBackground),
        actionTint = provider(actionTint),
    )
}

private fun Context.withNightMode(enabled: Boolean): Context {
    val configuration = Configuration(resources.configuration)
    val nightMode =
        if (enabled) {
            Configuration.UI_MODE_NIGHT_YES
        } else {
            Configuration.UI_MODE_NIGHT_NO
        }
    configuration.uiMode =
        (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
    return createConfigurationContext(configuration)
}

internal class WordOfDayWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(RESPONSIVE_SIZES)

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val words =
            GlobalContext
                .get()
                .get<WordCatalogRepository>()
                .getWords()
                .filter(String::isNotBlank)
        val word = DailyItemSelector.select(words)
        val intent = widgetLaunchIntent(context, ACTION_WIDGET_WORD_OF_DAY, word)
        val palette = wordOfDayPalette.resolve(context)

        provideContent {
            KbbiWidgetContent(
                iconResId = CoreR.drawable.word,
                primaryText = word ?: context.getString(R.string.widget_catalog_unavailable),
                supportingText =
                    if (word == null) {
                        context.getString(R.string.widget_search_fallback)
                    } else {
                        context.getString(R.string.widget_word_of_day_heading)
                    },
                contentDescription = context.getString(R.string.widget_word_of_day_label),
                palette = palette,
                action = actionStartActivity(intent),
            )
        }
    }
}

internal class SavedWordWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(RESPONSIVE_SIZES)

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val bookmarkWords =
            GlobalContext
                .get()
                .get<BookmarkRepository>()
                .getBookmarks()
                .first()
                .map { it.word }
        val word = DailyItemSelector.selectSortedWords(bookmarkWords)
        val intent = widgetLaunchIntent(context, ACTION_WIDGET_SAVED_WORD, word)
        val palette = savedPalette.resolve(context)

        provideContent {
            KbbiWidgetContent(
                iconResId = CoreR.drawable.saved,
                primaryText = word ?: context.getString(R.string.widget_no_saved_words),
                supportingText =
                    if (word == null) {
                        context.getString(R.string.widget_save_word_prompt)
                    } else {
                        context.getString(R.string.widget_saved_word_heading)
                    },
                contentDescription = context.getString(R.string.widget_saved_word_label),
                palette = palette,
                action = actionStartActivity(intent),
            )
        }
    }
}

internal class QuickSearchWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(RESPONSIVE_SIZES)

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val intent = widgetLaunchIntent(context, ACTION_WIDGET_QUICK_SEARCH)
        val palette = searchPalette.resolve(context)
        provideContent {
            KbbiWidgetContent(
                iconResId = CoreR.drawable.ic_search,
                primaryText = context.getString(R.string.widget_quick_search_heading),
                supportingText = context.getString(R.string.widget_search_prompt),
                contentDescription = context.getString(R.string.widget_quick_search_label),
                palette = palette,
                action = actionStartActivity(intent),
            )
        }
    }
}

internal class WordOfDayWidgetReceiver : DataWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WordOfDayWidget()
}

internal class SavedWordWidgetReceiver : DataWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SavedWordWidget()
}

internal class QuickSearchWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickSearchWidget()
}

internal abstract class DataWidgetReceiver : GlanceAppWidgetReceiver() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshScheduler.reconcile(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefreshScheduler.reconcile(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetRefreshScheduler.reconcile(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        WidgetRefreshScheduler.reconcile(context)
    }
}

@Composable
private fun KbbiWidgetContent(
    iconResId: Int,
    primaryText: String,
    supportingText: String,
    contentDescription: String,
    palette: WidgetPalette,
    action: Action,
) {
    val width = LocalSize.current.width
    val expanded = width >= EXPANDED_SIZE.width
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(palette.background)
                .cornerRadius(18.dp)
                .appWidgetBackground()
                .clickable(action),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Row(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = if (expanded) 52.dp else 8.dp,
                        bottom = 8.dp,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    GlanceModifier
                        .size(36.dp)
                        .background(palette.iconBackground)
                        .cornerRadius(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    provider = ImageProvider(iconResId),
                    contentDescription = contentDescription,
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(palette.iconTint),
                )
            }
            Spacer(modifier = GlanceModifier.width(10.dp))
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = primaryText,
                    maxLines = 1,
                    style =
                        TextStyle(
                            color = palette.primaryText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                )
                if (expanded) {
                    Text(
                        text = supportingText,
                        maxLines = 1,
                        style =
                            TextStyle(
                                color = palette.supportingText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
            }
        }
        if (expanded) {
            Box(
                modifier = GlanceModifier.width(52.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        GlanceModifier
                            .size(36.dp)
                            .background(palette.actionBackground)
                            .cornerRadius(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_arrow),
                        contentDescription = null,
                        modifier = GlanceModifier.size(16.dp),
                        colorFilter = ColorFilter.tint(palette.actionTint),
                    )
                }
            }
        }
    }
}
