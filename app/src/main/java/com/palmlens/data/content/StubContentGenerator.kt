package com.palmlens.data.content

import com.palmlens.core.coroutines.DefaultDispatcher
import com.palmlens.data.DummyData
import com.palmlens.domain.content.ContentGenerator
import com.palmlens.domain.model.DailyBundle
import com.palmlens.domain.model.Hand
import com.palmlens.domain.model.LoveResult
import com.palmlens.domain.model.PalmReading
import com.palmlens.domain.model.TarotCard
import com.palmlens.domain.model.TarotResult
import com.palmlens.domain.model.UserProfile
import com.palmlens.domain.model.Zodiac
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 1 stand-in: bundled dummy content with artificial latency so every loading state
 * is exercised. Phase 3 replaces this binding with the OpenAI implementation.
 */
@Singleton
class StubContentGenerator @Inject constructor(
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : ContentGenerator {

    override suspend fun palmReading(
        imageJpeg: ByteArray,
        hand: Hand,
        profile: UserProfile,
    ): PalmReading = withContext(dispatcher) {
        delay(2200)
        DummyData.palmReading(hand)
    }

    override suspend fun dailyBundle(
        zodiac: Zodiac,
        locale: String,
        needWeekly: Boolean,
        needMonthly: Boolean,
    ): DailyBundle = withContext(dispatcher) {
        delay(900)
        DummyData.dailyBundle(zodiac)
    }

    override suspend fun loveResult(
        selfName: String,
        partnerName: String,
        partnerDob: LocalDate?,
        locale: String,
    ): LoveResult = withContext(dispatcher) {
        delay(1400)
        DummyData.loveResult(selfName, partnerName)
    }

    override suspend fun tarot(
        cards: List<TarotCard>,
        profile: UserProfile,
        locale: String,
    ): TarotResult = withContext(dispatcher) {
        delay(1400)
        DummyData.tarotResult(cards)
    }
}
