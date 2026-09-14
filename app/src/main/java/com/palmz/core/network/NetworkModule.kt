package com.palmz.core.network

import com.palmz.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun openAiConfig(): OpenAiConfig = OpenAiConfig(
        apiKey = BuildConfig.OPENAI_API_KEY,
        baseUrl = BuildConfig.OPENAI_BASE_URL,
        visionModel = BuildConfig.OPENAI_VISION_MODEL,
        textModel = BuildConfig.OPENAI_TEXT_MODEL,
    )

    @Provides
    @Singleton
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS) // vision + reasoning can be slow
        .callTimeout(150, TimeUnit.SECONDS)
        .build()
}
