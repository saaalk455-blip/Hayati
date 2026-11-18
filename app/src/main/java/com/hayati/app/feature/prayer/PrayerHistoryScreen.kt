package com.hayati.app.feature.prayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hayati.app.AppViewModelProvider
import com.hayati.app.R
import com.hayati.app.core.common.HijriDateFormatter
import com.hayati.app.core.common.labelRes
import com.hayati.app.core.designsystem.components.EmptyState
import com.hayati.app.core.designsystem.components.HayatiCard
import com.hayati.app.core.designsystem.components.HayatiTopBar
import com.hayati.app.core.designsystem.theme.HayatiSpacing
import com.hayati.app.core.designsystem.theme.LocalHayatiColors
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.usecase.HistoryDay
import java.time.LocalDate

@Composable
fun PrayerHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrayerHistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize()) {
        HayatiTopBar(title = stringResource(R.string.history_title), onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(HayatiSpacing.ScreenHorizontal, HayatiSpacing.ScreenVertical),
            verticalArrangement = Arrangement.spacedBy(HayatiSpacing.Item),
        ) {
            item(key = "subtitle") {
                Text(
                    text = stringResource(R.string.history_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.loaded && state.days.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Filled.Check,
                        title = stringResource(R.string.history_empty_title),
                        body = stringResource(R.string.history_empty_body),
                    )
                }
            }
            items(state.days, key = { it.epochDay }) { day -> HistoryRow(day = day, todayEpochDay = state.todayEpochDay) }
        }
    }
}

@Composable
private fun HistoryRow(day: HistoryDay, todayEpochDay: Long) {
    val colors = LocalHayatiColors.current
    val date = LocalDate.ofEpochDay(day.epochDay)
    val title = when (todayEpochDay - day.epochDay) {
        0L -> stringResource(R.string.history_today)
        1L -> stringResource(R.string.history_yesterday)
        else -> date.toString()
    }
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                HijriDateFormatter.format(date)?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
            }
            Text(
                text = stringResource(R.string.history_day_count, day.count),
                style = MaterialTheme.typography.titleMedium,
                color = if (day.count == Prayer.entries.size) colors.success else colors.gold,
            )
        }
        // Only prayers the user recorded are listed; nothing is implied about the others.
        val names = Prayer.entries.filter { it in day.completed }.map { stringResource(it.labelRes()) }
        Text(
            text = names.joinToString("، "),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
