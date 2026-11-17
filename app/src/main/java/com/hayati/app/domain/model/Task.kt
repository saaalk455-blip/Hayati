package com.hayati.app.domain.model

/**
 * A task planned for one day.
 *
 * @property epochDay the day the task belongs to (days since 1970-01-01, device time zone).
 * @property completedAtMillis when the task was completed, or null while it is open.
 */
data class Task(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val epochDay: Long,
    val createdAtMillis: Long,
    val completedAtMillis: Long?,
)

/** Rules for task titles, shared by the UI state holder and the repository. */
object TaskTitle {
    const val MAX_LENGTH = 120

    /** Returns the trimmed title cut to [MAX_LENGTH], or null when nothing meaningful was typed. */
    fun sanitize(raw: String): String? =
        raw.trim().takeIf { it.isNotEmpty() }?.take(MAX_LENGTH)
}
