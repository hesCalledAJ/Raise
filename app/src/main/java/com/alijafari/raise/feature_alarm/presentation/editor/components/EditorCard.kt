package com.alijafari.raise.feature_alarm.presentation.editor.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.alijafari.raise.core.ui.theme.CardPosition


@Composable
fun EditorCard(
    modifier: Modifier,
    icon: Painter?,
    title: String?,
    position: CardPosition,
    onClick: ()->Unit,
    colors : CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .padding(
                bottom = if (position in listOf(
                        CardPosition.LAST,
                        CardPosition.ONLY
                    )
                ) 8.dp else 4.dp
            )
            .height(45.dp),
        colors = colors,
        shape = RoundedCornerShape(
            topEnd = if (position in listOf(CardPosition.FIRST, CardPosition.ONLY)) 15.dp else 5.dp,
            topStart = if (position in listOf(
                    CardPosition.FIRST,
                    CardPosition.ONLY
                )
            ) 15.dp else 5.dp,
            bottomEnd = if (position in listOf(
                    CardPosition.LAST,
                    CardPosition.ONLY
                )
            ) 15.dp else 5.dp,
            bottomStart = if (position in listOf(
                    CardPosition.LAST,
                    CardPosition.ONLY
                )
            ) 15.dp else 5.dp,
        ),
        onClick = onClick
    ) {

        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.size(24.dp), painter = icon, contentDescription = title
                )
                Spacer(Modifier.width(7.dp))
            }
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.weight(1f))
            }
            Spacer(modifier.weight(1f))
            content()
        }

    }
}