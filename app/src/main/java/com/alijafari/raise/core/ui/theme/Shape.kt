package com.alijafari.raise.core.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
enum class CardPosition { FIRST, MIDDLE, LAST ,ONLY}

@Composable
fun AlarmCard(
    modifier: Modifier,
    position: CardPosition,
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val topRadius by animateDpAsState(
        targetValue = if (position in listOf(CardPosition.FIRST, CardPosition.ONLY)) 18.dp else 4.dp,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = ""
    )

    val bottomRadius by animateDpAsState(
        targetValue = if (position in listOf(CardPosition.LAST, CardPosition.ONLY)) 18.dp else 4.dp,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = ""
    )

    val containerColor by animateColorAsState(
        targetValue = if (enabled)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = ""
    )

    val contentColor by animateColorAsState(
        targetValue = if (enabled)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = ""
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = topRadius,
            topEnd = topRadius,
            bottomStart = bottomRadius,
            bottomEnd = bottomRadius
        ),
        colors = CardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (enabled)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        enabled = true,
        onClick = {
            onClick()
        },
        content = content
    )
}
