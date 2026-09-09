package com.palmlens.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val choices: List<Choice> = emptyList(),
    val usage: Usage? = null,
)

@Serializable
data class Choice(
    val message: ResponseMessage,
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class ResponseMessage(
    val content: String? = null,
    val refusal: String? = null,
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0,
)

@Serializable
data class OpenAiErrorEnvelope(val error: OpenAiError? = null)

@Serializable
data class OpenAiError(
    val message: String = "",
    val type: String? = null,
    val code: String? = null,
)
