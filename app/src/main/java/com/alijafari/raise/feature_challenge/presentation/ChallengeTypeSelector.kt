package com.alijafari.raise.feature_challenge.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alijafari.raise.feature_challenge.domain.model.ChallengeType


@Composable
fun ChallengeTypeSelector(
    modifier: Modifier = Modifier,
    onChallengeSelected: (ChallengeType) -> Unit,
) {
    val options = listOf(
        ChallengeOption(
            type = ChallengeType.MEMORY,
            title = "Memory",
            subtitle = "Match pairs of tiles",
            icon = Icons.Default.Memory
        ),
        ChallengeOption(
            type = ChallengeType.MATH,
            title = "Math",
            subtitle = "Solve arithmetic problems",
            icon = Icons.Default.Calculate
        ),
    )
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Select Challenge",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Choose a task to dismiss this alarm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        items(options) { option ->
            ChallengeItemRow(
                option = option,
                onClick = { onChallengeSelected(option.type) }
            )
        }
    }
}

@Composable
fun ChallengeItemRow(
    option: ChallengeOption,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = option.subtitle,
                fontSize = 14.sp
            )
        }
    }
}

data class ChallengeOption(
    val type: ChallengeType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)