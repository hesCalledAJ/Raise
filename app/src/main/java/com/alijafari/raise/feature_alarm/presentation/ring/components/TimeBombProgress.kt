package com.alijafari.raise.feature_alarm.presentation.ring.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alijafari.raise.R

@Composable
fun TimeBombPill(
    progress: Float,
    remainingSeconds: Long,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "TimeBombProgress"
    )

    val containerColor = MaterialTheme.colorScheme.errorContainer
    val barColor = MaterialTheme.colorScheme.error.copy(alpha = .1f)
    val contentColor = MaterialTheme.colorScheme.onErrorContainer

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(CircleShape)
            .background(containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_bomb),
                    contentDescription = "Danger",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    text = if (progress==1f) "Time Bomb is Active" else "Loud Sound in ${remainingSeconds}s !",
                    color = contentColor,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )
            }
        }

        Box( // progress overlay
            modifier = Modifier
                .matchParentSize()
        ) {
            Box(
                Modifier
                    .fillMaxWidth(1f - animatedProgress)
                    .fillMaxHeight()
                    .background(barColor, shape = CircleShape)
                    .align(Alignment.CenterStart)
            )
        }
    }
}