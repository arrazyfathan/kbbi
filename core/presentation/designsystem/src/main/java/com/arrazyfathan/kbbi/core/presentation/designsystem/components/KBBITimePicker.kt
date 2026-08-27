package com.arrazyfathan.kbbi.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.math.abs

private val PickerItemHeight = 48.dp
private val PickerContentPadding = PickerItemHeight * 2

internal const val HOUR_WHEEL_TEST_TAG = "kbbi_hour_wheel"
internal const val MINUTE_WHEEL_TEST_TAG = "kbbi_minute_wheel"
internal const val CANCEL_TIME_PICKER_TEST_TAG = "kbbi_time_picker_cancel"
internal const val DONE_TIME_PICKER_TEST_TAG = "kbbi_time_picker_done"

/**
 * A reusable 24-hour wheel picker. The caller owns the selected time.
 */
@Composable
fun KBBIWheelTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(hour in 0..23) { "hour must be between 0 and 23" }
    require(minute in 0..59) { "minute must be between 0 and 59" }

    Box(
        modifier = modifier.fillMaxWidth().height(PickerItemHeight * 5),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier =
                    Modifier.fillMaxWidth().height(PickerItemHeight).clip(ShapeDefaults.Small).background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.primary,
                                    ),
                            ),
                    ),
            )
        }

        Row(
            modifier = Modifier.width(248.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WheelColumn(
                values = 0..23,
                selectedValue = hour,
                optionDescription = { stringResource(R.string.time_picker_hour_option, it) },
                onValueSelected = { onTimeChange(it, minute) },
                modifier = Modifier.width(96.dp).testTag(HOUR_WHEEL_TEST_TAG),
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            WheelColumn(
                values = 0..59,
                selectedValue = minute,
                optionDescription = { stringResource(R.string.time_picker_minute_option, it) },
                onValueSelected = { onTimeChange(hour, it) },
                modifier = Modifier.width(96.dp).testTag(MINUTE_WHEEL_TEST_TAG),
            )
        }
    }
}

@Composable
private fun WheelColumn(
    values: IntRange,
    selectedValue: Int,
    optionDescription: @Composable (Int) -> String,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedValue - values.first)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val latestOnValueSelected by rememberUpdatedState(onValueSelected)

    LaunchedEffect(selectedValue, listState, values) {
        snapshotFlow { listState.isScrollInProgress }.first { isScrolling -> !isScrolling }
        val targetIndex = selectedValue - values.first
        if (listState.layoutInfo.centeredItemIndex() != targetIndex) {
            listState.scrollToItem(targetIndex)
        }
    }

    LaunchedEffect(listState, values) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                layoutInfo.centeredItemIndex()?.let { values.first + it }
            }.filterNotNull()
            .distinctUntilChanged()
            .collect { latestOnValueSelected(it) }
    }

    LazyColumn(
        modifier = modifier.height(PickerItemHeight * 5),
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(vertical = PickerContentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(
            items = values.toList(),
            key = { _, value -> value },
        ) { _, value ->
            val isSelected = value == selectedValue
            val description = optionDescription(value)
            Box(
                modifier =
                    Modifier.fillMaxWidth().height(PickerItemHeight).semantics {
                        selected = isSelected
                        contentDescription = description
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "%02d".format(value),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    fontSize = if (isSelected) 22.sp else 18.sp,
                    modifier = Modifier.alpha(if (isSelected) 1f else 0.38f),
                )
            }
        }
    }
}

private fun LazyListLayoutInfo.centeredItemIndex(): Int? {
    val viewportCenter = (viewportStartOffset + viewportEndOffset) / 2
    return visibleItemsInfo.minByOrNull { item -> abs((item.offset + item.size / 2) - viewportCenter) }?.index
}

/**
 * A modal time picker that keeps wheel changes as a draft until the user presses Done.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KBBITimePickerBottomSheet(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.time_picker_title),
) {
    require(initialHour in 0..23) { "initialHour must be between 0 and 23" }
    require(initialMinute in 0..59) { "initialMinute must be between 0 and 59" }

    var draftHour by rememberSaveable(initialHour) { mutableIntStateOf(initialHour) }
    var draftMinute by rememberSaveable(initialMinute) { mutableIntStateOf(initialMinute) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        TimePickerSheetContent(
            title = title,
            hour = draftHour,
            minute = draftMinute,
            onTimeChange = { hour, minute ->
                draftHour = hour
                draftMinute = minute
            },
            onCancel = onDismissRequest,
            onDone = { onConfirm(draftHour, draftMinute) },
        )
    }
}

@Composable
private fun TimePickerSheetContent(
    title: String,
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.CenterStart).testTag(CANCEL_TIME_PICKER_TEST_TAG),
            ) {
                Text(stringResource(R.string.cancel))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(
                onClick = onDone,
                modifier = Modifier.align(Alignment.CenterEnd).testTag(DONE_TIME_PICKER_TEST_TAG),
            ) {
                Text(stringResource(R.string.done))
            }
        }
        KBBIWheelTimePicker(
            hour = hour,
            minute = minute,
            onTimeChange = onTimeChange,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KBBIWheelTimePickerPreview() {
    KBBITheme {
        KBBIWheelTimePicker(
            hour = 9,
            minute = 30,
            onTimeChange = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KBBITimePickerSheetContentPreview() {
    KBBITheme {
        TimePickerSheetContent(
            title = stringResource(R.string.time_picker_title),
            hour = 19,
            minute = 15,
            onTimeChange = { _, _ -> },
            onCancel = {},
            onDone = {},
        )
    }
}
