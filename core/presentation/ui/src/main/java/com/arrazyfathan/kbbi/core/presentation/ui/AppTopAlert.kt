package com.arrazyfathan.kbbi.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1

enum class AppAlertType {
    Warning,
    Success,
    Failed,
}

data class AppAlertState(
    val message: UiText,
    val type: AppAlertType,
)

@Composable
fun AppTopAlert(
    state: AppAlertState?,
    modifier: Modifier = Modifier,
) {
    if (state == null) return

    val containerColor =
        when (state.type) {
            AppAlertType.Warning -> Color(0xFFFFF3CD)
            AppAlertType.Success -> TextH1
            AppAlertType.Failed -> Color(0xFFF3254F)
        }
    val contentColor =
        when (state.type) {
            AppAlertType.Warning -> Color(0xFF6B4E00)
            AppAlertType.Success, AppAlertType.Failed -> Color.White
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .background(containerColor)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.message.asString(),
                    color = contentColor,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}
