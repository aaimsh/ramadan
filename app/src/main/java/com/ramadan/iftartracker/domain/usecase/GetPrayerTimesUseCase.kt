package com.ramadan.iftartracker.domain.usecase

import com.ramadan.iftartracker.data.location.LocationProvider
import com.ramadan.iftartracker.data.prayer.PrayerTimeCalculator
import com.ramadan.iftartracker.data.preferences.UserPreferences
import com.ramadan.iftartracker.domain.model.CalculationMethod
import com.ramadan.iftartracker.domain.model.Coordinates
import com.ramadan.iftartracker.domain.model.PrayerTimes
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class GetPrayerTimesUseCase @Inject constructor(
    private val calculator: PrayerTimeCalculator,
    private val locationProvider: LocationProvider,
    private val preferences: UserPreferences,
) {
    suspend operator fun invoke(
        date: LocalDate = LocalDate.now(),
        coordinatesOverride: Coordinates? = null,
    ): Result<PrayerTimes> {
        val coordinates = coordinatesOverride ?: locationProvider.getCoordinates()
            ?: return Result.failure(IllegalStateException("Unable to determine location"))

        val method = preferences.calculationMethod.first()

        return Result.success(
            calculator.calculate(
                date = date,
                coordinates = coordinates,
                method = method,
            )
        )
    }
}
