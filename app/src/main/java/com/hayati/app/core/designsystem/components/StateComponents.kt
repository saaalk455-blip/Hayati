package com.hayati.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hayati.app.R
import com.hayati.app.core.designsystem.theme.LocalHayatiColors

/** Round icon container used by empty states and shortcut cards. */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Int = 56,
) {
    val colors = LocalHayatiColors.current
    Box(
        modifier = modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.gold,
            modifier = Modifier.size((size / 2).dp),
        )
    }
}

/** Honest empty state: explains what will live here without showing any invented data. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    HayatiCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconBadge(icon = icon, size = 72)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Marks a value that has no real data yet, so it is never mistaken for stored or computed data. */
@Composable
fun UnavailableBadge(modifier: Modifier = Modifier) {
    val colors = LocalHayatiColors.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = colors.gold.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.45f)),
    ) {
        Text(
            text = stringResource(R.string.unavailable_badge),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.gold,
        )
    }
}
