package com.palmz.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Highlights(
    val mood: String,
    val luckyNumber: Int,
    val luckyColor: String,
    val focusOfDay: String,
    val oneLineAdvice: String,
)

@Serializable
data class HoroscopeEntry(
    val overview: String,
    val rating: Int, // 1..5
    val love: String? = null,
    val career: String? = null,
    val health: String? = null,
)

/** One model call per user per day returns all of this (spec §3.4). */
@Serializable
data class DailyBundle(
    val date: String,
    val zodiac: Zodiac,
    val highlights: Highlights,
    val daily: HoroscopeEntry,
    val weekly: HoroscopeEntry,
    val monthly: HoroscopeEntry,
)
