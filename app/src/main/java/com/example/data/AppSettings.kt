package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettingsData(
    val themeMode: String = "DARK", // DARK, LIGHT, SYSTEM
    val timeFormat: String = "SYSTEM", // SYSTEM, 12H, 24H
    val showSecondsOnMainClock: Boolean = true,
    val showNextAlarmOnMainClock: Boolean = true,
    val firstDayOfWeek: String = "SYSTEM", // SYSTEM, SUNDAY, MONDAY
    val defaultSnoozeMinutes: Int = 5,
    val defaultAllowSnooze: Boolean = true,
    val defaultSnoozeChallenge: String = "NONE",
    val defaultDismissChallenge: String = "NONE",
    val defaultSecurityCode: String = "",
    val defaultSoundUri: String = "preset_zenith",
    val defaultSoundTitle: String = "Zenith Chimes",
    val defaultFadeVolume: Boolean = true,
    val defaultFadeDurationSeconds: Int = 30,
    val defaultVibrate: Boolean = true,
    val defaultMaxSnoozeCount: Int = 3,
    val alarmVolume: Int = 80,
    val autoSilenceMinutes: Int = 10,
    val volumeButtonAction: String = "SNOOZE", // SNOOZE, DISMISS, DO_NOTHING
    val keepScreenOnTimer: Boolean = true,
    val keepScreenOnStopwatch: Boolean = true,
    val timerVibrate: Boolean = true
)

class AppSettings(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<AppSettingsData> = _settingsState.asStateFlow()

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        _settingsState.value = loadSettings()
    }

    fun loadSettings(): AppSettingsData {
        return AppSettingsData(
            themeMode = prefs.getString("theme_mode", "DARK") ?: "DARK",
            timeFormat = prefs.getString("time_format", "SYSTEM") ?: "SYSTEM",
            showSecondsOnMainClock = prefs.getBoolean("show_seconds_main", true),
            showNextAlarmOnMainClock = prefs.getBoolean("show_next_alarm_main", true),
            firstDayOfWeek = prefs.getString("first_day_of_week", "SYSTEM") ?: "SYSTEM",
            defaultSnoozeMinutes = prefs.getInt("default_snooze", 5),
            defaultAllowSnooze = prefs.getBoolean("default_allow_snooze", true),
            defaultSnoozeChallenge = prefs.getString("default_snooze_challenge", "NONE") ?: "NONE",
            defaultDismissChallenge = prefs.getString("default_dismiss_challenge", "NONE") ?: "NONE",
            defaultSecurityCode = prefs.getString("default_security_code", "") ?: "",
            defaultSoundUri = prefs.getString("default_sound_uri", "preset_zenith") ?: "preset_zenith",
            defaultSoundTitle = prefs.getString("default_sound_title", "Zenith Chimes") ?: "Zenith Chimes",
            defaultFadeVolume = prefs.getBoolean("default_fade_vol", true),
            defaultFadeDurationSeconds = prefs.getInt("default_fade_duration", 30),
            defaultVibrate = prefs.getBoolean("default_vibrate", true),
            defaultMaxSnoozeCount = prefs.getInt("default_max_snooze", 3),
            alarmVolume = prefs.getInt("alarm_volume", 80),
            autoSilenceMinutes = prefs.getInt("auto_silence_minutes", 10),
            volumeButtonAction = prefs.getString("volume_button_action", "SNOOZE") ?: "SNOOZE",
            keepScreenOnTimer = prefs.getBoolean("keep_screen_on_timer", true),
            keepScreenOnStopwatch = prefs.getBoolean("keep_screen_on_stopwatch", true),
            timerVibrate = prefs.getBoolean("timer_vibrate", true)
        )
    }

    var themeMode: String
        get() = prefs.getString("theme_mode", "DARK") ?: "DARK"
        set(value) = prefs.edit().putString("theme_mode", value).apply()

    var timeFormat: String
        get() = prefs.getString("time_format", "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString("time_format", value).apply()

    var showSecondsOnMainClock: Boolean
        get() = prefs.getBoolean("show_seconds_main", true)
        set(value) = prefs.edit().putBoolean("show_seconds_main", value).apply()

    var showNextAlarmOnMainClock: Boolean
        get() = prefs.getBoolean("show_next_alarm_main", true)
        set(value) = prefs.edit().putBoolean("show_next_alarm_main", value).apply()

    var firstDayOfWeek: String
        get() = prefs.getString("first_day_of_week", "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString("first_day_of_week", value).apply()

    var defaultSnoozeMinutes: Int
        get() = prefs.getInt("default_snooze", 5)
        set(value) = prefs.edit().putInt("default_snooze", value).apply()

    var defaultAllowSnooze: Boolean
        get() = prefs.getBoolean("default_allow_snooze", true)
        set(value) = prefs.edit().putBoolean("default_allow_snooze", value).apply()

    var defaultSnoozeChallenge: String
        get() = prefs.getString("default_snooze_challenge", "NONE") ?: "NONE"
        set(value) = prefs.edit().putString("default_snooze_challenge", value).apply()

    var defaultDismissChallenge: String
        get() = prefs.getString("default_dismiss_challenge", "NONE") ?: "NONE"
        set(value) = prefs.edit().putString("default_dismiss_challenge", value).apply()

    var defaultSecurityCode: String
        get() = prefs.getString("default_security_code", "") ?: ""
        set(value) = prefs.edit().putString("default_security_code", value).apply()

    var defaultSoundUri: String
        get() = prefs.getString("default_sound_uri", "preset_zenith") ?: "preset_zenith"
        set(value) = prefs.edit().putString("default_sound_uri", value).apply()

    var defaultSoundTitle: String
        get() = prefs.getString("default_sound_title", "Zenith Chimes") ?: "Zenith Chimes"
        set(value) = prefs.edit().putString("default_sound_title", value).apply()

    var defaultFadeVolume: Boolean
        get() = prefs.getBoolean("default_fade_vol", true)
        set(value) = prefs.edit().putBoolean("default_fade_vol", value).apply()

    var defaultFadeDurationSeconds: Int
        get() = prefs.getInt("default_fade_duration", 30)
        set(value) = prefs.edit().putInt("default_fade_duration", value).apply()

    var defaultVibrate: Boolean
        get() = prefs.getBoolean("default_vibrate", true)
        set(value) = prefs.edit().putBoolean("default_vibrate", value).apply()

    var defaultMaxSnoozeCount: Int
        get() = prefs.getInt("default_max_snooze", 3)
        set(value) = prefs.edit().putInt("default_max_snooze", value).apply()

    var alarmVolume: Int
        get() = prefs.getInt("alarm_volume", 80)
        set(value) = prefs.edit().putInt("alarm_volume", value).apply()

    var autoSilenceMinutes: Int
        get() = prefs.getInt("auto_silence_minutes", 10)
        set(value) = prefs.edit().putInt("auto_silence_minutes", value).apply()

    var volumeButtonAction: String
        get() = prefs.getString("volume_button_action", "SNOOZE") ?: "SNOOZE"
        set(value) = prefs.edit().putString("volume_button_action", value).apply()

    var keepScreenOnTimer: Boolean
        get() = prefs.getBoolean("keep_screen_on_timer", true)
        set(value) = prefs.edit().putBoolean("keep_screen_on_timer", value).apply()

    var keepScreenOnStopwatch: Boolean
        get() = prefs.getBoolean("keep_screen_on_stopwatch", true)
        set(value) = prefs.edit().putBoolean("keep_screen_on_stopwatch", value).apply()

    var timerVibrate: Boolean
        get() = prefs.getBoolean("timer_vibrate", true)
        set(value) = prefs.edit().putBoolean("timer_vibrate", value).apply()
}

