package com.palmz.data.repository

import com.palmz.data.asset.AssetLoader
import com.palmz.domain.model.DailyBundle
import com.palmz.domain.model.Highlights
import com.palmz.domain.model.HoroscopeEntry
import com.palmz.domain.model.Zodiac
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Last-resort daily bundle when generation fails and Room has nothing cached for this sign
 * (spec §3.8). Serves an evergreen line from `assets/fallback/horoscopes.json`, rotated by
 * day so a stuck user doesn't stare at the same sentence. Falls back to English when the
 * user's locale isn't filled in yet.
 */
@Singleton
class FallbackHoroscope @Inject constructor(
    private val assetLoader: AssetLoader,
    private val json: Json,
) {
    suspend fun bundle(zodiac: Zodiac, locale: String): DailyBundle =
        fallbackBundle(json, assetLoader.readText("fallback/horoscopes.json"), zodiac, locale)
}

private const val GENERIC = "The sky is quiet today. Trust your own read on things."

/** Pure builder — extracted so it can be tested against the real asset without a Context. */
internal fun fallbackBundle(
    json: Json,
    rawJson: String,
    zodiac: Zodiac,
    locale: String,
    today: LocalDate = LocalDate.now(),
): DailyBundle {
    val bySign = json.parseToJsonElement(rawJson).jsonObject[zodiac.key]?.jsonObject

    fun linesFor(loc: String): List<String> =
        bySign?.get(loc)?.jsonArray
            ?.map { it.jsonPrimitive.content }
            ?.filterNot { it.isBlank() || it.equals("TODO", ignoreCase = true) }
            .orEmpty()

    val lines = linesFor(locale).ifEmpty { linesFor("en") }.ifEmpty { listOf(GENERIC) }
    val line = lines[today.dayOfYear % lines.size]
    val entry = HoroscopeEntry(overview = line, rating = 3)

    return DailyBundle(
        date = today.toString(),
        zodiac = zodiac,
        highlights = Highlights(
            mood = "Quietly steady",
            luckyNumber = 1 + today.dayOfYear % 9,
            luckyColor = "Indigo",
            focusOfDay = line,
            oneLineAdvice = line,
        ),
        daily = entry,
        weekly = entry,
        monthly = entry,
    )
}
