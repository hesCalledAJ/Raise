package com.alijafari.raise.feature_challenge.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    onDismiss : ()->Unit,
    onChallengeSelected: (ChallengeType) -> Unit,
) {
    val options = listOf(
        ChallengeOption(
            type = ChallengeType.CAPTCHA,
            title = "Captcha",
            subtitle = "Type the displayed text",
            icon = Icons.Default.Keyboard
        ),
        ChallengeOption(
            type = ChallengeType.MATH,
            title = "Math",
            subtitle = "Solve arithmetic problems",
            icon = Icons.Default.Calculate
        ),
    )
    Column (
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Column(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth() , horizontalAlignment = Alignment.CenterHorizontally) {
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
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(options) { option ->
                ChallengeItemRow(
                    option = option,
                    onClick = { onChallengeSelected(option.type) }
                )
            }
        }
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Cancel")
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
            .clickable { onClick() }
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(5.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint =  MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = option.subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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