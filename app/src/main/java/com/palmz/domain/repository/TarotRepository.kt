package com.palmz.domain.repository

import com.palmz.domain.model.TarotCard
import com.palmz.domain.model.TarotResult

interface TarotRepository {
    /** Full 78-card deck, loaded from a bundled asset (off the main thread). */
    suspend fun deck(): List<TarotCard>

    /** 1 free spread per day (spec §5). */
    suspend fun canDrawToday(): Boolean

    suspend fun reading(picked: List<TarotCard>, locale: String): TarotResult
}
