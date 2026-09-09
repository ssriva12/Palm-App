package com.palmlens.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
enum class Zodiac(val displayName: String, val symbol: String) {
    ARIES("Aries", "♈"),
    TAURUS("Taurus", "♉"),
    GEMINI("Gemini", "♊"),
    CANCER("Cancer", "♋"),
    LEO("Leo", "♌"),
    VIRGO("Virgo", "♍"),
    LIBRA("Libra", "♎"),
    SCORPIO("Scorpio", "♏"),
    SAGITTARIUS("Sagittarius", "♐"),
    CAPRICORN("Capricorn", "♑"),
    AQUARIUS("Aquarius", "♒"),
    PISCES("Pisces", "♓");

    /** Lower-case key used for Room `daily_content` rows, prompts and the fallback pool. */
    val key: String get() = name.lowercase()

    companion object {
        fun fromKey(key: String): Zodiac = entries.firstOrNull { it.key == key } ?: CAPRICORN

        /** Standard tropical sun-sign ranges; `md` = month * 100 + day. */
        fun fromDate(date: LocalDate): Zodiac = when (date.monthValue * 100 + date.dayOfMonth) {
            in 120..218 -> AQUARIUS
            in 219..320 -> PISCES
            in 321..419 -> ARIES
            in 420..520 -> TAURUS
            in 521..620 -> GEMINI
            in 621..722 -> CANCER
            in 723..822 -> LEO
            in 823..922 -> VIRGO
            in 923..1022 -> LIBRA
            in 1023..1121 -> SCORPIO
            in 1122..1221 -> SAGITTARIUS
            else -> CAPRICORN // Dec 22 – Jan 19
        }
    }
}
