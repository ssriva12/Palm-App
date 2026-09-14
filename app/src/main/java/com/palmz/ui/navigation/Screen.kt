package com.palmz.ui.navigation

/**
 * The screen graph. Hand-rolled because the proto flow is linear and shallow.
 * ponytail: move to navigation-compose when you need deep links or per-screen
 * ViewModel scoping.
 */
sealed interface Screen {
    data object Splash : Screen

    /** Email/password gate — shown whenever no Firebase user is signed in. */
    data object Auth : Screen

    /** [fromSettings] true = changing language later (pop back), false = the onboarding step. */
    data class Language(val fromSettings: Boolean = false) : Screen

    data object Onboarding : Screen

    /** [fromScanner] true = opened as the hard paywall once free scans run out (pop back),
     *  false = the skippable onboarding offer (go to Home). */
    data class Subscription(val fromScanner: Boolean = false) : Screen

    data object Today : Screen
    data object Readings : Screen
    data object Scanner : Screen
    data object Highlights : Screen

    /** [initialTab] 0=Daily, 1=Weekly, 2=Monthly, 3=Yearly. */
    data class Horoscope(val initialTab: Int = 0) : Screen

    data object Love : Screen
    data object Tarot : Screen
    data object Settings : Screen
}
