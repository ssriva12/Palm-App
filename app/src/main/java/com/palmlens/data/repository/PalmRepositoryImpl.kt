package com.palmlens.data.repository

import com.palmlens.core.coroutines.IoDispatcher
import com.palmlens.core.network.OpenAiConfig
import com.palmlens.data.local.dao.ReadingDao
import com.palmlens.data.local.entity.ReadingEntity
import com.palmlens.domain.content.ContentGenerator
import com.palmlens.domain.model.Hand
import com.palmlens.domain.model.ImageQuality
import com.palmlens.domain.model.PalmReading
import com.palmlens.domain.repository.PalmRepository
import com.palmlens.domain.repository.ProfileRepository
import com.palmlens.domain.repository.ScanQuotaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PalmRepositoryImpl @Inject constructor(
    private val contentGenerator: ContentGenerator,
    private val readingDao: ReadingDao,
    private val scanQuota: ScanQuotaRepository,
    private val profileRepository: ProfileRepository,
    private val openAiConfig: OpenAiConfig,
    private val json: Json,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PalmRepository {

    override suspend fun scan(hand: Hand, imageJpeg: ByteArray): PalmReading = withContext(io) {
        val profile = profileRepository.profile.first()
        val reading = contentGenerator.palmReading(imageJpeg, hand, profile)

        // Don't spend a scan on a rejected image (spec §3.6).
        if (reading.isPalm && reading.imageQuality != ImageQuality.POOR) {
            readingDao.insert(
                ReadingEntity(
                    createdAt = System.currentTimeMillis(),
                    hand = hand.name,
                    promptVersion = "palm_v1",
                    modelId = if (openAiConfig.isConfigured) openAiConfig.visionModel else "stub",
                    readingJson = json.encodeToString(reading),
                ),
            )
            scanQuota.recordScan()
        }
        reading
    }
}
