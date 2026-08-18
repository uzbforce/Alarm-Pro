package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppSettings
import com.example.service.AlarmAccessibilityService
import com.example.service.SoundLibrary
import com.example.ui.components.PasswordKeypadView
import com.example.ui.components.PatternLockView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings = remember { AppSettings(context) }

    // Observe active settings
    val settingsData by settings.settingsState.collectAsState()

    var isPreviewPlaying by remember { mutableStateOf(false) }
    var showDefaultPatternSetup by remember { mutableStateOf(false) }
    var showDefaultPasswordSetup by remember { mutableStateOf(false) }

    // Stop sound on dismiss
    DisposableEffect(Unit) {
        onDispose {
            SoundLibrary.stopSound()
        }
    }

    Dialog(onDismissRequest = {
        SoundLibrary.stopSound()
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = {
                        SoundLibrary.stopSound()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Settings")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // SECTION 1: App Appearance & Time Display
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Appearance & Clock Display",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Theme Mode
                                Text("App Theme", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                val themes = listOf("DARK" to "Dark Mode", "LIGHT" to "Light Mode", "SYSTEM" to "System Theme")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    themes.forEach { (mode, label) ->
                                        FilterChip(
                                            selected = settingsData.themeMode == mode,
                                            onClick = { settings.themeMode = mode },
                                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Time Format
                                Text("Time Format", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                val timeFormats = listOf("SYSTEM" to "System Default", "12H" to "12-Hour (AM/PM)", "24H" to "24-Hour (13:00)")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    timeFormats.forEach { (fmt, label) ->
                                        FilterChip(
                                            selected = settingsData.timeFormat == fmt,
                                            onClick = { settings.timeFormat = fmt },
                                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // First Day of Week
                                Text("First Day of Week", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                val dayOrders = listOf("SYSTEM" to "System", "SUNDAY" to "Sunday", "MONDAY" to "Monday")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    dayOrders.forEach { (day, label) ->
                                        FilterChip(
                                            selected = settingsData.firstDayOfWeek == day,
                                            onClick = { settings.firstDayOfWeek = day },
                                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Switches
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Show Seconds on Main Clock", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Switch(
                                        checked = settingsData.showSecondsOnMainClock,
                                        onCheckedChange = { settings.showSecondsOnMainClock = it }
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Show Next Alarm Banner", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Switch(
                                        checked = settingsData.showNextAlarmOnMainClock,
                                        onCheckedChange = { settings.showNextAlarmOnMainClock = it }
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 2: Default Alarm Parameters
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Default Alarm Configurations",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Default Snooze Minutes
                                Text("Default Snooze: ${settingsData.defaultSnoozeMinutes} minutes", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Slider(
                                    value = settingsData.defaultSnoozeMinutes.toFloat(),
                                    onValueChange = { settings.defaultSnoozeMinutes = it.toInt() },
                                    valueRange = 1f..30f,
                                    steps = 29
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Allow Snooze Switch
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Allow Snooze by Default", fontSize = 14.sp)
                                    Switch(
                                        checked = settingsData.defaultAllowSnooze,
                                        onCheckedChange = { settings.defaultAllowSnooze = it }
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Default Snooze Challenge
                                Text("Default Snooze Challenge:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                val snoozeChallenges = listOf("NONE", "MATH", "SHAKE")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    snoozeChallenges.forEach { ch ->
                                        FilterChip(
                                            selected = settingsData.defaultSnoozeChallenge == ch,
                                            onClick = { settings.defaultSnoozeChallenge = ch },
                                            label = { Text(ch, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Default Dismiss Challenge
                                Text("Default Stop / Dismiss Challenge:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                val dismissChallenges = listOf("NONE", "MATH", "PATTERN", "PASSWORD", "SHAKE")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    dismissChallenges.forEach { ch ->
                                        FilterChip(
                                            selected = settingsData.defaultDismissChallenge == ch,
                                            onClick = {
                                                settings.defaultDismissChallenge = ch
                                                if ((ch == "PATTERN" || ch == "PASSWORD") && settingsData.defaultSecurityCode.isBlank()) {
                                                    if (ch == "PATTERN") showDefaultPatternSetup = true
                                                    else if (ch == "PASSWORD") showDefaultPasswordSetup = true
                                                }
                                            },
                                            label = { Text(ch, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }

                                if (settingsData.defaultDismissChallenge == "PATTERN" || settingsData.defaultDismissChallenge == "PASSWORD") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (settingsData.defaultSecurityCode.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Default security code set ✓",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            TextButton(onClick = {
                                                if (settingsData.defaultDismissChallenge == "PATTERN") showDefaultPatternSetup = true
                                                else showDefaultPasswordSetup = true
                                            }) {
                                                Text("Change Code", fontSize = 12.sp)
                                            }
                                        }
                                    } else {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Default Passcode/Pattern NOT Set!",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "You selected ${if (settingsData.defaultDismissChallenge == "PATTERN") "Pattern Lock" else "Password"} as default, but no code is configured yet.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = {
                                                        if (settingsData.defaultDismissChallenge == "PATTERN") showDefaultPatternSetup = true
                                                        else showDefaultPasswordSetup = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                ) {
                                                    Text(
                                                        text = "Set Default ${if (settingsData.defaultDismissChallenge == "PATTERN") "Pattern" else "Passcode"} Now",
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Default Sound Selector
                                Text("Default Alarm Ringtone:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(settingsData.defaultSoundTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("Selected default wake-up sound", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }

                                    IconButton(onClick = {
                                        if (isPreviewPlaying) {
                                            SoundLibrary.stopSound()
                                            isPreviewPlaying = false
                                        } else {
                                            isPreviewPlaying = true
                                            SoundLibrary.playSound(
                                                context = context,
                                                soundUri = settingsData.defaultSoundUri,
                                                initialVolume = settingsData.alarmVolume / 100f,
                                                scope = coroutineScope
                                            )
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Test Sound",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(SoundLibrary.PRESET_SOUNDS) { snd ->
                                        FilterChip(
                                            selected = settingsData.defaultSoundUri == snd.id,
                                            onClick = {
                                                settings.defaultSoundUri = snd.id
                                                settings.defaultSoundTitle = snd.title
                                                if (isPreviewPlaying) {
                                                    SoundLibrary.playSound(
                                                        context = context,
                                                        soundUri = snd.id,
                                                        initialVolume = settingsData.alarmVolume / 100f,
                                                        scope = coroutineScope
                                                    )
                                                }
                                            },
                                            label = { Text(snd.title, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Fade Volume Switches
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Gentle Fade-in Volume", fontSize = 14.sp)
                                    Switch(
                                        checked = settingsData.defaultFadeVolume,
                                        onCheckedChange = { settings.defaultFadeVolume = it }
                                    )
                                }

                                if (settingsData.defaultFadeVolume) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Fade Duration: ${settingsData.defaultFadeDurationSeconds}s", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                    val fadeOptions = listOf(10, 30, 60)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        fadeOptions.forEach { sec ->
                                            FilterChip(
                                                selected = settingsData.defaultFadeDurationSeconds == sec,
                                                onClick = { settings.defaultFadeDurationSeconds = sec },
                                                label = { Text("${sec}s", fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Vibrate by Default", fontSize = 14.sp)
                                    Switch(
                                        checked = settingsData.defaultVibrate,
                                        onCheckedChange = { settings.defaultVibrate = it }
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 3: Ringing & Volume Controls
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Volume & Auto-Silence Rules",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Alarm Volume: ${settingsData.alarmVolume}%", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Slider(
                                    value = settingsData.alarmVolume.toFloat(),
                                    onValueChange = {
                                        settings.alarmVolume = it.toInt()
                                        if (isPreviewPlaying) {
                                            SoundLibrary.setVolume(it / 100f)
                                        }
                                    },
                                    valueRange = 10f..100f,
                                    steps = 18
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Auto Silence
                                Text("Auto-Silence Ringing Alarm After:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                val autoSilenceOptions = listOf(0 to "Off", 1 to "1 min", 3 to "3 min", 5 to "5 min", 10 to "10 min", 15 to "15 min")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(autoSilenceOptions) { (mins, label) ->
                                        FilterChip(
                                            selected = settingsData.autoSilenceMinutes == mins,
                                            onClick = { settings.autoSilenceMinutes = mins },
                                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Hardware Volume Buttons
                                Text("Volume Button Press Action When Ringing:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                val volActions = listOf("SNOOZE" to "Snooze Alarm", "DISMISS" to "Dismiss Alarm", "DO_NOTHING" to "Lock (Ignore)")
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    volActions.forEach { (act, label) ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { settings.volumeButtonAction = act }
                                                .padding(vertical = 2.dp)
                                        ) {
                                            RadioButton(
                                                selected = settingsData.volumeButtonAction == act,
                                                onClick = { settings.volumeButtonAction = act }
                                            )
                                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 4: Timer & Stopwatch Settings
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Timer & Stopwatch Settings",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Keep Screen On during Active Timer", fontSize = 14.sp)
                                    Switch(
                                        checked = settingsData.keepScreenOnTimer,
                                        onCheckedChange = { settings.keepScreenOnTimer = it }
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Keep Screen On during Stopwatch", fontSize = 14.sp)
                                    Switch(
                                        checked = settingsData.keepScreenOnStopwatch,
                                        onCheckedChange = { settings.keepScreenOnStopwatch = it }
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Vibrate when Timer Expires", fontSize = 14.sp)
                                    Switch(
                                        checked = settingsData.timerVibrate,
                                        onCheckedChange = { settings.timerVibrate = it }
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 5: System Permissions & Lock Setup
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Permissions & Lock Controls",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ensure these system capabilities are enabled so alarms reliably show over locked screens and prevent accidental muting.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 1. Display over other apps
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Display Over Other Apps", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("Required for alarm pop-ups over locked screen", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                val intent = Intent(
                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:${context.packageName}")
                                                )
                                                context.startActivity(intent)
                                            }
                                        }
                                    ) {
                                        Text("Grant", fontSize = 12.sp)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                // 2. Accessibility Lock Service
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Volume Button Lock Service", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("Enables Aura Accessibility to handle volume buttons during alarms", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                            context.startActivity(intent)
                                        }
                                    ) {
                                        Text("Setup", fontSize = 12.sp)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                // 3. Battery Optimization
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Unrestricted Battery Usage", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("Prevents system from suppressing deep sleep alarms", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                            context.startActivity(intent)
                                        }
                                    ) {
                                        Text("Allow", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        SoundLibrary.stopSound()
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Done")
                }
            }
        }
    }

    // Default Pattern Setup Dialog
    if (showDefaultPatternSetup) {
        Dialog(onDismissRequest = { showDefaultPatternSetup = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                PatternLockView(
                    isSetupMode = true,
                    onPatternComplete = { pattern, isValid ->
                        if (isValid) {
                            settings.defaultSecurityCode = pattern
                            showDefaultPatternSetup = false
                        }
                    }
                )
            }
        }
    }

    // Default Password Setup Dialog
    if (showDefaultPasswordSetup) {
        Dialog(onDismissRequest = { showDefaultPasswordSetup = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                PasswordKeypadView(
                    isSetupMode = true,
                    onPasswordSubmitted = { pwd, isValid ->
                        if (isValid) {
                            settings.defaultSecurityCode = pwd
                            showDefaultPasswordSetup = false
                        }
                    }
                )
            }
        }
    }
}
