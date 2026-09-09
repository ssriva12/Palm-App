package com.palmlens.domain.repository

import com.palmlens.domain.model.DailyBundle
import com.palmlens.domain.model.Zodiac

interface HoroscopeRepository {
    /** Room-cached by (sign, date, locale); generates only on a miss (spec §3.4). */
    suspend fun dailyBundle(zodiac: Zodiac, locale: String): DailyBundle
}
