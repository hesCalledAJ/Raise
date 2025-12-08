package com.alijafari.raise.feature_alarm.presentation.ring.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.alijafari.raise.R
import com.alijafari.raise.core.utils.getSnoozedTimeString
import com.alijafari.raise.core.utils.getTimeString
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.presentation.ring.RingDragState
import com.alijafari.raise.feature_alarm.presentation.ring.RingScreenState
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


@Composable
fun DraggableAlarmDataContainer(
    alarm: Alarm,
    isSnoozed: Boolean,
    state: RingScreenState,
    maxDismissSheetFraction: Float,
    effectiveDismissSheetFraction: Float,
    screenHeightPx: Float,
    snoozedUntil : Long?,
    snoozeRemaining : Long?
) {
    val dragOffset by remember { state.dragOffsetState }
    val sheetFraction by remember { state.sheetFractionState }

    val isDragging by remember { derivedStateOf { state.isDragging } }

    LaunchedEffect(dragOffset) {
        Log.e("DBG","state.dragOffset.collect -> $dragOffset")
    }
    val scale = animateFloatAsState(
        targetValue = if (!isDragging) .95f else 1.05f,
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
        AnimatedVisibility(state.screenState.value == RingDragState.IDLE && !isSnoozed) {
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
                    Log.e("DEBUG", "finalOffset : $finalOffset", )
                    Log.e("DEBUG", "dragOffset : $dragOffset", )
                    IntOffset(0, finalOffset.roundToInt())
                }
                .ringDrag(state, 1.5f)
                .padding(vertical = 6.dp)
            ,
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
            Button(
                onClick = state.onDismissOrSkip,
                modifier = Modifier.fillMaxWidth(.7f).height(55.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    "Skip",
                    fontSize = 15.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmData(
    isSnoozed: Boolean,
    dragProgress: Float,
    alarm: Alarm,
    snoozedUntil: Long?,
    snoozeRemaining: Long?
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationSpeed = lerp(1f, 0.4f, dragProgress)
    val rotation by infiniteTransition.animateFloat(
        0f, 360f, infiniteRepeatable(
            animation = tween((8600 / rotationSpeed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val animatedPadding by animateDpAsState(
        targetValue = if (isSnoozed) 5.dp else 0.dp, animationSpec = tween(400)
    )

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSnoozed) MaterialTheme.colorScheme.surface else lerp(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.surface,
            dragProgress
        ),
        animationSpec = tween(400)
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (isSnoozed) MaterialTheme.colorScheme.primary else lerp(
            MaterialTheme.colorScheme.onPrimary,
            MaterialTheme.colorScheme.primary,
            dragProgress
        ),
        animationSpec = tween(400)
    )

    val strokeBackgroundModifier = if (isSnoozed) Modifier
        .background(
            shape = MaterialShapes.Cookie12Sided.toShape(),
            color = MaterialTheme.colorScheme.primary
        )
        .padding(animatedPadding) else Modifier
    Box(
        modifier = Modifier
            .padding(15.dp)
            .graphicsLayer(rotationZ = rotation)
            .then(strokeBackgroundModifier)
            .background(
                shape = MaterialShapes.Cookie12Sided.toShape(),
                color = animatedBgColor
            )
            .aspectRatio(1f)
            .wrapContentSize(Alignment.Center)
    ) {
        //Alarm Data
        Column(
            modifier = Modifier.graphicsLayer(rotationZ = rotation * -1),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (isSnoozed) {
                Text(
                    stringResource(R.string.snoozed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = animatedContentColor
                )
                Spacer(Modifier.height(5.dp))
            }
            Text(
                text = alarm.getSnoozedTimeString(),
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
    dragMultiplier: Float
): Modifier = this.pointerInput(Unit) {
    detectVerticalDragGestures(
        onDragStart = {
            state.isDragging = true
            Log.e("DEBUG", "ringDrag: drag started", )
        },
        onVerticalDrag = { change, dragAmount ->

            change.consume()
            val adjustedDragAmount = if (dragAmount < 0) {
                dragAmount * dragMultiplier.coerceAtLeast(0f) //only for upward drag , because dismiss gesture can be hard to drag fully up
            } else {
                dragAmount
            }

            Log.e("DEBUG", "ringDrag: adjustedDragAmount $adjustedDragAmount", )
            state.onDrag(adjustedDragAmount)
        },
        onDragEnd = {
            Log.e("DEBUG", "ringDrag: drag ended", )
            state.onDragEnd()
        }
    )
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CookieProgress(
    progress: Float, // 0f..1f
    isSnoozed: Boolean,
    animatedPadding: Dp
) {
    val strokeWidth = 8.dp
    val shape = MaterialShapes.Cookie12Sided.toShape()
    Box(
        modifier = if (isSnoozed) Modifier
            .padding(animatedPadding)
            .size(100.dp) // adjust size
            .drawBehind {
                val stroke = Stroke(width = strokeWidth.toPx())
                val shapePath = shape.createOutline(
                    size = size,
                    layoutDirection = layoutDirection,
                    density = this
                )
                drawOutline(
                    outline = shapePath,
                    color = Color.Blue,
                    style = stroke
                )
                // Progress
                drawArc(
                    color = Color.Gray,
                    startAngle = -90f,
                    sweepAngle = 360 * progress,
                    useCenter = false,
                    style = stroke
                )
            } else Modifier
    )
}
@Preview(showBackground = true)
@Composable
fun CookieProgressPreview() {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(50)
            progress += 0.01f
        }
    }

    CookieProgress(
        progress = progress,
        isSnoozed = true,
        animatedPadding = 8.dp
    )
}
