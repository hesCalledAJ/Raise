package com.alijafari.raise

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.alijafari.raise.core.ui.theme.Wakee2Theme
import com.alijafari.raise.feature_alarm.data.AlarmBroadcastEvent
import com.alijafari.raise.feature_alarm.data.AlarmIntentExtra
import com.alijafari.raise.feature_alarm.data.AlarmReceiver
import com.alijafari.raise.feature_alarm.presentation.AlarmsScreen
import com.alijafari.raise.feature_alarm.presentation.AlarmsViewModel
import com.alijafari.raise.feature_alarm.presentation.editor.EditorBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AlarmsViewModel by viewModels()

    private var showExactAlarmDialog = mutableStateOf(false)
    private var showNotificationDialog = mutableStateOf(false)
    private var showFSIDialog = mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            showExactAlarmDialog.value = !alarmManager.canScheduleExactAlarms()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val allowed = nm.areNotificationsEnabled() &&
                    nm.canUseFullScreenIntent()

            showFSIDialog.value = !allowed
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showNotificationDialog.value = true
            }
        }

        enableEdgeToEdge()
        setContent {
            Wakee2Theme {
                PermissionRequiredCheck(
                    exactAlarm = showExactAlarmDialog,
                    notifications = showNotificationDialog,
                    fsi = showFSIDialog
                )
                MainScreen(
                    modifier = Modifier,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier,
    viewModel: AlarmsViewModel,
) {
    val context = LocalContext.current
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Alarms")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                {
                    viewModel.openEditor(null)
                }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { paddingValues ->
        if (viewModel.editingAlarm.collectAsState().value != null) {
            EditorBottomSheet(
                viewModel = viewModel,
                onPreview = {
                    context.sendBroadcast(
                        Intent(
                            context, AlarmReceiver::class.java
                        ).apply {
                            action = AlarmBroadcastEvent.PREVIEW()
                            putExtra(AlarmIntentExtra.ID(), it.id)
                        }
                    )
                }
            ) {
                viewModel.hideEditor()
            }
        }
        AlarmsScreen(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 9.dp)
                ,
            viewModel = viewModel
        )
    }
}
@Composable
fun PermissionRequiredCheck(
    exactAlarm: MutableState<Boolean>,
    notifications: MutableState<Boolean>,
    fsi: MutableState<Boolean>,
) {
    val context = LocalContext.current

    if (exactAlarm.value) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(textAlign = TextAlign.Center,text = "Alarm Permission") },
            text = { Text(textAlign = TextAlign.Center,text = "Enable Exact Alarms.") },
            confirmButton = {
                TextButton({
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        )
                    }
                }) { Text("Open Settings") }
            }
        )
        return
    }

    if (notifications.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            notifications.value = !granted
        }

        AlertDialog(
            onDismissRequest = {},
            title = { Text(textAlign = TextAlign.Center,text = "Notifications Permission") },
            text = { Text(textAlign = TextAlign.Center,text = "Allow notifications so alarms can alert you.") },
            confirmButton = {
                TextButton ( onClick = {launcher.launch(Manifest.permission.POST_NOTIFICATIONS)} ) {
                    Text("Allow")
                }
            }
        )
        return
    }

    if (fsi.value) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(textAlign = TextAlign.Center,text = "Fullscreen Intent Permission") },
            text = { Text(textAlign = TextAlign.Center,text = "Allow Fullscreen Interruptions for alarms.") },
            confirmButton = {
                TextButton({
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                data = "package:${context.packageName}".toUri()
                            }
                        )
                    }
                }) { Text("Open Settings") }
            }
        )
        return
    }
}


