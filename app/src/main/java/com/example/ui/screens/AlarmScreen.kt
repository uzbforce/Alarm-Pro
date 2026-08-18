package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AlarmEntity
import com.example.service.SoundLibrary
import com.example.ui.components.AppleTimePicker
import com.example.ui.components.PasswordKeypadView
import com.example.ui.components.PatternLockView
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    alarms: List<AlarmEntity>,
    onAddAlarm: (AlarmEntity) -> Unit,
    onUpdateAlarm: (AlarmEntity) -> Unit,
    onDeleteAlarm: (AlarmEntity) -> Unit,
    onToggleAlarm: (AlarmEntity) -> Unit
) {
    val context = LocalContext.current
    val appSettings = remember { com.example.data.AppSettings(context) }
    val appSettingsData by appSettings.settingsState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }

    val nextAlarmInfo = remember(alarms) {
        val enabledAlarms = alarms.filter { it.enabled }
        if (enabledAlarms.isEmpty()) null
        else {
            val now = Calendar.getInstance()
            val currentMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            enabledAlarms.minByOrNull { alarm ->
                var alarmMin = alarm.hour * 60 + alarm.minute
                if (alarmMin <= currentMin) alarmMin += 24 * 60
                alarmMin - currentMin
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
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
                        text = "Alarms",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${alarms.count { it.enabled }} active alarms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    FloatingActionButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            editingAlarm = AlarmEntity(
                                hour = cal.get(Calendar.HOUR_OF_DAY),
                                minute = cal.get(Calendar.MINUTE),
                                snoozeDurationMinutes = appSettingsData.defaultSnoozeMinutes,
                                allowSnooze = appSettingsData.defaultAllowSnooze,
                                snoozeChallenge = appSettingsData.defaultSnoozeChallenge,
                                dismissChallenge = appSettingsData.defaultDismissChallenge,
                                securityCode = appSettingsData.defaultSecurityCode,
                                soundUri = appSettingsData.defaultSoundUri,
                                soundTitle = appSettingsData.defaultSoundTitle,
                                fadeVolume = appSettingsData.defaultFadeVolume,
                                fadeDurationSeconds = appSettingsData.defaultFadeDurationSeconds,
                                vibrate = appSettingsData.defaultVibrate,
                                volume = appSettingsData.alarmVolume / 100f
                            )
                            showEditDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Alarm")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next Alarm Info Chip
            if (appSettingsData.showNextAlarmOnMainClock) {
                nextAlarmInfo?.let { next ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Next Alarm",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${next.formattedTime(timeFormat = appSettingsData.timeFormat, context = context)} • ${next.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AlarmOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No alarms configured yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Tap + to create your secure alarm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            timeFormat = appSettingsData.timeFormat,
                            onToggle = { onToggleAlarm(alarm) },
                            onClick = {
                                editingAlarm = alarm
                                showEditDialog = true
                            },
                            onDelete = { onDeleteAlarm(alarm) }
                        )
                    }
                }
            }
        }

        // Sub-menu Zoom In / Out Animated Modal Dialog for Creating/Editing Alarm
        AnimatedVisibility(
            visible = showEditDialog,
            enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            editingAlarm?.let { alarmToEdit ->
                AlarmEditDialog(
                    alarm = alarmToEdit,
                    onDismiss = { showEditDialog = false },
                    onSave = { updatedAlarm ->
                        if (alarmToEdit.id == 0L) {
                            onAddAlarm(updatedAlarm)
                        } else {
                            onUpdateAlarm(updatedAlarm)
                        }
                        showEditDialog = false
                    }
                )
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(onDismiss = { showSettingsDialog = false })
        }
    }
}

@Composable
private fun AlarmItemCard(
    alarm: AlarmEntity,
    timeFormat: String = "SYSTEM",
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (alarm.enabled)
            MaterialTheme.colorScheme.surfaceContainerHigh
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (alarm.enabled) 6.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.formattedTime(timeFormat = timeFormat, context = context),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (alarm.enabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alarm.label.ifBlank { "Alarm" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.enabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = " • ${alarm.formattedRepeatDays()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Security Badge & Sound
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (alarm.dismissChallenge != "NONE") {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = alarm.dismissChallenge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = alarm.soundTitle,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    if (!alarm.allowSnooze) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Snooze,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "No Snooze",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Alarm",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }

                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditDialog(
    alarm: AlarmEntity,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit
) {
    var hour by remember { mutableIntStateOf(alarm.hour) }
    var minute by remember { mutableIntStateOf(alarm.minute) }
    var label by remember { mutableStateOf(alarm.label) }
    var repeatDaysMask by remember { mutableIntStateOf(alarm.repeatDaysMask) }
    var soundTitle by remember { mutableStateOf(alarm.soundTitle) }
    var soundUri by remember { mutableStateOf(alarm.soundUri) }
    var volume by remember { mutableFloatStateOf(alarm.volume) }
    var fadeVolume by remember { mutableStateOf(alarm.fadeVolume) }
    var fadeDurationSeconds by remember { mutableIntStateOf(alarm.fadeDurationSeconds) }
    var vibrate by remember { mutableStateOf(alarm.vibrate) }
    var allowSnooze by remember { mutableStateOf(alarm.allowSnooze) }
    var snoozeDurationMinutes by remember { mutableIntStateOf(alarm.snoozeDurationMinutes) }
    var maxSnoozeCount by remember { mutableIntStateOf(alarm.maxSnoozeCount) }
    var dismissChallenge by remember { mutableStateOf(alarm.dismissChallenge) }
    var snoozeChallenge by remember { mutableStateOf(alarm.snoozeChallenge) }
    var securityCode by remember { mutableStateOf(alarm.securityCode) }
    var volumeButtonAction by remember { mutableStateOf(alarm.volumeButtonAction) }
    var screenQuote by remember { mutableStateOf(alarm.screenQuote) }

    var showPatternSetup by remember { mutableStateOf(false) }
    var showPasswordSetup by remember { mutableStateOf(false) }
    var soundDropdownExpanded by remember { mutableStateOf(false) }
    var isPreviewPlaying by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            SoundLibrary.stopSound()
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            soundUri = it.toString()
            soundTitle = "Custom Song (${it.lastPathSegment ?: "Audio"})"
        }
    }

    Dialog(onDismissRequest = {
        SoundLibrary.stopSound()
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = if (alarm.id == 0L) "New Expressive Alarm" else "Edit Alarm Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. Time Selector Wheel (Apple-style Spinner)
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Alarm Time",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            AppleTimePicker(
                                hour = hour,
                                minute = minute,
                                onTimeChanged = { h, m ->
                                    hour = h
                                    minute = m
                                }
                            )
                        }
                    }

                    // 2. Label & Repeat Days
                    item {
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Alarm Label / Quote") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(
                            text = "Repeat Days",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val days = listOf("S", "M", "T", "W", "T", "F", "S")
                            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                            days.forEachIndexed { index, day ->
                                val isSelected = (repeatDaysMask and (1 shl index)) != 0
                                Surface(
                                    selected = isSelected,
                                    onClick = {
                                        repeatDaysMask = repeatDaysMask xor (1 shl index)
                                    },
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(day, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Sound & Song Selection Dropdown with Play/Stop Preview
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Alarm Sound & Songs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ExposedDropdownMenuBox(
                                        expanded = soundDropdownExpanded,
                                        onExpandedChange = { soundDropdownExpanded = !soundDropdownExpanded },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = soundTitle,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Sound Option") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soundDropdownExpanded) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                        )

                                        ExposedDropdownMenu(
                                            expanded = soundDropdownExpanded,
                                            onDismissRequest = { soundDropdownExpanded = false }
                                        ) {
                                            SoundLibrary.PRESET_SOUNDS.forEach { preset ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(preset.title, fontWeight = FontWeight.Bold)
                                                            Text(preset.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                                        }
                                                    },
                                                    onClick = {
                                                        soundTitle = preset.title
                                                        soundUri = preset.id
                                                        soundDropdownExpanded = false
                                                        if (isPreviewPlaying) {
                                                            SoundLibrary.playSound(context, soundUri, volume, coroutineScope)
                                                        }
                                                    }
                                                )
                                            }

                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.FileOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Import Custom Audio File...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                    }
                                                },
                                                onClick = {
                                                    soundDropdownExpanded = false
                                                    audioPickerLauncher.launch("audio/*")
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    FilledIconButton(
                                        onClick = {
                                            if (isPreviewPlaying) {
                                                SoundLibrary.stopSound()
                                                isPreviewPlaying = false
                                            } else {
                                                SoundLibrary.playSound(context, soundUri, volume, coroutineScope)
                                                isPreviewPlaying = true
                                            }
                                        },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = if (isPreviewPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Preview Alarm Sound",
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                if (soundUri.startsWith("content://") || soundUri.startsWith("file://")) {
                                    Text(
                                        text = "Selected audio: $soundTitle",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Volume & Fade
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Volume & Gradual Fade-In",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                                    Slider(
                                        value = volume,
                                        onValueChange = { volume = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp)
                                    )
                                    Text("${(volume * 100).toInt()}%", fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Gradually Increase Volume")
                                    Switch(
                                        checked = fadeVolume,
                                        onCheckedChange = { fadeVolume = it }
                                    )
                                }
                            }
                        }
                    }

                    // 5. SECURITY DISMISS CHALLENGE (Requirement)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Required Stop Challenge",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Text(
                                    text = "Requires verification before anyone can stop the alarm!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val challenges = listOf(
                                    "NONE" to "Normal Button",
                                    "PASSWORD" to "PIN / Password Lock",
                                    "PATTERN" to "3x3 Pattern Lock",
                                    "MATH" to "Math Puzzle",
                                    "SHAKE" to "Shake Device"
                                )

                                challenges.forEach { (code, title) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                dismissChallenge = code
                                                if (code == "PATTERN" && securityCode.isEmpty()) {
                                                    showPatternSetup = true
                                                } else if (code == "PASSWORD" && securityCode.isEmpty()) {
                                                    showPasswordSetup = true
                                                }
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = dismissChallenge == code,
                                            onClick = {
                                                dismissChallenge = code
                                                if (code == "PATTERN" && securityCode.isEmpty()) {
                                                    showPatternSetup = true
                                                } else if (code == "PASSWORD" && securityCode.isEmpty()) {
                                                    showPasswordSetup = true
                                                }
                                            }
                                        )
                                        Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                                    }
                                }

                                if (dismissChallenge == "PATTERN" || dismissChallenge == "PASSWORD") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (securityCode.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Security key is set ✓",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        TextButton(onClick = {
                                            if (dismissChallenge == "PATTERN") showPatternSetup = true
                                            else if (dismissChallenge == "PASSWORD") showPasswordSetup = true
                                        }) {
                                            Text("Change Security Code")
                                        }
                                    } else {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Security Code NOT Set Yet!",
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "You chose a ${if (dismissChallenge == "PATTERN") "Pattern Lock" else "Password"} challenge, but no passcode is saved yet.",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = {
                                                        if (dismissChallenge == "PATTERN") showPatternSetup = true
                                                        else if (dismissChallenge == "PASSWORD") showPasswordSetup = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                ) {
                                                    Text("Set ${if (dismissChallenge == "PATTERN") "Pattern" else "Passcode"} Now")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Advanced Snooze Options
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Allow Snooze",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (allowSnooze) "Snoozing is enabled for this alarm" else "Snoozing is disabled (alarm must be dismissed)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = allowSnooze,
                                        onCheckedChange = { allowSnooze = it }
                                    )
                                }

                                if (allowSnooze) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Snooze Duration: $snoozeDurationMinutes minutes", fontWeight = FontWeight.Medium)
                                    Slider(
                                        value = snoozeDurationMinutes.toFloat(),
                                        onValueChange = { snoozeDurationMinutes = it.toInt() },
                                        valueRange = 1f..30f,
                                        steps = 29
                                    )

                                    Text("Max Snooze Count Limit: ${if (maxSnoozeCount == 0) "Unlimited" else maxSnoozeCount}")
                                    Slider(
                                        value = maxSnoozeCount.toFloat(),
                                        onValueChange = { maxSnoozeCount = it.toInt() },
                                        valueRange = 0f..10f,
                                        steps = 10
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = {
                        if ((dismissChallenge == "PATTERN" || dismissChallenge == "PASSWORD") && securityCode.isBlank()) {
                            if (dismissChallenge == "PATTERN") showPatternSetup = true
                            else if (dismissChallenge == "PASSWORD") showPasswordSetup = true
                        } else {
                            val finalAlarm = alarm.copy(
                                hour = hour,
                                minute = minute,
                                label = label,
                                repeatDaysMask = repeatDaysMask,
                                soundTitle = soundTitle,
                                soundUri = soundUri,
                                volume = volume,
                                fadeVolume = fadeVolume,
                                fadeDurationSeconds = fadeDurationSeconds,
                                vibrate = vibrate,
                                allowSnooze = allowSnooze,
                                snoozeDurationMinutes = snoozeDurationMinutes,
                                maxSnoozeCount = maxSnoozeCount,
                                dismissChallenge = dismissChallenge,
                                snoozeChallenge = snoozeChallenge,
                                securityCode = securityCode,
                                volumeButtonAction = volumeButtonAction,
                                screenQuote = screenQuote,
                                enabled = true
                            )
                            onSave(finalAlarm)
                        }
                    }) {
                        Text("Save Alarm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Pattern Setup Dialog
    if (showPatternSetup) {
        Dialog(onDismissRequest = { showPatternSetup = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                PatternLockView(
                    isSetupMode = true,
                    onPatternComplete = { pattern, isValid ->
                        if (isValid) {
                            securityCode = pattern
                            showPatternSetup = false
                        }
                    }
                )
            }
        }
    }

    // Password Setup Dialog
    if (showPasswordSetup) {
        Dialog(onDismissRequest = { showPasswordSetup = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                PasswordKeypadView(
                    isSetupMode = true,
                    onPasswordSubmitted = { pwd, isValid ->
                        if (isValid) {
                            securityCode = pwd
                            showPasswordSetup = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NumberWheel(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            val next = if (value + 1 > range.last) range.first else value + 1
            onValueChange(next)
        }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
        }

        Text(
            text = String.format("%02d", value),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(onClick = {
            val prev = if (value - 1 < range.first) range.last else value - 1
            onValueChange(prev)
        }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
        }

        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
