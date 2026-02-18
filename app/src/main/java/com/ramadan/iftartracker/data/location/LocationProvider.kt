package com.ramadan.iftartracker.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.ramadan.iftartracker.domain.model.Coordinates
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCoordinates(): Coordinates? {
        if (!hasLocationPermission()) return null

        return try {
            val location = getLastKnownOrCurrent()
            location?.let { Coordinates(it.latitude, it.longitude) }
        } catch (e: SecurityException) {
            null
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressWarnings("MissingPermission")
    private suspend fun getLastKnownOrCurrent(): Location? {
        if (!hasLocationPermission()) return null
        val last = fusedClient.lastLocation.await()
        if (last != null) return last

        val cancellation = CancellationTokenSource()
        return fusedClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellation.token,
        ).await()
    }
}
