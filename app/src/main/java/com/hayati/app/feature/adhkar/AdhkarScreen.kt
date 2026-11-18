package com.hayati.app.feature.adhkar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hayati.app.AppViewModelProvider
import com.hayati.app.R
import com.hayati.app.core.designsystem.components.EmptyState
import com.hayati.app.core.designsystem.components.HayatiCard
import com.hayati.app.core.designsystem.components.HayatiTopBar
import com.hayati.app.core.designsystem.theme.HayatiSpacing
import com.hayati.app.core.designsystem.theme.LocalHayatiColors
import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.CategoryProgress
import com.hayati.app.domain.adhkar.model.DhikrProgress

@Composable
fun AdhkarScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: AdhkarCategory? = null,
    viewModel: AdhkarViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.refreshDay() }
    LaunchedEffect(initialCategory) { initialCategory?.let(viewModel::select) }
    Column(modifier = modifier.fillMaxSize()) {
        HayatiTopBar(title = stringResource(R.string.adhkar_title), onBack = onBack)
        AdhkarContent(
            state = state,
            onSelect = viewModel::select,
            onTap = viewModel::tap,
            onUndo = viewModel::undo,
            onNext = viewModel::next,
            onPrevious = viewModel::previous,
            onDismissError = viewModel::dismissError,
        )
    }
}

/** Stateless so it can be tested without a ViewModel. */
@Composable
fun AdhkarContent(
    state: AdhkarUiState,
    onSelect: (AdhkarCategory) -> Unit,
    onTap: () -> Unit,
    onUndo: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(HayatiSpacing.ScreenHorizontal, HayatiSpacing.ScreenVertical),
        verticalArrangement = Arrangement.spacedBy(HayatiSpacing.Item),
    ) {
        when (state.status) {
            AdhkarStatus.LOADING -> Text(
                text = stringResource(R.string.adhkar_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AdhkarStatus.ERROR -> EmptyState(
                icon = Icons.Filled.Check,
                title = stringResource(R.string.adhkar_error_title),
                body = stringResource(R.string.adhkar_error_body),
            )
            AdhkarStatus.EMPTY -> EmptyState(
                icon = Icons.Filled.Check,
                title = stringResource(R.string.adhkar_empty_title),
                body = stringResource(R.string.adhkar_empty_body),
            )
            AdhkarStatus.READY -> Ready(state, onSelect, onTap, onUndo, onNext, onPrevious, onDismissError)
        }
    }
}

@Composable
private fun Ready(
    state: AdhkarUiState,
    onSelect: (AdhkarCategory) -> Unit,
    onTap: () -> Unit,
    onUndo: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDismissError: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        state.categories.forEach { category ->
            FilterChip(
                selected = category.category == state.selected,
                onClick = { onSelect(category.category) },
                label = { Text("${stringResource(category.category.labelRes())} ${category.doneCount}/${category.total}") },
            )
        }
    }
    if (state.saveFailed) {
        HayatiCard(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.adhkar_save_failed), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onDismissError) { Text(stringResource(R.string.adhkar_dismiss)) }
        }
    }
    val progress = state.selectedProgress
    val current = state.current
    if (progress == null || current == null) {
        Text(stringResource(R.string.adhkar_category_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    CategorySummary(progress)
    DhikrCard(current, state.currentIndex, progress.total, onTap)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = onPrevious, enabled = state.currentIndex > 0) {
            Text(stringResource(R.string.adhkar_previous))
        }
        OutlinedButton(onClick = onUndo, enabled = current.count > 0) {
            Text(stringResource(R.string.adhkar_undo))
        }
        OutlinedButton(onClick = onNext, enabled = state.currentIndex < progress.total - 1) {
            Text(stringResource(R.string.adhkar_next))
        }
    }
}

@Composable
private fun CategorySummary(progress: CategoryProgress) {
    val colors = LocalHayatiColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = if (progress.isComplete) {
                stringResource(R.string.adhkar_all_done)
            } else {
                stringResource(R.string.adhkar_progress_format, progress.doneCount, progress.total)
            },
            style = MaterialTheme.typography.titleSmall,
            color = if (progress.isComplete) colors.success else MaterialTheme.colorScheme.onSurface,
        )
        LinearProgressIndicator(
            progress = { if (progress.total == 0) 0f else progress.doneCount.toFloat() / progress.total },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DhikrCard(current: DhikrProgress, index: Int, total: Int, onTap: () -> Unit) {
    val colors = LocalHayatiColors.current
    val description = stringResource(R.string.adhkar_tap_description, current.remaining)
    HayatiCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        onClick = if (current.isDone) null else onTap,
    ) {
        Text(
            text = stringResource(R.string.adhkar_item_position, index + 1, total),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = current.dhikr.text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        if (current.dhikr.source.isNotEmpty()) {
            Text(
                text = stringResource(R.string.adhkar_source_format, current.dhikr.source),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (current.dhikr.takhrij.isNotEmpty()) {
            Text(
                text = stringResource(R.string.adhkar_takhrij_format, current.dhikr.takhrij),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(R.string.adhkar_repeat_format, current.count, current.dhikr.repeat),
            style = MaterialTheme.typography.titleMedium,
            color = if (current.isDone) colors.success else colors.gold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )
        Text(
            text = if (current.isDone) stringResource(R.string.adhkar_done) else stringResource(R.string.adhkar_tap_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun AdhkarCategory.labelRes(): Int = when (this) {
    AdhkarCategory.MORNING -> R.string.adhkar_cat_morning
    AdhkarCategory.EVENING -> R.string.adhkar_cat_evening
    AdhkarCategory.AFTER_PRAYER -> R.string.adhkar_cat_after_prayer
}
