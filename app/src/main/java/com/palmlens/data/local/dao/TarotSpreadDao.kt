package com.palmlens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.palmlens.data.local.entity.TarotSpreadEntity

@Dao
interface TarotSpreadDao {
    @Insert
    suspend fun insert(entity: TarotSpreadEntity): Long

    @Query("SELECT COUNT(*) FROM tarot_spreads WHERE forDate = :date")
    suspend fun countForDate(date: String): Int

    @Query("DELETE FROM tarot_spreads")
    suspend fun clear()
}
