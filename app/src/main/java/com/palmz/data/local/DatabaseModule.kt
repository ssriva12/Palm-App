package com.palmz.data.local

import android.content.Context
import androidx.room.Room
import com.palmz.data.local.dao.AppStateDao
import com.palmz.data.local.dao.DailyContentDao
import com.palmz.data.local.dao.LoveTestDao
import com.palmz.data.local.dao.ReadingDao
import com.palmz.data.local.dao.TarotSpreadDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): PalmlensDatabase =
        Room.databaseBuilder(context, PalmlensDatabase::class.java, "palmlens.db")
            // proto: no migrations yet — wipe on schema change.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun readingDao(db: PalmlensDatabase): ReadingDao = db.readingDao()

    @Provides
    fun dailyContentDao(db: PalmlensDatabase): DailyContentDao = db.dailyContentDao()

    @Provides
    fun loveTestDao(db: PalmlensDatabase): LoveTestDao = db.loveTestDao()

    @Provides
    fun tarotSpreadDao(db: PalmlensDatabase): TarotSpreadDao = db.tarotSpreadDao()

    @Provides
    fun appStateDao(db: PalmlensDatabase): AppStateDao = db.appStateDao()
}
