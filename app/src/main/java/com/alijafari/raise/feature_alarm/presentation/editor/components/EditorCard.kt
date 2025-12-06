package com.alijafari.raise.feature_alarm.presentation.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alijafari.raise.core.ui.theme.CardPosition
@Composable
fun EditorCard(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    title: String? = null,
    subTitle: String? = null,
    position: CardPosition,
    onClick: () -> Unit = {},
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    bigContent: @Composable (() -> Unit)? = null,
    onBigContentClicked: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit = {},
) {
    val animatedShape by animateDpAsState(
        targetValue = if (position.isFirstOrOnly) 15.dp else 5.dp,
        label = ""
    )

    val animatedBottomShape by animateDpAsState(
        targetValue = if (position.isLastOrOnly) 15.dp else 5.dp,
        label = ""
    )

    val containerColor by animateColorAsState(
        targetValue = colors.containerColor,
        label = ""
    )

    val contentColor by animateColorAsState(
        targetValue = colors.contentColor,
        label = ""
    )

    Card(
        modifier = modifier
            .padding(
                bottom = if (position.isLastOrOnly) 8.dp else 4.dp
            )
            .heightIn(min = 51.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(
            topStart = animatedShape,
            topEnd = animatedShape,
            bottomStart = animatedBottomShape,
            bottomEnd = animatedBottomShape,
        ),
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        painter = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = LocalContentColor.current
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (subTitle != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }
                }
                content()
            }

            AnimatedVisibility(
                bigContent!=null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(7.dp)
                        )
                        .padding(15.dp)
                        .clickable(
                            enabled = onBigContentClicked!=null,
                            onClick = onBigContentClicked?:{},
                            role = Role.Button
                        )
                ){
                    bigContent?.invoke()
                }
            }
        }
    }
}

private val CardPosition.isFirstOrOnly: Boolean
    get() = this == CardPosition.FIRST || this == CardPosition.ONLY

private val CardPosition.isLastOrOnly: Boolean
    get() = this == CardPosition.LAST || this == CardPosition.ONLY