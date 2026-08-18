package com.example.service

import android.media.AudioManager
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AlarmEntity
import com.example.data.AppDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

class AlarmService : Service() {

    companion object {
        const val ACTION_START_ALARM = "com.aistudio.alarm.ACTION_START"
        const val ACTION_SNOOZE = "com.aistudio.alarm.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.aistudio.alarm.ACTION_DISMISS"

        const val CHANNEL_ID = "aura_alarm_ringing_channel"
        const val NOTIFICATION_ID = 9001

        private val _activeRingingAlarm = MutableStateFlow<AlarmEntity?>(null)
        val activeRingingAlarm: StateFlow<AlarmEntity?> = _activeRingingAlarm
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentAlarm: AlarmEntity? = null
    private var fadeVolumeJob: Job? = null
    private var volumeEnforcerJob: Job? = null
    private var autoSilenceJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val alarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L) ?: -1L

        when (action) {
            ACTION_START_ALARM -> {
                if (alarmId != -1L) {
                    startRinging(alarmId)
                }
            }
            ACTION_SNOOZE -> {
                handleSnooze(alarmId)
            }
            ACTION_DISMISS -> {
                handleDismiss(alarmId)
            }
        }
        return START_STICKY
    }

    private fun startRinging(alarmId: Long) {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val alarm = db.alarmDao().getAlarmById(alarmId) ?: return@launch
            currentAlarm = alarm
            _activeRingingAlarm.value = alarm

            acquireWakeLock()

            // Build Notification
            val fullScreenIntent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("RINGING_ALARM_ID", alarmId)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                applicationContext,
                NOTIFICATION_ID,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val snoozeIntent = Intent(applicationContext, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_SNOOZE_ALARM
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                1001,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(this@AlarmService, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(alarm.label.ifBlank { "Alarm Ringing!" })
                .setContentText("Time: ${alarm.formattedTime()}")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setOngoing(true)

            if (alarm.allowSnooze) {
                notificationBuilder.addAction(0, "Snooze", snoozePendingIntent)
            }

            val notification = notificationBuilder.build()
            startForeground(NOTIFICATION_ID, notification)

            // Start sound & volume logic
            val initialVol = if (alarm.fadeVolume) 0.1f else alarm.volume
            SoundLibrary.playSound(applicationContext, alarm.soundUri, initialVol, serviceScope)

            if (alarm.fadeVolume && alarm.fadeDurationSeconds > 0) {
                fadeVolumeJob?.cancel()
                fadeVolumeJob = serviceScope.launch {
                    val steps = 20
                    val delayStepMs = (alarm.fadeDurationSeconds * 1000L) / steps
                    val volIncrement = (alarm.volume - 0.1f) / steps
                    var currentVol = 0.1f

                    for (i in 1..steps) {
                        delay(delayStepMs)
                        currentVol += volIncrement
                        SoundLibrary.setVolume(currentVol)
                    }
                }
            }

            // Start vibration
            if (alarm.vibrate) {
                startVibration()
            }

            // Auto-silence rule from settings
            val settings = com.example.data.AppSettings(applicationContext)
            val autoSilenceMins = settings.autoSilenceMinutes
            if (autoSilenceMins > 0) {
                autoSilenceJob?.cancel()
                autoSilenceJob = serviceScope.launch {
                    delay(autoSilenceMins * 60 * 1000L)
                    if (alarm.allowSnooze) {
                        handleSnooze(alarmId)
                    } else {
                        handleDismiss(alarmId)
                    }
                }
            }

            // Enforce max audio stream volume continuously during ringing
            volumeEnforcerJob?.cancel()
            volumeEnforcerJob = serviceScope.launch(Dispatchers.IO) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                val maxAlarmVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 15
                val maxMusicVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
                while (isActive) {
                    try {
                        audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVol, 0)
                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusicVol, 0)
                    } catch (e: Exception) {
                        // ignore permission restrictions
                    }
                    delay(300L)
                }
            }
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 500, 500, 500, 500, 800)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                vibrator.vibrate(pattern, 0)
            }
        }
    }

    private fun stopVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.cancel()
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.cancel()
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun handleSnooze(alarmId: Long) {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val alarm = currentAlarm ?: db.alarmDao().getAlarmById(alarmId)
            if (alarm != null && alarm.allowSnooze) {
                val nextCount = alarm.currentSnoozeCount + 1
                if (alarm.maxSnoozeCount <= 0 || nextCount <= alarm.maxSnoozeCount) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MINUTE, alarm.snoozeDurationMinutes)

                    val snoozedAlarm = alarm.copy(
                        currentSnoozeCount = nextCount
                    )
                    db.alarmDao().updateAlarm(snoozedAlarm)

                    // Schedule snooze trigger
                    val triggerTime = cal.timeInMillis
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(applicationContext, AlarmReceiver::class.java).apply {
                        action = AlarmReceiver.ACTION_TRIGGER_ALARM
                        putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        applicationContext,
                        alarm.id.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    try {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            stopSelfRinging()
        }
    }

    private fun handleDismiss(alarmId: Long) {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val alarm = currentAlarm ?: db.alarmDao().getAlarmById(alarmId)
            if (alarm != null) {
                val resetAlarm = alarm.copy(
                    currentSnoozeCount = 0,
                    enabled = if (alarm.isRepeating()) true else false
                )
                db.alarmDao().updateAlarm(resetAlarm)

                if (resetAlarm.enabled) {
                    AlarmScheduler.scheduleAlarm(applicationContext, resetAlarm)
                } else {
                    AlarmScheduler.cancelAlarm(applicationContext, resetAlarm.id)
                }
            }
            stopSelfRinging()
        }
    }

    private fun stopSelfRinging() {
        autoSilenceJob?.cancel()
        fadeVolumeJob?.cancel()
        volumeEnforcerJob?.cancel()
        SoundLibrary.stopSound()
        stopVibration()
        releaseWakeLock()
        _activeRingingAlarm.value = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "AuraAlarm::WakeLock"
            )
        }
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aura Alarm Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for active alarm notifications"
                setSound(null, null)
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopSelfRinging()
        serviceScope.cancel()
        super.onDestroy()
    }
}
