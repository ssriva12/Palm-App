package com.palmlens.data.repository

import com.palmlens.data.local.dao.AppStateDao
import com.palmlens.data.local.entity.AppStateEntity
import com.palmlens.domain.repository.DEFAULT_FREE_SCAN_CAP
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanQuotaRepositoryImplTest {

    private class FakeAppStateDao(initial: AppStateEntity? = AppStateEntity()) : AppStateDao {
        val state = MutableStateFlow(initial)
        override suspend fun ensureRow() {
            if (state.value == null) state.value = AppStateEntity()
        }
        override fun observe(): Flow<AppStateEntity?> = state
        override suspend fun get(): AppStateEntity? = state.value
        override suspend fun incrementScans() =
            state.update { (it ?: AppStateEntity()).let { e -> e.copy(scansUsed = e.scansUsed + 1) } }
        override suspend fun setLastTarotDate(date: String) =
            state.update { it?.copy(lastTarotDate = date) }
        override suspend fun clear() { state.value = null }
    }

    private fun repo(dao: AppStateDao) =
        ScanQuotaRepositoryImpl(dao, UnconfinedTestDispatcher())

    @Test
    fun `canScan is true below the cap and false at or above it`() = runTest {
        val cap = DEFAULT_FREE_SCAN_CAP
        val dao = FakeAppStateDao(AppStateEntity(scansUsed = cap - 1))
        val repo = repo(dao)

        assertTrue(repo.canScan.first())
        assertEquals(1, repo.remaining.first())

        dao.state.value = AppStateEntity(scansUsed = cap)
        assertFalse(repo.canScan.first())
        assertEquals(0, repo.remaining.first())

        dao.state.value = AppStateEntity(scansUsed = cap + 1)
        assertFalse(repo.canScan.first())
        assertEquals(0, repo.remaining.first())
    }

    @Test
    fun `recordScan increments the counter`() = runTest {
        val dao = FakeAppStateDao(AppStateEntity(scansUsed = 0))
        val repo = repo(dao)

        repo.recordScan()
        repo.recordScan()

        assertEquals(2, repo.scansUsed.first())
    }
}
