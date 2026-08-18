package com.example.data

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val enabled: Boolean = true,
    val repeatDaysMask: Int = 0, // Bitmask: Bit 0=Sun, 1=Mon, ..., 6=Sat
    val soundTitle: String = "Zenith Chimes",
    val soundUri: String = "preset_zenith",
    val volume: Float = 0.8f,
    val fadeVolume: Boolean = true,
    val fadeDurationSeconds: Int = 30,
    val vibrate: Boolean = true,
    val allowSnooze: Boolean = true,
    val snoozeDurationMinutes: Int = 5,
    val maxSnoozeCount: Int = 3,
    val currentSnoozeCount: Int = 0,
    val dismissChallenge: String = "NONE", // NONE, PASSWORD, PATTERN, MATH, SHAKE
    val snoozeChallenge: String = "NONE",   // NONE, MATH, SHAKE
    val securityCode: String = "",         // PIN / Password / Pattern string (e.g., "0-1-2-5-8")
    val volumeButtonAction: String = "SNOOZE", // SNOOZE, DISMISS, NONE
    val screenQuote: String = "Rise and shine! Success awaits today."
) {
    fun isRepeating(): Boolean = repeatDaysMask > 0

    fun isDaySelected(dayIndex: Int): Boolean { // 0=Sun, 1=Mon...
        return (repeatDaysMask and (1 shl dayIndex)) != 0
    }

    fun toggleDay(dayIndex: Int): AlarmEntity {
        val newMask = repeatDaysMask xor (1 shl dayIndex)
        return copy(repeatDaysMask = newMask)
    }

    fun formattedTime(timeFormat: String = "SYSTEM", context: Context? = null): String {
        val is24 = when (timeFormat) {
            "24H" -> true
            "12H" -> false
            else -> context?.let { android.text.format.DateFormat.is24HourFormat(it) } ?: false
        }
        return if (is24) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val amPm = if (hour >= 12) "PM" else "AM"
            val m = String.format("%02d", minute)
            "$h:$m $amPm"
        }
    }

    fun formattedRepeatDays(): String {
        if (repeatDaysMask == 0) return "Once"
        if (repeatDaysMask == 127) return "Everyday"
        if (repeatDaysMask == 62) return "Weekdays"
        if (repeatDaysMask == 65) return "Weekends"

        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val selected = mutableListOf<String>()
        for (i in 0..6) {
            if (isDaySelected(i)) selected.add(days[i])
        }
        return selected.joinToString(", ")
    }
}
