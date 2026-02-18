package com.ramadan.iftartracker.data.prayer

import com.ramadan.iftartracker.domain.model.AsrJurisprudence
import com.ramadan.iftartracker.domain.model.CalculationMethod
import com.ramadan.iftartracker.domain.model.Coordinates
import com.ramadan.iftartracker.domain.model.PrayerTimes
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

@Singleton
class PrayerTimeCalculator @Inject constructor() {

    fun calculate(
        date: LocalDate,
        coordinates: Coordinates,
        method: CalculationMethod = CalculationMethod.MWL,
        asrJurisprudence: AsrJurisprudence = AsrJurisprudence.SHAFI,
    ): PrayerTimes {
        val jd = julianDate(date.year, date.monthValue, date.dayOfMonth)
        val decl = sunDeclination(jd)
        val eqt = equationOfTime(jd)
        val lat = coordinates.latitude
        val lng = coordinates.longitude

        // Solar noon in hours (UTC)
        val transit = 12.0 + (-eqt / 60.0) - (lng / 15.0)

        val fajrTime = transit - hourAngle(lat, decl, method.fajrAngle) / 15.0
        val sunriseTime = transit - hourAngle(lat, decl, 0.833) / 15.0
        val asrTime = transit + asrHourAngle(lat, decl, asrJurisprudence) / 15.0
        val sunsetTime = transit + hourAngle(lat, decl, 0.833) / 15.0

        val ishaTime = if (method == CalculationMethod.MAKKAH) {
            // Umm Al-Qura: Isha is 90 minutes after Maghrib
            sunsetTime + 1.5
        } else {
            transit + hourAngle(lat, decl, method.ishaAngle) / 15.0
        }

        // Apply timezone offset — convert UTC hours to local time
        val tzOffset = date.atStartOfDay(java.time.ZoneId.systemDefault())
            .offset.totalSeconds / 3600.0

        return PrayerTimes(
            date = date,
            fajr = hoursToLocalTime(fajrTime + tzOffset),
            sunrise = hoursToLocalTime(sunriseTime + tzOffset),
            dhuhr = hoursToLocalTime(transit + tzOffset),
            asr = hoursToLocalTime(asrTime + tzOffset),
            maghrib = hoursToLocalTime(sunsetTime + tzOffset),
            isha = hoursToLocalTime(ishaTime + tzOffset),
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunDeclination(jd: Double): Double {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))
        val e = 23.439 - 0.00000036 * d
        return radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))
    }

    private fun equationOfTime(jd: Double): Double {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = radToDeg(
            atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))
        ) / 15.0
        val raFixed = ra - floor(ra / 24.0) * 24.0
        return (q / 15.0 - fixHour(raFixed)) * 60.0
    }

    private fun hourAngle(lat: Double, decl: Double, angle: Double): Double {
        val latRad = degToRad(lat)
        val declRad = degToRad(decl)
        val cosHA = (-sin(degToRad(angle)) - sin(latRad) * sin(declRad)) /
            (cos(latRad) * cos(declRad))
        return radToDeg(acos(cosHA.coerceIn(-1.0, 1.0)))
    }

    private fun asrHourAngle(
        lat: Double,
        decl: Double,
        jurisprudence: AsrJurisprudence,
    ): Double {
        val latRad = degToRad(lat)
        val declRad = degToRad(decl)
        val factor = jurisprudence.shadowFactor
        val acot = atan(1.0 / (factor + tan(abs(latRad - declRad))))
        val cosHA = (sin(acot) - sin(latRad) * sin(declRad)) /
            (cos(latRad) * cos(declRad))
        return radToDeg(acos(cosHA.coerceIn(-1.0, 1.0)))
    }

    private fun hoursToLocalTime(hours: Double): LocalTime {
        val h = fixHour(hours)
        val totalSeconds = (h * 3600).toLong()
        val hr = (totalSeconds / 3600).toInt().coerceIn(0, 23)
        val min = ((totalSeconds % 3600) / 60).toInt().coerceIn(0, 59)
        val sec = (totalSeconds % 60).toInt().coerceIn(0, 59)
        return LocalTime.of(hr, min, sec)
    }

    private fun degToRad(d: Double) = d * Math.PI / 180.0
    private fun radToDeg(r: Double) = r * 180.0 / Math.PI

    private fun fixAngle(a: Double): Double {
        val result = a - 360.0 * floor(a / 360.0)
        return if (result < 0) result + 360.0 else result
    }

    private fun fixHour(h: Double): Double {
        val result = h - 24.0 * floor(h / 24.0)
        return if (result < 0) result + 24.0 else result
    }
}
