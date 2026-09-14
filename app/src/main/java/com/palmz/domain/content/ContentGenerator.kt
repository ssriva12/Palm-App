package com.palmz.domain.content

import com.palmz.domain.model.DailyBundle
import com.palmz.domain.model.Hand
import com.palmz.domain.model.LoveResult
import com.palmz.domain.model.PalmReading
import com.palmz.domain.model.TarotCard
import com.palmz.domain.model.TarotResult
import com.palmz.domain.model.UserProfile
import com.palmz.domain.model.Zodiac
import java.time.LocalDate

/**
 * The one seam between "the app" and "where readings come from".
 * Phase 1 binds [com.palmz.data.content.StubContentGenerator] (bundled dummy data);
 * Phase 3 swaps in the OpenAI (Chat Completions, vision + Structured Outputs) implementation
 * with no change above. Every method is a suspend function — the implementation owns its
 * threading.
 */
interface ContentGenerator {

    suspend fun palmReading(
        imageJpeg: ByteArray,
        hand: Hand,
        profile: UserProfile,
    ): PalmReading

    suspend fun dailyBundle(
        zodiac: Zodiac,
        locale: String,
        needWeekly: Boolean,
        needMonthly: Boolean,
    ): DailyBundle

    suspend fun loveResult(
        selfName: String,
        partnerName: String,
        partnerDob: LocalDate?,
        locale: String,
    ): LoveResult

    suspend fun tarot(
        cards: List<TarotCard>,
        profile: UserProfile,
        locale: String,
    ): TarotResult
}
