package com.palmlens.data.asset

import android.content.Context
import com.palmlens.core.coroutines.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    suspend fun readText(path: String): String = withContext(io) {
        context.assets.open(path).bufferedReader().use { it.readText() }
    }
}
