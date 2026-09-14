package com.palmz.ui.today

import com.palmz.domain.model.Zodiac
import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random

/**
 * "For entertainment" numbers that don't need a model call: stable for a given sign + day
 * (same inputs always reproduce the same output), computed on-device.
 */
object DailyLuck {
    enum class MatchCategory { LOVE, CAREER, FRIENDSHIP }

    fun matchPercent(zodiac: Zodiac, category: MatchCategory, date: LocalDate = LocalDate.now()): Int {
        val seed = "${zodiac.key}-${category.name}-$date".hashCode().toLong()
        return 55 + Random(seed).nextInt(41) // 55..95 — stays upbeat, never reads as "bad luck"
    }

    fun luckyTime(zodiac: Zodiac, date: LocalDate = LocalDate.now()): LocalTime {
        val seed = "${zodiac.key}-time-$date".hashCode().toLong()
        val minuteOfDay = Random(seed).nextInt(24 * 60)
        return LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
    }
}
