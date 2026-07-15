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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import com.arrazyfathan.kbbi.core.presentation.ui.LocalAppLoadingController
import com.arrazyfathan.kbbi.core.presentation.ui.rememberAppLoadingController
import com.arrazyfathan.kbbi.core.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarkRoute
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailRoute
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeRoute
import com.arrazyfathan.kbbi.feature.proverb.presentation.navigation.ProverbRoute
import com.arrazyfathan.kbbi.feature.words.presentation.navigation.WordsRoute
import com.github.skydoves.navgraph.annotations.NavGraphRoot
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel

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
    data object Bookmarks : Screen
}

private val Screen.titleResId: Int
    get() =
        when (this) {
            Screen.Home -> R.string.home_title
            Screen.WordList -> R.string.word_list_tab_title
            Screen.Proverb -> R.string.proverb_title
            Screen.Bookmarks -> R.string.bookmarks_title
        }

private val Screen.iconResId: Int
    get() =
        when (this) {
            Screen.Home -> R.drawable.home
            Screen.WordList -> R.drawable.word
            Screen.Proverb -> R.drawable.ic_proverb
            Screen.Bookmarks -> R.drawable.saved
        }

private val Screen.iconSelectedResId: Int
    get() =
        when (this) {
            Screen.Home -> R.drawable.home_selected
            Screen.WordList -> R.drawable.word_selected
            Screen.Proverb -> R.drawable.ic_proverb
            Screen.Bookmarks -> R.drawable.saved_selected
        }

@Serializable
data class DetailNavRoute(
    val dataJson: String,
) : NavKey

@Composable
fun MainApp(
    externalSearchQuery: String? = null,
    externalSearchRequestKey: Long = 0L,
    onExternalSearchConsumed: () -> Unit = {},
    shortcutRequest: AppShortcutRequest? = null,
    shortcutRequestKey: Long = 0L,
    onShortcutConsumed: () -> Unit = {},
    appUpdateViewModel: AppUpdateViewModel = koinViewModel(),
) {
    val context = LocalContext.current
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

    LaunchedEffect(externalSearchRequestKey) {
        if (externalSearchQuery != null) {
            navigator.navigateToRoot(Screen.Home)
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

            null -> {
                Unit
            }
        }
    }

    LaunchedEffect(currentRoute) {
        val activity = context.findActivity() ?: return@LaunchedEffect
        val colorResId =
            if (isDetailVisible) {
                R.color.blue_bg
            } else {
                R.color.blue_primary
            }
        activity.updateSystemBarStyle(
            ContextCompat.getColor(activity, colorResId),
            ContextCompat.getColor(activity, android.R.color.white),
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
                        externalSearchQuery = externalSearchQuery,
                        externalSearchRequestKey = externalSearchRequestKey,
                        onExternalSearchConsumed = onExternalSearchConsumed,
                        focusSearchRequestKey =
                            if (shortcutRequest == AppShortcutRequest.Search) {
                                shortcutRequestKey
                            } else {
                                0L
                            },
                        randomWordRequestKey =
                            if (shortcutRequest == AppShortcutRequest.RandomWord) {
                                shortcutRequestKey
                            } else {
                                0L
                            },
                        onShortcutConsumed = onShortcutConsumed,
                        onNavigateToDetail = { word ->
                            navigator.navigate(DetailNavRoute(routeJson.encodeToString(word)))
                        },
                        onNavigateToProverb = {
                            navigator.navigate(Screen.Proverb)
                        },
                    )
                }
                entry<Screen.WordList> {
                    WordsRoute(
                        onNavigateToDetail = { word ->
                            navigator.navigate(DetailNavRoute(routeJson.encodeToString(word)))
                        },
                    )
                }
                entry<Screen.Proverb> {
                    ProverbRoute(
                        onNavigateBack = {
                            if (!isUiBlocked) {
                                navigator.goBack()
                            }
                        },
                    )
                }
                entry<Screen.Bookmarks> {
                    BookmarkRoute(
                        onNavigateToDetail = { word ->
                            navigator.navigate(DetailNavRoute(routeJson.encodeToString(word)))
                        },
                    )
                }
                entry<DetailNavRoute> { route ->
                    val listWordModel =
                        remember(route.dataJson) {
                            routeJson.decodeFromString<ListWordModel>(route.dataJson)
                        }
                    DetailRoute(listWordModel = listWordModel)
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
                            .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    screens.forEach { screen ->
                        val isSelected = navigationState.topLevelRoute == screen
                        Box(
                            modifier =
                                Modifier.weight(1f).fillMaxHeight().clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false, radius = 24.dp),
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
                                contentDescription = null,
                                tint = Color.Unspecified,
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
