package com.palmz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single row (id = 0). Device-local counters.
 * ponytail: [scansUsed] is unread — quota moved to Firestore (see FirestoreScanQuotaRepository)
 * so a data clear can't reset it. Left in place rather than a Room migration for one dead column.
 */
@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey val id: Int = 0,
    val scansUsed: Int = 0,
    val lastTarotDate: String? = null,
)
