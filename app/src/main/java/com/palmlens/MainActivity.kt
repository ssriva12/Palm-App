package com.palmlens

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.ads.AdManager
import com.palmlens.data.locale.LocaleStore
import com.palmlens.data.locale.withAppLocale
import com.palmlens.domain.repository.ThemePreferenceRepository
import com.palmlens.ui.PalmlensApp
import com.palmlens.ui.theme.PalmlensTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adManager: AdManager
    @Inject lateinit var themePreferenceRepository: ThemePreferenceRepository

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withAppLocale(LocaleStore.get(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adManager.gatherConsentAndInitialize(this)
        setContent {
            val darkOverride by themePreferenceRepository.darkModeOverride
                .collectAsStateWithLifecycle(initialValue = null)
            PalmlensTheme(darkTheme = darkOverride ?: isSystemInDarkTheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    PalmlensApp()
                }
            }
        }
    }
}
