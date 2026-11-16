package com.hayati.app.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hayati.app.R
import com.hayati.app.core.designsystem.components.CrescentDecoration
import com.hayati.app.core.designsystem.components.HayatiCard
import com.hayati.app.core.designsystem.components.IconBadge
import com.hayati.app.core.designsystem.components.SectionTitle
import com.hayati.app.core.designsystem.components.UnavailableBadge
import com.hayati.app.core.designsystem.theme.HayatiPalette
import com.hayati.app.core.designsystem.theme.HayatiSpacing
import com.hayati.app.core.designsystem.theme.LocalHayatiColors
import com.hayati.app.domain.model.OverviewEntry
import com.hayati.app.domain.model.OverviewItem
import com.hayati.app.domain.model.OverviewValue
import com.hayati.app.feature.worship.WorshipSection
import com.hayati.app.feature.worship.icon

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenSection: (WorshipSection) -> Unit,
    onOpenTasks: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = HayatiSpacing.ScreenHorizontal,
            vertical = HayatiSpacing.ScreenVertical,
        ),
        verticalArrangement = Arrangement.spacedBy(HayatiSpacing.Section),
    ) {
        item(key = "header") { HomeHeader(onOpenSettings = onOpenSettings) }
        item(key = "welcome") { WelcomeCard() }
        item(key = "today_title") { SectionTitle(stringResource(R.string.today_title)) }
        item(key = "overview") { OverviewCard(entries = state.overview) }
        item(key = "quick_title") { SectionTitle(stringResource(R.string.quick_actions_title)) }
        item(key = "quick") {
            QuickActionsRow(
                onOpenSection = onOpenSection,
                onOpenTasks = onOpenTasks,
            )
        }
    }
}

@Composable
private fun HomeHeader(onOpenSettings: () -> Unit) {
    val colors = LocalHayatiColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Simple original typographic identity: the letter "ح" in a gold-ringed circle.
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .border(1.5.dp, colors.gold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ح",
                style = MaterialTheme.typography.titleLarge,
                color = colors.gold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(44.dp)
                .background(colors.card, CircleShape)
                .border(1.dp, colors.border, CircleShape),
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.home_settings),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun WelcomeCard() {
    val shape = MaterialTheme.shapes.extraLarge
    val colors = LocalHayatiColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(HayatiPalette.Primary, HayatiPalette.Secondary),
                ),
                shape = shape,
            )
            .border(1.dp, colors.border, shape),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.gold,
                )
                Text(
                    text = stringResource(R.string.welcome_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            CrescentDecoration(modifier = Modifier.size(104.dp))
        }
    }
}

@Composable
private fun OverviewCard(entries: List<OverviewEntry>) {
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        entries.forEachIndexed { index, entry ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = LocalHayatiColors.current.border,
                )
            }
            OverviewRow(entry = entry)
        }
    }
}

@Composable
private fun OverviewRow(entry: OverviewEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(entry.item.labelRes()),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        when (val value = entry.value) {
            OverviewValue.Unavailable -> UnavailableBadge()
            is OverviewValue.Available -> Text(
                text = value.text,
                style = MaterialTheme.typography.titleSmall,
                color = LocalHayatiColors.current.success,
            )
        }
    }
}

@StringRes
private fun OverviewItem.labelRes(): Int = when (this) {
    OverviewItem.NextPrayer -> R.string.overview_next_prayer
    OverviewItem.TimeRemaining -> R.string.overview_time_remaining
    OverviewItem.QuranWird -> R.string.overview_quran_wird
    OverviewItem.Adhkar -> R.string.overview_adhkar
    OverviewItem.Tasks -> R.string.overview_tasks
}

@Composable
private fun QuickActionsRow(
    onOpenSection: (WorshipSection) -> Unit,
    onOpenTasks: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HayatiSpacing.Item),
    ) {
        QuickAction(
            icon = WorshipSection.Quran.icon,
            label = stringResource(R.string.section_quran),
            onClick = { onOpenSection(WorshipSection.Quran) },
            modifier = Modifier.weight(1f),
        )
        QuickAction(
            icon = WorshipSection.Adhkar.icon,
            label = stringResource(R.string.section_adhkar),
            onClick = { onOpenSection(WorshipSection.Adhkar) },
            modifier = Modifier.weight(1f),
        )
        QuickAction(
            icon = WorshipSection.Tasbeeh.icon,
            label = stringResource(R.string.section_tasbeeh),
            onClick = { onOpenSection(WorshipSection.Tasbeeh) },
            modifier = Modifier.weight(1f),
        )
        QuickAction(
            icon = Icons.Filled.CheckCircle,
            label = stringResource(R.string.nav_tasks),
            onClick = onOpenTasks,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HayatiCard(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconBadge(icon = icon, size = 44)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
