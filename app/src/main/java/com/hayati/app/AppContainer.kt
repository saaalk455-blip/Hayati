package com.hayati.app

import android.content.Context
import androidx.room.Room
import com.hayati.app.core.common.SystemTimeProvider
import com.hayati.app.core.common.TimeProvider
import com.hayati.app.data.local.HayatiDatabase
import com.hayati.app.data.repository.TaskRepositoryImpl
import com.hayati.app.domain.repository.TaskRepository

/**
 * Manual dependency container (constructor injection without a DI framework).
 * See docs/DECISIONS.md D-010 for why Hilt is not used yet.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val timeProvider: TimeProvider = SystemTimeProvider

    private val database: HayatiDatabase by lazy {
        Room.databaseBuilder(appContext, HayatiDatabase::class.java, HayatiDatabase.NAME).build()
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(dao = database.taskDao(), time = timeProvider)
    }
}
