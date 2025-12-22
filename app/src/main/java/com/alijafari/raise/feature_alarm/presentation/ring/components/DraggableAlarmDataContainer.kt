package com.alijafari.raise.feature_alarm.presentation.ring.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.alijafari.raise.R
import com.alijafari.raise.core.utils.getTimeString
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.presentation.ring.RingDragState
import com.alijafari.raise.feature_alarm.presentation.ring.RingScreenState
import kotlin.math.roundToInt

@Composable
fun DraggableAlarmDataContainer(
    alarm: Alarm,
    isSnoozed: Boolean,
    state: RingScreenState,
    maxDismissSheetFraction: Float,
    screenHeightPx: Float,
    snoozedUntil: Long,
    snoozeRemaining: Long,
) {
    val dragOffset by remember { state.dragOffsetState }
    val sheetFraction by remember { state.sheetFractionState }

    val isDragging by remember { derivedStateOf { state.isDragging } }
    val effectiveDismissSheetFraction by state.effectiveSheetFraction

    val scale = animateFloatAsState(
        targetValue = if (!isDragging || isSnoozed) .95f else 1.05f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = 500f
        ),
        label = "bounceScale"
    ).value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(state.screenState.value == RingDragState.IDLE) {
            SwipeHintArrows(true, stringResource(R.string.dismiss))
        }

        Box(
            modifier = Modifier
                .scale(scale)
                .offset {
                    val sheetHeightPx = screenHeightPx * sheetFraction
                    val normalized = (sheetFraction / maxDismissSheetFraction).coerceIn(0f, 1f)
                    val dragBased = (dragOffset * 0.5f)
                    val maxUpOffset = -sheetHeightPx / 2f
                    val interpolated = dragBased * (1f - normalized) + maxUpOffset * normalized
                    val finalOffset = if (interpolated < maxUpOffset) maxUpOffset else interpolated
                    IntOffset(0, finalOffset.roundToInt())
                }
                .ringDrag(state, 1.5f)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            AlarmData(
                isSnoozed = isSnoozed,
                snoozedUntil = snoozedUntil,
                snoozeRemaining = snoozeRemaining,
                dragProgress = (effectiveDismissSheetFraction / maxDismissSheetFraction).coerceIn(
                    0f,
                    1f
                ),
                alarm = alarm
            )
        }

        AnimatedVisibility(state.screenState.value == RingDragState.IDLE && !isSnoozed) {
            SwipeHintArrows(false, stringResource(R.string.snooze))
        }
        AnimatedVisibility(isSnoozed) {
            Spacer(Modifier.height(12.dp))
//            Button(
//                onClick = state.onDismissOrSkip,
//                modifier = Modifier
//                    .fillMaxWidth(.7f)
//                    .height(55.dp),
//                shape = RoundedCornerShape(17.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.errorContainer,
//                    contentColor = MaterialTheme.colorScheme.onErrorContainer
//                )
//            ) {
//                Text(
//                    "Skip",
//                    fontSize = 15.sp
//                )
//            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmData(
    isSnoozed: Boolean,
    dragProgress: Float,
    alarm: Alarm,
    snoozedUntil: Long,
    snoozeRemaining: Long,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationSpeed = lerp(1f, 0f, dragProgress)
    val rotation by infiniteTransition.animateFloat(
        0f, 360f, infiniteRepeatable(
            animation = tween((10000 / rotationSpeed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val alarmTime =
        remember { if (isSnoozed) getTimeString(snoozedUntil) else alarm.getTimeString() }

    val animatedContentColor by animateColorAsState(
        targetValue = if (isSnoozed) MaterialTheme.colorScheme.primary else lerp(
            MaterialTheme.colorScheme.onPrimary,
            MaterialTheme.colorScheme.primary,
            dragProgress
        ),
        animationSpec = tween(600)
    )
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSnoozed) MaterialTheme.colorScheme.primary else lerp(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0f),
            dragProgress
        ),
        animationSpec = tween(600)
    )

    Box(
        modifier = Modifier
            .padding(15.dp)
            .wrapContentSize(Alignment.Center)
            .aspectRatio(1f)
    ) {
        AnimatedContent(
            isSnoozed
        ) {
            val density = LocalDensity.current
            if (!it) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(rotationZ = rotation)
                        .background(
                            shape = MaterialShapes.Cookie12Sided.toShape(),
                            color = animatedContainerColor
                        )
                        .wrapContentSize(Alignment.Center)
                        .fillMaxSize()
                ) {}
            } else {
                CircularWavyProgressIndicator(
                    progress = {
                        val progress = 1f - snoozeRemaining / (alarm.snoozeMinutes * 60000f)
                        progress.coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxSize().alpha(1 - dragProgress),
                    wavelength = 100.dp,
                    stroke = Stroke(width = with(density) { 13.dp.toPx() }, cap = StrokeCap.Round),
                    trackStroke = Stroke(
                        width = with(density) { 10.dp.toPx() },
                        cap = StrokeCap.Round
                    ),
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    gapSize = 9.dp,
                    waveSpeed = 60.dp,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {

            if (isSnoozed) {
                Text(
                    stringResource(R.string.snoozed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = animatedContentColor
                )
                Spacer(Modifier.height(5.dp))
            }
            Text(
                text = alarmTime,
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                color = animatedContentColor
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = alarm.label,
                style = MaterialTheme.typography.bodyLarge,
                color = animatedContentColor
            )
        }
    }
}

private fun Modifier.ringDrag(
    state: RingScreenState,
    dragMultiplier: Float,
): Modifier = this.pointerInput(state) {
    detectVerticalDragGestures(
        onDragStart = {
            state.isDragging = true
        },
        onVerticalDrag = { change, dragAmount ->
            change.consume()
            val adjustedDragAmount = if (dragAmount < 0) {
                dragAmount * dragMultiplier.coerceAtLeast(0f) //only for upward drag , because dismiss gesture can be hard to drag fully up
            } else {
                dragAmount
            }
            state.onDrag(adjustedDragAmount)
        },
        onDragEnd = {
            state.onDragEnd()
        }
    )
}

