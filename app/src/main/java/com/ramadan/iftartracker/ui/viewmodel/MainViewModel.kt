package com.ramadan.iftartracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramadan.iftartracker.data.preferences.UserPreferences
import com.ramadan.iftartracker.domain.model.CalculationMethod
import com.ramadan.iftartracker.domain.model.CountdownState
import com.ramadan.iftartracker.domain.model.PrayerTimes
import com.ramadan.iftartracker.domain.usecase.GetCountdownUseCase
import com.ramadan.iftartracker.domain.usecase.GetPrayerTimesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPrayerTimes: GetPrayerTimesUseCase,
    private val getCountdown: GetCountdownUseCase,
    private val preferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _countdown = MutableStateFlow<CountdownState?>(null)
    val countdown: StateFlow<CountdownState?> = _countdown.asStateFlow()

    val calculationMethod = preferences.calculationMethod
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalculationMethod.MWL)

    init {
        loadPrayerTimes()
    }

    fun loadPrayerTimes() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = getPrayerTimes()
            result.fold(
                onSuccess = { times ->
                    _uiState.value = UiState.Success(times)
                    startCountdownTicker(times)
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(
                        error.message ?: "Failed to load prayer times"
                    )
                },
            )
        }
    }

    fun onPermissionGranted() {
        loadPrayerTimes()
    }

    fun setCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch {
            preferences.setCalculationMethod(method)
            loadPrayerTimes()
        }
    }

    private fun startCountdownTicker(prayerTimes: PrayerTimes) {
        viewModelScope.launch {
            while (isActive) {
                _countdown.value = getCountdown(prayerTimes, LocalDateTime.now())
                delay(1000)
            }
        }
    }

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val prayerTimes: PrayerTimes) : UiState
        data class Error(val message: String) : UiState
    }
}
