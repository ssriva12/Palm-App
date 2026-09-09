package com.palmlens.data.content

import com.palmlens.core.coroutines.DefaultDispatcher
import com.palmlens.data.asset.AssetLoader
import com.palmlens.domain.model.Hand
import com.palmlens.domain.model.TarotCard
import com.palmlens.domain.model.UserProfile
import com.palmlens.domain.model.Zodiac
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the bundled prompt templates + JSON schemas and fills the `{placeholders}`.
 * Assets are cached after first read. Parsing / string work runs on the Default dispatcher.
 */
@Singleton
class PromptBuilder @Inject constructor(
    private val assetLoader: AssetLoader,
    private val json: Json,
    @DefaultDispatcher private val cpu: CoroutineDispatcher,
) {

    private val cache = mutableMapOf<String, String>()

    private suspend fun asset(path: String): String =
        cache[path] ?: assetLoader.readText(path).also { cache[path] = it }

    suspend fun schema(name: String): JsonObject = withContext(cpu) {
        json.decodeFromString(JsonObject.serializer(), asset("schemas/$name.json"))
    }

    suspend fun palmSystemPrompt(profile: UserProfile, hand: Hand): String = withContext(cpu) {
        asset("prompts/palm_v1.txt")
            .replace("{locale}", profile.languageCode)
            .replace("{name}", profile.name.ifBlank { "friend" })
            .replace("{hand}", hand.label.lowercase())
            .replace("{detail_level}", "full")
            .replace("{four_line_variation_tables}", fourLinesReference())
            .replace("{profile_json}", profileJson(profile))
    }

    suspend fun dailySystemPrompt(
        profile: UserProfile,
        zodiac: Zodiac,
        dateIso: String,
        needWeekly: Boolean,
        needMonthly: Boolean,
    ): String = withContext(cpu) {
        asset("prompts/daily_v1.txt")
            .replace("{locale}", profile.languageCode)
            .replace("{name}", profile.name.ifBlank { "friend" })
            .replace("{zodiac}", zodiac.displayName)
            .replace("{date}", dateIso)
            .replace("{need_weekly}", needWeekly.toString())
            .replace("{need_monthly}", needMonthly.toString())
    }

    suspend fun loveSystemPrompt(
        locale: String,
        selfName: String,
        partnerName: String,
        partnerDobIso: String?,
    ): String = withContext(cpu) {
        asset("prompts/love_v1.txt")
            .replace("{locale}", locale)
            .replace("{self_name}", selfName)
            .replace("{partner_name}", partnerName)
            .replace(
                "{partner_dob_clause}",
                partnerDobIso?.let { " (born $it)" } ?: "",
            )
    }

    suspend fun tarotSystemPrompt(
        profile: UserProfile,
        cards: List<TarotCard>,
    ): String = withContext(cpu) {
        val cardList = cards.joinToString("; ") {
            it.name + if (it.reversed) " (reversed)" else ""
        }
        asset("prompts/tarot_v1.txt")
            .replace("{locale}", profile.languageCode)
            .replace("{name}", profile.name.ifBlank { "friend" })
            .replace("{cards}", cardList)
            .replace("{profile_json}", profileJson(profile))
    }

    fun profileJson(p: UserProfile): String = buildJsonObject {
        put("name", p.name)
        p.gender?.let { put("gender", it.label) }
        p.dob?.let { put("dob", it.toString()) }
        if (p.birthTimeKnown) p.birthTime?.let { put("birthTime", it.toString()) }
        if (p.birthPlace.isNotBlank()) put("birthPlace", p.birthPlace)
        p.zodiac?.let { put("zodiac", it.displayName) }
        put("locale", p.languageCode)
    }.toString()

    private suspend fun fourLinesReference(): String {
        val file = json.decodeFromString(FourLinesFile.serializer(), asset("reference/four_lines.json"))
        return buildString {
            file.lines.forEach { line ->
                appendLine("${line.label} — ${line.position} Governs: ${line.governs}")
                line.variations.forEach { appendLine("  - ${it.match}: ${it.text}") }
                appendLine()
            }
        }
    }

    @Serializable
    private data class FourLinesFile(val lines: List<RefLine>)

    @Serializable
    private data class RefLine(
        val id: String,
        val label: String,
        val position: String,
        val governs: String,
        val variations: List<RefVariation>,
    )

    @Serializable
    private data class RefVariation(val match: String, val text: String)
}
