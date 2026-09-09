package com.palmlens.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single row (id = 0). Device-local counters (spec §1.4). */
@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey val id: Int = 0,
    val scansUsed: Int = 0,
    val lastTarotDate: String? = null,
)
