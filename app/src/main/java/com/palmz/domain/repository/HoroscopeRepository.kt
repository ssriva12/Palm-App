package com.palmz.domain.repository

import com.palmz.domain.model.DailyBundle
import com.palmz.domain.model.Zodiac

interface HoroscopeRepository {
    /** Room-cached by (sign, date, locale); generates only on a miss (spec §3.4). */
    suspend fun dailyBundle(zodiac: Zodiac, locale: String): DailyBundle
}
