package com.palmlens.ui.navigation

/**
 * The screen graph. Hand-rolled because the proto flow is linear and shallow.
 * ponytail: move to navigation-compose when you need deep links or per-screen
 * ViewModel scoping.
 */
sealed interface Screen {
    data object Splash : Screen

    /** [fromSettings] true = changing language later (pop back), false = the onboarding step. */
    data class Language(val fromSettings: Boolean = false) : Screen

    data object Onboarding : Screen

    /** [fromScanner] true = opened as the hard paywall once free scans run out (pop back),
     *  false = the skippable onboarding offer (go to Home). */
    data class Subscription(val fromScanner: Boolean = false) : Screen

    data object Home : Screen
    data object Scanner : Screen
    data object Highlights : Screen
    data object Horoscope : Screen
    data object Love : Screen
    data object Tarot : Screen
    data object Settings : Screen
}
