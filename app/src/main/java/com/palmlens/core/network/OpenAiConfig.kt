package com.palmlens.core.network

/** Read from BuildConfig (which reads local.properties) in [NetworkModule]. */
data class OpenAiConfig(
    val apiKey: String,
    val baseUrl: String, // e.g. https://api.openai.com/v1 — or a proxy
    val visionModel: String,
    val textModel: String,
) {
    /** No key → the app falls back to StubContentGenerator (still builds/runs). */
    val isConfigured: Boolean get() = apiKey.isNotBlank()

    val chatCompletionsUrl: String get() = "${baseUrl.trimEnd('/')}/chat/completions"
}
