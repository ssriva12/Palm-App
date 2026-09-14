package com.palmz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Keyed by hash of both DOBs — same pairing always returns the same result (spec §5). */
@Entity(tableName = "love_tests")
data class LoveTestEntity(
    @PrimaryKey val pairHash: String,
    val resultJson: String,
    val createdAt: Long,
)
