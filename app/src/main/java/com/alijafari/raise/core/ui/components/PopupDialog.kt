package com.alijafari.raise.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupDialog(
    isVisible: Boolean,
    title: String? = null,
    onDismiss: () -> Unit,
    positiveButton: Pair<String, () -> Unit>? = null,
    negativeButton: Pair<String, () -> Unit>? = null,
    neutralButton: Pair<String, () -> Unit>? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(isVisible) {
        BasicAlertDialog(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(25.dp)
            ), onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 15.dp)
                    )
                }

                content()

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    neutralButton?.let { (text, onClick) ->
                        TextButton(onClick = {
                            onClick()
                            onDismiss()
                        }) { Text(text) }
                    }
                    negativeButton?.let { (text, onClick) ->
                        TextButton(onClick = {
                            onClick()
                            onDismiss()
                        }) { Text(text) }
                    }
                    positiveButton?.let { (text, onClick) ->
                        TextButton(onClick = {
                            onClick()
                            onDismiss()
                        }) { Text(text) }
                    }
                }
            }
        }

    }
}
