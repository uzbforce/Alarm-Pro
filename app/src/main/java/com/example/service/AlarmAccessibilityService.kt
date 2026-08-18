package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class AlarmAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Event processing not needed for key filtering
    }

    override fun onInterrupt() {
        // Interruption handler
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val isRinging = AlarmService.activeRingingAlarm.value != null
        if (isRinging) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_MUTE,
                KeyEvent.KEYCODE_POWER -> {
                    Log.d("AlarmAccessibility", "Intercepted key during active alarm: ${event.keyCode}")
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        val settings = com.example.data.AppSettings(applicationContext)
                        when (settings.volumeButtonAction) {
                            "SNOOZE" -> {
                                val intent = android.content.Intent(applicationContext, AlarmService::class.java).apply {
                                    action = AlarmService.ACTION_SNOOZE
                                }
                                startService(intent)
                            }
                            "DISMISS" -> {
                                val intent = android.content.Intent(applicationContext, AlarmService::class.java).apply {
                                    action = AlarmService.ACTION_DISMISS
                                }
                                startService(intent)
                            }
                            "DO_NOTHING" -> {
                                // Consume event to lock volume
                            }
                        }
                    }
                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }
}
