package com.hayati.app.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hayati.app.AppViewModelProvider
import com.hayati.app.R
import com.hayati.app.core.designsystem.components.EmptyState
import com.hayati.app.core.designsystem.components.HayatiCard
import com.hayati.app.core.designsystem.components.ScreenHeader
import com.hayati.app.core.designsystem.theme.HayatiSpacing
import com.hayati.app.core.designsystem.theme.LocalHayatiColors
import com.hayati.app.domain.model.Task

/** Tasks tab: today's tasks, stored in the local database. */
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.refreshDay() }

    TasksContent(
        state = state,
        onNewTitleChange = viewModel::onNewTitleChange,
        onAddTask = viewModel::addTask,
        onToggle = viewModel::toggleCompleted,
        onEdit = viewModel::startEditing,
        onDelete = viewModel::requestDelete,
        onDismissError = viewModel::dismissError,
        modifier = modifier,
    )

    state.editing?.let { editing ->
        EditTaskDialog(
            editing = editing,
            onTextChange = viewModel::onEditTextChange,
            onConfirm = viewModel::confirmEdit,
            onCancel = viewModel::cancelEdit,
        )
    }
    state.pendingDelete?.let { task ->
        DeleteTaskDialog(
            task = task,
            onConfirm = viewModel::confirmDelete,
            onCancel = viewModel::cancelDelete,
        )
    }
}

@Composable
private fun TasksContent(
    state: TasksUiState,
    onNewTitleChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onToggle: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = HayatiSpacing.ScreenHorizontal,
            vertical = HayatiSpacing.ScreenVertical,
        ),
        verticalArrangement = Arrangement.spacedBy(HayatiSpacing.Item),
    ) {
        item(key = "header") {
            ScreenHeader(
                title = stringResource(R.string.tasks_title),
                subtitle = stringResource(R.string.tasks_subtitle),
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        if (state.tasks.isNotEmpty()) {
            item(key = "progress") {
                TasksProgress(completed = state.completedCount, total = state.tasks.size)
            }
        }
        item(key = "input") {
            AddTaskCard(
                value = state.newTitle,
                showError = state.showNewTitleError,
                onValueChange = onNewTitleChange,
                onAdd = onAddTask,
            )
        }
        if (state.operationFailed) {
            item(key = "error") { ErrorBanner(onDismiss = onDismissError) }
        }
        if (!state.isLoading) {
            if (state.tasks.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Filled.CheckCircle,
                        title = stringResource(R.string.tasks_empty_title),
                        body = stringResource(R.string.tasks_empty_body),
                    )
                }
            } else {
                items(state.tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onToggle = { onToggle(task) },
                        onEdit = { onEdit(task) },
                        onDelete = { onDelete(task) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TasksProgress(completed: Int, total: Int) {
    val colors = LocalHayatiColors.current
    Column {
        Text(
            text = stringResource(R.string.tasks_progress, completed, total),
            style = MaterialTheme.typography.labelLarge,
            color = if (completed == total) colors.success else colors.gold,
        )
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else completed.toFloat() / total },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = if (completed == total) colors.success else colors.gold,
            trackColor = colors.border,
        )
    }
}

@Composable
private fun AddTaskCard(
    value: String,
    showError: Boolean,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.tasks_input_hint)) },
                singleLine = true,
                isError = showError,
                supportingText = if (showError) {
                    { Text(stringResource(R.string.tasks_input_error)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
            )
            Spacer(Modifier.width(12.dp))
            FilledIconButton(
                onClick = onAdd,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.tasks_add),
                )
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalHayatiColors.current
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.success,
                    uncheckedColor = colors.textSecondary,
                    checkmarkColor = MaterialTheme.colorScheme.background,
                ),
            )
            Text(
                text = task.title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.isCompleted) colors.textSecondary else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            )
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.task_edit),
                    tint = colors.textSecondary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.task_delete),
                    tint = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(onDismiss: () -> Unit) {
    HayatiCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.tasks_error),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.tasks_error_dismiss))
            }
        }
    }
}

@Composable
private fun EditTaskDialog(
    editing: EditingTask,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = LocalHayatiColors.current.card,
        title = { Text(stringResource(R.string.task_edit_title)) },
        text = {
            OutlinedTextField(
                value = editing.text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = editing.showError,
                supportingText = if (editing.showError) {
                    { Text(stringResource(R.string.tasks_input_error)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm() }),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.task_save)) }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.task_cancel)) }
        },
    )
}

@Composable
private fun DeleteTaskDialog(
    task: Task,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = LocalHayatiColors.current.card,
        title = { Text(stringResource(R.string.task_delete_title)) },
        text = { Text(stringResource(R.string.task_delete_message, task.title)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.task_confirm_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.task_cancel)) }
        },
    )
}
