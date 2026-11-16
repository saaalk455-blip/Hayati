package com.hayati.app.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hayati.app.R
import com.hayati.app.core.designsystem.components.EmptyState
import com.hayati.app.core.designsystem.components.ScreenHeader
import com.hayati.app.core.designsystem.theme.HayatiSpacing

/** Tasks tab. Phase 2 only shows an honest empty state; there is no task storage yet. */
@Composable
fun TasksScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = HayatiSpacing.ScreenHorizontal,
                vertical = HayatiSpacing.ScreenVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(HayatiSpacing.Section),
    ) {
        ScreenHeader(
            title = stringResource(R.string.tasks_title),
            subtitle = stringResource(R.string.tasks_subtitle),
        )
        EmptyState(
            icon = Icons.Filled.CheckCircle,
            title = stringResource(R.string.tasks_empty_title),
            body = stringResource(R.string.tasks_empty_body),
        )
    }
}
