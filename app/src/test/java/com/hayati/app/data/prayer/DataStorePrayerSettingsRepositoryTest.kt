package com.hayati.app.data.prayer

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.hayati.app.data.prayer.repository.DataStorePrayerSettingsRepository
import com.hayati.app.domain.prayer.model.AsrMethod
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.NotificationSound
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerNotificationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class DataStorePrayerSettingsRepositoryTest {
    private lateinit var dir: File
    private lateinit var scope: CoroutineScope

    @Before
    fun setUp() {
        dir = Files.createTempDirectory("hayati-ds").toFile()
        scope = CoroutineScope(Dispatchers.IO + Job())
    }

    @After
    fun tearDown() {
        scope.cancel()
        dir.deleteRecursively()
    }

    private fun repository(default: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE) =
        DataStorePrayerSettingsRepository(
            PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(dir, "prayer.preferences_pb") }),
            default,
        )

    @Test
    fun `defaults are safe - no location, notifications off, default method`() = runBlocking {
        val s = repository(CalculationMethod.EGYPTIAN).settings.first()
        assertNull(s.location)
        assertEquals(CalculationMethod.EGYPTIAN, s.calculation.method)
        assertEquals(AsrMethod.STANDARD, s.calculation.asr)
        assertFalse(s.notifications.enabled)
        assertTrue(Prayer.entries.none { s.notifications.config(it).enabled })
        assertFalse(s.methodChosenByUser)
    }

    @Test
    fun `settings survive restart - a second repository on the same file sees them`() = runBlocking {
        val first = repository()
        first.update { s ->
            s.copy(
                location = PrayerLocation("القاهرة", "مصر", 30.04, 31.23, "Africa/Cairo", LocationSource.MANUAL),
                calculation = s.calculation.copy(
                    method = CalculationMethod.UMM_AL_QURA,
                    asr = AsrMethod.HANAFI,
                    adjustments = s.calculation.adjustments.with(Prayer.DHUHR, 2),
                ),
                methodChosenByUser = true,
                use24Hour = true,
                notifications = s.notifications.copy(
                    enabled = true,
                    perPrayer = s.notifications.perPrayer + (
                        Prayer.MAGHRIB to PrayerNotificationConfig(true, 10, NotificationSound.SILENT)
                    ),
                ),
            )
        }
        scope.cancel()
        scope = CoroutineScope(Dispatchers.IO + Job())

        val s = repository().settings.first()
        assertEquals("القاهرة", s.location?.name)
        assertEquals("Africa/Cairo", s.location?.zoneId)
        assertEquals(LocationSource.MANUAL, s.location?.source)
        assertEquals(CalculationMethod.UMM_AL_QURA, s.calculation.method)
        assertEquals(AsrMethod.HANAFI, s.calculation.asr)
        assertEquals(2, s.calculation.adjustments.of(Prayer.DHUHR))
        assertEquals(0, s.calculation.adjustments.of(Prayer.FAJR))
        assertTrue(s.methodChosenByUser)
        assertTrue(s.use24Hour)
        assertTrue(s.notifications.enabled)
        assertEquals(PrayerNotificationConfig(true, 10, NotificationSound.SILENT), s.notifications.config(Prayer.MAGHRIB))
        assertFalse(s.notifications.config(Prayer.FAJR).enabled)
    }

    @Test
    fun `adjustments and reminders outside the allowed range are clamped on write and read`() = runBlocking {
        val repo = repository()
        repo.update { s ->
            s.copy(calculation = s.calculation.copy(adjustments = s.calculation.adjustments.with(Prayer.FAJR, 500)))
        }
        assertEquals(30, repo.settings.first().calculation.adjustments.of(Prayer.FAJR))
    }
}
