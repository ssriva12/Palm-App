package com.palmlens.domain.repository

import com.palmlens.domain.model.Hand
import com.palmlens.domain.model.PalmReading

interface PalmRepository {
    /**
     * Preprocessed JPEG bytes in, reading out. Persists the reading + increments the scan
     * counter on success (unless the image was rejected as not-a-palm). Caller must have
     * already checked [ScanQuotaRepository.canScan] — this does not gate the paywall.
     */
    suspend fun scan(hand: Hand, imageJpeg: ByteArray): PalmReading
}
