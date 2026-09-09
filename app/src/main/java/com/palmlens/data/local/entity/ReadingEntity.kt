package com.palmlens.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Reading JSON + line coordinates only — never the image (spec §2.7, §3.11). */
@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val hand: String,
    val promptVersion: String,
    val modelId: String,
    val readingJson: String,
)
