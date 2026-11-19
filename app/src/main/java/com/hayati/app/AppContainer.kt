package com.hayati.app

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.hayati.app.core.common.SystemTimeProvider
import com.hayati.app.core.common.TimeProvider
import com.hayati.app.core.notifications.AlarmManagerPrayerScheduler
import com.hayati.app.core.notifications.PrayerAutomation
import com.hayati.app.core.notifications.PrayerNotificationCoordinator
import com.hayati.app.core.notifications.PrayerNotificationPresenter
import com.hayati.app.data.adhkar.content.AssetAdhkarContentSource
import com.hayati.app.data.adhkar.repository.AdhkarProgressRepositoryImpl
import com.hayati.app.data.local.HayatiDatabase
import com.hayati.app.data.prayer.calculator.AdhanPrayerTimeCalculator
import com.hayati.app.data.prayer.location.AndroidLocationProvider
import com.hayati.app.data.prayer.location.LocationProvider
import com.hayati.app.data.prayer.repository.DataStorePrayerSettingsRepository
import com.hayati.app.data.prayer.repository.PrayerLogRepositoryImpl
import com.hayati.app.data.repository.TaskRepositoryImpl
import com.hayati.app.domain.adhkar.repository.AdhkarContentSource
import com.hayati.app.domain.adhkar.repository.AdhkarProgressRepository
import com.hayati.app.domain.adhkar.usecase.AdhkarHomeProvider
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.repository.PrayerAlarmScheduler
import com.hayati.app.domain.prayer.repository.PrayerLogRepository
import com.hayati.app.domain.prayer.repository.PrayerSettingsRepository
import com.hayati.app.domain.prayer.repository.PrayerTimeCalculator
import com.hayati.app.domain.prayer.usecase.PrayerSnapshotProvider
import com.hayati.app.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.Locale

private val Context.prayerSettingsDataStore by preferencesDataStore(name = "prayer_settings")

/**
 * Manual dependency container (constructor injection without a DI framework).
 * See docs/DECISIONS.md D-010 for why Hilt is not used yet.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val timeProvider: TimeProvider = SystemTimeProvider

    /** Application-lifetime scope for receiver work. Bound to the process, never GlobalScope. */
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: HayatiDatabase by lazy {
        Room.databaseBuilder(appContext, HayatiDatabase::class.java, HayatiDatabase.NAME)
            .addMigrations(HayatiDatabase.MIGRATION_1_2, HayatiDatabase.MIGRATION_2_3)
            .build()
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(dao = database.taskDao(), time = timeProvider)
    }

    val prayerSettingsRepository: PrayerSettingsRepository by lazy {
        // A region hint only: Egyptian authority is the default for Egypt, Muslim World League
        // elsewhere. The user can always change it in settings.
        val default = if (Locale.getDefault().country == "EG") {
            CalculationMethod.EGYPTIAN
        } else {
            CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
        DataStorePrayerSettingsRepository(appContext.prayerSettingsDataStore, default)
    }

    val prayerLogRepository: PrayerLogRepository by lazy {
        PrayerLogRepositoryImpl(dao = database.prayerLogDao(), time = timeProvider)
    }

    val adhkarContentSource: AdhkarContentSource by lazy { AssetAdhkarContentSource(appContext) }

    val adhkarProgressRepository: AdhkarProgressRepository by lazy {
        AdhkarProgressRepositoryImpl(dao = database.adhkarProgressDao(), time = timeProvider)
    }

    val adhkarHomeProvider: AdhkarHomeProvider by lazy {
        AdhkarHomeProvider(adhkarContentSource, adhkarProgressRepository)
    }

    val prayerTimeCalculator: PrayerTimeCalculator by lazy { AdhanPrayerTimeCalculator() }

    val locationProvider: LocationProvider by lazy { AndroidLocationProvider(appContext) }

    val prayerSnapshotProvider: PrayerSnapshotProvider by lazy {
        PrayerSnapshotProvider(prayerSettingsRepository, prayerLogRepository, prayerTimeCalculator)
    }

    val alarmScheduler: PrayerAlarmScheduler by lazy { AlarmManagerPrayerScheduler(appContext) }

    val notificationPresenter: PrayerNotificationPresenter by lazy { PrayerNotificationPresenter(appContext) }

    val notificationCoordinator: PrayerNotificationCoordinator by lazy {
        PrayerNotificationCoordinator(
            settingsRepository = prayerSettingsRepository,
            calculator = prayerTimeCalculator,
            scheduler = alarmScheduler,
            permission = notificationPresenter,
            time = timeProvider,
        )
    }

    /** Starts the settings-driven re-scheduling. Called once from [HayatiApplication.onCreate]. */
    fun startPrayerAutomation() {
        notificationPresenter.ensureChannels()
        PrayerAutomation(appContext, prayerSettingsRepository, notificationCoordinator, applicationScope).start()
    }
}
