package com.alijafari.raise.feature_alarm.presentation.ring.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DragHandleSizes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.alijafari.raise.feature_alarm.presentation.ring.RingDragState
import com.alijafari.raise.feature_alarm.presentation.ring.RingScreenState

@Composable
fun DismissBottomSheet(
    effectiveSheetFraction: Float,
    state: RingScreenState,
    onDragDown: (Float) -> Unit,
    onRelease: () -> Unit
) {
    val scrimModifier = if (effectiveSheetFraction != 0f) Modifier.pointerInput(Unit) {} else Modifier

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(
                        alpha = effectiveSheetFraction.coerceAtMost(0.4f)
                    )
                )
                .then(scrimModifier)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            BottomSheetOverlay(
                fraction = effectiveSheetFraction,
                onDragDown = onDragDown,
                onRelease = onRelease
            ) {

                LaunchedEffect(effectiveSheetFraction,state) {
                    Log.e("DEBUG", "DMBS: ${state.screenState.value.name}", )
                    Log.e("DEBUG", "DMBS: $effectiveSheetFraction", )
                }
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VerticalDragHandle(
                        sizes = DragHandleSizes(
                            size = DpSize(40.dp, 4.dp),
                            pressedSize = DpSize(55.dp, 7.dp),
                            draggedSize = DpSize(50.dp, 5.dp)
                        )
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = if (state.screenState.value == RingDragState.DRAGGING_UP_DONE){
                            if (!state.isDragging) "Alarm Dismissed"
                            else "Release to dismiss"
                        }  else "Drag up to dismiss",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomSheetOverlay(
    fraction: Float,
    onDragDown: (deltaPx: Float) -> Unit,
    onRelease: () -> Unit,
    content: @Composable () -> Unit
) {
    val screenHeightPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val sheetHeightPx = (screenHeightPx * fraction)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(with(LocalDensity.current) { sheetHeightPx.toDp() }.also {
                Log.e("DEBUG", "BottomSheetOverlay: $it", )
            })
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0f) onDragDown(dragAmount)
                    },
                    onDragEnd = { onRelease() }
                )
            }
    ) {
        content()
    }
}