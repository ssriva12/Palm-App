package com.palmlens.domain.repository

import kotlinx.coroutines.flow.Flow

/** Fallback cap used before Remote Config is wired in (Phase 3). */
const val DEFAULT_FREE_SCAN_CAP = 40

/**
 * The 40 free lifetime scans (spec §1.4). UX only, not security — a reinstall resets it.
 */
interface ScanQuotaRepository {
    val scansUsed: Flow<Int>
    val cap: Flow<Int>
    val remaining: Flow<Int>
    val canScan: Flow<Boolean>

    suspend fun recordScan()
}
