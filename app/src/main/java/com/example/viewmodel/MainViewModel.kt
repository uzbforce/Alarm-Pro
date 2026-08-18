package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.service.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository

    val alarms: StateFlow<List<AlarmEntity>>
    val worldClocks: StateFlow<List<WorldClockEntity>>
    val timerPresets: StateFlow<List<TimerPresetEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AlarmRepository(
            alarmDao = database.alarmDao(),
            worldClockDao = database.worldClockDao(),
            timerPresetDao = database.timerPresetDao()
        )

        alarms = repository.allAlarms.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        worldClocks = repository.allWorldClocks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        timerPresets = repository.allTimerPresets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate initial world clocks if empty
        viewModelScope.launch {
            repository.allWorldClocks.collect { list ->
                if (list.isEmpty()) {
                    repository.insertWorldClock(WorldClockEntity(cityName = "London", countryName = "United Kingdom", timeZoneId = "Europe/London"))
                    repository.insertWorldClock(WorldClockEntity(cityName = "New York", countryName = "United States", timeZoneId = "America/New_York"))
                    repository.insertWorldClock(WorldClockEntity(cityName = "Tokyo", countryName = "Japan", timeZoneId = "Asia/Tokyo"))
                }
            }
        }
    }

    fun addAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = repository.insertAlarm(alarm)
            val savedAlarm = alarm.copy(id = id)
            if (savedAlarm.enabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), savedAlarm)
            }
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
            if (alarm.enabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), alarm)
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
            }
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        val updated = alarm.copy(enabled = !alarm.enabled)
        updateAlarm(updated)
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
        }
    }

    fun addWorldClock(clock: WorldClockEntity) {
        viewModelScope.launch {
            repository.insertWorldClock(clock)
        }
    }

    fun deleteWorldClock(clock: WorldClockEntity) {
        viewModelScope.launch {
            repository.deleteWorldClock(clock)
        }
    }

    fun addTimerPreset(preset: TimerPresetEntity) {
        viewModelScope.launch {
            repository.insertTimerPreset(preset)
        }
    }

    fun deleteTimerPreset(preset: TimerPresetEntity) {
        viewModelScope.launch {
            repository.deleteTimerPreset(preset)
        }
    }
}
