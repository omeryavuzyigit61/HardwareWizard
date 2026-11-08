package com.panda.myapplication.domain.util

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * KMP kuralları gereği (pre_prompt_kmp.md) ortak kodda java.lang.System.currentTimeMillis()
 * çağrısı iOS Kotlin/Native hedefini kıracağından, enjekte edilebilir ve saf Kotlin tabanlı
 * saat soyutlaması.
 */
interface AppClock {
    fun now(): Instant
    fun nowEpochMillis(): Long = now().toEpochMilliseconds()
}

class SystemAppClock : AppClock {
    override fun now(): Instant = Clock.System.now()
}

/**
 * commonMain içinde UI ve Entity ID üretimleri için platform-bağımsız milisaniye zaman damgası.
 */
fun currentEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
