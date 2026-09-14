package com.palmz.di

import com.palmz.core.network.OpenAiConfig
import com.palmz.data.content.OpenAiContentGenerator
import com.palmz.data.content.StubContentGenerator
import com.palmz.domain.content.ContentGenerator
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Picks the real OpenAI generator when a key is configured, otherwise the bundled stub —
 * so the app still builds and runs with no secrets. `Lazy` so only the chosen one is built.
 */
@Module
@InstallIn(SingletonComponent::class)
object ContentGeneratorModule {

    @Provides
    @Singleton
    fun contentGenerator(
        config: OpenAiConfig,
        openai: Lazy<OpenAiContentGenerator>,
        stub: Lazy<StubContentGenerator>,
    ): ContentGenerator = if (config.isConfigured) openai.get() else stub.get()
}
