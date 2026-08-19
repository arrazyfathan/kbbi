package com.arrazyfathan.kbbi.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueSecondary
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.KbbiCompactExpandedPreviews
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.style.m3VariantColors
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrariesVariant
import com.arrazyfathan.kbbi.core.R as CoreR

@Composable
fun OpenSourceLicensesScreen(onNavigateBack: () -> Unit) {
    val resources = LocalResources.current
    val libraries by produceLibraries {
        resources
            .openRawResource(R.raw.aboutlibraries)
            .bufferedReader()
            .use { reader -> reader.readText() }
    }

    OpenSourceLicensesContent(
        libraries = libraries,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpenSourceLicensesContent(
    libraries: Libs?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
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
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LibrariesContainer(
                modifier = Modifier.fillMaxSize().widthIn(max = 840.dp),
                libraries = libraries,
                variant = LibrariesVariant.Traditional,
                divider = { HorizontalDivider(thickness = 0.5.dp) },
                variantColors =
                    LibraryDefaults.m3VariantColors(
                        rowExpandedBackground = Color.White,
                    ),
            )
        }
    }
}

@KbbiCompactExpandedPreviews
@Composable
fun OpenSourceLicensesScreenPreview() {
    val sampleJson =
        """
        {
            "libraries": [
                {
                    "uniqueId": "androidx.compose.ui:ui",
                    "artifactVersion": "1.7.0",
                    "name": "Compose UI",
                    "description": "Compose UI components",
                    "website": "https://developer.android.com/jetpack/compose",
                    "licenses": ["Apache-2.0"]
                },
                {
                    "uniqueId": "com.mikepenz:aboutlibraries",
                    "artifactVersion": "15.0.4",
                    "name": "AboutLibraries",
                    "description": "AboutLibraries library",
                    "website": "https://github.com/mikepenz/AboutLibraries",
                    "licenses": ["Apache-2.0"]
                }
            ],
            "licenses": {
                "Apache-2.0": {
                    "name": "Apache License 2.0",
                    "url": "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
        }
        """.trimIndent()

    val sampleLibs =
        remember {
            Libs.Builder().withJson(sampleJson).build()
        }

    KBBITheme {
        OpenSourceLicensesContent(
            libraries = sampleLibs,
            onNavigateBack = {},
        )
    }
}
