package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TimerPresetEntity
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    presets: List<TimerPresetEntity>,
    onAddPreset: (TimerPresetEntity) -> Unit,
    onDeletePreset: (TimerPresetEntity) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appSettings = remember { com.example.data.AppSettings(context) }
    val appSettingsData by appSettings.settingsState.collectAsState()

    var initialTotalSeconds by remember { mutableLongStateOf(300L) } // Default 5 mins
    var remainingSeconds by remember { mutableLongStateOf(300L) }
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var showTimeEditDialog by remember { mutableStateOf(false) }

    // Keep screen on if active and setting enabled
    val shouldKeepScreenOn = appSettingsData.keepScreenOnTimer && isRunning && !isPaused
    DisposableEffect(shouldKeepScreenOn) {
        val activity = context as? android.app.Activity
        if (shouldKeepScreenOn) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Timer Countdown Coroutine
    LaunchedEffect(isRunning, isPaused, remainingSeconds) {
        if (isRunning && !isPaused && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
            if (remainingSeconds == 0L) {
                isRunning = false
            }
        }
    }

    val progress = if (initialTotalSeconds > 0) {
        (remainingSeconds.toFloat() / initialTotalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer_progress")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Timer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isRunning) "Countdown Active" else "Select or edit duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                IconButton(onClick = { showTimeEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Duration", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Circular Progress Dial Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(290.dp)
                    .clickable {
                        if (!isRunning) {
                            showTimeEditDialog = true
                        }
                    }
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = 18.dp.toPx()
                    val arcRadius = (size.minDimension - strokeWidthPx) / 2f

                    drawCircle(
                        color = trackColor,
                        radius = arcRadius,
                        style = Stroke(width = strokeWidthPx)
                    )

                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val hrs = remainingSeconds / 3600
                    val mins = (remainingSeconds % 3600) / 60
                    val secs = remainingSeconds % 60

                    Text(
                        text = if (hrs > 0)
                            String.format("%02d:%02d:%02d", hrs, mins, secs)
                        else
                            String.format("%02d:%02d", mins, secs),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (!isRunning) "Tap to edit time" else if (isPaused) "PAUSED" else "RUNNING",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Giant Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRunning || isPaused) {
                    // Reset Button
                    OutlinedIconButton(
                        onClick = {
                            isRunning = false
                            isPaused = false
                            remainingSeconds = initialTotalSeconds
                        },
                        modifier = Modifier.size(68.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(28.dp))
                    }

                    // Play/Pause Button
                    Button(
                        onClick = {
                            if (isPaused) {
                                isPaused = false
                            } else {
                                isPaused = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // +1 Min Fast Add Button
                    OutlinedIconButton(
                        onClick = {
                            remainingSeconds += 60L
                            initialTotalSeconds += 60L
                        },
                        modifier = Modifier.size(68.dp)
                    ) {
                        Text("+1m", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    // Giant Start Button
                    Button(
                        onClick = {
                            if (remainingSeconds > 0) {
                                isRunning = true
                                isPaused = false
                            }
                        },
                        modifier = Modifier
                            .height(72.dp)
                            .width(220.dp),
                        shape = CircleShape
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("START", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Presets Bar
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val defaultPresetSeconds = listOf(60L, 180L, 300L, 600L, 900L, 1800L, 3600L)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp)
            ) {
                items(defaultPresetSeconds) { sec ->
                    val mins = sec / 60
                    FilterChip(
                        selected = initialTotalSeconds == sec,
                        onClick = {
                            initialTotalSeconds = sec
                            remainingSeconds = sec
                            isRunning = false
                            isPaused = false
                        },
                        label = { Text("${mins}m", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        if (showTimeEditDialog) {
            TimeDurationDialog(
                currentSeconds = remainingSeconds,
                onDismiss = { showTimeEditDialog = false },
                onSetDuration = { newSecs ->
                    initialTotalSeconds = newSecs
                    remainingSeconds = newSecs
                    isRunning = false
                    isPaused = false
                    showTimeEditDialog = false
                }
            )
        }
    }
}

@Composable
private fun TimeDurationDialog(
    currentSeconds: Long,
    onDismiss: () -> Unit,
    onSetDuration: (Long) -> Unit
) {
    var hours by remember { mutableIntStateOf((currentSeconds / 3600).toInt()) }
    var minutes by remember { mutableIntStateOf(((currentSeconds % 3600) / 60).toInt()) }
    var seconds by remember { mutableIntStateOf((currentSeconds % 60).toInt()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Edit Timer Duration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPickerColumn("Hours", hours, 0..23) { hours = it }
                    Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    NumberPickerColumn("Mins", minutes, 0..59) { minutes = it }
                    Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    NumberPickerColumn("Secs", seconds, 0..59) { seconds = it }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val totalSec = hours * 3600L + minutes * 60L + seconds
                        onSetDuration(totalSec)
                    }) {
                        Text("Set Timer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPickerColumn(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            val next = if (value + 1 > range.last) range.first else value + 1
            onValueChange(next)
        }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
        }

        Text(
            text = String.format("%02d", value),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = {
            val prev = if (value - 1 < range.first) range.last else value - 1
            onValueChange(prev)
        }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }

        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
