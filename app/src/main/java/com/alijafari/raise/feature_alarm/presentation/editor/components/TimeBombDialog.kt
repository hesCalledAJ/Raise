package com.alijafari.raise.feature_alarm.presentation.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alijafari.raise.R

@Composable
fun TimeBombDialog(
    isVisible: Boolean,
    initialDelay: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var sliderValue by remember(initialDelay) { mutableIntStateOf(initialDelay.coerceIn(30, 120)) }

    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.editor_loud_bomb_delay_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.editor_loud_bomb_delay_description,sliderValue))
                    Spacer(Modifier.height(12.dp))
                    Slider(
                        value = sliderValue.toFloat(),
                        onValueChange = { sliderValue = it.toInt() },
                        valueRange = 30f..120f,
                        steps = 17, // (120-30)/5 = 18 intervals → 17 steps
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onSave(sliderValue) }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}