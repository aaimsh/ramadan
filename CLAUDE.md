# Iftar Tracker - Android App

## Project Overview
An Android app that tracks the time remaining until Iftar (breaking of fast) during Ramadan.
The app calculates accurate prayer times based on the user's location and displays a
real-time countdown to the next Iftar.

## Tech Stack
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture principles
- **Build System**: Gradle with Kotlin DSL
- **DI**: Hilt (Dagger)
- **Async**: Kotlin Coroutines + Flow

## Project Structure
```
app/src/main/java/com/ramadan/iftartracker/
├── data/
│   ├── location/        # Location provider implementations
│   └── prayer/          # Prayer time calculation engine
├── di/                  # Hilt dependency injection modules
├── domain/
│   ├── model/           # Domain models (PrayerTimes, Location)
│   └── usecase/         # Use cases (GetPrayerTimes, GetCountdown)
├── ui/
│   ├── components/      # Reusable Compose components
│   ├── screen/          # Screen composables
│   ├── theme/           # Material 3 theme definitions
│   └── viewmodel/       # ViewModels
├── service/             # Foreground service for countdown notifications
├── receiver/            # Broadcast receivers for alarms
└── util/                # Extension functions and helpers
```

## Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run all checks (lint + tests)
./gradlew check

# Clean build
./gradlew clean assembleDebug

# Run specific test class
./gradlew testDebugUnitTest --tests "com.ramadan.iftartracker.ClassName"
```

## Code Conventions

### Kotlin Style
- Follow official Kotlin coding conventions
- Use `data class` for models, `sealed class`/`sealed interface` for state
- Prefer expression functions for single-expression methods
- Use trailing commas in multi-line parameter lists
- Max line length: 120 characters

### Compose
- Composable functions are PascalCase
- State hoisting: UI components receive state via parameters and emit events via lambdas
- Use `remember` and `derivedStateOf` appropriately
- Preview functions suffixed with `Preview`

### Architecture
- ViewModels expose `StateFlow` for UI state
- Use cases are single-responsibility classes with `operator fun invoke()`
- Repository pattern for data access
- No Android framework dependencies in domain layer

### Testing
- Unit tests for ViewModels, use cases, and prayer time calculations
- Test files mirror source structure under `src/test/`
- Use JUnit 5 with AssertJ assertions
- Use Turbine for Flow testing

## Prayer Time Calculation
The app uses astronomical calculations to determine prayer times:
- **Fajr**: Sun angle -18° (Muslim World League) or configurable
- **Sunrise**: Sun angle -0.833°
- **Dhuhr**: Solar noon
- **Asr**: Shadow length = object height + shadow at noon (Shafi'i) or 2x (Hanafi)
- **Maghrib/Iftar**: Sunset (sun angle -0.833°)
- **Isha**: Sun angle -17° (Muslim World League) or configurable

Calculation methods supported:
- Muslim World League (MWL)
- Islamic Society of North America (ISNA)
- Egyptian General Authority of Survey
- Umm Al-Qura University, Makkah
- University of Islamic Sciences, Karachi

## Key Features
1. Real-time countdown to next Iftar
2. GPS-based automatic location detection
3. Manual city selection fallback
4. All five daily prayer times display
5. Notification alerts before Iftar and Suhoor
6. Hijri date display
7. Multiple calculation method support
8. Dark/light theme with Ramadan-themed colors

## Dependencies (key libraries)
- `androidx.compose.material3` - Material Design 3
- `androidx.hilt:hilt-navigation-compose` - Hilt + Compose integration
- `com.google.android.gms:play-services-location` - Fused location provider
- `org.jetbrains.kotlinx:kotlinx-coroutines-android` - Coroutines
- `androidx.datastore:datastore-preferences` - Preferences storage

## Notes for AI Assistants
- Always run `./gradlew check` after making changes
- Prayer time math is sensitive — test edge cases at high latitudes
- Location permissions require runtime checks on Android 6+
- Countdown timer should survive configuration changes via ViewModel
- Use `System.currentTimeMillis()` sparingly; prefer `Clock` abstraction for testability
