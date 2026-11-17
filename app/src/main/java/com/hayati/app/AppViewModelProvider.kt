package com.hayati.app

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hayati.app.feature.home.HomeViewModel
import com.hayati.app.feature.tasks.TasksViewModel

/** Single place that builds every ViewModel with its dependencies from [AppContainer]. */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val container = hayatiApplication().container
            HomeViewModel(container.taskRepository, container.timeProvider)
        }
        initializer {
            val container = hayatiApplication().container
            TasksViewModel(container.taskRepository, container.timeProvider)
        }
    }
}

private fun CreationExtras.hayatiApplication(): HayatiApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HayatiApplication
