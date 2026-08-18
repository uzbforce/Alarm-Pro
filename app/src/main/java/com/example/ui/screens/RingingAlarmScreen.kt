package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AlarmEntity
import com.example.ui.components.MathChallengeView
import com.example.ui.components.PasswordKeypadView
import com.example.ui.components.PatternLockView
import com.example.ui.components.ShakeChallengeView

@Composable
fun RingingAlarmScreen(
    alarm: AlarmEntity,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeChallengeMode by remember { mutableStateOf("IDLE") } // IDLE, DISMISS_CHALLENGE, SNOOZE_CHALLENGE

    val infiniteTransition = rememberInfiniteTransition(label = "ringing_glow")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Time & Label Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = alphaAnim),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = alarm.formattedTime(),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = alarm.label.ifBlank { "Wake Up!" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primaryContainer
                )

                if (alarm.screenQuote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${alarm.screenQuote}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Challenge Active Area or Main Buttons
            when {
                alarm.dismissChallenge == "SHAKE" -> {
                    // Shake challenge is immediately active upon ringing - no button press required
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        ShakeChallengeView(
                            title = "Shake Device to Stop Alarm",
                            onCompleted = onDismiss
                        )

                        if (alarm.allowSnooze) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    if (alarm.snoozeChallenge == "NONE") {
                                        onSnooze()
                                    } else {
                                        activeChallengeMode = "SNOOZE_CHALLENGE"
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Snooze, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SNOOZE (${alarm.snoozeDurationMinutes} mins)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                activeChallengeMode == "DISMISS_CHALLENGE" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                when (alarm.dismissChallenge) {
                                    "PATTERN" -> {
                                        if (alarm.securityCode.isBlank()) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "No Pattern Code Set!",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "This alarm has a pattern challenge enabled, but no pattern code was configured.",
                                                    fontSize = 13.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Button(
                                                    onClick = onDismiss,
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                ) {
                                                    Text("EMERGENCY DISMISS")
                                                }
                                            }
                                        } else {
                                            PatternLockView(
                                                targetPattern = alarm.securityCode,
                                                onPatternComplete = { _, isValid ->
                                                    if (isValid) onDismiss()
                                                }
                                            )
                                        }
                                    }
                                    "PASSWORD" -> {
                                        if (alarm.securityCode.isBlank()) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "No Passcode Set!",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "This alarm has a password challenge enabled, but no passcode was configured.",
                                                    fontSize = 13.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Button(
                                                    onClick = onDismiss,
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                ) {
                                                    Text("EMERGENCY DISMISS")
                                                }
                                            }
                                        } else {
                                            PasswordKeypadView(
                                                targetPassword = alarm.securityCode,
                                                onPasswordSubmitted = { _, isValid ->
                                                    if (isValid) onDismiss()
                                                }
                                            )
                                        }
                                    }
                                    "MATH" -> MathChallengeView(onSolved = onDismiss)
                                    "SHAKE" -> ShakeChallengeView(onCompleted = onDismiss)
                                    else -> onDismiss()
                                }

                                TextButton(onClick = { activeChallengeMode = "IDLE" }) {
                                    Text("Back")
                                }
                            }
                        }
                    }
                }
                activeChallengeMode == "SNOOZE_CHALLENGE" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                when (alarm.snoozeChallenge) {
                                    "MATH" -> MathChallengeView(onSolved = onSnooze)
                                    "SHAKE" -> ShakeChallengeView(
                                        title = "Shake Device to Snooze",
                                        onCompleted = onSnooze
                                    )
                                    else -> onSnooze()
                                }

                                TextButton(onClick = { activeChallengeMode = "IDLE" }) {
                                    Text("Back")
                                }
                            }
                        }
                    }
                }
                else -> {
                    // IDLE: Main Dismiss and Snooze buttons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 40.dp)
                    ) {
                        // Dismiss Button
                        Button(
                            onClick = {
                                if (alarm.dismissChallenge == "NONE") {
                                    onDismiss()
                                } else {
                                    activeChallengeMode = "DISMISS_CHALLENGE"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (alarm.dismissChallenge != "NONE") {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (alarm.dismissChallenge != "NONE") "UNLOCK & DISMISS (${alarm.dismissChallenge})" else "DISMISS ALARM",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (alarm.allowSnooze) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Snooze Button
                            OutlinedButton(
                                onClick = {
                                    if (alarm.snoozeChallenge == "NONE") {
                                        onSnooze()
                                    } else {
                                        activeChallengeMode = "SNOOZE_CHALLENGE"
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Snooze, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SNOOZE (${alarm.snoozeDurationMinutes} mins)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Snooze is disabled for this alarm",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
