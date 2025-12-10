package com.alijafari.raise.feature_alarm.presentation.ring

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alijafari.raise.core.ui.theme.Wakee2Theme
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.presentation.ring.components.DismissBottomSheet
import com.alijafari.raise.feature_alarm.presentation.ring.components.DraggableAlarmDataContainer
import com.alijafari.raise.feature_alarm.presentation.ring.components.SnoozeTopShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.max


enum class RingDragState {
    IDLE, DRAGGING_DOWN, DRAGGING_DOWN_DONE, DRAGGING_UP, DRAGGING_UP_DONE
}


private const val SNOOZE_THRESHOLD_DP = 140
private const val MAX_SHEET_FRACTION = 0.6f
private const val SHEET_STAY_OPEN_THRESHOLD = 0.8f
private const val DISMISS_DELAY_MS = 800L


class RingScreenState(
    private val scope: CoroutineScope,
    private val screenHeightPx: Float,
    private val dragThresholdPx: Float,
    private val maxSheetFraction: Float,
    private val onSnooze: () -> Unit,
    val onDismissOrSkip: () -> Unit
) {
    private val _dragOffset = Animatable(0f)
    private val _sheetFraction = Animatable(0f)
    // add these
    val dragOffsetState: State<Float> = derivedStateOf { _dragOffset.value }
    val sheetFractionState: State<Float> = derivedStateOf { _sheetFraction.value }


    var isDragging by mutableStateOf(false)

    val effectiveSheetFraction = derivedStateOf {
        val fraction = (-_dragOffset.value / screenHeightPx).coerceIn(0f, null)
        max(fraction, _sheetFraction.value)
    }

    val screenState = derivedStateOf {
        computeDragState(
            isDragging = isDragging,
            dragOffset = _dragOffset.value,
            sheetFraction = _sheetFraction.value,
            dragThresholdPx = dragThresholdPx,
            maxSheetFraction = maxSheetFraction
        )
    }

    fun onDrag(dragAmount: Float) {
        scope.launch {
            val newOffset = (_dragOffset.value + dragAmount).coerceIn(
                -screenHeightPx,
                screenHeightPx
            )
            _dragOffset.snapTo(newOffset)

            if (newOffset < 0f) {
                val targetFraction = (-newOffset / screenHeightPx)
                _sheetFraction.snapTo(targetFraction.coerceIn(0f, maxSheetFraction))
            } else if (_sheetFraction.value > 0f && dragAmount > 0f) {
                val collapseAmount = dragAmount / screenHeightPx
                _sheetFraction.snapTo((_sheetFraction.value - collapseAmount).coerceAtLeast(0f))
            }
        }
    }

    fun onDragEnd() {
        isDragging = false
        scope.launch {
            if (_dragOffset.value >= dragThresholdPx) {
                scope.launch {
                    delay(DISMISS_DELAY_MS)
                    onSnooze()
                }
            } else if (_dragOffset.value < 0f) {
                bottomSheetRelease()
            }
            _dragOffset.animateTo(
                0f, spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    fun bottomSheetDragDown(deltaPx: Float) {
        scope.launch {
            val newFraction = (_sheetFraction.value - (deltaPx / screenHeightPx))
                .coerceIn(0f, maxSheetFraction)
            _sheetFraction.snapTo(newFraction)
        }
    }

    fun bottomSheetRelease() {
        scope.launch {
            val shouldStayOpen = _sheetFraction.value > maxSheetFraction * SHEET_STAY_OPEN_THRESHOLD
            val target = if (shouldStayOpen) maxSheetFraction else 0f
            _sheetFraction.animateTo(
                target, spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            if (shouldStayOpen) {
                dismissRequested()
            }
        }
    }

    private suspend fun dismissRequested() {
        delay(DISMISS_DELAY_MS)
        onDismissOrSkip()
    }

    private fun computeDragState(
        dragOffset: Float,
        isDragging: Boolean,
        sheetFraction: Float,
        dragThresholdPx: Float,
        maxSheetFraction: Float,
    ): RingDragState = when {

        sheetFraction >= maxSheetFraction * SHEET_STAY_OPEN_THRESHOLD -> RingDragState.DRAGGING_UP_DONE
        !isDragging && dragOffset == 0f -> RingDragState.IDLE
        dragOffset >= dragThresholdPx -> RingDragState.DRAGGING_DOWN_DONE
        dragOffset > 0 -> RingDragState.DRAGGING_DOWN
        dragOffset < 0 -> RingDragState.DRAGGING_UP

        else -> RingDragState.IDLE
    }
}

@Composable
fun rememberRingScreenState(
    isSnoozed: Boolean,
    onSnooze: () -> Unit,
    screenHeightPx: Float,
    dragThresholdPx: Float,
    onDismissOrSkip: () -> Unit,
    scope: CoroutineScope = rememberCoroutineScope(),
    maxSheetFraction: Float = MAX_SHEET_FRACTION
): RingScreenState =
    remember(isSnoozed,scope, screenHeightPx, dragThresholdPx, maxSheetFraction, onSnooze, onDismissOrSkip) {
        RingScreenState(
            scope,
            screenHeightPx,
            dragThresholdPx,
            maxSheetFraction,
            onSnooze,
            onDismissOrSkip
        )
    }


@Composable
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class
)

fun RingScreen(
    modifier: Modifier = Modifier,
    alarm: Alarm?,
    isSnoozed: Boolean,
    snoozeRemaining: Long = -1L,
    snoozeUntil: Long = -1L,
    onDismiss: () -> Unit,
    onSkipSnooze: () -> Unit,
    onSnooze: () -> Unit,
) {
    val density = LocalDensity.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenHeightPx = remember { with(density) { screenHeight.dp.toPx() } }
    val dragThresholdPx = remember { with(density) { SNOOZE_THRESHOLD_DP.dp.toPx() } }

    val state = rememberRingScreenState(
        isSnoozed = isSnoozed,
        screenHeightPx = screenHeightPx,
        dragThresholdPx = dragThresholdPx,
        onSnooze = onSnooze,
        onDismissOrSkip = { if (isSnoozed) onSkipSnooze() else onDismiss() }
    )

    val dragOffset by remember { state.dragOffsetState }
    val effectiveSheetFraction by state.effectiveSheetFraction
    val screenState by state.screenState

    Surface(modifier = modifier.fillMaxSize()) {
        if (alarm == null) {
            LoadingIndicator()
            return@Surface
        }

        Box(modifier = Modifier.fillMaxSize()) {
            SnoozeTopShape(
                screenState = screenState,
                dragOffset = dragOffset,
                dragThresholdPx = dragThresholdPx,
                alarm = alarm
            )

            DraggableAlarmDataContainer(
                alarm = alarm,
                isSnoozed = isSnoozed,
                state = state,
                maxDismissSheetFraction = MAX_SHEET_FRACTION,
                screenHeightPx = screenHeightPx,
                effectiveDismissSheetFraction = effectiveSheetFraction,
                snoozedUntil = snoozeUntil,
                snoozeRemaining = snoozeRemaining,
            )

            LaunchedEffect(effectiveSheetFraction,state) {
                Log.e("DEBUG", "RS: ${state.screenState.value.name}", )
                Log.e("DEBUG", "RS: $effectiveSheetFraction", )
            }
            DismissBottomSheet(
                effectiveSheetFraction = effectiveSheetFraction,
                state = state,
                onDragDown = state::bottomSheetDragDown,
                onRelease = state::bottomSheetRelease
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RingScreenPreview() {
    Wakee2Theme {
        RingScreen(
            modifier = Modifier,
            alarm = Alarm(),
            isSnoozed = false,
            onDismiss = {},
            onSkipSnooze = {},
        ) { }
    }
}