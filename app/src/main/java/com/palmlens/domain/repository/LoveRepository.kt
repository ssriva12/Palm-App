package com.palmlens.domain.repository

import com.palmlens.domain.model.LoveResult
import java.time.LocalDate

interface LoveRepository {
    /** Room-cached by hash of both DOBs — the same pairing always returns the same result. */
    suspend fun compatibility(
        selfName: String,
        partnerName: String,
        partnerDob: LocalDate?,
        locale: String,
    ): LoveResult
}
