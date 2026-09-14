package com.palmz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tarot_spreads")
data class TarotSpreadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val forDate: String,
    val cardsJson: String,
    val resultJson: String,
    val createdAt: Long,
)
