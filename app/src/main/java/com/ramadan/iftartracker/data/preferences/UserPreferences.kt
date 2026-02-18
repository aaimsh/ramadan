package com.ramadan.iftartracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ramadan.iftartracker.domain.model.CalculationMethod
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val IFTAR_ALERT_MINUTES = intPreferencesKey("iftar_alert_minutes")
        val SUHOOR_ALERT_MINUTES = intPreferencesKey("suhoor_alert_minutes")
    }

    val calculationMethod: Flow<CalculationMethod> = context.dataStore.data.map { prefs ->
        val name = prefs[Keys.CALCULATION_METHOD] ?: CalculationMethod.MWL.name
        CalculationMethod.valueOf(name)
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val iftarAlertMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.IFTAR_ALERT_MINUTES] ?: 15
    }

    val suhoorAlertMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.SUHOOR_ALERT_MINUTES] ?: 30
    }

    suspend fun setCalculationMethod(method: CalculationMethod) {
        context.dataStore.edit { it[Keys.CALCULATION_METHOD] = method.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setIftarAlertMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.IFTAR_ALERT_MINUTES] = minutes }
    }

    suspend fun setSuhoorAlertMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.SUHOOR_ALERT_MINUTES] = minutes }
    }
}
