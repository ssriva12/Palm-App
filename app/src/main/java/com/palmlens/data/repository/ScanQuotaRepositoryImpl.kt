package com.palmlens.data.repository

import com.palmlens.core.coroutines.IoDispatcher
import com.palmlens.data.local.dao.AppStateDao
import com.palmlens.domain.repository.DEFAULT_FREE_SCAN_CAP
import com.palmlens.domain.repository.ScanQuotaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanQuotaRepositoryImpl @Inject constructor(
    private val appStateDao: AppStateDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ScanQuotaRepository {

    override val scansUsed: Flow<Int> = appStateDao.observe().map { it?.scansUsed ?: 0 }

    // Phase 3: swap for RemoteConfigRepository.freeScanCap.
    override val cap: Flow<Int> = flowOf(DEFAULT_FREE_SCAN_CAP)

    override val remaining: Flow<Int> =
        combine(scansUsed, cap) { used, c -> (c - used).coerceAtLeast(0) }

    override val canScan: Flow<Boolean> = remaining.map { it > 0 }

    override suspend fun recordScan() = withContext(io) {
        appStateDao.ensureRow()
        appStateDao.incrementScans()
    }
}
