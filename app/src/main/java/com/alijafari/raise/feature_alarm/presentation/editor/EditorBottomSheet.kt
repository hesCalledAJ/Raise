package com.alijafari.raise.feature_alarm.presentation.editor

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alijafari.raise.R
import com.alijafari.raise.core.ui.theme.CardPosition
import com.alijafari.raise.core.utils.WeekdayUtils.formatSelectedDays
import com.alijafari.raise.core.utils.getRelativeNextRingText
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.presentation.AlarmsViewModel
import com.alijafari.raise.feature_alarm.presentation.components.TimeText
import com.alijafari.raise.feature_alarm.presentation.editor.components.AlarmLabelDialog
import com.alijafari.raise.feature_alarm.presentation.editor.components.EditorCard
import com.alijafari.raise.feature_alarm.presentation.editor.components.MinuteSelectorDialog
import com.alijafari.raise.feature_alarm.presentation.editor.components.RepeatDaysDialog
import com.alijafari.raise.feature_alarm.presentation.editor.components.RingtoneSelectorDialog
import com.alijafari.raise.feature_alarm.presentation.editor.components.TimePickerDialog
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorBottomSheet(
    viewModel: AlarmsViewModel,
    onPreview: (alarm: Alarm) -> Unit,
    onDismiss: () -> Unit,
) {

    val editingAlarmState by viewModel.editingAlarm.collectAsStateWithLifecycle()
    val editingAlarm = editingAlarmState ?: return viewModel.hideEditor().also { onDismiss() }

    var isExpanded by remember { mutableStateOf(false) }

    val isUpdating = editingAlarm.id != 0

    var isRingtoneDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isTitleDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isRepeatDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isSnoozePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isTimePickerVisible by rememberSaveable { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = editingAlarm.hour, initialMinute = editingAlarm.minute
    )

    LaunchedEffect(editingAlarm) {
        if (editingAlarm.ringtoneData == null) {
            viewModel.setEditingAlarmEdit(
                editingAlarm.copy(ringtoneData = viewModel.deviceDefaultRingtones)
            )
        }
        Log.e("TAG", "EditorBottomSheet: selected ${editingAlarm.ringtoneData}")
    }
    AlarmLabelDialog(
        isVisible = isTitleDialogVisible,
        initialValue = editingAlarm.label,
        onDismiss = { isTitleDialogVisible = false },
        onSave = {
            viewModel.setEditingAlarmEdit(
                editingAlarm.copy(
                    label = it
                )
            )
        })
    RepeatDaysDialog(
        isVisible = isRepeatDialogVisible,
        initialDays = editingAlarm.repeatDays,
        onDismiss = { isRepeatDialogVisible = false },
        onSave = {
            viewModel.setEditingAlarmEdit(
                editingAlarm.copy(
                    repeatDays = it
                )
            )
        })

    TimePickerDialog(
        isVisible = isTimePickerVisible,
        timePickerState = timePickerState,
        onDismiss = { isTimePickerVisible = false },
        onConfirm = {
            isTimePickerVisible = false
            viewModel.setEditingAlarmEdit(
                editingAlarm.copy(hour = timePickerState.hour, minute = timePickerState.minute)
            )
        })
    MinuteSelectorDialog(
        isVisible = isSnoozePickerVisible,
        initialMinute = editingAlarm.snoozeMinutes,
        onDismiss = { isSnoozePickerVisible = false },
        onSave = {
            viewModel.setEditingAlarmEdit(
                editingAlarm.copy(
                    snoozeMinutes = it
                )
            )
        })

    val deviceRingtones by viewModel.deviceRingtones.collectAsState()
    if (editingAlarm.ringtoneData != null) {
        RingtoneSelectorDialog(
            isVisible = isRingtoneDialogVisible,
            selectedRingtone = editingAlarm.ringtoneData,
            selectedVolume = editingAlarm.ringtoneVolume,
            onDismiss = {
                isRingtoneDialogVisible = false
            },
            ringtonesList = deviceRingtones,
            ringtonePreviewPlayer = viewModel.ringtonePreviewPlayer
        ) { ringtone, volume ->
            viewModel.setEditingAlarmEdit(
                editingAlarm.copy(
                    ringtoneVolume = volume, ringtoneData = ringtone
                )
            )
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    TimeText(
                        modifier = Modifier.clickable(
                            true
                        ) {
                            isTimePickerVisible = true
                        }, editingAlarm
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LocalContext.current.getRelativeNextRingText(editingAlarm.getNextTriggerAtMillis()),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                IconButton(
                    modifier = Modifier.size(45.dp), colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ), onClick = {
                        isTimePickerVisible = true
                    }) {
                    Icon(
                        painterResource(R.drawable.ic_pen), null, Modifier.padding(5.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(11.dp))
            Column(
                Modifier
                    .weight(1f,fill = false)
                    .verticalScroll(rememberScrollState())
            )
            {
                EditorCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isTitleDialogVisible = true
                    },
                    icon = painterResource(R.drawable.ic_label),
                    title = "Title",
                    position = CardPosition.FIRST
                ) {
                    Text(text = editingAlarm.label)
                }
                EditorCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isRepeatDialogVisible = true
                    },
                    icon = painterResource(R.drawable.ic_repeat),
                    title = "Repeat Days",
                    position = CardPosition.MIDDLE
                ) {
                    Text(text = formatSelectedDays(editingAlarm.repeatDays))
                }
                EditorCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isSnoozePickerVisible = true
                    },
                    icon = painterResource(R.drawable.ic_snooze),
                    title = "Snooze",
                    position = CardPosition.LAST
                ) {
                    Text(text = stringResource(R.string.n_minutes, editingAlarm.snoozeMinutes))
                }
                EditorCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isRingtoneDialogVisible = true
                    },
                    icon = painterResource(if (editingAlarm.ringtoneVolume == 0f) R.drawable.ic_speaker_disabled else if (editingAlarm.ringtoneVolume <= 0.4f) R.drawable.ic_speaker_low else R.drawable.ic_speaker_loud),
                    title = "Ringtone",
                    position = CardPosition.FIRST
                ) {
                    if (editingAlarm.ringtoneData != null && editingAlarm.ringtoneVolume != 0f) {
                        Text(text = editingAlarm.ringtoneData.name ?: "Unknown")
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "${(editingAlarm.ringtoneVolume * 100f).roundToInt()}%")
                    } else Text(text = stringResource(R.string.silent))
                }
                EditorCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isRepeatDialogVisible = true
                    },
                    icon = painterResource(R.drawable.ic_vibrate),
                    title = "Vibrate",
                    position = CardPosition.LAST
                ) {
                    Switch(
                        modifier = Modifier.height(24.dp),
                        checked = editingAlarm.vibrate,
                        onCheckedChange = {
                            viewModel.setEditingAlarmEdit(
                                editingAlarm.copy(
                                    vibrate = it
                                )
                            )
                        })
                }

                AnimatedContent(isExpanded) { expanded ->
                    Box {
                        this@Column.AnimatedVisibility(expanded.not()) {
                            Box {
                                ShadowedSeparator(modifier = Modifier.offset(y = 4.dp))
                                EditorCard(
                                    modifier = Modifier
                                        .fillMaxWidth(.96f)
                                        .offset(y = 4.dp)
                                        .zIndex(-1f)
                                        .align(Alignment.Center),
                                    onClick = {},
                                    icon = null,
                                    title = "",
                                    position = CardPosition.ONLY
                                ) {}
                                ShadowedSeparator(modifier = Modifier.offset(y = 7.dp))
                                EditorCard(
                                    modifier = Modifier
                                        .fillMaxWidth(.92f)
                                        .offset(y = 7.dp)
                                        .zIndex(-2f)
                                        .align(Alignment.Center),
                                    onClick = {},
                                    icon = null,
                                    title = "",
                                    position = CardPosition.ONLY
                                ) {}
                            }
                        }
                        EditorCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(2f),
                            onClick = {
                                isExpanded = isExpanded.not()
                            },
                            icon = painterResource(R.drawable.ic_plus),
                            title = "More",
                            position = if (expanded) CardPosition.FIRST else CardPosition.ONLY,
                            colors = CardDefaults.cardColors(
                                containerColor = if (expanded) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (expanded) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            if (expanded.not()) {
                                Row {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource(R.drawable.ic_challenge),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource(R.drawable.ic_timer),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource(R.drawable.ic_bomb),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else Text(
                                "Hide"
                            )
                        }
                    }
                }
                AnimatedVisibility(isExpanded) {
                    Column() {
                        EditorCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(2f),
                            onClick = {
                                isExpanded = isExpanded.not()
                            },
                            icon = painterResource(R.drawable.ic_challenge),
                            title = "Challenges",
                            position = CardPosition.MIDDLE,
                        ) {
                            Text(text = stringResource(R.string.silent))
                        }
                        EditorCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(2f),
                            onClick = {
                                isExpanded = isExpanded.not()
                            },
                            icon = painterResource(R.drawable.ic_timer),
                            title = "Smart Delay",
                            position = CardPosition.MIDDLE,
                        ) {
                            Text(text = stringResource(R.string.silent))
                        }
                        EditorCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(2f),
                            onClick = {
                                isExpanded = isExpanded.not()
                            },
                            icon = painterResource(R.drawable.ic_bomb),
                            title = "Loud Bomb",
                            position = CardPosition.LAST,
                        ) {
                            Text(text = stringResource(R.string.silent))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(9.dp))
            Row {
                Button(
                    onClick = {
                        if (isUpdating) viewModel.deleteAlarm(editingAlarm)
                        else onDismiss()
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(if (isUpdating) "Delete" else "Cancel")
                }

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        onPreview(editingAlarm)
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ), shape = RoundedCornerShape(
                        topStart = 100f, topEnd = 10f, bottomEnd = 10f, bottomStart = 100f
                    )
                ) {
                    Text("Preview")
                }
                Spacer(Modifier.width(3.dp))
                Button(
                    onClick = { viewModel.saveEditingAlarm() }, shape = RoundedCornerShape(
                        topStart = 10f, topEnd = 100f, bottomEnd = 100f, bottomStart = 10f
                    )
                ) { Text("Save") }
                Spacer(Modifier.width(5.dp))
            }
        }
    }
}

@Composable
fun ShadowedSeparator(
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    alpha: Float = 0.1f,
    height: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = alpha), Color.Transparent)
                )
            )
    )
}