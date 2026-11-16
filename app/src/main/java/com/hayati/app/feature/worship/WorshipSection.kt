package com.hayati.app.feature.worship

import androidx.annotation.StringRes
import com.hayati.app.R

/** Worship areas reachable from the Worship tab and the Home shortcuts. */
enum class WorshipSection(
    val route: String,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
) {
    Prayer("section/prayer", R.string.section_prayer, R.string.section_prayer_desc),
    Quran("section/quran", R.string.section_quran, R.string.section_quran_desc),
    Adhkar("section/adhkar", R.string.section_adhkar, R.string.section_adhkar_desc),
    Tasbeeh("section/tasbeeh", R.string.section_tasbeeh, R.string.section_tasbeeh_desc),
}
