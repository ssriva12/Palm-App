package com.palmz.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class HoroscopeSchedulerTest {

    private val six = LocalTime.of(6, 0)
    private val zone = ZoneId.of("Europe/Berlin")

    private fun at(h: Int, m: Int) =
        ZonedDateTime.of(2026, 3, 10, h, m, 0, 0, zone)

    @Test
    fun `before target time today is later the same day`() {
        assertEquals(Duration.ofHours(1).toMillis(), millisUntilNext(six, at(5, 0)))
    }

    @Test
    fun `after target time rolls to tomorrow`() {
        assertEquals(Duration.ofHours(23).toMillis(), millisUntilNext(six, at(7, 0)))
    }

    @Test
    fun `exactly at target time waits a full day, never zero`() {
        assertEquals(Duration.ofDays(1).toMillis(), millisUntilNext(six, at(6, 0)))
    }
}
