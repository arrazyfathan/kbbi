package com.arrazyfathan.kbbi.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.presentation.AppUpdateAction
import com.arrazyfathan.kbbi.core.appupdate.presentation.AppUpdatePrompt
import com.arrazyfathan.kbbi.core.appupdate.presentation.AppUpdateViewModel
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.AnalyticsScreen
import com.arrazyfathan.kbbi.core.observability.ContentType
import com.arrazyfathan.kbbi.core.observability.CrashReporter
import com.arrazyfathan.kbbi.core.observability.EventSource
import com.arrazyfathan.kbbi.core.observability.ReminderKind
import com.arrazyfathan.kbbi.core.observability.WidgetKind
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.core.presentation.designsystem.perform
import com.arrazyfathan.kbbi.core.presentation.ui.LocalAppLoadingController
import com.arrazyfathan.kbbi.core.presentation.ui.rememberAppLoadingController
import com.arrazyfathan.kbbi.core.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarkRoute
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailRoute
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeRoute
import com.arrazyfathan.kbbi.feature.proverb.presentation.navigation.ProverbRoute
import com.arrazyfathan.kbbi.feature.settings.presentation.legal.PrivacyPolicyScreen
import com.arrazyfathan.kbbi.feature.settings.presentation.legal.TermsConditionsScreen
import com.arrazyfathan.kbbi.feature.settings.presentation.settings.SettingsRoute
import com.arrazyfathan.kbbi.feature.words.presentation.navigation.WordsRoute
import com.arrazyfathan.kbbi.intent.NotificationLaunchRequest
import com.arrazyfathan.kbbi.ui.AppUiViewModel
import com.arrazyfathan.kbbi.widgets.WidgetLaunchRequest
import com.github.skydoves.navgraph.annotations.NavGraphRoot
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private const val IOS_NAVIGATION_TRANSITION_DURATION_MILLIS = 350
private const val IOS_NAVIGATION_PARALLAX_DIVISOR = 3
private const val BOTTOM_NAVIGATION_TRANSITION_DURATION_MILLIS = 220

sealed interface Screen : NavKey {
    @NavGraphRoot
    @Serializable
    data object Home : Screen

    @Serializable
    data object WordList : Screen

    @Serializable
    data object Proverb : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object Bookmarks : Screen
}

private val Screen.titleResId: Int
    get() =
        when (this) {
            Screen.Home -> R.string.home_title
            Screen.WordList -> R.string.word_list_tab_title
            Screen.Proverb -> R.string.proverb_title
            Screen.Settings -> R.string.settings_title
            Screen.Bookmarks -> R.string.bookmarks_title
        }

private val Screen.iconResId: Int
    get() =
        when (this) {
            Screen.Home -> R.drawable.home
            Screen.WordList -> R.drawable.word
            Screen.Proverb -> R.drawable.ic_proverb
            Screen.Settings -> R.drawable.settings
            Screen.Bookmarks -> R.drawable.saved
        }

private val Screen.iconSelectedResId: Int
    get() =
        when (this) {
            Screen.Home -> R.drawable.home_selected
            Screen.WordList -> R.drawable.word_selected
            Screen.Proverb -> R.drawable.ic_proverb
            Screen.Settings -> R.drawable.settings
            Screen.Bookmarks -> R.drawable.saved_selected
        }

@Serializable
data class DetailNavRoute(
    val dataJson: String,
) : NavKey

@Serializable
data object OpenSourceLicensesRoute : NavKey

@Serializable
data object PrivacyPolicyRoute : NavKey

@Serializable
data object TermsConditionsRoute : NavKey

@Immutable
internal data class MainAppLaunchRequests(
    val externalSearchQuery: String? = null,
    val externalSearchRequestKey: Long = 0L,
    val shortcutRequest: AppShortcutRequest? = null,
    val shortcutRequestKey: Long = 0L,
    val notificationRequest: NotificationLaunchRequest? = null,
    val notificationRequestKey: Long = 0L,
    val widgetRequest: WidgetLaunchRequest? = null,
    val widgetRequestKey: Long = 0L,
)

@Composable
internal fun MainApp(
    launchRequests: MainAppLaunchRequests = MainAppLaunchRequests(),
    onExternalSearchConsumed: () -> Unit = {},
    onShortcutConsumed: () -> Unit = {},
    onNotificationRequestConsumed: () -> Unit = {},
    onWidgetRequestConsumed: () -> Unit = {},
) {
    val appUpdateViewModel: AppUpdateViewModel = koinViewModel()
    val appUiViewModel: AppUiViewModel = koinViewModel()
    val analyticsReporter: AnalyticsReporter = koinInject()
    val crashReporter: CrashReporter = koinInject()
    val externalSearchQuery = launchRequests.externalSearchQuery
    val externalSearchRequestKey = launchRequests.externalSearchRequestKey
    val shortcutRequest = launchRequests.shortcutRequest
    val shortcutRequestKey = launchRequests.shortcutRequestKey
    val notificationRequest = launchRequests.notificationRequest
    val notificationRequestKey = launchRequests.notificationRequestKey
    val widgetRequest = launchRequests.widgetRequest
    val widgetRequestKey = launchRequests.widgetRequestKey
    val widgetSearchQuery =
        when (widgetRequest) {
            is WidgetLaunchRequest.WordOfDay -> widgetRequest.word
            is WidgetLaunchRequest.SavedWord -> widgetRequest.word
            WidgetLaunchRequest.QuickSearch,
            null,
            -> null
        }
    val effectiveExternalSearchQuery = externalSearchQuery ?: widgetSearchQuery
    val effectiveExternalSearchRequestKey =
        if (externalSearchQuery != null) externalSearchRequestKey else widgetRequestKey
    val context = LocalContext.current
    val platformHapticFeedback = LocalHapticFeedback.current
    val screens =
        listOf(
            Screen.Home,
            Screen.WordList,
            Screen.Bookmarks,
        )
    val navigationState =
        rememberNavigationState(
            startRoute = Screen.Home,
            topLevelRoutes = screens,
        )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val currentRoute = navigationState.currentRoute
    val isDetailVisible = currentRoute is DetailNavRoute
    val showBottomNavigation = screens.any { screen -> currentRoute == screen }
    val loadingController = rememberAppLoadingController()
    val routeJson =
        remember {
            Json {
                ignoreUnknownKeys = true
            }
        }
    val isUiBlocked by remember {
        derivedStateOf { loadingController.isBlocking }
    }
    val appUpdateState by appUpdateViewModel.state.collectAsStateWithLifecycle()
    val appUiState by appUiViewModel.state.collectAsStateWithLifecycle()
    val primarySystemBarColor = MaterialTheme.colorScheme.primary.toArgb()
    val backgroundSystemBarColor = MaterialTheme.colorScheme.background.toArgb()
    val performHaptic: (KBBIHapticType) -> Unit =
        remember(appUiState.hapticsEnabled, platformHapticFeedback) {
            { type ->
                if (appUiState.hapticsEnabled) {
                    platformHapticFeedback.perform(type)
                }
            }
        }

    LaunchedEffect(effectiveExternalSearchRequestKey) {
        if (effectiveExternalSearchQuery != null) {
            navigator.navigateToRoot(Screen.Home)
        }
    }

    LaunchedEffect(widgetRequestKey) {
        when (widgetRequest) {
            WidgetLaunchRequest.QuickSearch ->
                analyticsReporter.log(
                    AnalyticsEvent.WidgetOpened(WidgetKind.QuickSearch),
                )
            is WidgetLaunchRequest.WordOfDay ->
                analyticsReporter.log(
                    AnalyticsEvent.WidgetOpened(WidgetKind.WordOfDay),
                )
            is WidgetLaunchRequest.SavedWord ->
                analyticsReporter.log(
                    AnalyticsEvent.WidgetOpened(WidgetKind.SavedWord),
                )
            null -> Unit
        }
        when (widgetRequest) {
            WidgetLaunchRequest.QuickSearch -> navigator.navigateToRoot(Screen.Home)
            is WidgetLaunchRequest.WordOfDay -> {
                navigator.navigateToRoot(Screen.Home)
                if (widgetRequest.word == null) onWidgetRequestConsumed()
            }
            is WidgetLaunchRequest.SavedWord -> {
                if (widgetRequest.word == null) {
                    navigator.navigateToRoot(Screen.Bookmarks)
                    onWidgetRequestConsumed()
                } else {
                    navigator.navigateToRoot(Screen.Home)
                }
            }
            null -> {}
        }
    }

    LaunchedEffect(shortcutRequestKey) {
        when (shortcutRequest) {
            AppShortcutRequest.Search,
            AppShortcutRequest.RandomWord,
            -> {
                navigator.navigateToRoot(Screen.Home)
            }

            AppShortcutRequest.Bookmarks -> {
                navigator.navigateToRoot(Screen.Bookmarks)
                onShortcutConsumed()
            }

            AppShortcutRequest.Proverbs -> {
                navigator.navigateToRoot(Screen.Home)
                navigator.navigate(Screen.Proverb)
                onShortcutConsumed()
            }

            null -> {}
        }
    }

    LaunchedEffect(notificationRequestKey) {
        when (notificationRequest) {
            is NotificationLaunchRequest.Proverb -> {
                analyticsReporter.log(AnalyticsEvent.NotificationOpened(ReminderKind.DailyProverb))
                navigator.navigateToRoot(Screen.Home)
                navigator.navigate(Screen.Proverb)
                onNotificationRequestConsumed()
            }

            NotificationLaunchRequest.Bookmarks -> {
                analyticsReporter.log(AnalyticsEvent.NotificationOpened(ReminderKind.BookmarkReview))
                navigator.navigateToRoot(Screen.Bookmarks)
                onNotificationRequestConsumed()
            }

            null -> return@LaunchedEffect
        }
    }

    LaunchedEffect(currentRoute) {
        currentRoute.toAnalyticsScreen()?.let { screen ->
            analyticsReporter.screenViewed(screen)
            crashReporter.setKey("current_screen", screen.value)
        }
    }

    LaunchedEffect(currentRoute, primarySystemBarColor, backgroundSystemBarColor) {
        val activity = context.findActivity() ?: return@LaunchedEffect
        val statusBarColor =
            if (isDetailVisible) {
                backgroundSystemBarColor
            } else {
                primarySystemBarColor
            }
        activity.updateSystemBarStyle(
            statusBarColor,
            backgroundSystemBarColor,
        )
    }

    LaunchedEffect(Unit) {
        appUpdateViewModel.onAction(AppUpdateAction.OnAppStarted)
    }

    val entries =
        navigationState.toEntries(
            entryProvider {
                entry<Screen.Home> {
                    HomeRoute(
                        onHaptic = performHaptic,
                        externalSearchQuery = effectiveExternalSearchQuery,
                        externalSearchRequestKey = effectiveExternalSearchRequestKey,
                        onExternalSearchConsumed = {
                            if (widgetSearchQuery != null) onWidgetRequestConsumed() else onExternalSearchConsumed()
                        },
                        focusSearchRequestKey =
                            when {
                                widgetRequest == WidgetLaunchRequest.QuickSearch -> widgetRequestKey
                                shortcutRequest == AppShortcutRequest.Search -> shortcutRequestKey
                                else -> 0L
                            },
                        randomWordRequestKey =
                            if (shortcutRequest == AppShortcutRequest.RandomWord) {
                                shortcutRequestKey
                            } else {
                                0L
                            },
                        onShortcutConsumed = {
                            if (widgetRequest == WidgetLaunchRequest.QuickSearch) {
                                onWidgetRequestConsumed()
                            } else {
                                onShortcutConsumed()
                            }
                        },
                        onNavigateToDetail = { word ->
                            navigator.navigate(DetailNavRoute(routeJson.encodeToString(word)))
                        },
                        onNavigateToProverb = {
                            navigator.navigate(Screen.Proverb)
                        },
                        onNavigateToSettings = {
                            navigator.navigate(Screen.Settings)
                        },
                    )
                }
                entry<Screen.WordList> {
                    WordsRoute(
                        onHaptic = performHaptic,
                        onNavigateToDetail = { word ->
                            navigator.navigate(DetailNavRoute(routeJson.encodeToString(word)))
                        },
                    )
                }
                entry<Screen.Proverb> {
                    ProverbRoute(
                        onHaptic = performHaptic,
                        onNavigateBack = {
                            if (!isUiBlocked) {
                                navigator.goBack()
                            }
                        },
                    )
                }
                entry<Screen.Settings> {
                    SettingsRoute(
                        onHaptic = performHaptic,
                        onNavigateBack = { if (!isUiBlocked) navigator.goBack() },
                        onOpenPrivacyPolicy = {
                            navigator.navigate(PrivacyPolicyRoute)
                        },
                        onOpenTermsConditions = {
                            navigator.navigate(TermsConditionsRoute)
                        },
                        onOpenSourceLicenses = {
                            navigator.navigate(OpenSourceLicensesRoute)
                        },
                    )
                }
                entry<Screen.Bookmarks> {
                    BookmarkRoute(
                        onHaptic = performHaptic,
                        onNavigateToDetail = { word ->
                            analyticsReporter.log(
                                AnalyticsEvent.ContentOpened(ContentType.Word, EventSource.Bookmarks),
                            )
                            navigator.navigate(DetailNavRoute(routeJson.encodeToString(word)))
                        },
                    )
                }
                entry<DetailNavRoute> { route ->
                    val listWordModel =
                        remember(route.dataJson) {
                            routeJson.decodeFromString<ListWordModel>(route.dataJson)
                        }
                    DetailRoute(
                        listWordModel = listWordModel,
                        onHaptic = performHaptic,
                    )
                }
                entry<OpenSourceLicensesRoute> {
                    OpenSourceLicensesScreen(
                        onNavigateBack = { if (!isUiBlocked) navigator.goBack() },
                    )
                }
                entry<PrivacyPolicyRoute> {
                    PrivacyPolicyScreen(
                        onNavigateBack = { if (!isUiBlocked) navigator.goBack() },
                    )
                }
                entry<TermsConditionsRoute> {
                    TermsConditionsScreen(
                        onNavigateBack = { if (!isUiBlocked) navigator.goBack() },
                    )
                }
            },
        )

    CompositionLocalProvider(LocalAppLoadingController provides loadingController) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                entries = entries,
                onBack = {
                    if (!isUiBlocked) {
                        navigator.goBack()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                transitionSpec = { appNavigationTransition(showBottomNavigation) },
                popTransitionSpec = { appPopNavigationTransition(showBottomNavigation) },
                predictivePopTransitionSpec = { appPopNavigationTransition(showBottomNavigation) },
            )

            if (showBottomNavigation) {
                Row(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(70.dp)
                            .shadow(elevation = 16.dp)
                            .background(MaterialTheme.colorScheme.surface),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    screens.forEach { screen ->
                        val isSelected = navigationState.topLevelRoute == screen
                        Box(
                            modifier =
                                Modifier.weight(1f).fillMaxHeight().clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication =
                                        ripple(
                                            bounded = false,
                                            radius = 24.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                        ),
                                ) {
                                    if (!isUiBlocked && !isSelected) {
                                        navigator.navigate(screen)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter =
                                    painterResource(
                                        id = if (isSelected) screen.iconSelectedResId else screen.iconResId,
                                    ),
                                contentDescription = stringResource(screen.titleResId),
                                tint =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                    },
                            )
                        }
                    }
                }
            }

            appUpdateState.availableUpdate?.let { update ->
                AppUpdatePrompt(
                    update = update,
                    currentVersion = appUpdateState.currentVersion,
                    onDismiss = {
                        appUpdateViewModel.onAction(AppUpdateAction.OnPromptDismissed)
                    },
                )
            }

            if (isUiBlocked) {
                BlockingLoadingOverlay()
            }
        }
    }
}

private fun NavKey.toAnalyticsScreen(): AnalyticsScreen? =
    when (this) {
        Screen.Home -> AnalyticsScreen.Home
        Screen.WordList -> AnalyticsScreen.Words
        Screen.Proverb -> AnalyticsScreen.Proverbs
        Screen.Settings -> AnalyticsScreen.Settings
        Screen.Bookmarks -> AnalyticsScreen.Bookmarks
        is DetailNavRoute -> AnalyticsScreen.WordDetail
        PrivacyPolicyRoute -> AnalyticsScreen.PrivacyPolicy
        TermsConditionsRoute -> AnalyticsScreen.TermsConditions
        OpenSourceLicensesRoute -> AnalyticsScreen.OpenSourceLicenses
        else -> null
    }

@Composable
private fun BlockingLoadingOverlay() {
    Box(
        modifier =
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            val searchLoadingComposition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.loading_search),
            )
            LottieAnimation(
                composition = searchLoadingComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun rememberNavigationState(
    startRoute: Screen,
    topLevelRoutes: List<Screen>,
): NavigationState {
    val topLevelRoute =
        rememberSerializable(
            startRoute,
            topLevelRoutes,
            serializer = MutableStateSerializer(NavKeySerializer()),
        ) {
            mutableStateOf<NavKey>(startRoute)
        }

    val backStacks: Map<NavKey, NavBackStack<NavKey>> =
        topLevelRoutes.associate { key ->
            key to rememberNavBackStack(key)
        }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
        )
    }
}

private class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var topLevelRoute: NavKey by topLevelRoute

    val currentRoute: NavKey
        get() = backStacks[topLevelRoute]?.lastOrNull() ?: topLevelRoute

    val stacksInUse: List<NavKey>
        get() =
            if (topLevelRoute == startRoute) {
                listOf(startRoute)
            } else {
                listOf(startRoute, topLevelRoute)
            }
}

private class Navigator(
    private val state: NavigationState,
) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun navigateToRoot(route: NavKey) {
        val stack = state.backStacks[route] ?: error("Stack for $route not found")
        while (stack.lastOrNull() != route) {
            if (stack.removeLastOrNull() == null) {
                stack.add(route)
                break
            }
        }
        state.topLevelRoute = route
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}

@Composable
private fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries =
        backStacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                entryProvider = entryProvider,
            )
        }

    return stacksInUse.flatMap { decoratedEntries[it] ?: emptyList() }.toMutableStateList()
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun appNavigationTransition(showBottomNavigation: Boolean): ContentTransform =
    if (showBottomNavigation) {
        bottomNavigationTransition()
    } else {
        iosNavigationTransition()
    }

private fun appPopNavigationTransition(showBottomNavigation: Boolean): ContentTransform =
    if (showBottomNavigation) {
        bottomNavigationTransition()
    } else {
        iosPopNavigationTransition()
    }

private fun bottomNavigationTransition(): ContentTransform {
    val animationSpec = tween<Float>(durationMillis = BOTTOM_NAVIGATION_TRANSITION_DURATION_MILLIS)

    return fadeIn(animationSpec = animationSpec) togetherWith fadeOut(animationSpec = animationSpec)
}

private fun iosNavigationTransition(): ContentTransform {
    val animationSpec = tween<IntOffset>(durationMillis = IOS_NAVIGATION_TRANSITION_DURATION_MILLIS)

    return slideInHorizontally(
        animationSpec = animationSpec,
        initialOffsetX = { fullWidth -> fullWidth },
    ) togetherWith
        slideOutHorizontally(
            animationSpec = animationSpec,
            targetOffsetX = { fullWidth -> -fullWidth / IOS_NAVIGATION_PARALLAX_DIVISOR },
        )
}

private fun iosPopNavigationTransition(): ContentTransform {
    val animationSpec = tween<IntOffset>(durationMillis = IOS_NAVIGATION_TRANSITION_DURATION_MILLIS)

    return slideInHorizontally(
        animationSpec = animationSpec,
        initialOffsetX = { fullWidth -> -fullWidth / IOS_NAVIGATION_PARALLAX_DIVISOR },
    ) togetherWith
        slideOutHorizontally(
            animationSpec = animationSpec,
            targetOffsetX = { fullWidth -> fullWidth },
        )
}
