package com.palmlens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LoveResult(
    val score: Int, // 0..100
    val strengths: List<String>,
    val challenges: List<String>,
    val verdict: String,
)
