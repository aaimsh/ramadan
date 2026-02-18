package com.ramadan.iftartracker

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

class MainActivity : Activity() {

    private lateinit var hoursText: TextView
    private lateinit var minutesText: TextView
    private lateinit var secondsText: TextView
    private lateinit var countdownLabel: TextView
    private lateinit var targetName: TextView
    private lateinit var dateText: TextView
    private lateinit var locationText: TextView
    private lateinit var methodText: TextView

    private lateinit var rowFajr: View
    private lateinit var rowSunrise: View
    private lateinit var rowDhuhr: View
    private lateinit var rowAsr: View
    private lateinit var rowMaghrib: View
    private lateinit var rowIsha: View

    private val handler = Handler(Looper.getMainLooper())
    private var prayerTimes: PrayerTimesData? = null
    private val LOCATION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        hoursText = findViewById(R.id.hoursText) as TextView
        minutesText = findViewById(R.id.minutesText) as TextView
        secondsText = findViewById(R.id.secondsText) as TextView
        countdownLabel = findViewById(R.id.countdownLabel) as TextView
        targetName = findViewById(R.id.targetName) as TextView
        dateText = findViewById(R.id.dateText) as TextView
        locationText = findViewById(R.id.locationText) as TextView
        methodText = findViewById(R.id.methodText) as TextView
        rowFajr = findViewById(R.id.rowFajr)
        rowSunrise = findViewById(R.id.rowSunrise)
        rowDhuhr = findViewById(R.id.rowDhuhr)
        rowAsr = findViewById(R.id.rowAsr)
        rowMaghrib = findViewById(R.id.rowMaghrib)
        rowIsha = findViewById(R.id.rowIsha)

        // Set date
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        dateText.text = dateFormat.format(Date())
        methodText.text = "Calculation: Muslim World League (MWL)"

        requestLocationAndCalculate()
    }

    private fun requestLocationAndCalculate() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            // Use default location (Makkah) while waiting
            calculateAndDisplay(21.4225, 39.8262)
            locationText.text = "Using default location (Makkah) — grant location for accuracy"
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            getLocation()
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun getLocation() {
        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // Try last known location first
            val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val last = lastGps ?: lastNet

            if (last != null) {
                onLocationObtained(last)
            } else {
                // Request fresh location
                val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER

                locationManager.requestSingleUpdate(provider, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        onLocationObtained(location)
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String?) {}
                    override fun onProviderDisabled(provider: String?) {}
                }, Looper.getMainLooper())

                // Use default while waiting
                calculateAndDisplay(21.4225, 39.8262)
                locationText.text = "Locating... using Makkah times as default"
            }
        } catch (e: Exception) {
            calculateAndDisplay(21.4225, 39.8262)
            locationText.text = "Location unavailable — using Makkah"
        }
    }

    private fun onLocationObtained(location: Location) {
        calculateAndDisplay(location.latitude, location.longitude)
        locationText.text = String.format(Locale.US, "Location: %.2f°, %.2f°", location.latitude, location.longitude)
    }

    private fun calculateAndDisplay(lat: Double, lng: Double) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        prayerTimes = PrayerTimeCalculator.calculate(year, month, day, lat, lng)
        displayPrayerTimes()
        startCountdown()
    }

    private fun displayPrayerTimes() {
        val pt = prayerTimes ?: return
        setPrayerRow(rowFajr, "Fajr (Suhoor)", pt.fajr, pt.isFajrHighlighted)
        setPrayerRow(rowSunrise, "Sunrise", pt.sunrise, false)
        setPrayerRow(rowDhuhr, "Dhuhr", pt.dhuhr, false)
        setPrayerRow(rowAsr, "Asr", pt.asr, false)
        setPrayerRow(rowMaghrib, "Maghrib (Iftar)", pt.maghrib, pt.isMaghribHighlighted)
        setPrayerRow(rowIsha, "Isha", pt.isha, false)
    }

    private fun setPrayerRow(row: View, name: String, timeMinutes: Double, highlighted: Boolean) {
        val nameView = row.findViewById(R.id.prayerName) as TextView
        val timeView = row.findViewById(R.id.prayerTime) as TextView
        nameView.text = name
        timeView.text = formatTime(timeMinutes)

        if (highlighted) {
            (row as? LinearLayout)?.setBackgroundColor(Color.parseColor("#2A1B45"))
            nameView.setTextColor(Color.parseColor("#FFD700"))
            timeView.setTextColor(Color.parseColor("#FFD700"))
        } else {
            (row as? LinearLayout)?.setBackgroundColor(Color.parseColor("#152238"))
            nameView.setTextColor(Color.parseColor("#BBC5D5"))
            timeView.setTextColor(Color.parseColor("#FFF8E7"))
        }
    }

    private fun startCountdown() {
        handler.removeCallbacksAndMessages(null)
        val ticker = object : Runnable {
            override fun run() {
                updateCountdown()
                handler.postDelayed(this, 1000)
            }
        }
        ticker.run()
    }

    private fun updateCountdown() {
        val pt = prayerTimes ?: return

        val cal = Calendar.getInstance()
        val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60.0 +
            cal.get(Calendar.MINUTE) +
            cal.get(Calendar.SECOND) / 60.0

        val isFasting = nowMinutes >= pt.fajr && nowMinutes < pt.maghrib

        val targetMinutes: Double
        val label: String
        val target: String

        if (isFasting) {
            targetMinutes = pt.maghrib
            label = "Time to Iftar"
            target = "Iftar"
        } else if (nowMinutes < pt.fajr) {
            targetMinutes = pt.fajr
            label = "Time to Suhoor"
            target = "Suhoor"
        } else {
            // After maghrib, count to next day fajr (approximate)
            targetMinutes = pt.fajr + 24 * 60
            label = "Time to Suhoor"
            target = "Suhoor"
        }

        var diffSeconds = ((targetMinutes - nowMinutes) * 60).toLong()
        if (diffSeconds < 0) diffSeconds = 0

        val h = (diffSeconds / 3600).toInt()
        val m = ((diffSeconds % 3600) / 60).toInt()
        val s = (diffSeconds % 60).toInt()

        hoursText.text = String.format("%02d", h)
        minutesText.text = String.format("%02d", m)
        secondsText.text = String.format("%02d", s)
        countdownLabel.text = label
        targetName.text = target

        // Update highlights
        pt.isMaghribHighlighted = isFasting
        pt.isFajrHighlighted = !isFasting
        displayPrayerTimes()
    }

    private fun formatTime(minutes: Double): String {
        val totalMin = ((minutes % 1440) + 1440) % 1440
        val h = (totalMin / 60).toInt()
        val m = (totalMin % 60).toInt()
        val ampm = if (h < 12) "AM" else "PM"
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return String.format("%02d:%02d %s", h12, m, ampm)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}

data class PrayerTimesData(
    val fajr: Double,       // minutes since midnight
    val sunrise: Double,
    val dhuhr: Double,
    val asr: Double,
    val maghrib: Double,
    val isha: Double,
    var isFajrHighlighted: Boolean = false,
    var isMaghribHighlighted: Boolean = false
)

object PrayerTimeCalculator {
    // MWL: Fajr 18°, Isha 17°
    private const val FAJR_ANGLE = 18.0
    private const val ISHA_ANGLE = 17.0

    fun calculate(year: Int, month: Int, day: Int, lat: Double, lng: Double): PrayerTimesData {
        val jd = julianDate(year, month, day)
        val decl = sunDeclination(jd)
        val eqt = equationOfTime(jd)

        val tz = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000.0
        val transit = 12.0 + (-eqt / 60.0) - (lng / 15.0) + tz
        val transitMin = transit * 60.0

        val fajrHA = hourAngle(lat, decl, FAJR_ANGLE)
        val sunriseHA = hourAngle(lat, decl, 0.833)
        val asrHA = asrHourAngle(lat, decl, 1) // Shafi'i
        val ishaHA = hourAngle(lat, decl, ISHA_ANGLE)

        val fajr = transitMin - fajrHA * 4.0       // HA in degrees * 4 min/degree
        val sunrise = transitMin - sunriseHA * 4.0
        val dhuhr = transitMin
        val asr = transitMin + asrHA * 4.0
        val maghrib = transitMin + sunriseHA * 4.0  // Sunset = symmetric to sunrise
        val isha = transitMin + ishaHA * 4.0

        return PrayerTimesData(
            fajr = fajr,
            sunrise = sunrise,
            dhuhr = dhuhr,
            asr = asr,
            maghrib = maghrib,
            isha = isha
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunDeclination(jd: Double): Double {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(rad(g)) + 0.020 * sin(rad(2 * g)))
        val e = 23.439 - 0.00000036 * d
        return deg(asin(sin(rad(e)) * sin(rad(l))))
    }

    private fun equationOfTime(jd: Double): Double {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(rad(g)) + 0.020 * sin(rad(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = deg(atan2(cos(rad(e)) * sin(rad(l)), cos(rad(l)))) / 15.0
        val raFixed = ra - floor(ra / 24.0) * 24.0
        return (q / 15.0 - fixHour(raFixed)) * 60.0
    }

    private fun hourAngle(lat: Double, decl: Double, angle: Double): Double {
        val cosHA = (-sin(rad(angle)) - sin(rad(lat)) * sin(rad(decl))) /
            (cos(rad(lat)) * cos(rad(decl)))
        return deg(acos(cosHA.coerceIn(-1.0, 1.0)))
    }

    private fun asrHourAngle(lat: Double, decl: Double, factor: Int): Double {
        val acot = atan(1.0 / (factor + tan(abs(rad(lat) - rad(decl)))))
        val cosHA = (sin(acot) - sin(rad(lat)) * sin(rad(decl))) /
            (cos(rad(lat)) * cos(rad(decl)))
        return deg(acos(cosHA.coerceIn(-1.0, 1.0)))
    }

    private fun rad(d: Double) = d * Math.PI / 180.0
    private fun deg(r: Double) = r * 180.0 / Math.PI
    private fun fixAngle(a: Double): Double { val r = a - 360.0 * floor(a / 360.0); return if (r < 0) r + 360.0 else r }
    private fun fixHour(h: Double): Double { val r = h - 24.0 * floor(h / 24.0); return if (r < 0) r + 24.0 else r }
}
