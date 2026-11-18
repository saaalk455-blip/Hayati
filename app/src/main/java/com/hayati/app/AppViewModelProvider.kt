package com.hayati.app

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hayati.app.feature.adhkar.AdhkarViewModel
import com.hayati.app.feature.home.HomeViewModel
import com.hayati.app.feature.prayer.PrayerHistoryViewModel
import com.hayati.app.feature.prayer.PrayerSettingsViewModel
import com.hayati.app.feature.prayer.PrayerViewModel
import com.hayati.app.feature.tasks.TasksViewModel

/** Single place that builds every ViewModel with its dependencies from [AppContainer]. */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val container = hayatiApplication().container
            HomeViewModel(container.taskRepository, container.timeProvider, container.prayerSnapshotProvider)
        }
        initializer {
            val container = hayatiApplication().container
            TasksViewModel(container.taskRepository, container.timeProvider)
        }
        initializer {
            val container = hayatiApplication().container
            PrayerViewModel(container.prayerSnapshotProvider, container.prayerLogRepository, container.timeProvider)
        }
        initializer {
            val container = hayatiApplication().container
            PrayerHistoryViewModel(container.prayerSettingsRepository, container.prayerLogRepository, container.timeProvider)
        }
        initializer {
            val container = hayatiApplication().container
            PrayerSettingsViewModel(
                repository = container.prayerSettingsRepository,
                locationProvider = container.locationProvider,
                scheduler = container.alarmScheduler,
                permission = container.notificationPresenter,
            )
        }
        initializer {
            val container = hayatiApplication().container
            AdhkarViewModel(container.adhkarContentSource, container.adhkarProgressRepository, container.timeProvider)
        }
    }
}

private fun CreationExtras.hayatiApplication(): HayatiApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HayatiApplication
