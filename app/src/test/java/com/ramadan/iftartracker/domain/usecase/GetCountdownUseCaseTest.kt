package com.ramadan.iftartracker.domain.usecase

import com.ramadan.iftartracker.domain.model.PrayerTimes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class GetCountdownUseCaseTest {

    private lateinit var useCase: GetCountdownUseCase
    private lateinit var prayerTimes: PrayerTimes

    @Before
    fun setUp() {
        useCase = GetCountdownUseCase()
        prayerTimes = PrayerTimes(
            date = LocalDate.of(2025, 3, 15),
            fajr = LocalTime.of(5, 30),
            sunrise = LocalTime.of(6, 45),
            dhuhr = LocalTime.of(12, 15),
            asr = LocalTime.of(15, 30),
            maghrib = LocalTime.of(18, 30),
            isha = LocalTime.of(20, 0),
        )
    }

    @Test
    fun `during fasting hours shows countdown to iftar`() {
        val now = LocalDateTime.of(2025, 3, 15, 12, 0) // Noon

        val result = useCase(prayerTimes, now)

        assertTrue(result.isFasting)
        assertEquals("Iftar", result.targetName)
        assertEquals(6, result.hours)
        assertEquals(30, result.minutes)
    }

    @Test
    fun `before fajr shows countdown to suhoor`() {
        val now = LocalDateTime.of(2025, 3, 15, 4, 30) // 4:30 AM

        val result = useCase(prayerTimes, now)

        assertFalse(result.isFasting)
        assertEquals("Suhoor", result.targetName)
        assertEquals(1, result.hours)
        assertEquals(0, result.minutes)
    }

    @Test
    fun `after maghrib shows countdown to suhoor`() {
        val now = LocalDateTime.of(2025, 3, 15, 19, 0) // 7 PM

        val result = useCase(prayerTimes, now)

        assertFalse(result.isFasting)
        assertEquals("Suhoor", result.targetName)
    }

    @Test
    fun `just before iftar shows small countdown`() {
        val now = LocalDateTime.of(2025, 3, 15, 18, 29) // 6:29 PM

        val result = useCase(prayerTimes, now)

        assertTrue(result.isFasting)
        assertEquals("Iftar", result.targetName)
        assertEquals(0, result.hours)
        assertEquals(1, result.minutes)
    }

    @Test
    fun `display time is properly formatted`() {
        val now = LocalDateTime.of(2025, 3, 15, 12, 0)

        val result = useCase(prayerTimes, now)

        assertEquals("06:30:00", result.displayTime)
    }
}
