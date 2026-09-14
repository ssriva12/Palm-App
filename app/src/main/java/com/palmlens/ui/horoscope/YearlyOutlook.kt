package com.palmlens.ui.horoscope

import com.palmlens.domain.model.Zodiac
import java.time.Year
import kotlin.random.Random

/**
 * On-device "yearly outlook" — no model call, no cache: stable for a sign across the whole
 * calendar year, picked from [overviewCount] localized template strings.
 */
object YearlyOutlook {
    data class Outlook(val overviewIndex: Int, val rating: Int)

    fun forYear(zodiac: Zodiac, overviewCount: Int, year: Int = Year.now().value): Outlook {
        val random = Random("${zodiac.key}-yearly-$year".hashCode().toLong())
        return Outlook(
            overviewIndex = random.nextInt(overviewCount),
            rating = 3 + random.nextInt(3), // 3..5, stays upbeat
        )
    }
}
