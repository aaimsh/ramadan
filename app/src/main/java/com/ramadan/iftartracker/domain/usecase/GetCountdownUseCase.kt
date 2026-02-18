package com.ramadan.iftartracker.domain.usecase

import com.ramadan.iftartracker.domain.model.CountdownState
import com.ramadan.iftartracker.domain.model.PrayerTimes
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class GetCountdownUseCase @Inject constructor() {

    operator fun invoke(
        prayerTimes: PrayerTimes,
        now: LocalDateTime = LocalDateTime.now(),
    ): CountdownState {
        val currentTime = now.toLocalTime()

        // Determine what we're counting down to:
        // Between Fajr and Maghrib → counting down to Iftar (Maghrib)
        // Otherwise → counting down to Suhoor (next Fajr)
        val isFasting = currentTime.isAfter(prayerTimes.fajr) &&
            currentTime.isBefore(prayerTimes.maghrib)

        val targetTime: LocalTime
        val targetName: String

        if (isFasting) {
            targetTime = prayerTimes.maghrib
            targetName = "Iftar"
        } else {
            // If before Fajr today, count to today's Fajr
            // If after Maghrib, count to tomorrow's Fajr (approximate same time)
            targetTime = prayerTimes.fajr
            targetName = "Suhoor"
        }

        val duration = if (isFasting) {
            Duration.between(currentTime, targetTime)
        } else if (currentTime.isBefore(prayerTimes.fajr)) {
            Duration.between(currentTime, targetTime)
        } else {
            // After Maghrib — count to next day Fajr
            val remaining = Duration.between(currentTime, LocalTime.MAX)
            val morning = Duration.between(LocalTime.MIN, targetTime)
            remaining.plus(morning).plusSeconds(1)
        }

        val totalSec = duration.seconds.coerceAtLeast(0)
        val h = (totalSec / 3600).toInt()
        val m = ((totalSec % 3600) / 60).toInt()
        val s = (totalSec % 60).toInt()

        return CountdownState(
            hours = h,
            minutes = m,
            seconds = s,
            targetName = targetName,
            isFasting = isFasting,
        )
    }
}
