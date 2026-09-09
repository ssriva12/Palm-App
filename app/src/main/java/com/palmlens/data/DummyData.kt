package com.palmlens.data

import com.palmlens.domain.model.DailyBundle
import com.palmlens.domain.model.Hand
import com.palmlens.domain.model.Highlights
import com.palmlens.domain.model.HoroscopeEntry
import com.palmlens.domain.model.ImageQuality
import com.palmlens.domain.model.LineAttributes
import com.palmlens.domain.model.LineId
import com.palmlens.domain.model.LoveResult
import com.palmlens.domain.model.NormPoint
import com.palmlens.domain.model.PalmLine
import com.palmlens.domain.model.PalmReading
import com.palmlens.domain.model.TarotCard
import com.palmlens.domain.model.TarotResult
import com.palmlens.domain.model.Zodiac
import java.time.LocalDate

/**
 * Hardcoded content for the UI prototype — no model calls.
 * Reading text is lifted from the spec's grounding tables (§4).
 */
object DummyData {

    fun todayIso(): String = LocalDate.now().toString()

    // ---- Palm scan ----------------------------------------------------------

    fun palmReading(hand: Hand): PalmReading = PalmReading(
        hand = hand,
        isPalm = true,
        imageQuality = ImageQuality.GOOD,
        summary = "A steady, expressive hand. Your heart leads, your head keeps pace, " +
            "and a clear fate line says you have been pointed somewhere for a while.",
        luckyNumber = 7,
        luckyColor = "Indigo",
        lines = listOf(
            PalmLine(
                id = LineId.LIFE,
                visible = true,
                attributes = LineAttributes("long", "deep", "curved", breaks = 0, forked = true, doubled = false),
                path = listOf(
                    NormPoint(0.34f, 0.16f), NormPoint(0.30f, 0.30f), NormPoint(0.27f, 0.46f),
                    NormPoint(0.29f, 0.62f), NormPoint(0.35f, 0.76f), NormPoint(0.43f, 0.86f),
                ),
                teaser = "Strong vitality — you recover fast from setbacks.",
                reading = "Long and deep: strong vitality and resilience, and you bounce back quickly. " +
                    "The wide curve says you are warm, generous and drawn to travel and open space. " +
                    "A fork near the end hints at a second chapter later — a move or a reinvention.",
                confidence = 0.82f,
            ),
            PalmLine(
                id = LineId.HEAD,
                visible = true,
                attributes = LineAttributes("long", "medium", "curved", breaks = 0, forked = true, doubled = false),
                path = listOf(
                    NormPoint(0.30f, 0.40f), NormPoint(0.44f, 0.43f), NormPoint(0.58f, 0.47f),
                    NormPoint(0.70f, 0.53f), NormPoint(0.80f, 0.60f),
                ),
                teaser = "A thorough thinker who turns ideas over before deciding.",
                reading = "Long and gently sloping: you think things through and your ideas arrive " +
                    "sideways rather than in straight lines — imaginative, not rigid. The writer's fork " +
                    "at the end means you are good at explaining complex things simply.",
                confidence = 0.77f,
            ),
            PalmLine(
                id = LineId.HEART,
                visible = true,
                attributes = LineAttributes("long", "deep", "curved", breaks = 0, forked = false, doubled = false),
                path = listOf(
                    NormPoint(0.26f, 0.28f), NormPoint(0.40f, 0.24f), NormPoint(0.55f, 0.22f),
                    NormPoint(0.68f, 0.23f), NormPoint(0.78f, 0.27f),
                ),
                teaser = "Idealistic in love, with a clear picture of the right person.",
                reading = "Long and reaching toward the index finger: idealistic in love, with high " +
                    "standards and a clear picture of the right partner. Deep and curved upward — " +
                    "intense, loyal and openly affectionate; you tend to make the first move.",
                confidence = 0.8f,
            ),
            PalmLine(
                id = LineId.FATE,
                visible = true,
                attributes = LineAttributes("medium", "medium", "straight", breaks = 1, forked = false, doubled = false),
                path = listOf(
                    NormPoint(0.52f, 0.88f), NormPoint(0.52f, 0.70f), NormPoint(0.53f, 0.52f),
                    NormPoint(0.54f, 0.36f), NormPoint(0.55f, 0.22f),
                ),
                teaser = "A career that reinvents itself, then settles into a stronger run.",
                reading = "Rising steadily toward the middle finger with one clean break: a career change " +
                    "or reinvention, then a stronger second run. Starting near the life line means the " +
                    "success you build is mostly your own effort.",
                confidence = 0.68f,
            ),
        ),
    )

    // ---- Daily bundle ------------------------------------------------------

    fun dailyBundle(zodiac: Zodiac): DailyBundle = DailyBundle(
        date = todayIso(),
        zodiac = zodiac,
        highlights = Highlights(
            mood = "Focused and a little restless",
            luckyNumber = 7,
            luckyColor = "Indigo",
            focusOfDay = "Finishing something you started last week",
            oneLineAdvice = "Say the plain version of what you mean — it lands better today.",
        ),
        daily = HoroscopeEntry(
            overview = "Momentum is on your side this morning. A conversation you have been " +
                "putting off turns out easier than expected once you start it.",
            rating = 4,
            love = "Warmth is returned in kind — reach out first.",
            career = "A small win gets noticed by the right person.",
            health = "Protect your evening; an early night pays off tomorrow.",
        ),
        weekly = HoroscopeEntry(
            overview = "The week rewards steady effort over big gestures. Midweek brings a choice " +
                "between the familiar and the interesting — you can afford the interesting one.",
            rating = 3,
        ),
        monthly = HoroscopeEntry(
            overview = "A month of consolidation. What felt scattered in the last few weeks starts " +
                "to line up, and by the end you will see the shape of the next step.",
            rating = 4,
        ),
    )

    // ---- Love test ------------------------------------------------------------

    fun loveResult(selfName: String, partnerName: String): LoveResult {
        val a = selfName.trim().ifBlank { "You" }
        val b = partnerName.trim().ifBlank { "Them" }
        return LoveResult(
            score = 78,
            strengths = listOf(
                "$a brings steadiness; $b brings spark. The pairing rarely goes flat.",
                "You process conflict at a similar speed — neither of you lets it fester.",
                "Shared taste in the small daily things: food, music, how a weekend should feel.",
            ),
            challenges = listOf(
                "Both of you wait for the other to name the hard thing first.",
                "$b needs more room than $a instinctively gives — worth saying out loud.",
            ),
            verdict = "A strong match with real staying power. The work is in the talking, not the feeling.",
        )
    }

    // ---- Tarot -------------------------------------------------------------
    // The real 78-card deck lives in assets/tarot/deck.json; the stub only interprets.

    fun tarotResult(picked: List<TarotCard>): TarotResult {
        val c = picked.take(3)
        val names = c.map { it.name }
        return TarotResult(
            cards = c,
            past = "${names.getOrElse(0) { "The Fool" }} in the past: a leap you took without a map. " +
                "It cost more than you expected and taught you more than you admit.",
            present = "${names.getOrElse(1) { "The Star" }} now: a quiet, hopeful stretch. The pressure " +
                "has eased enough for you to think clearly about what you actually want.",
            future = "${names.getOrElse(2) { "The Sun" }} ahead: things get simpler, not harder. A period " +
                "of warmth and visible progress — enjoy it rather than bracing against it.",
            overall = "The spread reads as a turn from surviving to choosing. You are past the hard part.",
        )
    }
}
