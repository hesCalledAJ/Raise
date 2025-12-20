package com.alijafari.raise.feature_alarm.presentation.editor.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alijafari.raise.R
import kotlin.math.roundToInt

@Composable
fun OffsetSelectorDialog(
    isVisible: Boolean,
    initialRange: IntRange,
    onDismiss: () -> Unit,
    onSave: (IntRange) -> Unit
) {
    var selectedRange by remember { mutableStateOf(initialRange) }

    if (isVisible) AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.editor_offset_title)) },
        text = {
            Column {
                Text(stringResource(R.string.editor_offset_sub))
                Spacer(Modifier.height(12.dp))
                OffsetSlider(
                    range = selectedRange,
                    onRangeChanged = { selectedRange = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedRange) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OffsetSlider(modifier: Modifier = Modifier,range: IntRange, onRangeChanged: (range : IntRange) -> Unit) {
    var selectedValue by remember { mutableStateOf(range.first.toFloat()..range.last.toFloat()) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        RangeSlider(
            value = selectedValue,
            onValueChange = { newRange ->
                var start = newRange.start.roundToInt()
                var end = newRange.endInclusive.roundToInt()
                if (start > 0) start = 0
                if (end < 0) end = 0
                selectedValue = start.toFloat() .. end.toFloat()
                onRangeChanged(
                    start .. end
                )
            },
            valueRange = -30f..30f,
            steps = 11,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "${selectedValue.start.toInt()}m ~ ${selectedValue.endInclusive.toInt()}m",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}