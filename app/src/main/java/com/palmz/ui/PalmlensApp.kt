package com.palmz.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.palmz.ui.auth.AuthScreen
import com.palmz.ui.highlights.HighlightsScreen
import com.palmz.ui.horoscope.HoroscopeScreen
import com.palmz.ui.language.LanguageScreen
import com.palmz.ui.love.LoveScreen
import com.palmz.ui.navigation.PalmlensBottomBar
import com.palmz.ui.navigation.Screen
import com.palmz.ui.navigation.TOP_LEVEL_TABS
import com.palmz.ui.navigation.rememberNavigator
import com.palmz.ui.onboarding.OnboardingScreen
import com.palmz.ui.readings.ReadingsScreen
import com.palmz.ui.scanner.ScannerScreen
import com.palmz.ui.settings.SettingsScreen
import com.palmz.ui.splash.SplashScreen
import com.palmz.ui.subscription.SubscriptionScreen
import com.palmz.ui.tarot.TarotScreen
import com.palmz.ui.today.TodayScreen

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
