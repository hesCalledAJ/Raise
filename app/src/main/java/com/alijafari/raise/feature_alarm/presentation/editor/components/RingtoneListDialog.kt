package com.alijafari.raise.feature_alarm.presentation.editor.components

import android.content.Context
import android.media.MediaPlayer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alijafari.raise.R
import com.alijafari.raise.core.ui.components.PopupDialog
import com.alijafari.raise.feature_ringtone.data.infrastructure.RingtonePreviewPlayerImpl
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData

@Composable
fun RingtoneSelectorDialog(
    isVisible: Boolean,
    selectedRingtone: RingtoneData?,
    selectedVolume: Float,
    onDismiss: () -> Unit,
    ringtonesList: List<RingtoneData>,
    ringtonePreviewPlayer: RingtonePreviewPlayerImpl,
    onSave: (ringtone: RingtoneData?, volume: Float) -> Unit,
) {
    var selectedRingtone by remember { mutableStateOf(selectedRingtone) }
    var selectedVolume by remember { mutableStateOf(selectedVolume) }

    var isPlaying by remember { mutableStateOf(false) }

    fun stopPlayer() {
        ringtonePreviewPlayer.stop()
        isPlaying = false
    }

    fun playSelected() {
        selectedRingtone?.let { ringtonePreviewPlayer.play(it,selectedVolume) }
        isPlaying = true
    }

    LaunchedEffect(selectedRingtone) {
        stopPlayer()
    }
    LaunchedEffect(isVisible) {
        if (!isVisible){
            ringtonePreviewPlayer.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopPlayer()
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
                Log.e("TAG", "RingtoneSelectorDialog: $selectedRingtone $ringtone", )
                val isSelected = selectedRingtone?.uri == ringtone.uri
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
                    Text(ringtone.name ?: "Unknown")
                }
            }
        }
        LaunchedEffect(Unit) {
            listState.animateScrollToItem(ringtonesList.indexOf(selectedRingtone).takeIf { it >=0 } ?: 0, 0)
        }
        Spacer(modifier = Modifier.height(7.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    if (selectedVolume == 0f) R.drawable.ic_speaker_disabled
                    else if (selectedVolume <= 0.4f) R.drawable.ic_speaker_low
                    else R.drawable.ic_speaker_loud
                ),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )

            Spacer(Modifier.width(5.dp))

            Slider(
                modifier = Modifier.weight(1f),
                value = selectedVolume,
                onValueChange = {
                    selectedVolume = it
                    ringtonePreviewPlayer.setVolume(it)
                }
            )

            Spacer(Modifier.width(6.dp))


            AnimatedImageVector.animatedVectorResource(if (isPlaying) R.drawable.ic_pause_animated else R.drawable.ic_play_animated)
            IconButton(onClick = {
                val ringtone = selectedRingtone ?: return@IconButton

                if (isPlaying) {
                    stopPlayer()
                } else {
                    playSelected()
                }
            }) {
                Image(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(
                            if (isPlaying) R.drawable.ic_pause_animated
                            else R.drawable.ic_play_animated
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
