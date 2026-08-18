package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot

@Composable
fun PatternLockView(
    targetPattern: String = "",
    isSetupMode: Boolean = false,
    onPatternComplete: (drawnPattern: String, isValid: Boolean) -> Unit
) {
    var selectedDots by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentTouchPosition by remember { mutableStateOf<Offset?>(null) }
    var isErrorState by remember { mutableStateOf(false) }
    var setupFirstPattern by remember { mutableStateOf("") }
    var hintText by remember {
        mutableStateOf(
            if (isSetupMode) "Draw pattern to set" else "Draw pattern to unlock"
        )
    }

    val dotCenters = remember { mutableStateListOf<Offset>() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = hintText,
            color = if (isErrorState) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    )
                )
                .pointerInput(isErrorState) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (isErrorState) {
                                isErrorState = false
                                selectedDots = emptyList()
                            }
                            dotCenters.forEachIndexed { index, center ->
                                if (hypot(offset.x - center.x, offset.y - center.y) < 110f) {
                                    if (index !in selectedDots) {
                                        selectedDots = listOf(index)
                                    }
                                }
                            }
                            currentTouchPosition = offset
                        },
                        onDrag = { change, _ ->
                            val touch = change.position
                            currentTouchPosition = touch
                            dotCenters.forEachIndexed { index, center ->
                                if (hypot(touch.x - center.x, touch.y - center.y) < 110f) {
                                    if (index !in selectedDots) {
                                        selectedDots = selectedDots + index
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            currentTouchPosition = null
                            val patternString = selectedDots.joinToString("-")
                            if (patternString.isNotBlank()) {
                                if (isSetupMode) {
                                    if (setupFirstPattern.isEmpty()) {
                                        setupFirstPattern = patternString
                                        hintText = "Confirm pattern"
                                        selectedDots = emptyList()
                                    } else {
                                        if (patternString == setupFirstPattern) {
                                            hintText = "Pattern matched!"
                                            onPatternComplete(patternString, true)
                                        } else {
                                            isErrorState = true
                                            hintText = "Patterns do not match. Try again."
                                            setupFirstPattern = ""
                                            onPatternComplete(patternString, false)
                                        }
                                    }
                                } else {
                                    val isValid = patternString == targetPattern
                                    if (isValid) {
                                        hintText = "Pattern correct!"
                                        onPatternComplete(patternString, true)
                                    } else {
                                        isErrorState = true
                                        hintText = "Incorrect pattern. Try again."
                                        onPatternComplete(patternString, false)
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            val lineColor = when {
                isErrorState -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val sizePx = size.width
                val gridSpacing = sizePx / 3f
                val padding = gridSpacing / 2f

                if (dotCenters.isEmpty()) {
                    for (row in 0..2) {
                        for (col in 0..2) {
                            val x = padding + col * gridSpacing
                            val y = padding + row * gridSpacing
                            dotCenters.add(Offset(x, y))
                        }
                    }
                }

                // Draw connecting lines
                if (selectedDots.isNotEmpty()) {
                    val path = Path().apply {
                        val first = dotCenters[selectedDots[0]]
                        moveTo(first.x, first.y)
                        for (i in 1 until selectedDots.size) {
                            val pt = dotCenters[selectedDots[i]]
                            lineTo(pt.x, pt.y)
                        }
                        currentTouchPosition?.let { touch ->
                            val last = dotCenters[selectedDots.last()]
                            lineTo(touch.x, touch.y)
                        }
                    }

                    // Outer Glowing aura path
                    drawPath(
                        path = path,
                        color = lineColor.copy(alpha = 0.35f),
                        style = Stroke(width = 40f, cap = StrokeCap.Round)
                    )

                    // Core line path
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )
                }

                // Draw 3x3 Dots
                dotCenters.forEachIndexed { index, center ->
                    val isSelected = index in selectedDots
                    val dotRadius = if (isSelected) 28f else 18f
                    val dotColor = if (isSelected) lineColor else Color.White.copy(alpha = 0.6f)

                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = center
                    )

                    if (isSelected) {
                        drawCircle(
                            color = lineColor.copy(alpha = 0.35f),
                            radius = 48f,
                            center = center
                        )
                    }
                }
            }
        }
    }
}
