package com.palmlens.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.palmlens.ui.components.MysticBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    val fade by animateFloatAsState(if (shown) 1f else 0f, label = "splashFade")

    LaunchedEffect(Unit) {
        shown = true
        delay(1600)
        onDone()
    }

    MysticBackground {
        Column(
            Modifier.fillMaxSize().alpha(fade),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🤚", fontSize = 76.sp)
            Spacer(Modifier.height(20.dp))
            Text(
                "Palmlens",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Read what your hands remember",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
