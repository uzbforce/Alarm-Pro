package com.example.data

import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val worldClockDao: WorldClockDao,
    private val timerPresetDao: TimerPresetDao
) {
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()
    val allWorldClocks: Flow<List<WorldClockEntity>> = worldClockDao.getAllWorldClocks()
    val allTimerPresets: Flow<List<TimerPresetEntity>> = timerPresetDao.getAllPresets()

    suspend fun getEnabledAlarms() = alarmDao.getEnabledAlarms()
    suspend fun getAlarmById(id: Long) = alarmDao.getAlarmById(id)
    suspend fun insertAlarm(alarm: AlarmEntity) = alarmDao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: AlarmEntity) = alarmDao.deleteAlarm(alarm)
    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)

    suspend fun insertWorldClock(clock: WorldClockEntity) = worldClockDao.insertWorldClock(clock)
    suspend fun deleteWorldClock(clock: WorldClockEntity) = worldClockDao.deleteWorldClock(clock)

    suspend fun insertTimerPreset(preset: TimerPresetEntity) = timerPresetDao.insertPreset(preset)
    suspend fun deleteTimerPreset(preset: TimerPresetEntity) = timerPresetDao.deletePreset(preset)
}
