package com.hayati.app.domain.adhkar.model

/** The adhkar groups the app supports. [key] is the value used in the content file and database. */
enum class AdhkarCategory(val key: String) {
    MORNING("morning"),
    EVENING("evening"),
    AFTER_PRAYER("after_prayer"),
    ;

    companion object {
        fun fromKey(key: String): AdhkarCategory? = entries.firstOrNull { it.key == key }
    }
}

/**
 * One dhikr as stored in the content file. [repeat] is how many times it is recited (at least 1).
 * [source] is the reference printed under the text; it must come from the content file, never be
 * invented by the app.
 */
data class Dhikr(
    val id: String,
    val category: AdhkarCategory,
    val text: String,
    val repeat: Int,
    val source: String,
)

/** A dhikr with today's counter. [count] is always within 0..[Dhikr.repeat]. */
data class DhikrProgress(
    val dhikr: Dhikr,
    val count: Int,
) {
    val isDone: Boolean get() = count >= dhikr.repeat
    val remaining: Int get() = (dhikr.repeat - count).coerceAtLeast(0)
}

/** Progress of one category for one day. */
data class CategoryProgress(
    val category: AdhkarCategory,
    val items: List<DhikrProgress>,
) {
    val total: Int get() = items.size
    val doneCount: Int get() = items.count { it.isDone }
    val isComplete: Boolean get() = items.isNotEmpty() && doneCount == items.size
}

/** Pure counter rules, shared by the repository and the tests. */
object AdhkarCounter {
    fun increment(count: Int, repeat: Int): Int = (count + 1).coerceIn(0, repeat.coerceAtLeast(1))

    fun decrement(count: Int, repeat: Int): Int = (count - 1).coerceIn(0, repeat.coerceAtLeast(1))

    fun clamp(count: Int, repeat: Int): Int = count.coerceIn(0, repeat.coerceAtLeast(1))
}
