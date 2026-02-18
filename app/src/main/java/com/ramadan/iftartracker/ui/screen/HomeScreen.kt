package com.ramadan.iftartracker.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramadan.iftartracker.domain.model.PrayerTimes
import com.ramadan.iftartracker.ui.components.CountdownDisplay
import com.ramadan.iftartracker.ui.components.PrayerTimeCard
import com.ramadan.iftartracker.ui.viewmodel.MainViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val countdown by viewModel.countdown.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            viewModel.onPermissionGranted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Iftar Tracker",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            when (val state = uiState) {
                is MainViewModel.UiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading prayer times...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                is MainViewModel.UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                )
                            )
                        }) {
                            Text("Grant Location Permission")
                        }
                    }
                }

                is MainViewModel.UiState.Success -> {
                    SuccessContent(
                        prayerTimes = state.prayerTimes,
                        countdownHours = countdown?.hours ?: 0,
                        countdownMinutes = countdown?.minutes ?: 0,
                        countdownSeconds = countdown?.seconds ?: 0,
                        isFasting = countdown?.isFasting ?: false,
                        countdown = countdown,
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    prayerTimes: PrayerTimes,
    countdownHours: Int,
    countdownMinutes: Int,
    countdownSeconds: Int,
    isFasting: Boolean,
    countdown: com.ramadan.iftartracker.domain.model.CountdownState?,
) {
    val currentTime = LocalTime.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Date display
        Text(
            text = prayerTimes.date.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        // Ramadan greeting
        Text(
            text = "Ramadan Mubarak",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Countdown
        if (countdown != null) {
            CountdownDisplay(countdownState = countdown)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prayer times list
        Text(
            text = "Prayer Times",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        val isBeforeMaghrib = currentTime.isBefore(prayerTimes.maghrib)
        val isBeforeFajr = currentTime.isBefore(prayerTimes.fajr)

        PrayerTimeCard(
            name = "Fajr (Suhoor)",
            time = prayerTimes.fajr,
            isHighlighted = !isFasting && isBeforeFajr,
        )
        PrayerTimeCard(
            name = "Sunrise",
            time = prayerTimes.sunrise,
        )
        PrayerTimeCard(
            name = "Dhuhr",
            time = prayerTimes.dhuhr,
        )
        PrayerTimeCard(
            name = "Asr",
            time = prayerTimes.asr,
        )
        PrayerTimeCard(
            name = "Maghrib (Iftar)",
            time = prayerTimes.maghrib,
            isHighlighted = isFasting,
        )
        PrayerTimeCard(
            name = "Isha",
            time = prayerTimes.isha,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
