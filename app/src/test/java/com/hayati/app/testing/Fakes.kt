package com.hayati.app.testing

import com.hayati.app.core.common.TimeProvider
import com.hayati.app.data.local.TaskDao
import com.hayati.app.data.local.TaskEntity
import com.hayati.app.domain.model.Task
import com.hayati.app.domain.model.TaskTitle
import com.hayati.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTimeProvider(
    var today: Long = 100L,
    var now: Long = 1_000L,
) : TimeProvider {
    override fun todayEpochDay(): Long = today

    override fun nowMillis(): Long = now
}

/** In-memory [TaskRepository]; set [failure] to make every write throw. */
class FakeTaskRepository(
    private val time: TimeProvider = FakeTimeProvider(),
) : TaskRepository {

    private val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L

    var failure: Exception? = null

    val allTasks: List<Task> get() = tasks.value

    fun seed(
        title: String,
        epochDay: Long,
        completed: Boolean = false,
    ): Task {
        val task = Task(
            id = nextId++,
            title = title,
            isCompleted = completed,
            epochDay = epochDay,
            createdAtMillis = time.nowMillis(),
            completedAtMillis = if (completed) time.nowMillis() else null,
        )
        tasks.update { it + task }
        return task
    }

    override fun observeTasksForDay(epochDay: Long): Flow<List<Task>> =
        tasks.map { all -> all.filter { it.epochDay == epochDay } }

    override suspend fun addTask(title: String, epochDay: Long) {
        failIfRequested()
        val clean = TaskTitle.sanitize(title) ?: throw IllegalArgumentException("blank title")
        seed(clean, epochDay)
    }

    override suspend fun updateTitle(id: Long, title: String) {
        failIfRequested()
        val clean = TaskTitle.sanitize(title) ?: throw IllegalArgumentException("blank title")
        tasks.update { all -> all.map { if (it.id == id) it.copy(title = clean) else it } }
    }

    override suspend fun setCompleted(id: Long, completed: Boolean) {
        failIfRequested()
        tasks.update { all ->
            all.map {
                if (it.id == id) {
                    it.copy(
                        isCompleted = completed,
                        completedAtMillis = if (completed) time.nowMillis() else null,
                    )
                } else {
                    it
                }
            }
        }
    }

    override suspend fun deleteTask(id: Long) {
        failIfRequested()
        tasks.update { all -> all.filterNot { it.id == id } }
    }

    private fun failIfRequested() {
        failure?.let { throw it }
    }
}

/** In-memory [TaskDao] that mimics the real query ordering. */
class FakeTaskDao : TaskDao {
    private val rows = MutableStateFlow<List<TaskEntity>>(emptyList())
    private var nextId = 1L

    val all: List<TaskEntity> get() = rows.value

    override fun observeByDay(epochDay: Long): Flow<List<TaskEntity>> =
        rows.map { list ->
            list.filter { it.epochDay == epochDay }
                .sortedWith(compareBy({ it.isCompleted }, { it.createdAtMillis }, { it.id }))
        }

    override suspend fun insert(task: TaskEntity): Long {
        val saved = task.copy(id = nextId++)
        rows.update { it + saved }
        return saved.id
    }

    override suspend fun updateTitle(id: Long, title: String) {
        rows.update { list -> list.map { if (it.id == id) it.copy(title = title) else it } }
    }

    override suspend fun setCompleted(id: Long, completed: Boolean, completedAtMillis: Long?) {
        rows.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(isCompleted = completed, completedAtMillis = completedAtMillis)
                } else {
                    it
                }
            }
        }
    }

    override suspend fun deleteById(id: Long) {
        rows.update { list -> list.filterNot { it.id == id } }
    }
}

// ---- Prayer fakes (Phase 4) ----

class FakePrayerSettingsRepository(
    initial: com.hayati.app.domain.prayer.model.PrayerSettings,
) : com.hayati.app.domain.prayer.repository.PrayerSettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: Flow<com.hayati.app.domain.prayer.model.PrayerSettings> = state
    var writes = 0
        private set

    val current get() = state.value

    override suspend fun update(transform: (com.hayati.app.domain.prayer.model.PrayerSettings) -> com.hayati.app.domain.prayer.model.PrayerSettings) {
        val updated = transform(state.value)
        if (updated != state.value) {
            writes++
            state.value = updated
        }
    }
}

class FakePrayerLogRepository : com.hayati.app.domain.prayer.repository.PrayerLogRepository {
    private val logs = MutableStateFlow<List<com.hayati.app.domain.prayer.model.PrayerLog>>(emptyList())

    override fun observeDay(epochDay: Long) = logs.map { all -> all.filter { it.epochDay == epochDay } }

    override fun observeRange(fromEpochDay: Long, toEpochDay: Long) =
        logs.map { all -> all.filter { it.epochDay in fromEpochDay..toEpochDay } }

    override suspend fun setCompleted(epochDay: Long, prayer: com.hayati.app.domain.prayer.model.Prayer, completed: Boolean) {
        val existing = logs.value.firstOrNull { it.epochDay == epochDay && it.prayer == prayer }
        logs.value = when {
            existing == null && !completed -> logs.value
            existing == null -> logs.value + com.hayati.app.domain.prayer.model.PrayerLog(
                id = logs.value.size + 1L, epochDay = epochDay, prayer = prayer,
                completed = true, completedAtMillis = 1L,
            )
            else -> logs.value.map { if (it === existing) it.copy(completed = completed) else it }
        }
    }
}

/** Deterministic times: every event at a fixed UTC hour on the requested date. */
class FakePrayerTimeCalculator(
    private val hoursUtc: Map<com.hayati.app.domain.prayer.model.PrayerEvent, Int> = mapOf(
        com.hayati.app.domain.prayer.model.PrayerEvent.FAJR to 3,
        com.hayati.app.domain.prayer.model.PrayerEvent.SUNRISE to 5,
        com.hayati.app.domain.prayer.model.PrayerEvent.DHUHR to 10,
        com.hayati.app.domain.prayer.model.PrayerEvent.ASR to 13,
        com.hayati.app.domain.prayer.model.PrayerEvent.MAGHRIB to 16,
        com.hayati.app.domain.prayer.model.PrayerEvent.ISHA to 18,
    ),
) : com.hayati.app.domain.prayer.repository.PrayerTimeCalculator {
    var failure: com.hayati.app.domain.prayer.model.PrayerCalculationException? = null
    var calls = 0
        private set

    override suspend fun calculate(
        location: com.hayati.app.domain.prayer.model.PrayerLocation,
        date: java.time.LocalDate,
        settings: com.hayati.app.domain.prayer.model.PrayerCalculationSettings,
    ): com.hayati.app.domain.prayer.model.PrayerTimes {
        calls++
        failure?.let { throw it }
        return com.hayati.app.domain.prayer.model.PrayerTimes(
            date = date,
            zone = location.zone,
            times = hoursUtc.mapValues { (_, hour) -> date.atTime(hour, 0).toInstant(java.time.ZoneOffset.UTC) },
        )
    }
}

class FakeAlarmScheduler : com.hayati.app.domain.prayer.repository.PrayerAlarmScheduler {
    val alarms = linkedMapOf<Int, com.hayati.app.domain.prayer.model.PlannedAlert>()
    var exactAllowed = true
    var scheduleCalls = 0
        private set

    override fun canScheduleExact() = exactAllowed

    override fun schedule(alert: com.hayati.app.domain.prayer.model.PlannedAlert) {
        scheduleCalls++
        alarms[alert.requestCode] = alert
    }

    override fun cancel(requestCode: Int) {
        alarms.remove(requestCode)
    }
}

class FakeNotificationPermission(var allowed: Boolean = true) :
    com.hayati.app.domain.prayer.repository.NotificationPermissionChecker {
    override fun areNotificationsAllowed() = allowed
}

class FakeLocationProvider(var result: com.hayati.app.data.prayer.location.LocationResult) :
    com.hayati.app.data.prayer.location.LocationProvider {
    var calls = 0
        private set

    override suspend fun requestCurrentLocation(): com.hayati.app.data.prayer.location.LocationResult {
        calls++
        return result
    }
}

class FakeAdhkarContentSource(
    var adhkar: List<com.hayati.app.domain.adhkar.model.Dhikr> = emptyList(),
    var failure: Exception? = null,
) : com.hayati.app.domain.adhkar.repository.AdhkarContentSource {
    override suspend fun load(): List<com.hayati.app.domain.adhkar.model.Dhikr> {
        failure?.let { throw it }
        return adhkar
    }
}

/** In-memory counters; set [failure] to make writes throw. */
class FakeAdhkarProgressRepository : com.hayati.app.domain.adhkar.repository.AdhkarProgressRepository {
    private val store = MutableStateFlow<Map<Triple<Long, String, String>, Int>>(emptyMap())
    var failure: Exception? = null

    override fun observeCounts(epochDay: Long, category: com.hayati.app.domain.adhkar.model.AdhkarCategory) =
        store.map { all ->
            all.filterKeys { it.first == epochDay && it.second == category.key }
                .mapKeys { it.key.third }
        }

    override suspend fun setCount(
        epochDay: Long,
        category: com.hayati.app.domain.adhkar.model.AdhkarCategory,
        dhikrId: String,
        count: Int,
    ) {
        failure?.let { throw it }
        store.update { it + (Triple(epochDay, category.key, dhikrId) to count) }
    }
}
