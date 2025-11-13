package com.alijafari.raise.feature_alarm.presentation.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.alijafari.raise.R


@Composable
fun AlarmLabelDialog(
    isVisible : Boolean,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {

    AnimatedVisibility(isVisible) {
        var label by remember { mutableStateOf(initialValue) }
        AlertDialog(onDismissRequest = onDismiss, text = {
            Column {
                TextField(
                    value = label,
                    onValueChange = {
                        label = it
                    },
                    label = {
                        Text(stringResource(R.string.editor_field_label))
                    }
                )
            }
        }, confirmButton = {
            TextButton(
                onClick = {
                    onSave(label)
                    onDismiss()
                }) { Text(stringResource(R.string.save)) }
        }, dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        })
    }
}