package com.hayati.app.domain.model

/** Items shown in the Home "today" overview. */
enum class OverviewItem {
    NextPrayer,
    TimeRemaining,
    QuranWird,
    Adhkar,
    Tasks,
}

/**
 * Value of an overview item. [Unavailable] means no real data source exists yet; it must never be
 * replaced by a made-up value. [Available] is reserved for values that are really computed or stored.
 */
sealed interface OverviewValue {
    data object Unavailable : OverviewValue
    data class Available(val text: String) : OverviewValue
}

data class OverviewEntry(
    val item: OverviewItem,
    val value: OverviewValue,
)
