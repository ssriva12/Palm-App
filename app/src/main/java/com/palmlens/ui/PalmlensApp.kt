package com.palmlens.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.palmlens.ui.auth.AuthScreen
import com.palmlens.ui.highlights.HighlightsScreen
import com.palmlens.ui.horoscope.HoroscopeScreen
import com.palmlens.ui.language.LanguageScreen
import com.palmlens.ui.love.LoveScreen
import com.palmlens.ui.navigation.PalmlensBottomBar
import com.palmlens.ui.navigation.Screen
import com.palmlens.ui.navigation.TOP_LEVEL_TABS
import com.palmlens.ui.navigation.rememberNavigator
import com.palmlens.ui.onboarding.OnboardingScreen
import com.palmlens.ui.readings.ReadingsScreen
import com.palmlens.ui.scanner.ScannerScreen
import com.palmlens.ui.settings.SettingsScreen
import com.palmlens.ui.splash.SplashScreen
import com.palmlens.ui.subscription.SubscriptionScreen
import com.palmlens.ui.tarot.TarotScreen
import com.palmlens.ui.today.TodayScreen

@Composable
fun PalmlensApp() {
    val nav = rememberNavigator(Screen.Splash)
    BackHandler(enabled = nav.canPop) { nav.pop() }
    val current = nav.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (current in TOP_LEVEL_TABS) {
                PalmlensBottomBar(current = current, onSelect = nav::replaceAll)
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad)) {
            Crossfade(targetState = current, label = "screen") { screen ->
                when (screen) {
                    Screen.Splash -> SplashScreen(
                        onNeedsAuth = { nav.replaceAll(Screen.Auth) },
                        onOnboarded = { nav.replaceAll(Screen.Today) },
                        onNeedsOnboarding = { nav.replaceAll(Screen.Language()) },
                    )

                    Screen.Auth -> AuthScreen(onAuthenticated = { nav.replaceAll(Screen.Splash) })

                    is Screen.Language -> LanguageScreen(
                        fromSettings = screen.fromSettings,
                        onDone = { if (screen.fromSettings) nav.pop() else nav.goTo(Screen.Onboarding) },
                    )

                    Screen.Onboarding -> OnboardingScreen(
                        onExit = { nav.pop() },
                        onDone = { nav.goTo(Screen.Subscription()) },
                    )

                    is Screen.Subscription -> SubscriptionScreen(
                        onSkip = { if (screen.fromScanner) nav.pop() else nav.replaceAll(Screen.Today) },
                        onSubscribed = { if (screen.fromScanner) nav.pop() else nav.replaceAll(Screen.Today) },
                    )

                    Screen.Today -> TodayScreen(
                        onOpenHighlights = { nav.goTo(Screen.Highlights) },
                        onOpenHoroscope = { tab -> nav.goTo(Screen.Horoscope(tab)) },
                        onOpenScanner = { nav.goTo(Screen.Scanner) },
                    )
                    Screen.Readings -> ReadingsScreen(onOpen = { nav.goTo(it) })

                    Screen.Scanner -> ScannerScreen(
                        onBack = { nav.pop() },
                        onPaywall = { nav.goTo(Screen.Subscription(fromScanner = true)) },
                    )
                    Screen.Highlights -> HighlightsScreen(onBack = { nav.pop() })
                    is Screen.Horoscope -> HoroscopeScreen(initialTab = screen.initialTab, onBack = { nav.pop() })
                    Screen.Love -> LoveScreen()
                    Screen.Tarot -> TarotScreen(onBack = { nav.pop() })
                    Screen.Settings -> SettingsScreen(
                        onChangeLanguage = { nav.goTo(Screen.Language(fromSettings = true)) },
                        onDataDeleted = { nav.replaceAll(Screen.Splash) },
                        onSignedOut = { nav.replaceAll(Screen.Splash) },
                    )
                }
            }
        }
    }
}
