package com.arrazyfathan.kbbi.core.presentation.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class KbbiWindowWidthSizeClass {
    Compact,
    Medium,
    Expanded,
}

fun kbbiWindowWidthSizeClass(width: Dp): KbbiWindowWidthSizeClass =
    when {
        width < 600.dp -> KbbiWindowWidthSizeClass.Compact
        width < 840.dp -> KbbiWindowWidthSizeClass.Medium
        else -> KbbiWindowWidthSizeClass.Expanded
    }

@Composable
fun KbbiAdaptiveContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 720.dp,
    content: @Composable (KbbiWindowWidthSizeClass) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val widthSizeClass = kbbiWindowWidthSizeClass(this.maxWidth)
        Box(
            modifier = Modifier.fillMaxWidth().widthIn(max = maxWidth).align(Alignment.TopCenter),
        ) {
            content(widthSizeClass)
        }
    }
}

@Preview(name = "Phone portrait", widthDp = 390, heightDp = 844, showBackground = true)
@Preview(name = "Phone landscape", widthDp = 844, heightDp = 390, showBackground = true)
@Preview(name = "Folded foldable", widthDp = 673, heightDp = 841, showBackground = true)
@Preview(name = "Unfolded foldable", widthDp = 841, heightDp = 673, showBackground = true)
@Preview(name = "Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
annotation class KbbiFormFactorPreviews

@Preview(name = "Compact", widthDp = 390, heightDp = 844, showBackground = true)
@Preview(name = "Expanded", widthDp = 1280, heightDp = 800, showBackground = true)
annotation class KbbiCompactExpandedPreviews
