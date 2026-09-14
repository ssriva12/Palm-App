package com.palmz.domain.model

import kotlinx.serialization.Serializable

/** Topic-based takeaways synthesised across the four lines (spec: money/career/relationship ask). */
@Serializable
data class LifeAreaInsights(
    val money: String,
    val career: String,
    val relationships: String,
    val marriage: String,
    val family: String,
)
