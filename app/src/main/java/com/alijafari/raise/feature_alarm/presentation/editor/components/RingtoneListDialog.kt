package com.alijafari.raise.feature_alarm.presentation.editor.components

import android.media.MediaPlayer
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alijafari.raise.R
import com.alijafari.raise.core.ui.components.PopupDialog
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData


@Composable
fun RingtoneSelectorDialog(
    isVisible: Boolean,
    selectedRingtone: RingtoneData?,
    selectedVolume: Float,
    onDismiss: () -> Unit,
    ringtonesList: List<RingtoneData>,
    onSave: (ringtone: RingtoneData?, volume: Float) -> Unit,
) {
    var selectedRingtone by remember { mutableStateOf(selectedRingtone) }
    var selectedVolume by remember { mutableStateOf(selectedVolume) }

    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // When selected ringtone changes -> pause & release player
    LaunchedEffect(selectedRingtone) {
        mediaPlayer?.let {
            try {
                it.pause()
            } catch (_: Throwable) {
            }
            try {
                it.seekTo(0)
            } catch (_: Throwable) {
            }
            try {
                it.release()
            } catch (_: Throwable) {
            }
        }
        mediaPlayer = null
        isPlaying = false
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.let {
                try {
                    it.stop()
                } catch (_: Throwable) {
                }
                try {
                    it.release()
                } catch (_: Throwable) {
                }
            }
            mediaPlayer = null
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

        LaunchedEffect(selectedRingtone) {
            listState.animateScrollToItem(ringtonesList.indexOf(selectedRingtone), 0)
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .height(400.dp)
                .clip(shape = RoundedCornerShape(18.dp))
        ) {
            items(ringtonesList) { ringtone ->
                val isSelected = selectedRingtone == ringtone
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
                    mediaPlayer?.setVolume(it, it)
                }
            )

            Spacer(Modifier.width(6.dp))


            IconButton(
                onClick = {
                    val ringtone = selectedRingtone ?: return@IconButton
                    if (mediaPlayer == null) {
                        try {
                            mediaPlayer = ringtone.uri?.let {
                                MediaPlayer.create(context, it).apply {
                                    isLooping = false
                                    setVolume(selectedVolume, selectedVolume)
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }

                    mediaPlayer?.let { mp ->
                        if (isPlaying) {
                            try {
                                mp.pause()
                            } catch (_: Throwable) {
                            }
                            isPlaying = false
                        } else {
                            try {
                                mp.start()
                            } catch (_: Throwable) {
                            }
                            isPlaying = true
                        }
                    }
                }
            ) {
                val image =
                    AnimatedImageVector.animatedVectorResource(if (isPlaying) R.drawable.ic_pause_animated else R.drawable.ic_play_animated)
                var atEnd by remember { mutableStateOf(false) }
                Image(
                    painter = rememberAnimatedVectorPainter(image, atEnd),
                    contentDescription = "Timer",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            atEnd = !atEnd
                        },
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
