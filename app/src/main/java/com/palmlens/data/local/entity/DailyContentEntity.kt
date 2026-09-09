package com.palmlens.data.local.entity

import androidx.room.Entity

/** One row per (sign, date, locale) — the daily bundle cache (spec §3.4). */
@Entity(tableName = "daily_content", primaryKeys = ["sign", "date", "locale"])
data class DailyContentEntity(
    val sign: String,
    val date: String, // ISO LocalDate
    val locale: String,
    val bundleJson: String,
    val promptVersion: String,
    val weekIso: String, // e.g. "2026-W37"
    val monthIso: String, // e.g. "2026-09"
    val createdAt: Long,
)
