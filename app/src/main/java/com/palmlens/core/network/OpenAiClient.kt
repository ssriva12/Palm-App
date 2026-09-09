package com.palmlens.core.network

import com.palmlens.core.coroutines.IoDispatcher
import com.palmlens.core.network.model.ChatRequest
import com.palmlens.core.network.model.ChatResponse
import com.palmlens.core.network.model.OpenAiErrorEnvelope
import com.palmlens.core.network.model.Usage
import kotlinx.coroutines.CoroutineDispatcher
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

    data class Result(val content: String, val finishReason: String?, val usage: Usage?)

    class OpenAiException(message: String, val statusCode: Int? = null) : IOException(message)

    suspend fun complete(request: ChatRequest): Result = withContext(io) {
        require(config.isConfigured) { "OPENAI_API_KEY is not set" }

        val payload = json.encodeToString(ChatRequest.serializer(), request)
        val httpRequest = Request.Builder()
            .url(config.chatCompletionsUrl)
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(payload.toRequestBody(JSON_MEDIA))
            .build()

        okHttpClient.newCall(httpRequest).await().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw errorFrom(response.code, text)

            val parsed = json.decodeFromString(ChatResponse.serializer(), text)
            val choice = parsed.choices.firstOrNull()
                ?: throw OpenAiException("OpenAI returned no choices")
            choice.message.refusal?.let { throw OpenAiException("Model refused: $it") }

            Result(
                content = choice.message.content.orEmpty(),
                finishReason = choice.finishReason,
                usage = parsed.usage,
            )
        }
    }

    private fun errorFrom(code: Int, body: String): OpenAiException {
        val message = runCatching {
            json.decodeFromString(OpenAiErrorEnvelope.serializer(), body).error?.message
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "HTTP $code"
        return OpenAiException(message, code)
    }

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
    }
}
