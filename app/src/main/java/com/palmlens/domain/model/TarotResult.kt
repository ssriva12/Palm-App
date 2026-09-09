package com.palmlens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TarotCard(
    val name: String,
    val emoji: String,
    val reversed: Boolean = false,
)

@Serializable
enum class TarotPosition(val label: String) { PAST("Past"), PRESENT("Present"), FUTURE("Future") }

@Serializable
data class TarotResult(
    val cards: List<TarotCard>, // exactly 3, in past / present / future order
    val past: String,
    val present: String,
    val future: String,
    val overall: String,
) {
    fun textFor(position: TarotPosition): String = when (position) {
        TarotPosition.PAST -> past
        TarotPosition.PRESENT -> present
        TarotPosition.FUTURE -> future
    }
}
