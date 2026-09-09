package com.palmlens.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.palmlens.ui.highlights.HighlightsScreen
import com.palmlens.ui.home.HomeScreen
import com.palmlens.ui.horoscope.HoroscopeScreen
import com.palmlens.ui.language.LanguageScreen
import com.palmlens.ui.love.LoveScreen
import com.palmlens.ui.navigation.Screen
import com.palmlens.ui.navigation.rememberNavigator
import com.palmlens.ui.onboarding.OnboardingScreen
import com.palmlens.ui.scanner.ScannerScreen
import com.palmlens.ui.splash.SplashScreen
import com.palmlens.ui.subscription.SubscriptionScreen
import com.palmlens.ui.tarot.TarotScreen

@Composable
fun PalmlensApp() {
    val nav = rememberNavigator(Screen.Splash)
    BackHandler(enabled = nav.canPop) { nav.pop() }

    Crossfade(targetState = nav.current, label = "screen", modifier = Modifier) { screen ->
        when (screen) {
            Screen.Splash -> SplashScreen(onDone = { nav.replaceAll(Screen.Language) })

            Screen.Language -> LanguageScreen(onContinue = { nav.goTo(Screen.Onboarding) })

            Screen.Onboarding -> OnboardingScreen(
                onExit = { nav.pop() },
                onDone = { nav.goTo(Screen.Subscription()) },
            )

            is Screen.Subscription -> SubscriptionScreen(
                onSkip = { if (screen.fromScanner) nav.pop() else nav.replaceAll(Screen.Home) },
                onSubscribed = { if (screen.fromScanner) nav.pop() else nav.replaceAll(Screen.Home) },
            )

            Screen.Home -> HomeScreen(onOpen = { nav.goTo(it) })

            Screen.Scanner -> ScannerScreen(
                onBack = { nav.pop() },
                onPaywall = { nav.goTo(Screen.Subscription(fromScanner = true)) },
            )
            Screen.Highlights -> HighlightsScreen(onBack = { nav.pop() })
            Screen.Horoscope -> HoroscopeScreen(onBack = { nav.pop() })
            Screen.Love -> LoveScreen(onBack = { nav.pop() })
            Screen.Tarot -> TarotScreen(onBack = { nav.pop() })
        }
    }
}
