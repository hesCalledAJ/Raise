package com.alijafari.raise.feature_alarm.presentation.editor.components

import android.net.Uri
import android.util.Log
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alijafari.raise.R
import com.alijafari.raise.core.ui.components.PopupDialog
import com.alijafari.raise.feature_ringtone.data.infrastructure.RingtonePlayerImpl
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData

@Composable
fun RingtoneSelectorDialog(
    isVisible: Boolean,
    selectedRingtone: RingtoneData?,
    selectedVolume: Float,
    onDismiss: () -> Unit,
    ringtonesList: List<RingtoneData>,
    ringtonePreviewPlayer: RingtonePlayerImpl,
    onSave: (ringtone: RingtoneData?, volume: Float) -> Unit,
) {
    var selectedRingtone by remember { mutableStateOf(selectedRingtone) }
    var selectedVolume by remember { mutableStateOf(selectedVolume) }

    var playingUri by remember { mutableStateOf<Uri?>(null) }

    fun play(ringtone: RingtoneData) {
        ringtonePreviewPlayer.stop()
        ringtonePreviewPlayer.play(ringtone, selectedVolume)
        playingUri = ringtone.uri
    }

    fun stop() {
        ringtonePreviewPlayer.stop()
        playingUri = null
    }

    LaunchedEffect(isVisible) {
        if (!isVisible){
            playingUri = null
            ringtonePreviewPlayer.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stop()
        }
    }

    PopupDialog(
        isVisible = isVisible,
        title = stringResource(R.string.select_ringtone),
        onDismiss = onDismiss,
        positiveButton = stringResource(R.string.save) to {
            onSave(selectedRingtone, selectedVolume)
        },
        negativeButton = stringResource(R.string.cancel) to onDismiss,
    ) {
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .height(400.dp)
                .clip(shape = RoundedCornerShape(18.dp))
        ) {
            items(ringtonesList) { ringtone ->
                val isSelected = selectedRingtone?.uri == ringtone.uri
                val isPlaying = playingUri == ringtone.uri

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (ringtonesList.last() == ringtone) 0.dp else 5.dp)
                        .background(
                            if (ringtonesList.indexOf(ringtone) % 2 == 0) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f),
                            RoundedCornerShape(5.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedRingtone = ringtone }
                    )
                    Text(
                        modifier = Modifier
                            .weight(1f),
                        text = ringtone.name ?: "Unknown")
                    IconButton(
                        onClick = {
                            if (isPlaying) stop() else play(ringtone)
                        }
                    ) {
                        Image(
                            painter = rememberAnimatedVectorPainter(
                                AnimatedImageVector.animatedVectorResource(
                                    if (isPlaying)
                                        R.drawable.ic_pause_animated
                                    else
                                        R.drawable.ic_play_animated
                                ),
                                isPlaying
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            listState.animateScrollToItem(ringtonesList.indexOf(selectedRingtone).takeIf { it >=0 } ?: 0, 0)
        }
    }
}
