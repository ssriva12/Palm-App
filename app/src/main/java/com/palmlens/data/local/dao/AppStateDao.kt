package com.palmlens.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.palmlens.data.local.entity.AppStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppStateDao {
    @Query("INSERT OR IGNORE INTO app_state (id, scansUsed, lastTarotDate) VALUES (0, 0, NULL)")
    suspend fun ensureRow()

    @Query("SELECT * FROM app_state WHERE id = 0")
    fun observe(): Flow<AppStateEntity?>

    @Query("SELECT * FROM app_state WHERE id = 0")
    suspend fun get(): AppStateEntity?

    @Query("UPDATE app_state SET scansUsed = scansUsed + 1 WHERE id = 0")
    suspend fun incrementScans()

    @Query("UPDATE app_state SET lastTarotDate = :date WHERE id = 0")
    suspend fun setLastTarotDate(date: String)

    @Query("DELETE FROM app_state")
    suspend fun clear()
}
