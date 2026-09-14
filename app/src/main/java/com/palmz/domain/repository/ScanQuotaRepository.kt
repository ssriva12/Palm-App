package com.palmz.domain.repository

import kotlinx.coroutines.flow.Flow

/** Free scans before the paywall. A plain constant (no Remote Config). */
const val DEFAULT_FREE_SCAN_CAP = 10

/**
 * The free lifetime scans (spec §1.4). UX only, not security: a reinstall resets it.
 */
interface ScanQuotaRepository {
    val scansUsed: Flow<Int>
    val cap: Flow<Int>
    val remaining: Flow<Int>
    val canScan: Flow<Boolean>

    suspend fun recordScan()
}
