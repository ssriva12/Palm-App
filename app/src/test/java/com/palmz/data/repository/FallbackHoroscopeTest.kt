package com.palmz.data.repository

import com.palmz.core.serialization.PalmlensJson
import com.palmz.domain.model.Zodiac
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

class FallbackHoroscopeTest {

    // Unit tests run with the module dir as the working directory.
    private val asset = File("src/main/assets/fallback/horoscopes.json").readText()

    private fun build(zodiac: Zodiac, locale: String, day: LocalDate) =
        fallbackBundle(PalmlensJson, asset, zodiac, locale, day)

    @Test
    fun `real asset yields a usable English bundle for every sign`() {
        Zodiac.entries.forEach { sign ->
            val bundle = build(sign, "en", LocalDate.of(2026, 1, 1))
            assertEquals(sign, bundle.zodiac)
            assertTrue("blank overview for $sign", bundle.daily.overview.isNotBlank())
            assertFalse("placeholder leaked for $sign", bundle.daily.overview.equals("TODO", true))
        }
    }

    @Test
    fun `rotates by day of year`() {
        val jan1 = build(Zodiac.ARIES, "en", LocalDate.of(2026, 1, 1)).daily.overview // dayOfYear 1
        val jan2 = build(Zodiac.ARIES, "en", LocalDate.of(2026, 1, 2)).daily.overview // dayOfYear 2
        assertFalse("expected two distinct evergreen lines", jan1 == jan2)
    }

    @Test
    fun `unfilled locale falls back to English, not a TODO placeholder`() {
        val hi = build(Zodiac.LEO, "hi", LocalDate.of(2026, 3, 15)).daily.overview
        val en = build(Zodiac.LEO, "en", LocalDate.of(2026, 3, 15)).daily.overview
        assertEquals(en, hi)
        assertFalse(hi.contains("TODO"))
    }
}
