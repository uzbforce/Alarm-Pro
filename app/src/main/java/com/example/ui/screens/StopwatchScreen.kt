package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class LapRecord(
    val lapNumber: Int,
    val splitTimeMs: Long,
    val totalTimeMs: Long
)

@Composable
fun StopwatchScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appSettings = remember { com.example.data.AppSettings(context) }
    val appSettingsData by appSettings.settingsState.collectAsState()

    var isRunning by remember { mutableStateOf(false) }
    var elapsedTimeMs by remember { mutableLongStateOf(0L) }
    var laps by remember { mutableStateOf<List<LapRecord>>(emptyList()) }

    // Keep screen on if active and setting enabled
    val shouldKeepScreenOn = appSettingsData.keepScreenOnStopwatch && isRunning
    DisposableEffect(shouldKeepScreenOn) {
        val activity = context as? android.app.Activity
        if (shouldKeepScreenOn) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Live Ticker
    LaunchedEffect(isRunning) {
        var lastTime = System.currentTimeMillis()
        while (isRunning) {
            delay(10)
            val now = System.currentTimeMillis()
            val delta = now - lastTime
            lastTime = now
            elapsedTimeMs += delta
        }
    }

    val minutes = (elapsedTimeMs / 60000)
    val seconds = (elapsedTimeMs % 60000) / 1000
    val millis = (elapsedTimeMs % 1000) / 10

    // Fastest and slowest laps calculations
    val fastestLapSplit = remember(laps) { laps.minOfOrNull { it.splitTimeMs } }
    val slowestLapSplit = remember(laps) { if (laps.size > 1) laps.maxOfOrNull { it.splitTimeMs } else null }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Stopwatch",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isRunning) "Precision Split Tracker" else "Ready",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Display Counter
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(".%02d", millis),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }

                if (laps.isNotEmpty()) {
                    val lastLapTotal = laps.first().totalTimeMs
                    val currentSplit = elapsedTimeMs - lastLapTotal
                    val splitMins = (currentSplit / 60000)
                    val splitSecs = (currentSplit % 60000) / 1000
                    val splitMillis = (currentSplit % 1000) / 10

                    Text(
                        text = "Current Lap: ${String.format("%02d:%02d.%02d", splitMins, splitSecs, splitMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Control Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap / Reset Button
            OutlinedIconButton(
                onClick = {
                    if (isRunning) {
                        // Flag/Lap Record
                        val previousTotal = if (laps.isEmpty()) 0L else laps.first().totalTimeMs
                        val split = elapsedTimeMs - previousTotal
                        val newRecord = LapRecord(
                            lapNumber = laps.size + 1,
                            splitTimeMs = split,
                            totalTimeMs = elapsedTimeMs
                        )
                        laps = listOf(newRecord) + laps
                    } else {
                        // Reset
                        elapsedTimeMs = 0L
                        laps = emptyList()
                    }
                },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.Refresh,
                    contentDescription = if (isRunning) "Lap" else "Reset",
                    tint = if (isRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Start / Pause Giant Main Action Button
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(84.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Laps List Header
        if (laps.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lap #", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Text("Split Time", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Text("Total Time", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(laps, key = { _, lap -> lap.lapNumber }) { _, lap ->
                    val isFastest = lap.splitTimeMs == fastestLapSplit
                    val isSlowest = lap.splitTimeMs == slowestLapSplit

                    val rowColor = when {
                        isFastest -> Color(0xFF2E7D32)
                        isSlowest -> Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lap ${lap.lapNumber}", fontWeight = FontWeight.Bold, color = rowColor)
                            Text(formatDurationMs(lap.splitTimeMs), fontWeight = FontWeight.SemiBold, color = rowColor)
                            Text(formatDurationMs(lap.totalTimeMs), color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    val mins = (ms / 60000)
    val secs = (ms % 60000) / 1000
    val millis = (ms % 1000) / 10
    return String.format("%02d:%02d.%02d", mins, secs, millis)
}
