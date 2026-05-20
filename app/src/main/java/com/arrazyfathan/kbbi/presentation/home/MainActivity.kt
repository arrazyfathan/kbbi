package com.arrazyfathan.kbbi.presentation.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.presentation.bookmark.BookmarksScreen
import com.arrazyfathan.kbbi.presentation.theme.KBBITheme
import com.arrazyfathan.kbbi.presentation.words.WordListScreen
import com.arrazyfathan.kbbi.utils.enableEdgeToEdgeSystemBars
import com.arrazyfathan.kbbi.utils.updateSystemBarStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSystemBars()
        updateSystemBarStyle(
            ContextCompat.getColor(this, R.color.blue_primary),
            ContextCompat.getColor(this, android.R.color.white),
        )

        setContent {
            KBBITheme {
                MainApp()
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val titleResId: Int,
    val iconResId: Int,
    val iconSelectedResId: Int,
) {
    object Home : Screen("home", R.string.home_title, R.drawable.home, R.drawable.home_selected)

    object WordList : Screen("word_list", R.string.word_list_tab_title, R.drawable.word, R.drawable.word_selected)

    object Bookmarks : Screen("bookmarks", R.string.bookmarks_title, R.drawable.saved, R.drawable.saved_selected)
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens =
        listOf(
            Screen.Home,
            Screen.WordList,
            Screen.Bookmarks,
        )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .background(Color.White),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false, radius = 24.dp),
                                ) {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
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
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.WordList.route) {
                WordListScreen()
            }
            composable(Screen.Bookmarks.route) {
                BookmarksScreen()
            }
        }
    }
}
