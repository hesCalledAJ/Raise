package com.alijafari.raise.feature_alarm.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alijafari.raise.feature_alarm.domain.model.Alarm

@Composable
fun TimeText(
    modifier: Modifier = Modifier,
    alarm : Alarm,
    is24hours: Boolean = false,
) {

    val isAm = alarm.hour < 12
    val displayHour = if (is24hours) alarm.hour else if (alarm.hour % 12 == 0) 12 else alarm.hour % 12
    val amPm = if (isAm) "AM" else "PM"

    Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = String.format("%02d:%02d", displayHour, alarm.minute),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = if (alarm.isEnabled) FontWeight.Bold else FontWeight.Normal,
            )
        )
        if (!is24hours) {
            Spacer(modifier.width(5.dp))
            Text(
                text = amPm,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize= 20.sp,
                ),
            )
        }
    }

}
