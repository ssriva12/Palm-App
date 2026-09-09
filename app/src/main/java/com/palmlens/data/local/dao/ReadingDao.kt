package com.palmlens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.palmlens.data.local.entity.ReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Insert
    suspend fun insert(entity: ReadingEntity): Long

    @Query("SELECT * FROM readings ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings ORDER BY createdAt DESC LIMIT 1")
    suspend fun latest(): ReadingEntity?

    @Query("DELETE FROM readings")
    suspend fun clear()
}
