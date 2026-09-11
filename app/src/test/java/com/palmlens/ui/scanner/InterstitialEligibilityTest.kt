package com.palmlens.ui.scanner

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialEligibilityTest {

    @Test
    fun `no interstitial before the second scan`() {
        assertFalse(interstitialEligible(0, cap = 10))
        assertFalse(interstitialEligible(1, cap = 10))
    }

    @Test
    fun `interstitial from the second scan through the free cap`() {
        assertTrue(interstitialEligible(2, cap = 10))
        assertTrue(interstitialEligible(10, cap = 10))
    }

    @Test
    fun `nothing past the cap (that user is at the paywall)`() {
        assertFalse(interstitialEligible(11, cap = 10))
    }
}
