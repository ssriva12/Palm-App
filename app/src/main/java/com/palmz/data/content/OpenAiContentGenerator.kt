package com.palmz.data.content

import com.palmz.core.coroutines.DefaultDispatcher
import com.palmz.core.network.OpenAiClient
import com.palmz.core.network.OpenAiConfig
import com.palmz.core.network.model.ChatMessage
import com.palmz.core.network.model.ChatRequest
import com.palmz.core.network.model.ContentPart
import com.palmz.core.network.model.ImageUrl
import com.palmz.core.network.model.JsonSchemaSpec
import com.palmz.core.network.model.ResponseFormat
import com.palmz.debug.DebugDiagnostics
import com.palmz.debug.PalmDiagnostics
import com.palmz.domain.content.ContentGenerator
import com.palmz.domain.model.DailyBundle
import com.palmz.domain.model.Hand
import com.palmz.domain.model.Highlights
import com.palmz.domain.model.HoroscopeEntry
import com.palmz.domain.model.LoveResult
import com.palmz.domain.model.PalmReading
import com.palmz.domain.model.TarotCard
import com.palmz.domain.model.TarotResult
import com.palmz.domain.model.UserProfile
import com.palmz.domain.model.Zodiac
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 3: real readings from OpenAI Chat Completions.
 * capture → image (base64 data URL) + templated prompt → GPT with `response_format:
 * json_schema` → JSON → domain model. Text features are the same, text-only.
 * Bound over [StubContentGenerator] only when a key is configured — see ContentGeneratorModule.
 */
@Singleton
class OpenAiContentGenerator @Inject constructor(
    private val client: OpenAiClient,
    private val config: OpenAiConfig,
    private val prompts: PromptBuilder,
    private val json: Json,
    private val diagnostics: DebugDiagnostics,
    @DefaultDispatcher private val cpu: CoroutineDispatcher,
) : ContentGenerator {

    override suspend fun palmReading(
        imageJpeg: ByteArray,
        hand: Hand,
        profile: UserProfile,
    ): PalmReading {
        val dataUrl = withContext(cpu) {
            "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageJpeg)
        }
        val request = ChatRequest(
            model = config.visionModel,
            messages = listOf(
                ChatMessage.system(prompts.palmSystemPrompt(profile, hand)),
                ChatMessage.user(
                    ContentPart.Text(
                        "Read this palm. Hand shown: ${hand.label}. " +
                            "Return JSON matching the schema; if it is not a clearly visible " +
                            "human palm set isPalm=false.",
                    ),
                    ContentPart.Image(ImageUrl(dataUrl)),
                ),
            ),
            responseFormat = responseFormat("palm_reading"),
        )
        val result = client.complete(request)
        diagnostics.recordPalm(
            PalmDiagnostics(
                model = config.visionModel,
                promptVersion = "palm_v1",
                latencyMs = result.latencyMs,
                rawJson = result.content,
                promptTokens = result.usage?.promptTokens,
                completionTokens = result.usage?.completionTokens,
                totalTokens = result.usage?.totalTokens,
                finishReason = result.finishReason,
            ),
        )
        return json.decodeFromString<PalmReading>(result.content).copy(hand = hand)
    }

    override suspend fun dailyBundle(
        zodiac: Zodiac,
        locale: String,
        needWeekly: Boolean,
        needMonthly: Boolean,
    ): DailyBundle {
        val today = LocalDate.now().toString()
        val profile = UserProfile(languageCode = locale)
        val request = ChatRequest(
            model = config.textModel,
            messages = listOf(
                ChatMessage.system(
                    prompts.dailySystemPrompt(profile, zodiac, today, needWeekly, needMonthly),
                ),
                ChatMessage.user(ContentPart.Text("Generate today's guidance as JSON.")),
            ),
            responseFormat = responseFormat("daily_bundle"),
        )
        val payload = json.decodeFromString<DailyPayload>(client.complete(request).content)
        return DailyBundle(
            date = today,
            zodiac = zodiac,
            highlights = payload.highlights,
            daily = payload.daily,
            weekly = payload.weekly,
            monthly = payload.monthly,
        )
    }

    override suspend fun loveResult(
        selfName: String,
        partnerName: String,
        partnerDob: LocalDate?,
        locale: String,
    ): LoveResult {
        val request = ChatRequest(
            model = config.textModel,
            messages = listOf(
                ChatMessage.system(
                    prompts.loveSystemPrompt(locale, selfName, partnerName, partnerDob?.toString()),
                ),
                ChatMessage.user(ContentPart.Text("Rate this pairing as JSON.")),
            ),
            responseFormat = responseFormat("love_result"),
        )
        return json.decodeFromString<LoveResult>(client.complete(request).content)
    }

    override suspend fun tarot(
        cards: List<TarotCard>,
        profile: UserProfile,
        locale: String,
    ): TarotResult {
        val request = ChatRequest(
            model = config.textModel,
            messages = listOf(
                ChatMessage.system(prompts.tarotSystemPrompt(profile.copy(languageCode = locale), cards)),
                ChatMessage.user(ContentPart.Text("Interpret the spread as JSON.")),
            ),
            responseFormat = responseFormat("tarot_result"),
        )
        val payload = json.decodeFromString<TarotPayload>(client.complete(request).content)
        return TarotResult(
            cards = cards,
            past = payload.past,
            present = payload.present,
            future = payload.future,
            overall = payload.overall,
        )
    }

    private suspend fun responseFormat(schemaName: String) = ResponseFormat(
        jsonSchema = JsonSchemaSpec(name = schemaName, schema = prompts.schema(schemaName)),
    )

    @Serializable
    private data class DailyPayload(
        val highlights: Highlights,
        val daily: HoroscopeEntry,
        val weekly: HoroscopeEntry,
        val monthly: HoroscopeEntry,
    )

    @Serializable
    private data class TarotPayload(
        val past: String,
        val present: String,
        val future: String,
        val overall: String,
    )
}
