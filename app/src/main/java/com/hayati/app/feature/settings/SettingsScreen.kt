package com.hayati.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hayati.app.BuildConfig
import com.hayati.app.R
import com.hayati.app.core.designsystem.components.HayatiCard
import com.hayati.app.core.designsystem.components.HayatiTopBar
import com.hayati.app.core.designsystem.theme.HayatiSpacing

/**
 * Settings screen. It intentionally has no switches or toggles: nothing is persisted yet, so a
 * control would only pretend to save a choice. It shows read-only information and the app version.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        HayatiTopBar(title = stringResource(R.string.settings_title), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = HayatiSpacing.ScreenHorizontal,
                    vertical = HayatiSpacing.ScreenVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(HayatiSpacing.Item),
        ) {
            InfoCard(
                title = stringResource(R.string.settings_appearance_title),
                body = stringResource(R.string.settings_appearance_body),
            )
            InfoCard(
                title = stringResource(R.string.settings_language_title),
                body = stringResource(R.string.settings_language_body),
            )
            HayatiCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.settings_about_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.settings_version_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = stringResource(R.string.settings_privacy_note),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = body,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
