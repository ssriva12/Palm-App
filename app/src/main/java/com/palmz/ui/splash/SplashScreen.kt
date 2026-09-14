package com.palmz.ui.splash

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.palmz.R
import com.palmz.ui.components.MysticBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import com.palmz.ui.theme.Spacing

@Composable
fun SplashScreen(onNeedsAuth: () -> Unit, onOnboarded: () -> Unit, onNeedsOnboarding: () -> Unit) {
    val vm: SplashViewModel = hiltViewModel()
    var shown by remember { mutableStateOf(false) }
    val fade by animateFloatAsState(if (shown) 1f else 0f, label = "splashFade")

    LaunchedEffect(Unit) {
        shown = true
        delay(1600)
        when {
            !vm.isSignedIn -> onNeedsAuth()
            vm.onboarded.filterNotNull().first() -> onOnboarded()
            else -> onNeedsOnboarding()
        }
    }

    MysticBackground {
        Column(
            Modifier.fillMaxSize().alpha(fade),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.space6))
            Text(
                stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
