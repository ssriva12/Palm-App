package com.palmlens.domain.model

import java.time.LocalDate
import java.time.LocalTime

enum class Gender(val label: String) { MALE("Male"), FEMALE("Female"), OTHER("Other") }

/** The context blob sent with every model call (spec §1.2). Persisted in DataStore. */
data class UserProfile(
    val name: String = "",
    val gender: Gender? = null,
    val dob: LocalDate? = null,
    val birthTime: LocalTime? = null,
    val birthTimeKnown: Boolean = false,
    val birthPlace: String = "",
    val languageCode: String = "en",
) {
    val language: Language get() = Language.fromCode(languageCode)
    val zodiac: Zodiac? get() = dob?.let(Zodiac::fromDate)
    val isOnboarded: Boolean get() = name.isNotBlank() && dob != null
}
