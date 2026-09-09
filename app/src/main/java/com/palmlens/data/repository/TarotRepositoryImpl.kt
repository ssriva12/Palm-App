package com.palmlens.data.repository

import com.palmlens.core.coroutines.IoDispatcher
import com.palmlens.data.asset.AssetLoader
import com.palmlens.data.local.dao.TarotSpreadDao
import com.palmlens.data.local.entity.TarotSpreadEntity
import com.palmlens.domain.content.ContentGenerator
import com.palmlens.domain.model.TarotCard
import com.palmlens.domain.model.TarotResult
import com.palmlens.domain.repository.ProfileRepository
import com.palmlens.domain.repository.TarotRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TarotRepositoryImpl @Inject constructor(
    private val contentGenerator: ContentGenerator,
    private val dao: TarotSpreadDao,
    private val profileRepository: ProfileRepository,
    private val assetLoader: AssetLoader,
    private val json: Json,
    @IoDispatcher private val io: CoroutineDispatcher,
) : TarotRepository {

    override suspend fun deck(): List<TarotCard> = withContext(io) {
        val raw = assetLoader.readText("tarot/deck.json")
        json.decodeFromString<DeckFile>(raw).cards.map { TarotCard(it.name, it.emoji) }
    }

    override suspend fun canDrawToday(): Boolean = withContext(io) {
        dao.countForDate(LocalDate.now().toString()) == 0
    }

    override suspend fun reading(picked: List<TarotCard>, locale: String): TarotResult = withContext(io) {
        val profile = profileRepository.profile.first()
        val result = contentGenerator.tarot(picked, profile, locale)
        dao.insert(
            TarotSpreadEntity(
                forDate = LocalDate.now().toString(),
                cardsJson = json.encodeToString(picked),
                resultJson = json.encodeToString(result),
                createdAt = System.currentTimeMillis(),
            ),
        )
        result
    }

    @Serializable
    private data class DeckFile(val cards: List<CardDto>)

    @Serializable
    private data class CardDto(
        val id: String,
        val name: String,
        val emoji: String,
        val arcana: String,
        val suit: String? = null,
    )
}
