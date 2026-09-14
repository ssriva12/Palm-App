package com.palmz.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.palmz.data.local.entity.LoveTestEntity

@Dao
interface LoveTestDao {
    @Query("SELECT * FROM love_tests WHERE pairHash = :hash")
    suspend fun get(hash: String): LoveTestEntity?

    @Upsert
    suspend fun upsert(entity: LoveTestEntity)

    @Query("DELETE FROM love_tests")
    suspend fun clear()
}
