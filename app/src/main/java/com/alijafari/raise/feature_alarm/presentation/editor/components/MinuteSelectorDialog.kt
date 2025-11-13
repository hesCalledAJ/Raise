package com.alijafari.raise.feature_alarm.presentation.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alijafari.raise.R
import com.alijafari.raise.core.ui.components.PopupDialog
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MinuteSelectorDialog(
    isVisible: Boolean,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onSave: (minutes: Int) -> Unit,
) {
    var selectedMinutes by remember { mutableIntStateOf(initialMinute) }
    PopupDialog(
        isVisible = isVisible,
        title = "Snooze Duration",
        onDismiss = onDismiss,
        positiveButton = stringResource(R.string.save) to { onSave(selectedMinutes) }
    ) {
        Text(
            stringResource(R.string.n_minutes, selectedMinutes)
        )
        Icon(painterResource(R.drawable.ic_triangle), null)
        MinuteRulerPicker(
            initialMinute = initialMinute
        ) {
            selectedMinutes = it
        }
    }
}

@Composable
fun MinuteRulerPicker(
    modifier: Modifier = Modifier,
    initialMinute: Int = 0,
    itemWidth: Dp = 30.dp,
    tickHeight: Dp = 30.dp,
    maxMinutes: Int = 30,
    onMinuteSelected: (Int) -> Unit,
) {
    val minutes = (0..maxMinutes).toList()
    val listState =
        rememberLazyListState(initialFirstVisibleItemIndex = initialMinute.coerceIn(0, maxMinutes))
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var containerWidthPx by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { containerWidthPx = it.width.toFloat() } // container width in px
    ) {
        val itemWidthPx = with(density) { itemWidth.toPx() }
        val centerPadding =
            with(density) { ((containerWidthPx.toDp() - itemWidth) / 2).coerceAtLeast(0.dp) }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = centerPadding),
            horizontalArrangement = Arrangement.Center
        ) {
            items(minutes) { minute ->
                Column(
                    modifier = Modifier
                        .width(itemWidth)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isMajor5 = minute % 5 == 0
                    val h = when {
                        isMajor5 -> tickHeight * 0.8f
                        else -> tickHeight * 0.4f
                    }

                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(h)
                            .background(
                                MaterialTheme.colorScheme.onSurface,
                                RoundedCornerShape(1.dp)
                            )
                    )

                    Spacer(Modifier.height(6.dp))

                    if (isMajor5) {
                        Text(
                            text = minute.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }

        // Snap animation
        LaunchedEffect(listState.isScrollInProgress, containerWidthPx) {
            snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
                if (!scrolling && containerWidthPx > 0) {
                    val firstVisible = listState.firstVisibleItemIndex
                    val offset = listState.firstVisibleItemScrollOffset
                    val contentPaddingPx = with(density) { centerPadding.toPx() }
                    val currentScrollPx = firstVisible * itemWidthPx + offset - contentPaddingPx
                    val targetIndex =
                        ((currentScrollPx + containerWidthPx / 2f - itemWidthPx / 2f) / itemWidthPx)
                            .roundToInt()
                            .coerceIn(0, 59)

                    coroutineScope.launch {
                        listState.animateScrollToItem(targetIndex)
                        onMinuteSelected(targetIndex)
                    }
                }
            }
        }
    }
}


