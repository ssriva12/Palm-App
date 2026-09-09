package com.palmlens.core.serialization

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/** The one JSON configuration the whole app uses — Room blobs, assets, and the OpenAI wire. */
val PalmlensJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    // OpenAI needs `response_format.type` / `json_schema.strict` on the wire even though
    // they equal their Kotlin defaults.
    encodeDefaults = true
}

@Module
@InstallIn(SingletonComponent::class)
object SerializationModule {

    @Provides
    @Singleton
    fun json(): Json = PalmlensJson
}
