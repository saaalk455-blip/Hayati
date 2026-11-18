package com.hayati.app.feature.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hayati.app.AppViewModelProvider
import com.hayati.app.R
import com.hayati.app.core.common.HijriDateFormatter
import com.hayati.app.core.common.TimeFormatter
import com.hayati.app.core.common.labelRes
import com.hayati.app.core.designsystem.components.HayatiCard
import com.hayati.app.core.designsystem.components.HayatiTopBar
import com.hayati.app.core.designsystem.components.SectionTitle
import com.hayati.app.core.designsystem.theme.HayatiPalette
import com.hayati.app.core.designsystem.theme.HayatiSpacing
import com.hayati.app.core.designsystem.theme.LocalHayatiColors
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.PrayerErrorReason
import com.hayati.app.domain.prayer.model.PrayerEvent
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerRow
import com.hayati.app.domain.prayer.model.PrayerRowState
import com.hayati.app.domain.prayer.model.PrayerSnapshot

@Composable
fun PrayerScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val snapshot by viewModel.uiState.collectAsStateWithLifecycle()
    val saveFailed by viewModel.saveFailed.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        HayatiTopBar(title = stringResource(R.string.prayer_screen_title), onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = HayatiSpacing.ScreenHorizontal,
                vertical = HayatiSpacing.ScreenVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(HayatiSpacing.Item),
        ) {
            if (saveFailed) {
                item(key = "error") {
                    HayatiCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.prayer_save_error),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            TextButton(onClick = viewModel::dismissError) { Text(stringResource(R.string.tasks_error_dismiss)) }
                        }
                    }
                }
            }
            when (val state = snapshot) {
                PrayerSnapshot.Loading -> item(key = "loading") {
                    Text(
                        text = stringResource(R.string.prayer_loading),
                        color = LocalHayatiColors.current.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                PrayerSnapshot.NotConfigured -> item(key = "setup") {
                    MessageCard(
                        title = stringResource(R.string.prayer_not_configured_title),
                        body = stringResource(R.string.prayer_not_configured_body),
                        actionLabel = stringResource(R.string.prayer_set_location),
                        onAction = onOpenSettings,
                    )
                }
                is PrayerSnapshot.Error -> item(key = "error_state") {
                    MessageCard(
                        title = stringResource(R.string.prayer_screen_title),
                        body = stringResource(
                            if (state.reason == PrayerErrorReason.INVALID_LOCATION) R.string.prayer_error_location
                            else R.string.prayer_error_calculation,
                        ),
                        actionLabel = stringResource(R.string.prayer_settings),
                        onAction = onOpenSettings,
                    )
                }
                is PrayerSnapshot.Ready -> readyItems(
                    state = state,
                    onOpenSettings = onOpenSettings,
                    onOpenHistory = onOpenHistory,
                    onToggle = viewModel::setCompleted,
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.readyItems(
    state: PrayerSnapshot.Ready,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onToggle: (com.hayati.app.domain.prayer.model.Prayer, Boolean) -> Unit,
) {
    item(key = "location") { LocationHeader(state, onUpdate = onOpenSettings) }
    item(key = "next") { NextPrayerCard(state) }
    item(key = "summary") { SummaryCard(state) }
    item(key = "schedule_title") { SectionTitle(stringResource(R.string.prayer_schedule_title)) }
    item(key = "schedule") {
        HayatiCard(modifier = Modifier.fillMaxWidth()) {
            state.rows.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LocalHayatiColors.current.border)
                PrayerRowItem(row = row, state = state, onToggle = onToggle)
            }
        }
    }
    item(key = "actions") {
        Row(horizontalArrangement = Arrangement.spacedBy(HayatiSpacing.Item), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onOpenHistory, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.prayer_history)) }
            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.prayer_settings)) }
        }
    }
}

@Composable
private fun MessageCard(title: String, body: String, actionLabel: String, onAction: () -> Unit) {
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = body,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onAction) { Text(actionLabel) }
    }
}

/** Human-readable place name; automatic fixes have no city name, so coordinates are shown. */
@Composable
fun locationTitle(location: PrayerLocation): String =
    if (location.name.isNotBlank()) location.name else stringResource(R.string.prayer_location_current)

@Composable
fun locationSubtitle(location: PrayerLocation): String = when {
    location.country.isNotBlank() -> location.country
    else -> stringResource(
        R.string.prayer_location_coordinates,
        "%.2f".format(java.util.Locale.ROOT, location.latitude),
        "%.2f".format(java.util.Locale.ROOT, location.longitude),
    )
}

@Composable
private fun LocationHeader(state: PrayerSnapshot.Ready, onUpdate: () -> Unit) {
    val colors = LocalHayatiColors.current
    val hijri = HijriDateFormatter.format(state.date, state.hijriOffsetDays)
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.gold)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = locationTitle(state.location),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = locationSubtitle(state.location) + " · " + stringResource(
                        if (state.location.source == LocationSource.AUTOMATIC) R.string.prayer_location_auto_tag
                        else R.string.prayer_location_manual_tag,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                if (hijri != null) {
                    Text(text = hijri, style = MaterialTheme.typography.bodySmall, color = colors.gold)
                }
            }
            TextButton(onClick = onUpdate) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text(text = stringResource(R.string.prayer_update_location), modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

@Composable
private fun NextPrayerCard(state: PrayerSnapshot.Ready) {
    val next = state.next ?: return
    val colors = LocalHayatiColors.current
    val shape = MaterialTheme.shapes.extraLarge
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(HayatiPalette.Primary, HayatiPalette.Secondary)), shape)
            .border(1.dp, colors.border, shape),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.prayer_next_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(next.prayer.labelRes()) +
                    if (next.isTomorrow) " (" + stringResource(R.string.prayer_next_tomorrow) + ")" else "",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.gold,
            )
            Text(
                text = clockText(next.time, state),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.prayer_remaining, TimeFormatter.countdown(state.remainingMillis ?: 0L)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun clockText(instant: java.time.Instant, state: PrayerSnapshot.Ready): String =
    TimeFormatter.clock(instant, state.zone, state.use24Hour, stringResource(R.string.time_am), stringResource(R.string.time_pm))

@Composable
private fun SummaryCard(state: PrayerSnapshot.Ready) {
    val colors = LocalHayatiColors.current
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.prayer_summary_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.prayer_summary_percent, state.summary.percent),
                style = MaterialTheme.typography.titleMedium,
                color = if (state.summary.percent == 100) colors.success else colors.gold,
            )
        }
        androidx.compose.material3.LinearProgressIndicator(
            progress = { state.summary.percent / 100f },
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            color = colors.success,
            trackColor = colors.border,
        )
        Text(
            text = stringResource(R.string.prayer_summary_done, state.summary.completed) + " · " +
                stringResource(R.string.prayer_summary_remaining, state.summary.remaining),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.prayer_summary_note),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun PrayerRowItem(
    row: PrayerRow,
    state: PrayerSnapshot.Ready,
    onToggle: (com.hayati.app.domain.prayer.model.Prayer, Boolean) -> Unit,
) {
    val colors = LocalHayatiColors.current
    val prayer = row.event.prayer
    val started = row.time.toEpochMilli() <= state.nowMillis
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(row.event.labelRes()),
                style = MaterialTheme.typography.titleMedium,
                color = if (row.state == PrayerRowState.NEXT) colors.gold else MaterialTheme.colorScheme.onSurface,
            )
            if (prayer == null) {
                Text(text = stringResource(R.string.prayer_sunrise_note), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            } else {
                Text(
                    text = stateLabel(row.state) + " · " + stringResource(
                        if (row.notificationActive == true) R.string.prayer_notification_on else R.string.prayer_notification_off,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = stateColor(row.state),
                )
            }
        }
        Text(
            text = clockText(row.time, state),
            style = MaterialTheme.typography.titleMedium,
            color = if (row.event == PrayerEvent.SUNRISE) colors.textSecondary else MaterialTheme.colorScheme.onSurface,
        )
        if (prayer != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 8.dp)) {
                Checkbox(
                    checked = row.completed == true,
                    onCheckedChange = { onToggle(prayer, it) },
                    enabled = started || row.completed == true,
                )
                Text(
                    text = stringResource(
                        when {
                            row.completed == true -> R.string.prayer_undo
                            started -> R.string.prayer_mark_done
                            else -> R.string.prayer_not_yet
                        },
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun stateLabel(state: PrayerRowState): String = stringResource(
    when (state) {
        PrayerRowState.UPCOMING -> R.string.prayer_state_upcoming
        PrayerRowState.NEXT -> R.string.prayer_state_next
        PrayerRowState.PASSED -> R.string.prayer_state_passed
        PrayerRowState.COMPLETED -> R.string.prayer_state_completed
        PrayerRowState.UNRECORDED -> R.string.prayer_state_unrecorded
    },
)

@Composable
private fun stateColor(state: PrayerRowState): Color {
    val colors = LocalHayatiColors.current
    return when (state) {
        PrayerRowState.COMPLETED -> colors.success
        PrayerRowState.NEXT -> colors.gold
        else -> colors.textSecondary
    }
}
