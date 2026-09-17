package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP

@Composable
internal fun ExploreMenuContent(
    onNavigateToProverb: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.explore_menu_title),
            color = TextH1,
            fontSize = 20.sp,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(id = R.string.explore_menu_subtitle),
            color = TextP,
            fontSize = 14.sp,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ExploreMenuCard(
                icon = R.drawable.ic_proverb,
                title = stringResource(id = R.string.proverb_menu_title),
                subtitle = stringResource(id = R.string.proverb_menu_subtitle),
                onClick = onNavigateToProverb,
                modifier = Modifier.weight(1f),
            )

            ExploreMenuCard(
                icon = R.drawable.settings,
                title = stringResource(id = R.string.settings_menu_title),
                subtitle = stringResource(id = R.string.settings_menu_subtitle),
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f),
            )

            ExploreMenuPlaceholderCard(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ExploreMenuCard(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = TextH1,
                fontSize = 13.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = TextP,
                fontSize = 11.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun ExploreMenuPlaceholderCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {}
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ExploreMenuContentPreview() {
    KBBITheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ExploreMenuContent(
                onNavigateToProverb = {},
                onNavigateToSettings = {},
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
