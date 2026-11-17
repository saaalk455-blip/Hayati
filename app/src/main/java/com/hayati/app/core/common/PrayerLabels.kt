package com.hayati.app.core.common

import androidx.annotation.StringRes
import com.hayati.app.R
import com.hayati.app.domain.prayer.model.AsrMethod
import com.hayati.app.domain.prayer.model.CalculationMethod
import com.hayati.app.domain.prayer.model.Prayer
import com.hayati.app.domain.prayer.model.PrayerEvent

@StringRes
fun Prayer.labelRes(): Int = PrayerEvent.of(this).labelRes()

@StringRes
fun PrayerEvent.labelRes(): Int = when (this) {
    PrayerEvent.FAJR -> R.string.prayer_fajr
    PrayerEvent.SUNRISE -> R.string.prayer_sunrise
    PrayerEvent.DHUHR -> R.string.prayer_dhuhr
    PrayerEvent.ASR -> R.string.prayer_asr
    PrayerEvent.MAGHRIB -> R.string.prayer_maghrib
    PrayerEvent.ISHA -> R.string.prayer_isha
}

@StringRes
fun CalculationMethod.labelRes(): Int = when (this) {
    CalculationMethod.MUSLIM_WORLD_LEAGUE -> R.string.method_mwl
    CalculationMethod.EGYPTIAN -> R.string.method_egyptian
    CalculationMethod.UMM_AL_QURA -> R.string.method_umm_al_qura
    CalculationMethod.NORTH_AMERICA -> R.string.method_isna
    CalculationMethod.KARACHI -> R.string.method_karachi
    CalculationMethod.DUBAI -> R.string.method_dubai
    CalculationMethod.QATAR -> R.string.method_qatar
    CalculationMethod.KUWAIT -> R.string.method_kuwait
    CalculationMethod.TURKEY -> R.string.method_turkey
    CalculationMethod.SINGAPORE -> R.string.method_singapore
}

@StringRes
fun AsrMethod.labelRes(): Int = when (this) {
    AsrMethod.STANDARD -> R.string.asr_standard
    AsrMethod.HANAFI -> R.string.asr_hanafi
}
