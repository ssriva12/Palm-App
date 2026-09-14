package com.palmz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.palmz.data.local.dao.AppStateDao
import com.palmz.data.local.dao.DailyContentDao
import com.palmz.data.local.dao.LoveTestDao
import com.palmz.data.local.dao.ReadingDao
import com.palmz.data.local.dao.TarotSpreadDao
import com.palmz.data.local.entity.AppStateEntity
import com.palmz.data.local.entity.DailyContentEntity
import com.palmz.data.local.entity.LoveTestEntity
import com.palmz.data.local.entity.ReadingEntity
import com.palmz.data.local.entity.TarotSpreadEntity

@Database(
    entities = [
        ReadingEntity::class,
        DailyContentEntity::class,
        LoveTestEntity::class,
        TarotSpreadEntity::class,
        AppStateEntity::class,
    ],
    version = 1,
    // ponytail: exportSchema off until the first real migration; flip to true + add
    // room.schemaLocation when v2 lands.
    exportSchema = false,
)
abstract class PalmlensDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao
    abstract fun dailyContentDao(): DailyContentDao
    abstract fun loveTestDao(): LoveTestDao
    abstract fun tarotSpreadDao(): TarotSpreadDao
    abstract fun appStateDao(): AppStateDao
}
