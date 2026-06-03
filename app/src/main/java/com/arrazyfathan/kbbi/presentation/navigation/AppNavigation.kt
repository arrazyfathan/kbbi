package com.arrazyfathan.kbbi.presentation.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.presentation.bookmark.BookmarksScreen
import com.arrazyfathan.kbbi.presentation.common.LocalAppLoadingController
import com.arrazyfathan.kbbi.presentation.common.rememberAppLoadingController
import com.arrazyfathan.kbbi.presentation.detail.DetailScreen
import com.arrazyfathan.kbbi.presentation.home.HomeScreen
import com.arrazyfathan.kbbi.presentation.words.WordListScreen
import com.arrazyfathan.kbbi.utils.updateSystemBarStyle
import com.google.gson.Gson
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {
    val titleResId: Int
    val iconResId: Int
    val iconSelectedResId: Int

    @Serializable
    data object Home : Screen {
        override val titleResId = R.string.home_title
        override val iconResId = R.drawable.home
        override val iconSelectedResId = R.drawable.home_selected
    }

    @Serializable
    data object WordList : Screen {
        override val titleResId = R.string.word_list_tab_title
        override val iconResId = R.drawable.word
        override val iconSelectedResId = R.drawable.word_selected
    }

    @Serializable
    data object Bookmarks : Screen {
        override val titleResId = R.string.bookmarks_title
        override val iconResId = R.drawable.saved
        override val iconSelectedResId = R.drawable.saved_selected
    }
}

@Serializable
private data class DetailRoute(
    val dataJson: String,
) : NavKey

@Composable
fun MainApp() {
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
    val isDetailVisible = currentRoute is DetailRoute
    val loadingController = rememberAppLoadingController()
    val isUiBlocked by remember {
        derivedStateOf { loadingController.isBlocking }
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

    val entries =
        navigationState.toEntries(
            entryProvider {
                entry<Screen.Home> {
                    HomeScreen(
                        onNavigateToDetail = { dataJson ->
                            navigator.navigate(DetailRoute(dataJson))
                        },
                    )
                }
                entry<Screen.WordList> {
                    WordListScreen(
                        onNavigateToDetail = { dataJson ->
                            navigator.navigate(DetailRoute(dataJson))
                        },
                    )
                }
                entry<Screen.Bookmarks> {
                    BookmarksScreen(
                        onNavigateToDetail = { dataJson ->
                            navigator.navigate(DetailRoute(dataJson))
                        },
                    )
                }
                entry<DetailRoute> { route ->
                    val listWordModel =
                        remember(route.dataJson) {
                            Gson().fromJson(route.dataJson, ListWordModel::class.java)
                        }
                    DetailScreen(listWordModel = listWordModel)
                }
            },
        )

    CompositionLocalProvider(LocalAppLoadingController provides loadingController) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (!isDetailVisible) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(70.dp).background(Color.White),
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
                },
            ) { innerPadding ->
                NavDisplay(
                    entries = entries,
                    onBack = {
                        if (!isUiBlocked) {
                            navigator.goBack()
                        }
                    },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
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
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .pointerInput(Unit) {
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
