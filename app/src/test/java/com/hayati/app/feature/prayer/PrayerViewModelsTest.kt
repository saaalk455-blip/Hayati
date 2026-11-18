package com.hayati.app.feature.prayer

import com.hayati.app.data.prayer.location.LocationResult
import com.hayati.app.domain.prayer.model.AsrMethod
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.KnownCities
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.NotificationSound
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerSettings
import com.hayati.app.domain.prayer.model.PrayerSnapshot
import com.hayati.app.domain.prayer.usecase.ManualLocationError
import com.hayati.app.domain.prayer.usecase.PrayerSnapshotProvider
import com.hayati.app.testing.FakeAlarmScheduler
import com.hayati.app.testing.FakeLocationProvider
import com.hayati.app.testing.FakeNotificationPermission
import com.hayati.app.testing.FakePrayerLogRepository
import com.hayati.app.testing.FakePrayerSettingsRepository
import com.hayati.app.testing.FakePrayerTimeCalculator
import com.hayati.app.testing.FakeTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerViewModelsTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val cairoLocation = PrayerLocation("القاهرة", "مصر", 30.0, 31.0, "UTC", LocationSource.MANUAL)
    private val configured = PrayerSettings.default(CalculationMethod.MUSLIM_WORLD_LEAGUE).copy(location = cairoLocation)
    private val day = LocalDate.of(2026, 6, 15)

    private fun atHour(hour: Int) = day.atTime(hour, 0).toInstant(java.time.ZoneOffset.UTC).toEpochMilli()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    // ---------- PrayerViewModel ----------

    private fun TestScope.prayerVm(nowHour: Int, logs: FakePrayerLogRepository = FakePrayerLogRepository()): PrayerViewModel {
        val time = FakeTimeProvider(now = atHour(nowHour))
        val provider = PrayerSnapshotProvider(FakePrayerSettingsRepository(configured), logs, FakePrayerTimeCalculator())
        val vm = PrayerViewModel(provider, logs, time, ticker = flowOf(atHour(nowHour)))
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }
        return vm
    }

    @Test
    fun `a passed prayer can be recorded and reverted by the user`() = runTest(dispatcher) {
        val logs = FakePrayerLogRepository()
        val vm = prayerVm(nowHour = 14, logs = logs)

        vm.setCompleted(Prayer.FAJR, true)
        assertEquals(1, (vm.uiState.value as PrayerSnapshot.Ready).summary.completed)

        vm.setCompleted(Prayer.FAJR, false)
        assertEquals(0, (vm.uiState.value as PrayerSnapshot.Ready).summary.completed)
    }

    @Test
    fun `a prayer whose time has not come cannot be recorded as prayed`() = runTest(dispatcher) {
        val vm = prayerVm(nowHour = 14) // Maghrib (16:00) and Isha (18:00) are still ahead

        vm.setCompleted(Prayer.MAGHRIB, true)
        vm.setCompleted(Prayer.ISHA, true)

        assertEquals(0, (vm.uiState.value as PrayerSnapshot.Ready).summary.completed)
    }

    @Test
    fun `nothing is ever marked as prayed automatically`() = runTest(dispatcher) {
        val vm = prayerVm(nowHour = 23)
        val state = vm.uiState.value as PrayerSnapshot.Ready
        assertEquals(0, state.summary.completed)
        assertTrue(state.rows.filter { it.completed != null }.none { it.completed == true })
    }

    // ---------- PrayerSettingsViewModel ----------

    private class Env(
        val repo: FakePrayerSettingsRepository,
        val location: FakeLocationProvider,
        val scheduler: FakeAlarmScheduler,
        val permission: FakeNotificationPermission,
        val vm: PrayerSettingsViewModel,
    )

    private fun TestScope.settingsEnv(
        initial: PrayerSettings = PrayerSettings.default(CalculationMethod.MUSLIM_WORLD_LEAGUE),
        result: LocationResult = LocationResult.Success(30.0, 31.0),
    ): Env {
        val repo = FakePrayerSettingsRepository(initial)
        val location = FakeLocationProvider(result)
        val scheduler = FakeAlarmScheduler()
        val permission = FakeNotificationPermission()
        val vm = PrayerSettingsViewModel(repo, location, scheduler, permission, deviceZone = { ZoneId.of("Africa/Cairo") })
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }
        return Env(repo, location, scheduler, permission, vm)
    }

    @Test
    fun `detecting the location saves it as automatic with the device zone`() = runTest(dispatcher) {
        val env = settingsEnv()
        env.vm.detectLocation()
        val saved = env.repo.current.location!!
        assertEquals(LocationSource.AUTOMATIC, saved.source)
        assertEquals("Africa/Cairo", saved.zoneId)
        assertEquals(30.0, saved.latitude, 0.0)
        assertFalse(env.vm.uiState.value.detecting)
    }

    @Test
    fun `failed detection never replaces an existing manual location`() = runTest(dispatcher) {
        for (failure in listOf(LocationResult.PermissionDenied, LocationResult.ServicesDisabled, LocationResult.Unavailable)) {
            val env = settingsEnv(initial = configured, result = failure)
            env.vm.detectLocation()
            assertEquals(cairoLocation, env.repo.current.location)
            assertTrue(env.vm.uiState.value.locationMessage != null)
            assertFalse(env.vm.uiState.value.detecting)
        }
    }

    @Test
    fun `no automatic lookup happens unless the user asks for one`() = runTest(dispatcher) {
        val env = settingsEnv(initial = configured)
        env.vm.refreshEnvironment()
        env.vm.setUse24Hour(true)
        assertEquals(0, env.location.calls)
        assertEquals(LocationSource.MANUAL, env.repo.current.location?.source)
    }

    @Test
    fun `picking an Egyptian city recommends the Egyptian method unless the user chose one`() = runTest(dispatcher) {
        val cairo = KnownCities.all.first { it.name == "القاهرة" }
        val fresh = settingsEnv()
        fresh.vm.selectCity(cairo)
        assertEquals(CalculationMethod.EGYPTIAN, fresh.repo.current.calculation.method)
        assertEquals(LocationSource.MANUAL, fresh.repo.current.location?.source)

        val chosen = settingsEnv()
        chosen.vm.setMethod(CalculationMethod.KARACHI)
        chosen.vm.selectCity(cairo)
        assertEquals(CalculationMethod.KARACHI, chosen.repo.current.calculation.method)
    }

    @Test
    fun `invalid manual input is rejected and nothing is saved`() = runTest(dispatcher) {
        val env = settingsEnv()
        assertFalse(env.vm.saveManualLocation("", "", "100", "31", "UTC"))
        assertNull(env.repo.current.location)
        assertEquals(setOf(ManualLocationError.LATITUDE), env.vm.uiState.value.manualErrors)

        assertTrue(env.vm.saveManualLocation("س", "ب", "30", "31", "Africa/Cairo"))
        assertEquals("Africa/Cairo", env.repo.current.location?.zoneId)
        assertTrue(env.vm.uiState.value.manualErrors.isEmpty())
    }

    @Test
    fun `calculation settings persist and adjustments are clamped`() = runTest(dispatcher) {
        val env = settingsEnv()
        env.vm.setAsr(AsrMethod.HANAFI)
        env.vm.adjust(Prayer.DHUHR, 2)
        repeat(40) { env.vm.adjust(Prayer.FAJR, 1) }
        assertEquals(AsrMethod.HANAFI, env.repo.current.calculation.asr)
        assertEquals(2, env.repo.current.calculation.adjustments.of(Prayer.DHUHR))
        assertEquals(30, env.repo.current.calculation.adjustments.of(Prayer.FAJR))
        assertTrue(env.repo.current.methodChosenByUser.not())
    }

    @Test
    fun `notifications are off by default and turning them on enables the five prayers once`() = runTest(dispatcher) {
        val env = settingsEnv()
        assertFalse(env.repo.current.notifications.enabled)
        assertTrue(Prayer.entries.none { env.repo.current.notifications.config(it).enabled })

        env.vm.setNotificationsEnabled(true)
        assertTrue(env.repo.current.notifications.enabled)
        assertTrue(Prayer.entries.all { env.repo.current.notifications.config(it).enabled })

        env.vm.setPrayerNotification(Prayer.FAJR, false)
        env.vm.setNotificationsEnabled(false)
        env.vm.setNotificationsEnabled(true)
        assertFalse(env.repo.current.notifications.config(Prayer.FAJR).enabled) // choice is kept
    }

    @Test
    fun `per prayer reminder and sound are independent and invalid reminders are ignored`() = runTest(dispatcher) {
        val env = settingsEnv()
        env.vm.setReminder(Prayer.ASR, 10)
        env.vm.setSound(Prayer.ASR, NotificationSound.SILENT)
        env.vm.setReminder(Prayer.ISHA, 7) // not an allowed option
        assertEquals(10, env.repo.current.notifications.config(Prayer.ASR).reminderMinutes)
        assertEquals(NotificationSound.SILENT, env.repo.current.notifications.config(Prayer.ASR).sound)
        assertEquals(0, env.repo.current.notifications.config(Prayer.ISHA).reminderMinutes)
        assertEquals(NotificationSound.DEFAULT, env.repo.current.notifications.config(Prayer.FAJR).sound)
    }

    @Test
    fun `permission denial is surfaced and exact alarm status follows the system`() = runTest(dispatcher) {
        val env = settingsEnv()
        env.vm.onNotificationPermissionDenied()
        assertTrue(env.vm.uiState.value.notificationPermissionDenied)
        assertFalse(env.vm.uiState.value.notificationsAllowed)

        env.scheduler.exactAllowed = false
        env.permission.allowed = true
        env.vm.refreshEnvironment()
        assertFalse(env.vm.uiState.value.exactAlarmsAvailable)
        assertTrue(env.vm.uiState.value.notificationsAllowed)
    }

    @Test
    fun `hijri offset is limited to two days either way`() = runTest(dispatcher) {
        val env = settingsEnv()
        repeat(5) { env.vm.adjustHijri(1) }
        assertEquals(2, env.repo.current.hijriOffsetDays)
        repeat(10) { env.vm.adjustHijri(-1) }
        assertEquals(-2, env.repo.current.hijriOffsetDays)
    }
}
