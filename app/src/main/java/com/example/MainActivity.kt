package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.app.KeyguardManager
import android.content.Context
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import android.provider.Settings
import com.example.service.AlarmAccessibilityService
import com.example.ui.screens.isAccessibilityServiceEnabled
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.AlarmService
import com.example.ui.components.LiquidGlassNavBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure activity to turn screen on and show over lock screen when alarms trigger
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val appSettings = remember { com.example.data.AppSettings(applicationContext) }
            val settingsData by appSettings.settingsState.collectAsStateWithLifecycle()

            MyApplicationTheme(themeMode = settingsData.themeMode) {
                val ringingAlarmState by AlarmService.activeRingingAlarm.collectAsStateWithLifecycle()
                val prefs = remember { getSharedPreferences("app_setup_prefs", Context.MODE_PRIVATE) }
                var hasCompletedSetup by remember { mutableStateOf(prefs.getBoolean("setup_completed", false)) }

                val isOverlayGranted = Settings.canDrawOverlays(this)
                val isAccessibilityGranted = isAccessibilityServiceEnabled(this, AlarmAccessibilityService::class.java)
                val needsSetup = !hasCompletedSetup || !isOverlayGranted || !isAccessibilityGranted

                val ringingAlarm = ringingAlarmState

                // Lock back button during ringing alarm so user cannot exit without solving pattern
                BackHandler(enabled = ringingAlarm != null) {
                    // Do nothing - prevent exiting ringing alarm screen
                }

                if (ringingAlarm != null) {
                    RingingAlarmScreen(
                        alarm = ringingAlarm,
                        onSnooze = {
                            val intent = Intent(this@MainActivity, AlarmService::class.java).apply {
                                action = AlarmService.ACTION_SNOOZE
                            }
                            startService(intent)
                        },
                        onDismiss = {
                            val intent = Intent(this@MainActivity, AlarmService::class.java).apply {
                                action = AlarmService.ACTION_DISMISS
                            }
                            startService(intent)
                        }
                    )
                } else if (needsSetup) {
                    OnboardingSetupScreen(
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onCompleteSetup = {
                            prefs.edit().putBoolean("setup_completed", true).apply()
                            hasCompletedSetup = true
                        }
                    )
                } else {
                    // Standard App Flow with liquid glass navigation
                    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
                    val worldClocks by viewModel.worldClocks.collectAsStateWithLifecycle()
                    val timerPresets by viewModel.timerPresets.collectAsStateWithLifecycle()

                    val tabs = remember { listOf("ALARM", "WORLD", "TIMER", "STOPWATCH") }
                    val pagerState = rememberPagerState(
                        initialPage = 0,
                        pageCount = { tabs.size }
                    )
                    val coroutineScope = rememberCoroutineScope()

                    Scaffold(
                        bottomBar = {
                            LiquidGlassNavBar(
                                currentPage = pagerState.currentPage,
                                pageOffsetFraction = pagerState.currentPageOffsetFraction,
                                onTabSelected = { index ->
                                    if (pagerState.targetPage != index) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                beyondViewportPageCount = 1,
                                key = { it },
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                when (page) {
                                    0 -> AlarmScreen(
                                        alarms = alarms,
                                        onAddAlarm = { viewModel.addAlarm(it) },
                                        onUpdateAlarm = { viewModel.updateAlarm(it) },
                                        onToggleAlarm = { viewModel.toggleAlarm(it) },
                                        onDeleteAlarm = { viewModel.deleteAlarm(it) }
                                    )
                                    1 -> WorldClockScreen(
                                        worldClocks = worldClocks,
                                        onAddWorldClock = { viewModel.addWorldClock(it) },
                                        onDeleteWorldClock = { viewModel.deleteWorldClock(it) }
                                    )
                                    2 -> TimerScreen(
                                        presets = timerPresets,
                                        onAddPreset = { viewModel.addTimerPreset(it) },
                                        onDeletePreset = { viewModel.deleteTimerPreset(it) }
                                    )
                                    3 -> StopwatchScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
