package com.palmz.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * OpenAI Chat Completions request. `content` is always the array form (OpenAI accepts it for
 * every role), so the discriminator field `type` on [ContentPart] lands exactly where the
 * API wants it: `{"type":"text","text":"…"}` / `{"type":"image_url", …}`.
 */
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
    val temperature: Double? = null,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int? = null,
)

@Serializable
data class ChatMessage(
    val role: String, // "system" | "user" | "assistant"
    val content: List<ContentPart>,
) {
    companion object {
        fun system(text: String) = ChatMessage("system", listOf(ContentPart.Text(text)))
        fun user(vararg parts: ContentPart) = ChatMessage("user", parts.toList())
    }
}

@Serializable
sealed interface ContentPart {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : ContentPart

    @Serializable
    @SerialName("image_url")
    data class Image(@SerialName("image_url") val imageUrl: ImageUrl) : ContentPart
}

@Serializable
data class ImageUrl(val url: String, val detail: String = "auto")

@Serializable
data class ResponseFormat(
    val type: String = "json_schema",
    @SerialName("json_schema") val jsonSchema: JsonSchemaSpec,
)

@Serializable
data class JsonSchemaSpec(
    val name: String,
    val schema: JsonObject,
    val strict: Boolean = true,
)
