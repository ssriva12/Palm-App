package com.palmlens.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.palmlens.data.local.entity.DailyContentEntity
import com.palmlens.data.local.entity.LoveTestEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PalmlensDatabaseTest {

    private lateinit var db: PalmlensDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PalmlensDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun appState_incrementScans_countsFromZero() = runBlocking {
        val dao = db.appStateDao()
        dao.ensureRow()
        dao.incrementScans()
        dao.incrementScans()
        dao.incrementScans()
        assertEquals(3, dao.get()!!.scansUsed)
    }

    @Test
    fun dailyContent_isKeyedBySignDateLocale() = runBlocking {
        val dao = db.dailyContentDao()
        dao.upsert(
            DailyContentEntity(
                sign = "leo", date = "2026-09-09", locale = "en",
                bundleJson = "{}", promptVersion = "daily_v1",
                weekIso = "2026-W37", monthIso = "2026-09", createdAt = 0,
            ),
        )
        assertNotNull(dao.get("leo", "2026-09-09", "en"))
        assertNull(dao.get("leo", "2026-09-10", "en"))
        assertNull(dao.get("leo", "2026-09-09", "de"))
    }

    @Test
    fun dailyContent_pruneBefore_dropsOldRows() = runBlocking {
        val dao = db.dailyContentDao()
        fun row(date: String) = DailyContentEntity("leo", date, "en", "{}", "daily_v1", "w", "m", 0)
        dao.upsert(row("2026-08-01"))
        dao.upsert(row("2026-09-09"))
        dao.pruneBefore("2026-08-10")
        assertNull(dao.get("leo", "2026-08-01", "en"))
        assertNotNull(dao.get("leo", "2026-09-09", "en"))
    }

    @Test
    fun loveTest_upsertReplacesByHash() = runBlocking {
        val dao = db.loveTestDao()
        dao.upsert(LoveTestEntity("hash-1", """{"score":50}""", 0))
        dao.upsert(LoveTestEntity("hash-1", """{"score":80}""", 1))
        assertEquals("""{"score":80}""", dao.get("hash-1")!!.resultJson)
    }
}
