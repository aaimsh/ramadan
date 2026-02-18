package com.ramadan.iftartracker.data.prayer

import com.ramadan.iftartracker.domain.model.CalculationMethod
import com.ramadan.iftartracker.domain.model.Coordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class PrayerTimeCalculatorTest {

    private lateinit var calculator: PrayerTimeCalculator

    @Before
    fun setUp() {
        calculator = PrayerTimeCalculator()
    }

    @Test
    fun `prayer times are in chronological order`() {
        // Makkah coordinates
        val coords = Coordinates(21.4225, 39.8262)
        val date = LocalDate.of(2025, 3, 15)

        val times = calculator.calculate(date, coords, CalculationMethod.MAKKAH)

        assertTrue("Fajr should be before Sunrise", times.fajr.isBefore(times.sunrise))
        assertTrue("Sunrise should be before Dhuhr", times.sunrise.isBefore(times.dhuhr))
        assertTrue("Dhuhr should be before Asr", times.dhuhr.isBefore(times.asr))
        assertTrue("Asr should be before Maghrib", times.asr.isBefore(times.maghrib))
        assertTrue("Maghrib should be before Isha", times.maghrib.isBefore(times.isha))
    }

    @Test
    fun `fajr is in early morning hours`() {
        val coords = Coordinates(21.4225, 39.8262) // Makkah
        val date = LocalDate.of(2025, 3, 15)

        val times = calculator.calculate(date, coords, CalculationMethod.MAKKAH)

        assertTrue("Fajr should be after 3 AM", times.fajr.isAfter(LocalTime.of(3, 0)))
        assertTrue("Fajr should be before 7 AM", times.fajr.isBefore(LocalTime.of(7, 0)))
    }

    @Test
    fun `maghrib is in evening hours`() {
        val coords = Coordinates(21.4225, 39.8262) // Makkah
        val date = LocalDate.of(2025, 3, 15)

        val times = calculator.calculate(date, coords, CalculationMethod.MAKKAH)

        assertTrue("Maghrib should be after 5 PM", times.maghrib.isAfter(LocalTime.of(17, 0)))
        assertTrue("Maghrib should be before 8 PM", times.maghrib.isBefore(LocalTime.of(20, 0)))
    }

    @Test
    fun `iftar equals maghrib time`() {
        val coords = Coordinates(40.7128, -74.0060) // New York
        val date = LocalDate.of(2025, 3, 10)

        val times = calculator.calculate(date, coords, CalculationMethod.ISNA)

        assertEquals(times.maghrib, times.iftar)
    }

    @Test
    fun `suhoor equals fajr time`() {
        val coords = Coordinates(40.7128, -74.0060) // New York
        val date = LocalDate.of(2025, 3, 10)

        val times = calculator.calculate(date, coords, CalculationMethod.ISNA)

        assertEquals(times.fajr, times.suhoor)
    }

    @Test
    fun `different calculation methods produce different fajr times`() {
        val coords = Coordinates(51.5074, -0.1278) // London
        val date = LocalDate.of(2025, 3, 15)

        val mwl = calculator.calculate(date, coords, CalculationMethod.MWL)
        val isna = calculator.calculate(date, coords, CalculationMethod.ISNA)

        // MWL uses 18° for Fajr, ISNA uses 15° — MWL should be earlier
        assertTrue(
            "MWL Fajr (18°) should be earlier than ISNA Fajr (15°)",
            mwl.fajr.isBefore(isna.fajr),
        )
    }

    @Test
    fun `calculation works for southern hemisphere`() {
        val coords = Coordinates(-33.8688, 151.2093) // Sydney
        val date = LocalDate.of(2025, 3, 15)

        val times = calculator.calculate(date, coords, CalculationMethod.MWL)

        assertTrue("Fajr should be before Sunrise", times.fajr.isBefore(times.sunrise))
        assertTrue("Maghrib should be after Asr", times.maghrib.isAfter(times.asr))
    }
}
