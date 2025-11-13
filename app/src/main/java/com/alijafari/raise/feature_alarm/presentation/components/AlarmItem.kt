package com.alijafari.raise.feature_alarm.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alijafari.raise.core.ui.theme.AlarmCard
import com.alijafari.raise.core.ui.theme.CardPosition
import com.alijafari.raise.core.utils.WeekdayUtils.formatSelectedDays
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
                0.dp, 0.dp, 0.dp, if (cardPosition in listOf(CardPosition.LAST, CardPosition.ONLY)) 7.dp else 4.dp
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
                .padding(10.dp)
        ) {
            Text(
                text = formatSelectedDays(alarm.repeatDays),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimeText(alarm = alarm)
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
