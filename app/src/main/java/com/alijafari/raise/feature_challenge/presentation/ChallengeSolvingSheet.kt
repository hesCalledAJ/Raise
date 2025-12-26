package com.alijafari.raise.feature_challenge.presentation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alijafari.raise.feature_alarm.data.service.ActiveChallenge
import com.alijafari.raise.feature_challenge.domain.model.ChallengeType
import kotlinx.coroutines.delay

@Composable
fun ChallengeSolvingSheet(
    challenge: ActiveChallenge?,
    onInputEntered: (String) -> Boolean
) {
    if (challenge == null) return

    var input by remember(challenge.id) { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val shake = remember { Animatable(0f) }
    val scale by animateFloatAsState(
        targetValue = if (showError) 0.96f else 1f,
        label = ""
    )
    val borderColor by animateColorAsState(
        targetValue = if (showError)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.outline,
        label = ""
    )

    LaunchedEffect(showError) {
        if (showError) {
            shake.snapTo(0f)
            repeat(4) {
                shake.animateTo(-12f, tween(40))
                shake.animateTo(12f, tween(40))
            }
            shake.animateTo(0f, tween(40))

            delay(300)
            showError = false
            input = ""
        }
    }

    fun submit() {
        val ok = onInputEntered(input)
        if (!ok) showError = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Solve to Dismiss",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = challenge.question,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(48.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Enter Answer") },
            singleLine = true,
            isError = showError,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (challenge.type == ChallengeType.MATH)
                    KeyboardType.Number else KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = shake.value
                    scaleX = scale
                    scaleY = scale
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor
            )
        )

        AnimatedVisibility(
            visible = showError,
            enter = slideInVertically { -10 } + fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                "Wrong answer",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { submit() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Submit")
        }
    }
}
