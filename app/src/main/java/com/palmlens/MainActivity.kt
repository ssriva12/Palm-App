package com.palmlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.palmlens.ui.PalmlensApp
import com.palmlens.ui.theme.PalmlensTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PalmlensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ) {
                    PalmlensApp()
                }
            }
        }
    }
}
