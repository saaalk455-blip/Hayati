package com.hayati.app.feature.worship

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hayati.app.R
import com.hayati.app.core.designsystem.components.EmptyState
import com.hayati.app.core.designsystem.components.HayatiCard
import com.hayati.app.core.designsystem.components.HayatiTopBar
import com.hayati.app.core.designsystem.components.IconBadge
import com.hayati.app.core.designsystem.components.ScreenHeader
import com.hayati.app.core.designsystem.theme.HayatiSpacing

/** Worship tab: introduction screen with a shortcut card for each worship section. */
@Composable
fun WorshipScreen(
    onOpenSection: (WorshipSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = HayatiSpacing.ScreenHorizontal,
            vertical = HayatiSpacing.ScreenVertical,
        ),
        horizontalArrangement = Arrangement.spacedBy(HayatiSpacing.Item),
        verticalArrangement = Arrangement.spacedBy(HayatiSpacing.Item),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ScreenHeader(
                title = stringResource(R.string.worship_title),
                subtitle = stringResource(R.string.worship_subtitle),
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        items(WorshipSection.entries, key = { it.route }) { section ->
            HayatiCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenSection(section) },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconBadge(icon = section.icon)
                    Text(
                        text = stringResource(section.labelRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(section.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Placeholder destination for a worship section that is not built yet. */
@Composable
fun WorshipSectionPlaceholderScreen(
    section: WorshipSection,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HayatiTopBar(title = stringResource(section.labelRes), onBack = onBack)
        Column(
            modifier = Modifier.padding(
                horizontal = HayatiSpacing.ScreenHorizontal,
                vertical = HayatiSpacing.ScreenVertical,
            ),
        ) {
            EmptyState(
                icon = section.icon,
                title = stringResource(R.string.coming_soon_title),
                body = stringResource(R.string.coming_soon_body),
            )
        }
    }
}
