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
 * Value of an overview item. [Unavailable] means no real data source exists (yet); it must never
 * be replaced by a made-up value. Every other case carries data that is really stored or computed.
 */
sealed interface OverviewValue {
    data object Unavailable : OverviewValue

    /** Today's task progress, read from the database. [total] may be zero. */
    data class TasksProgress(val completed: Int, val total: Int) : OverviewValue
}

data class OverviewEntry(
    val item: OverviewItem,
    val value: OverviewValue,
)
