package com.alijafari.raise.feature_alarm.presentation.ring.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alijafari.raise.R
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.presentation.ring.RingDragState
import kotlin.math.max


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SnoozeTopShape(
    screenState: RingDragState,
    dragOffset: Float,
    dragThresholdPx: Float,
    alarm: Alarm
) {
    val density = LocalDensity.current
    val topShapeHeightDp by remember(dragOffset) {
        derivedStateOf { with(density) { max(0f, dragOffset).toDp() } }
    }

    val scale = if (dragOffset < dragThresholdPx / 2) 1f else animateFloatAsState(
        targetValue = if (screenState == RingDragState.DRAGGING_DOWN_DONE) .94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = 500f
        ),
        label = "bounceScale"
    ).value

    val animatedCorner = if (dragOffset < dragThresholdPx / 2) 0.dp else animateDpAsState(
        targetValue = if (screenState == RingDragState.DRAGGING_DOWN_DONE) 35.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = 500f
        ),
        label = "cornerRadius"
    ).value.coerceAtLeast(0.dp)

    if (screenState in listOf(RingDragState.DRAGGING_DOWN, RingDragState.DRAGGING_DOWN_DONE)) {
        Box(
            modifier = Modifier
                .height(topShapeHeightDp)
                .fillMaxWidth()
                .scale(scale)
                .background(
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(
                        animatedCorner,
                        animatedCorner,
                        35.dp,
                        35.dp
                    )
                )
                .padding(8.dp)
        ) {
            AnimatedContent(
                targetState = screenState,
                transitionSpec = {
                    scaleIn(
                        initialScale = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) togetherWith fadeOut()
                },
                label = "content"
            ) { state ->
                if (state == RingDragState.DRAGGING_DOWN_DONE) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Rounded.Snooze,
                            contentDescription = "Snooze",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.n_minutes, alarm.snoozeMinutes),
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Spacer(Modifier)
                        Text(
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.bodyMedium,
                            text = "drag down to snooze"
                        )
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Drag down",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}