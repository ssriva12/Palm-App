package com.palmlens.data.repository

import com.palmlens.core.coroutines.IoDispatcher
import com.palmlens.data.local.dao.DailyContentDao
import com.palmlens.data.local.entity.DailyContentEntity
import com.palmlens.domain.content.ContentGenerator
import com.palmlens.domain.model.DailyBundle
import com.palmlens.domain.model.Zodiac
import com.palmlens.domain.repository.HoroscopeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.temporal.WeekFields
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HoroscopeRepositoryImpl @Inject constructor(
    private val contentGenerator: ContentGenerator,
    private val dao: DailyContentDao,
    private val json: Json,
    @IoDispatcher private val io: CoroutineDispatcher,
) : HoroscopeRepository {

    override suspend fun dailyBundle(zodiac: Zodiac, locale: String): DailyBundle = withContext(io) {
        val today = LocalDate.now()
        val dateIso = today.toString()
        val weekIso = isoWeek(today)
        val monthIso = isoMonth(today)

        dao.get(zodiac.key, dateIso, locale)?.let {
            return@withContext json.decodeFromString<DailyBundle>(it.bundleJson)
        }

        // Regenerate weekly / monthly only on their boundary (spec §3.4).
        val previousRow = dao.latestForSign(zodiac.key, locale)
        val previous = previousRow?.let { json.decodeFromString<DailyBundle>(it.bundleJson) }
        val needWeekly = previousRow?.weekIso != weekIso
        val needMonthly = previousRow?.monthIso != monthIso

        val generated = contentGenerator.dailyBundle(zodiac, locale, needWeekly, needMonthly)
        val bundle = generated.copy(
            date = dateIso,
            weekly = if (needWeekly || previous == null) generated.weekly else previous.weekly,
            monthly = if (needMonthly || previous == null) generated.monthly else previous.monthly,
        )

        dao.upsert(
            DailyContentEntity(
                sign = zodiac.key,
                date = dateIso,
                locale = locale,
                bundleJson = json.encodeToString(bundle),
                promptVersion = "daily_v1",
                weekIso = weekIso,
                monthIso = monthIso,
                createdAt = System.currentTimeMillis(),
            ),
        )
        dao.pruneBefore(today.minusDays(30).toString())
        bundle
    }

    private fun isoWeek(d: LocalDate): String {
        val wf = WeekFields.ISO
        return "%d-W%02d".format(d.get(wf.weekBasedYear()), d.get(wf.weekOfWeekBasedYear()))
    }

    private fun isoMonth(d: LocalDate): String = "%d-%02d".format(d.year, d.monthValue)
}
