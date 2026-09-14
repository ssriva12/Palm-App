package com.palmz.core.network

import com.palmz.core.coroutines.IoDispatcher
import com.palmz.core.network.model.ChatRequest
import com.palmz.core.network.model.ChatResponse
import com.palmz.core.network.model.OpenAiErrorEnvelope
import com.palmz.core.network.model.Usage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Thin hand-rolled OpenAI Chat Completions client over OkHttp. */
@Singleton
class OpenAiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val config: OpenAiConfig,
    private val json: Json,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    data class Result(
        val content: String,
        val finishReason: String?,
        val usage: Usage?,
        val latencyMs: Long = 0, // wall time across retries — for the debug overlay
    )

    /**
     * [retryable] transient failures (network drop, HTTP 5xx/429, a truncated reply) are
     * retried a few times with backoff before this surfaces; [retryAfterMs] carries a 429's
     * `Retry-After` when the server sent one.
     */
    class OpenAiException(
        message: String,
        val statusCode: Int? = null,
        val retryable: Boolean = false,
        val retryAfterMs: Long? = null,
    ) : IOException(message)

    /** Retries transient failures with backoff; non-retryable errors surface on the first try. */
    suspend fun complete(request: ChatRequest): Result = withContext(io) {
        require(config.isConfigured) { "OPENAI_API_KEY is not set" }
        val startNs = System.nanoTime()
        fun stamped(result: Result) = result.copy(latencyMs = (System.nanoTime() - startNs) / 1_000_000)
        repeat(MAX_RETRIES) { attempt ->
            try {
                return@withContext stamped(executeOnce(request))
            } catch (e: OpenAiException) {
                if (!e.retryable) throw e
                delay(e.retryAfterMs ?: backoffMs(attempt))
            } catch (e: IOException) { // network failure — no HTTP response
                delay(backoffMs(attempt))
            }
        }
        stamped(executeOnce(request)) // last attempt — its exception propagates
    }

    private suspend fun executeOnce(request: ChatRequest): Result {
        val payload = json.encodeToString(ChatRequest.serializer(), request)
        val httpRequest = Request.Builder()
            .url(config.chatCompletionsUrl)
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(payload.toRequestBody(JSON_MEDIA))
            .build()

        okHttpClient.newCall(httpRequest).await().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw errorFrom(response, text)

            val parsed = json.decodeFromString(ChatResponse.serializer(), text)
            val choice = parsed.choices.firstOrNull()
                ?: throw OpenAiException("OpenAI returned no choices", retryable = true)
            choice.message.refusal?.let { throw OpenAiException("Model refused: $it") }
            when (choice.finishReason) {
                null, "stop", "tool_calls", "function_call" -> Unit
                "length" -> throw OpenAiException("The reading was cut off.", retryable = true)
                else -> throw OpenAiException("The reading was blocked (${choice.finishReason}).")
            }

            return Result(
                content = choice.message.content.orEmpty(),
                finishReason = choice.finishReason,
                usage = parsed.usage,
            )
        }
    }

    private fun errorFrom(response: Response, body: String): OpenAiException {
        val code = response.code
        val message = runCatching {
            json.decodeFromString(OpenAiErrorEnvelope.serializer(), body).error?.message
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "HTTP $code"
        return OpenAiException(
            message = message,
            statusCode = code,
            retryable = code == 429 || code in 500..599,
            retryAfterMs = response.header("Retry-After")?.toLongOrNull()?.times(1000),
        )
    }

    // ponytail: fixed exponential backoff, no jitter — one client, one call at a time.
    private fun backoffMs(attempt: Int): Long = BASE_BACKOFF_MS shl attempt

    private suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation { runCatching { cancel() } }
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) = cont.resume(response)
            override fun onFailure(call: Call, e: IOException) {
                if (!cont.isCancelled) cont.resumeWithException(e)
            }
        })
    }

    private companion object {
        val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        const val MAX_RETRIES = 2
        const val BASE_BACKOFF_MS = 400L
    }
}
