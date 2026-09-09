package com.palmlens.data.repository

import com.palmlens.core.coroutines.IoDispatcher
import com.palmlens.data.local.dao.LoveTestDao
import com.palmlens.data.local.entity.LoveTestEntity
import com.palmlens.domain.content.ContentGenerator
import com.palmlens.domain.model.LoveResult
import com.palmlens.domain.repository.LoveRepository
import com.palmlens.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoveRepositoryImpl @Inject constructor(
    private val contentGenerator: ContentGenerator,
    private val dao: LoveTestDao,
    private val profileRepository: ProfileRepository,
    private val json: Json,
    @IoDispatcher private val io: CoroutineDispatcher,
) : LoveRepository {

    override suspend fun compatibility(
        selfName: String,
        partnerName: String,
        partnerDob: LocalDate?,
        locale: String,
    ): LoveResult = withContext(io) {
        val selfDob = profileRepository.profile.first().dob
        val key = pairKey(selfDob, partnerDob, partnerName)

        dao.get(key)?.let { return@withContext json.decodeFromString<LoveResult>(it.resultJson) }

        val result = contentGenerator.loveResult(selfName, partnerName, partnerDob, locale)
        dao.upsert(LoveTestEntity(key, json.encodeToString(result), System.currentTimeMillis()))
        result
    }

    // ponytail: plain string key, not a crypto hash — it's a cache lookup, collisions are harmless.
    private fun pairKey(a: LocalDate?, b: LocalDate?, partnerName: String): String =
        "${a?.toEpochDay() ?: 0}|${b?.toEpochDay() ?: 0}|${partnerName.trim().lowercase()}"
}
