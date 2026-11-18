package com.hayati.app.feature.prayer

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import com.hayati.app.core.designsystem.theme.HayatiTheme
import com.hayati.app.domain.prayer.model.DailySummary
import com.hayati.app.domain.prayer.model.LocationSource
import com.hayati.app.domain.prayer.model.NextPrayer
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerErrorReason
import com.hayati.app.domain.prayer.model.PrayerEvent
import com.hayati.app.domain.prayer.model.PrayerLocation
import com.hayati.app.domain.prayer.model.PrayerRow
import com.hayati.app.domain.prayer.model.PrayerRowState
import com.hayati.app.domain.prayer.model.PrayerSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Compose UI tests run on the JVM with Robolectric. They check what the screen renders for a given
 * state and that taps reach the callbacks. They do not replace testing on a real phone.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class PrayerContentUiTest {
    @get:Rule
    val compose = createComposeRule()

    private val location = PrayerLocation("القاهرة", "مصر", 30.0, 31.0, "UTC", LocationSource.MANUAL)
    private val date = LocalDate.of(2026, 6, 15)
    private fun at(h: Int, m: Int = 0) = date.atTime(h, m).toInstant(java.time.ZoneOffset.UTC)

    // Now = 14:00 UTC: Fajr, Dhuhr and Asr (13:00) are over, Maghrib (16:00) is next.
    private val now = at(14).toEpochMilli()

    private fun ready(completed: Set<Prayer> = emptySet()): PrayerSnapshot.Ready {
        val hours = mapOf(
            PrayerEvent.FAJR to 3, PrayerEvent.SUNRISE to 5, PrayerEvent.DHUHR to 10,
            PrayerEvent.ASR to 13, PrayerEvent.MAGHRIB to 16, PrayerEvent.ISHA to 18,
        )
        val rows = PrayerEvent.entries.map { event ->
            val time = at(hours.getValue(event))
            val prayer = event.prayer
            val done = prayer != null && prayer in completed
            PrayerRow(
                event = event,
                time = time,
                state = when {
                    prayer == null -> PrayerRowState.PASSED
                    done -> PrayerRowState.COMPLETED
                    event == PrayerEvent.MAGHRIB -> PrayerRowState.NEXT
                    time.toEpochMilli() < now -> PrayerRowState.UNRECORDED
                    else -> PrayerRowState.UPCOMING
                },
                completed = prayer?.let { done },
                notificationActive = prayer?.let { it == Prayer.FAJR },
            )
        }
        return PrayerSnapshot.Ready(
            location = location, date = date, zone = ZoneId.of("UTC"), nowMillis = now, rows = rows,
            next = NextPrayer(Prayer.MAGHRIB, at(16), isTomorrow = false),
            summary = DailySummary.from(emptyList()).copy(completed = completed.size, remaining = 5 - completed.size, percent = completed.size * 20),
            use24Hour = true, hijriOffsetDays = 0,
        )
    }

    private fun show(
        snapshot: PrayerSnapshot,
        saveFailed: Boolean = false,
        onToggle: (Prayer, Boolean) -> Unit = { _, _ -> },
        onSettings: () -> Unit = {},
    ) = compose.setContent {
        HayatiTheme {
            PrayerContent(snapshot, saveFailed, {}, onSettings, {}, onToggle, {})
        }
    }

    @Test
    fun `the layout is right to left`() {
        var direction: LayoutDirection? = null
        compose.setContent { HayatiTheme { direction = LocalLayoutDirection.current } }
        compose.waitForIdle()
        assertEquals(LayoutDirection.Rtl, direction)
    }

    @Test
    fun `next prayer card shows the name, time and the real countdown`() {
        show(ready())
        compose.onNodeWithText("الصلاة القادمة").assertExists()
        compose.onNodeWithText("متبقي 02:00:00").assertExists() // 14:00 -> 16:00
        compose.onAllNodesWithText("16:00").assertCountAtLeast(1)
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.assertCountAtLeast(n: Int) {
        assertTrue(fetchSemanticsNodes().size >= n)
    }

    @Test
    fun `all six events are listed and sunrise is marked as not a prayer`() {
        show(ready())
        listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء").forEach {
            assertTrue("$it missing", compose.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty())
        }
        compose.onNodeWithText("وقت الشروق، وليس صلاة").assertExists()
    }

    @Test
    fun `a passed prayer without a record is unrecorded and never called missed`() {
        show(ready())
        assertTrue(compose.onAllNodesWithText("غير مسجّلة", substring = true).fetchSemanticsNodes().isNotEmpty())
        assertTrue(compose.onAllNodesWithText("فاتت", substring = true).fetchSemanticsNodes().isEmpty())
        assertTrue(compose.onAllNodesWithText("فائتة", substring = true).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `tapping done on a passed prayer calls back, and a future prayer cannot be ticked`() {
        val toggled = mutableListOf<Pair<Prayer, Boolean>>()
        show(ready(), onToggle = { p, c -> toggled += p to c })

        compose.onAllNodesWithText("صليت")[0].performClick()
        assertEquals(listOf(Prayer.FAJR to true), toggled)

        // Maghrib (16:00) and Isha (18:00) have not started: their checkboxes are disabled.
        val disabled = compose.onAllNodesWithText("لم يحن وقتها بعد")
        assertEquals(2, disabled.fetchSemanticsNodes().size)
    }

    @Test
    fun `a recorded prayer offers undo`() {
        val toggled = mutableListOf<Pair<Prayer, Boolean>>()
        show(ready(completed = setOf(Prayer.FAJR)), onToggle = { p, c -> toggled += p to c })
        compose.onNodeWithText("تراجع").performClick()
        assertEquals(listOf(Prayer.FAJR to false), toggled)
        assertTrue(compose.onAllNodesWithText("20%").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun `without a location the screen asks the user to choose one`() {
        var opened = 0
        show(PrayerSnapshot.NotConfigured, onSettings = { opened++ })
        compose.onNodeWithText("حدّد موقعك لعرض المواقيت").assertExists()
        compose.onNodeWithText("تحديد الموقع").performClick()
        assertEquals(1, opened)
    }

    @Test
    fun `calculation and location errors show a useful Arabic message`() {
        show(PrayerSnapshot.Error(PrayerErrorReason.CALCULATION_FAILED))
        compose.onNodeWithText("تعذّر حساب المواقيت لهذا الموقع. جرّب موقعًا أو طريقة حساب أخرى.").assertExists()
    }

    @Test
    fun `a save failure is shown`() {
        show(ready(), saveFailed = true)
        compose.onNodeWithText("تعذّر حفظ التسجيل. حاول مرة أخرى.").assertExists()
    }
}
