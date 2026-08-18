package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.aistudio.alarm.ACTION_TRIGGER_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.aistudio.alarm.ACTION_SNOOZE_ALARM"
        const val ACTION_DISMISS_ALARM = "com.aistudio.alarm.ACTION_DISMISS_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        Log.d("AlarmReceiver", "Received intent action: ${intent.action}, alarmId: $alarmId")

        when (intent.action) {
            ACTION_TRIGGER_ALARM -> {
                if (alarmId != -1L) {
                    val serviceIntent = Intent(context, AlarmService::class.java).apply {
                        action = AlarmService.ACTION_START_ALARM
                        putExtra(EXTRA_ALARM_ID, alarmId)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
            ACTION_SNOOZE_ALARM -> {
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_SNOOZE
                    putExtra(EXTRA_ALARM_ID, alarmId)
                }
                context.startService(serviceIntent)
            }
            ACTION_DISMISS_ALARM -> {
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_DISMISS
                    putExtra(EXTRA_ALARM_ID, alarmId)
                }
                context.startService(serviceIntent)
            }
        }
    }
}
