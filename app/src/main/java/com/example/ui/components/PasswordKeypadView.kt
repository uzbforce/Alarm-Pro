package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordKeypadView(
    targetPassword: String = "",
    isSetupMode: Boolean = false,
    onPasswordSubmitted: (password: String, isValid: Boolean) -> Unit
) {
    var enteredText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = if (isSetupMode) "Set Unlock PIN/Password" else "Enter Password to Stop Alarm",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password dots or mask
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
        ) {
            val lengthToShow = maxOf(4, enteredText.length)
            for (i in 0 until lengthToShow) {
                val isFilled = i < enteredText.length
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keypad Grid
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("DEL", "0", "OK")
        )

        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        text = key,
                        onClick = {
                            when (key) {
                                "DEL" -> {
                                    if (enteredText.isNotEmpty()) {
                                        enteredText = enteredText.dropLast(1)
                                        errorMessage = ""
                                    }
                                }
                                "OK" -> {
                                    if (isSetupMode) {
                                        if (enteredText.length < 3) {
                                            errorMessage = "Password must be at least 3 digits"
                                        } else {
                                            onPasswordSubmitted(enteredText, true)
                                        }
                                    } else {
                                        val isValid = enteredText == targetPassword
                                        if (isValid) {
                                            onPasswordSubmitted(enteredText, true)
                                        } else {
                                            errorMessage = "Incorrect password"
                                            enteredText = ""
                                        }
                                    }
                                }
                                else -> {
                                    if (enteredText.length < 8) {
                                        enteredText += key
                                        errorMessage = ""
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = when (text) {
            "OK" -> MaterialTheme.colorScheme.primary
            "DEL" -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        tonalElevation = 4.dp,
        modifier = Modifier.size(68.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (text) {
                "DEL" -> Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                "OK" -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Submit",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                else -> Text(
                    text = text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
