package com.alijafari.raise.feature_alarm.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.alijafari.raise.core.ui.theme.CardPosition
import com.alijafari.raise.feature_alarm.presentation.components.AlarmItem

@Composable
fun AlarmsScreen(
    modifier: Modifier,
    viewModel: AlarmsViewModel,
) {
    val alarms by viewModel.alarms.collectAsState()
    Surface {
        val hapticFeedback = LocalHapticFeedback.current
        LazyColumn(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .fillMaxWidth()
        ) {
            items(
                items = alarms,
                key = { it.id }
            ) { alarm ->
                val enabledList = alarms.filter { it.isEnabled }
                val disabledList = alarms.filterNot { it.isEnabled }

                val cardPosition = when {
                    alarms.size == 1 -> CardPosition.ONLY
                    alarm == enabledList.firstOrNull() && enabledList.size == 1 -> CardPosition.ONLY
                    alarm == disabledList.firstOrNull() && disabledList.size == 1 -> CardPosition.ONLY
                    alarm == enabledList.firstOrNull() -> CardPosition.FIRST
                    alarm == enabledList.lastOrNull() -> CardPosition.LAST
                    alarm == disabledList.firstOrNull() -> CardPosition.FIRST
                    alarm == disabledList.lastOrNull() -> CardPosition.LAST

                    else -> CardPosition.MIDDLE
                }
                AlarmItem(
                    alarm = alarm,
                    modifier = Modifier.animateItem(
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    cardPosition = cardPosition,
                    onClick = { viewModel.openEditor(alarm) },
                    onToggle = {
                        viewModel.toggleAlarm(it)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )

            }
        }
    }
}
