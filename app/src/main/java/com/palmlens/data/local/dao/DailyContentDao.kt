package com.palmlens.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.palmlens.data.local.entity.DailyContentEntity

@Dao
interface DailyContentDao {
    @Query("SELECT * FROM daily_content WHERE sign = :sign AND date = :date AND locale = :locale")
    suspend fun get(sign: String, date: String, locale: String): DailyContentEntity?

    @Query("SELECT * FROM daily_content WHERE sign = :sign AND locale = :locale ORDER BY date DESC LIMIT 1")
    suspend fun latestForSign(sign: String, locale: String): DailyContentEntity?

    @Upsert
    suspend fun upsert(entity: DailyContentEntity)

    @Query("DELETE FROM daily_content WHERE date < :isoDate")
    suspend fun pruneBefore(isoDate: String)

    @Query("DELETE FROM daily_content")
    suspend fun clear()
}
