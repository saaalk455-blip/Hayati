package com.hayati.app.core.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Emits the current clock reading every [periodMillis]. It is cold and loops only while someone
 * collects it; ViewModels share it with `WhileSubscribed`, and screens collect with the lifecycle,
 * so nothing keeps running while the app is in the background.
 */
fun clockTicker(time: TimeProvider, periodMillis: Long = 1_000L): Flow<Long> = flow {
    while (true) {
        emit(time.nowMillis())
        delay(periodMillis)
    }
}
