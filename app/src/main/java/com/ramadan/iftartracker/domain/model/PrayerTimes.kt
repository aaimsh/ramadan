package com.ramadan.iftartracker.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class PrayerTimes(
    val date: LocalDate,
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime,
) {
    val iftar: LocalTime get() = maghrib
    val suhoor: LocalTime get() = fajr
}

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

enum class CalculationMethod(
    val displayName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
) {
    MWL("Muslim World League", 18.0, 17.0),
    ISNA("Islamic Society of North America", 15.0, 15.0),
    EGYPT("Egyptian General Authority", 19.5, 17.5),
    MAKKAH("Umm Al-Qura, Makkah", 18.5, 90.0), // Isha = 90 min after Maghrib
    KARACHI("University of Islamic Sciences, Karachi", 18.0, 18.0),
}

enum class AsrJurisprudence(val shadowFactor: Int) {
    SHAFI(1),
    HANAFI(2),
}
