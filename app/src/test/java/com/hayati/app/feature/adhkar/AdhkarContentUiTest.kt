package com.hayati.app.feature.adhkar

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.hayati.app.core.designsystem.theme.HayatiTheme
import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.CategoryProgress
import com.hayati.app.domain.adhkar.model.Dhikr
import com.hayati.app.domain.adhkar.model.DhikrProgress
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class AdhkarContentUiTest {
    @get:Rule
    val compose = createComposeRule()

    private val d1 = Dhikr("m1", AdhkarCategory.MORNING, "نص اختبار أول", 3, "مرجع اختبار")
    private val d2 = Dhikr("m2", AdhkarCategory.MORNING, "نص اختبار ثان", 1, "")

    private fun ready(count: Int = 1, index: Int = 0, vararg extra: Pair<AdhkarCategory, List<DhikrProgress>>) =
        AdhkarUiState(
            status = AdhkarStatus.READY,
            categories = listOf(
                CategoryProgress(AdhkarCategory.MORNING, listOf(DhikrProgress(d1, count), DhikrProgress(d2, 0))),
                CategoryProgress(AdhkarCategory.EVENING, emptyList()),
                CategoryProgress(AdhkarCategory.AFTER_PRAYER, emptyList()),
            ),
            currentIndex = index,
        )

    private fun show(
        state: AdhkarUiState,
        onTap: () -> Unit = {},
        onUndo: () -> Unit = {},
        onNext: () -> Unit = {},
    ) = compose.setContent {
        HayatiTheme {
            AdhkarContent(state, {}, onTap, onUndo, onNext, {}, {})
        }
    }

    @Test
    fun `shows the dhikr text, source, counter and the remaining count for accessibility`() {
        show(ready())
        compose.onNodeWithText("نص اختبار أول").assertExists()
        compose.onNodeWithText("المصدر: مرجع اختبار").assertExists()
        compose.onNodeWithText("التكرار: 1 من 3").assertExists()
        compose.onNodeWithContentDescription("عدّ مرة. المتبقي 2").assertExists()
    }

    @Test
    fun `tapping the card counts and the undo and next buttons reach their callbacks`() {
        val calls = mutableListOf<String>()
        show(ready(), onTap = { calls += "tap" }, onUndo = { calls += "undo" }, onNext = { calls += "next" })
        compose.onNodeWithContentDescription("عدّ مرة. المتبقي 2").performClick()
        compose.onNodeWithText("تراجع").performClick()
        compose.onNodeWithText("التالي").performClick()
        assertEquals(listOf("tap", "undo", "next"), calls)
    }

    @Test
    fun `previous is disabled on the first dhikr and undo is disabled at zero`() {
        show(ready(count = 0))
        compose.onNodeWithText("السابق").assertIsNotEnabled()
        compose.onNodeWithText("تراجع").assertIsNotEnabled()
        compose.onNodeWithText("التالي").assertIsEnabled()
    }

    @Test
    fun `a finished dhikr says so and is not tappable`() {
        var taps = 0
        show(ready(count = 3), onTap = { taps++ })
        compose.onNodeWithText("تم هذا الذكر").assertExists()
        compose.onNodeWithContentDescription("عدّ مرة. المتبقي 0").performClick()
        assertEquals(0, taps)
    }

    @Test
    fun `the source and takhrij are shown when the content provides them`() {
        val documented = d1.copy(takhrij = "تخريج اختبار")
        show(
            ready().copy(
                categories = listOf(
                    CategoryProgress(AdhkarCategory.MORNING, listOf(DhikrProgress(documented, 0))),
                    CategoryProgress(AdhkarCategory.EVENING, emptyList()),
                    CategoryProgress(AdhkarCategory.AFTER_PRAYER, emptyList()),
                ),
            ),
        )
        compose.onNodeWithText("المصدر: مرجع اختبار").assertExists()
        compose.onNodeWithText("التخريج: تخريج اختبار").assertExists()
    }

    @Test
    fun `empty content explains that the texts are not added yet and shows no adhkar`() {
        show(AdhkarUiState(status = AdhkarStatus.EMPTY))
        compose.onNodeWithText("المحتوى غير متوفر بعد").assertExists()
    }

    @Test
    fun `an invalid content file shows the error and no text`() {
        show(AdhkarUiState(status = AdhkarStatus.ERROR))
        compose.onNodeWithText("تعذّر تحميل الأذكار").assertExists()
    }

    @Test
    fun `a failed save is shown`() {
        show(ready().copy(saveFailed = true))
        compose.onNodeWithText("تعذّر حفظ العدّاد. حاول مرة أخرى.").assertExists()
    }
}
