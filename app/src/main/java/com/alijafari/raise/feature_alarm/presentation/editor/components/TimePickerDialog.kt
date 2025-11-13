package com.alijafari.raise.feature_alarm.presentation.editor.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alijafari.raise.R
import com.alijafari.raise.core.ui.components.PopupDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    isVisible: Boolean,
    timePickerState: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PopupDialog(
        isVisible = isVisible,
        title = stringResource(R.string.editor_time_selector_popup),
        onDismiss = onDismiss,
        positiveButton = stringResource(R.string.save) to onConfirm,
        negativeButton = stringResource(R.string.cancel) to onDismiss
    ) {
        TimePicker(state = timePickerState)
    }
}
