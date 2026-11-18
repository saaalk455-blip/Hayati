package com.hayati.app.domain.adhkar.usecase

import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.AdhkarCounter
import com.hayati.app.domain.adhkar.repository.AdhkarContentSource
import com.hayati.app.domain.adhkar.repository.AdhkarProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId

/** What the Home adhkar card shows. */
sealed interface AdhkarHomeState {
    data object Loading : AdhkarHomeState

    /** No adhkar content (missing, empty or invalid file). Home shows an honest unavailable state. */
    data object Unavailable : AdhkarHomeState

    /** Today's progress for [category], read from the same counters the Adhkar screen writes. */
    data class Ready(val category: AdhkarCategory, val doneCount: Int, val total: Int) : AdhkarHomeState {
        val isComplete: Boolean get() = total > 0 && doneCount >= total
    }
}

/**
 * Builds the Home card state from the single content source and progress repository.
 *
 * Which category the card shows is an app rule, not a religious ruling: local time before 12:00 ->
 * morning, otherwise evening. If that category has no adhkar, the other one of the two is used.
 */
class AdhkarHomeProvider(
    private val content: AdhkarContentSource,
    private val progress: AdhkarProgressRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(
        day: Flow<Long>,
        ticker: Flow<Long>,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Flow<AdhkarHomeState> =
        combine(day, ticker.map { suggestedCategory(it, zone) }.distinctUntilChanged()) { epochDay, category ->
            epochDay to category
        }.distinctUntilChanged().flatMapLatest { (epochDay, suggested) ->
            flow {
                val adhkar = try {
                    content.load()
                } catch (e: Exception) {
                    emit(AdhkarHomeState.Unavailable)
                    return@flow
                }
                val order = listOf(suggested, if (suggested == AdhkarCategory.MORNING) AdhkarCategory.EVENING else AdhkarCategory.MORNING)
                val category = order.firstOrNull { c -> adhkar.any { it.category == c } }
                if (category == null) {
                    emit(AdhkarHomeState.Unavailable)
                    return@flow
                }
                val items = adhkar.filter { it.category == category }
                emitAll(
                    progress.observeCounts(epochDay, category).map { counts ->
                        AdhkarHomeState.Ready(
                            category = category,
                            doneCount = items.count { AdhkarCounter.clamp(counts[it.id] ?: 0, it.repeat) >= it.repeat },
                            total = items.size,
                        )
                    },
                )
            }
        }

    companion object {
        fun suggestedCategory(nowMillis: Long, zone: ZoneId): AdhkarCategory =
            if (Instant.ofEpochMilli(nowMillis).atZone(zone).hour < 12) AdhkarCategory.MORNING else AdhkarCategory.EVENING
    }
}
