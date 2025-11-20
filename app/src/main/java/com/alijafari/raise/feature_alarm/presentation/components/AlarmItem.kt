package com.alijafari.raise.feature_alarm.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.alijafari.raise.core.ui.theme.AlarmCard
import com.alijafari.raise.core.ui.theme.CardPosition
import com.alijafari.raise.feature_alarm.domain.model.Alarm

@Composable
fun AlarmItem(
    modifier: Modifier = Modifier,
    alarm: Alarm,
    cardPosition: CardPosition,
    onToggle: (Alarm) -> Unit,
    onClick: (Alarm) -> Unit,
) {
    AlarmCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                0.dp,
                0.dp,
                0.dp,
                if (cardPosition in listOf(CardPosition.LAST, CardPosition.ONLY)) 7.dp else 4.dp
            ),
        position = cardPosition,
        enabled = alarm.isEnabled,
        onClick = {
            onClick(alarm)
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    vertical = 12.dp
                )
                .padding(end = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.width(6.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {
                    for (day in 0..6) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            Modifier
                                .width(if (day+1 in alarm.repeatDays)7.dp else 4.dp)
                                .height(3.dp)
                                .background(
                                    color = (if (alarm.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary).copy(
                                        alpha = if (day+1 in alarm.repeatDays) 1f else .7f
                                    ),
                                    shape = RoundedCornerShape(
                                        topEnd = 3.dp,
                                        bottomEnd = 3.dp
                                    )
                                )
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                }
                Spacer(Modifier.width(6.dp))
                TimeText(alarm = alarm)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { checked ->
                        onToggle(alarm)
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}
