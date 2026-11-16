package com.hayati.app.feature.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hayati.app.R
import com.hayati.app.core.designsystem.components.EmptyState
import com.hayati.app.core.designsystem.components.ScreenHeader
import com.hayati.app.core.designsystem.icons.HayatiIcons
import com.hayati.app.core.designsystem.theme.HayatiSpacing

/** Statistics tab. No statistics or charts are invented; real data arrives with later features. */
@Composable
fun StatisticsScreen(modifier: Modifier = Modifier) {
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
            title = stringResource(R.string.stats_title),
            subtitle = stringResource(R.string.stats_subtitle),
        )
        EmptyState(
            icon = HayatiIcons.Statistics,
            title = stringResource(R.string.stats_empty_title),
            body = stringResource(R.string.stats_empty_body),
        )
    }
}
