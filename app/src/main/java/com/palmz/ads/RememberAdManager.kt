package com.palmz.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors

/** Grabs the app-scoped [AdManager] from Compose (screens here only ever inject ViewModels). */
@Composable
fun rememberAdManager(): AdManager {
    val appContext = LocalContext.current.applicationContext
    return remember {
        EntryPointAccessors.fromApplication(appContext, AdManager.Provider::class.java).adManager()
    }
}
