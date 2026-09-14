package com.palmz.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ZodiacTest {

    @Test
    fun `sign boundaries resolve correctly`() {
        assertEquals(Zodiac.CAPRICORN, Zodiac.fromDate(LocalDate.of(2000, 1, 1)))
        assertEquals(Zodiac.CAPRICORN, Zodiac.fromDate(LocalDate.of(2000, 1, 19)))
        assertEquals(Zodiac.AQUARIUS, Zodiac.fromDate(LocalDate.of(2000, 1, 20)))
        assertEquals(Zodiac.AQUARIUS, Zodiac.fromDate(LocalDate.of(2000, 2, 18)))
        assertEquals(Zodiac.PISCES, Zodiac.fromDate(LocalDate.of(2000, 2, 19)))
        assertEquals(Zodiac.ARIES, Zodiac.fromDate(LocalDate.of(2000, 3, 21)))
        assertEquals(Zodiac.LEO, Zodiac.fromDate(LocalDate.of(2000, 8, 1)))
        assertEquals(Zodiac.SAGITTARIUS, Zodiac.fromDate(LocalDate.of(2000, 12, 21)))
        assertEquals(Zodiac.CAPRICORN, Zodiac.fromDate(LocalDate.of(2000, 12, 22)))
    }

    @Test
    fun `key round-trips`() {
        Zodiac.entries.forEach { assertEquals(it, Zodiac.fromKey(it.key)) }
    }
}
