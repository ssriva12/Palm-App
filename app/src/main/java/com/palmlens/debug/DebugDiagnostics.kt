package com.palmlens.debug

import javax.inject.Inject
import javax.inject.Singleton

/** What the last palm generation actually did — surfaced by the debug overlay (spec §7). */
data class PalmDiagnostics(
    val model: String,
    val promptVersion: String,
    val latencyMs: Long,
    val rawJson: String?, // null for the bundled stub (no model call)
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null,
    val finishReason: String? = null,
)

/**
 * Process-lifetime holder for the most recent palm reading's generation details. Written by
 * whichever [com.palmlens.domain.content.ContentGenerator] ran; read by the debug overlay.
 * Cheap enough to keep in release builds; only the overlay (DEBUG-gated) ever reads it.
 */
@Singleton
class DebugDiagnostics @Inject constructor() {

    @Volatile
    var lastPalm: PalmDiagnostics? = null
        private set

    fun recordPalm(diagnostics: PalmDiagnostics) {
        lastPalm = diagnostics
    }
}
