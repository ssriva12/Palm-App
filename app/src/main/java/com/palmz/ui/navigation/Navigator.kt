package com.palmz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

/** A tiny back-stack navigator over [Screen]. */
class Navigator(start: Screen) {
    private val stack = mutableStateListOf(start)

    val current: Screen get() = stack.last()
    val canPop: Boolean get() = stack.size > 1

    fun goTo(screen: Screen) {
        if (stack.last() != screen) stack.add(screen)
    }

    /** Clears history — use when moving past onboarding into the app proper. */
    fun replaceAll(screen: Screen) {
        stack.clear()
        stack.add(screen)
    }

    fun pop() {
        if (canPop) stack.removeAt(stack.lastIndex)
    }
}

@Composable
fun rememberNavigator(start: Screen): Navigator = remember { Navigator(start) }
