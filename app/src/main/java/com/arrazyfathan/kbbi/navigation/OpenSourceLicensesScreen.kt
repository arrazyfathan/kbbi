package com.arrazyfathan.kbbi.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueSecondary
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.arrazyfathan.kbbi.core.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreen(onNavigateBack: () -> Unit) {
    val resources = LocalResources.current
    val libraries by produceLibraries {
        resources
            .openRawResource(R.raw.aboutlibraries)
            .bufferedReader()
            .use { reader -> reader.readText() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BlueBg,
        topBar = {
            TopAppBar(
                modifier =
                    Modifier.background(
                        Brush.verticalGradient(listOf(BlueSecondary, BluePrimary)),
                    ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_arrow_back),
                            contentDescription = stringResource(CoreR.string.navigate_back),
                            tint = Color.White,
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(CoreR.string.open_source_licenses_title),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = MetropolisFontFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = Color.White,
                    ),
            )
        },
    ) { padding ->
        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}
