package com.palmlens.domain.model

/** The 9 launch locales (decision #4 in the spec). */
enum class Language(val code: String, val displayName: String, val endonym: String) {
    EN("en", "English", "English"),
    HI("hi", "Hindi", "हिन्दी"),
    ES("es", "Spanish", "Español"),
    PT_PT("pt-PT", "Portuguese (Portugal)", "Português"),
    JA("ja", "Japanese", "日本語"),
    ZH_HANS("zh-Hans", "Chinese (Simplified)", "简体中文"),
    FR("fr", "French", "Français"),
    DE("de", "German", "Deutsch"),
    IT("it", "Italian", "Italiano");

    companion object {
        fun fromCode(code: String): Language = entries.firstOrNull { it.code == code } ?: EN
    }
}
