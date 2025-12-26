package com.alijafari.raise.feature_challenge.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alijafari.raise.core.utils.ChallengeUtils
import com.alijafari.raise.feature_challenge.data.model.ChallengeFactory
import com.alijafari.raise.feature_challenge.domain.model.ChallengeModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeConfig(
    challenge: ChallengeModel,
    onDismiss: () -> Unit,
    onSave: (ChallengeModel) -> Unit,
) {
    var difficultyIndex by remember { mutableIntStateOf(challenge.difficulty.coerceIn(0, 4)) }
    var repeatCount by remember { mutableFloatStateOf(challenge.repeats.coerceIn(1, 5).toFloat()) }
    var previewData by remember {
        mutableStateOf(
             ChallengeFactory.generateChallengeData(challenge.type, difficultyIndex).first
        )
    }
    val difficultyOptions = (1..5).map { ChallengeUtils.difficultyLevel(it-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = challenge.type.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                )
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            ) {
                Text(
                    text = previewData,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = {
                        previewData = ChallengeFactory.generateChallengeData(challenge.type, difficultyIndex).first
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Regenerate")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Difficulty",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))
        DifficultyButtonGroup(
            difficultyIndex = difficultyIndex,
            options = difficultyOptions,
            onDifficultyChanged = { index ->
                difficultyIndex = index
                previewData = ChallengeFactory.generateChallengeData(challenge.type, index).first
            }
        )

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Repeats", style = MaterialTheme.typography.titleSmall)
            Text(
                "${repeatCount.toInt()} times",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = repeatCount,
            onValueChange = { repeatCount = it },
            valueRange = 1f..5f,
            steps = 3
        )

        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Cancel")
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    onSave(
                        challenge.copy(
                            difficulty = difficultyIndex,
                            repeats = repeatCount.toInt(),
                            data = previewData
                        )
                    )
                }
            ) {
                Text("Add Challenge")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DifficultyButtonGroup(
    difficultyIndex: Int,
    options: List<String>,
    onDifficultyChanged: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        // This is key for the "connected" look from the docs
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = difficultyIndex == index,
                onCheckedChange = { if (it) onDifficultyChanged(index) },
                // Shapes logic from the doc example for seamless connection
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier
                    .weight(1f) ,
            ) {
                // You can add logic for difficulty icons here if you like,
                // or just stick to the text labels.
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }
    }
}