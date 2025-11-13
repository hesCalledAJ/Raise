package com.alijafari.raise.feature_alarm.presentation.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alijafari.raise.R
import com.alijafari.raise.core.ui.components.PopupDialog
import java.util.Calendar
import java.util.Locale


@Composable
fun RepeatDaysDialog(
    isVisible: Boolean,
    initialDays: List<Int>,
    onDismiss: () -> Unit,
    onSave: (List<Int>) -> Unit,
) {
    val locale = Locale.getDefault()
    val weekdays = remember {
        (1..7).map { day ->
            val cal = Calendar.getInstance(locale)
            cal.set(Calendar.DAY_OF_WEEK, day)
            cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale) ?: day.toString()
        }
    }

    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(initialDays) } }

    PopupDialog(
        isVisible = isVisible,
        title = stringResource(R.string.repeat_dialog_title),
        onDismiss = onDismiss,
        positiveButton = stringResource(R.string.save) to {
            onSave(selectedDays.sorted())
        } ,
        negativeButton = stringResource(R.string.cancel) to onDismiss
    ) {
        weekdays.forEachIndexed { index, name ->
            val dayValue = index + 1
            val isSelected = dayValue in selectedDays
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isSelected) selectedDays.remove(dayValue)
                        else selectedDays.add(dayValue)
                    }
                    .padding(vertical = 5.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isSelected, onCheckedChange = {
                        if (it) selectedDays.add(dayValue)
                        else selectedDays.remove(dayValue)
                    })
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}