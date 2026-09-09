package com.palmlens.ui.navigation

/**
 * The screen graph. Hand-rolled because the proto flow is linear and shallow.
 * ponytail: move to navigation-compose when you need deep links or per-screen
 * ViewModel scoping.
 */
sealed interface Screen {
    data object Splash : Screen
    data object Language : Screen
    data object Onboarding : Screen

    /** [fromScanner] true = opened as the hard paywall on scan 41 (pop back), false = the
     *  skippable onboarding offer (go to Home). */
    data class Subscription(val fromScanner: Boolean = false) : Screen

    data object Home : Screen
    data object Scanner : Screen
    data object Highlights : Screen
    data object Horoscope : Screen
    data object Love : Screen
    data object Tarot : Screen
}
