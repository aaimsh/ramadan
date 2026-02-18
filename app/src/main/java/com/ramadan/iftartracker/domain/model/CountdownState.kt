package com.ramadan.iftartracker.domain.model

data class CountdownState(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val targetName: String,
    val isFasting: Boolean,
) {
    val totalSeconds: Long
        get() = hours * 3600L + minutes * 60L + seconds

    val displayTime: String
        get() = "%02d:%02d:%02d".format(hours, minutes, seconds)
}
