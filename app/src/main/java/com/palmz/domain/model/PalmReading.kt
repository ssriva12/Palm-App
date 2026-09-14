package com.palmz.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Hand(val label: String) { LEFT("Left"), RIGHT("Right") }

@Serializable
enum class ImageQuality { GOOD, USABLE, POOR }

@Serializable
enum class LineId(val label: String, val emoji: String) {
    LIFE("Life line", "🌿"),
    HEAD("Head line", "🧠"),
    HEART("Heart line", "❤️"),
    FATE("Fate line", "🧭"),
}

/** A normalised point (0..1) in the space of the image the model saw. */
@Serializable
data class NormPoint(val x: Float, val y: Float)

@Serializable
data class LineAttributes(
    val length: String,
    val depth: String,
    val shape: String,
    val breaks: Int,
    val forked: Boolean,
    val doubled: Boolean,
) {
    /** Short chips for the result card. */
    val chips: List<String>
        get() = buildList {
            add(length); add(depth); add(shape)
            if (breaks > 0) add("$breaks break${if (breaks > 1) "s" else ""}")
            if (forked) add("forked")
            if (doubled) add("doubled")
        }
}

@Serializable
data class PalmLine(
    val id: LineId,
    val visible: Boolean,
    val attributes: LineAttributes,
    val path: List<NormPoint>,
    val teaser: String,
    val reading: String,
    val confidence: Float,
)

@Serializable
data class PalmReading(
    val hand: Hand,
    val isPalm: Boolean,
    val imageQuality: ImageQuality,
    val summary: String,
    val lines: List<PalmLine>,
    val lifeAreas: LifeAreaInsights,
    val luckyNumber: Int,
    val luckyColor: String,
)
