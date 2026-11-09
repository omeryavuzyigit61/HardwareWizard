package com.panda.myapplication.domain.util

import kotlin.time.Clock

/**
 * commonMain içinde UI ve Entity ID üretimleri için platform-bağımsız milisaniye zaman damgası.
 */
fun currentEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
