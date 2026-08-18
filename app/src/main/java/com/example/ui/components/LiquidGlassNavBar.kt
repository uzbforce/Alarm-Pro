package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timer10
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Timer10
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class TabItem(
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

val MAIN_TABS = listOf(
    TabItem("Alarm", Icons.Filled.Alarm, Icons.Outlined.Alarm),
    TabItem("World Clock", Icons.Filled.Language, Icons.Outlined.Language),
    TabItem("Timer", Icons.Filled.Timer, Icons.Outlined.Timer),
    TabItem("Stopwatch", Icons.Filled.Timer10, Icons.Outlined.Timer10)
)

@Composable
fun LiquidGlassNavBar(
    currentPage: Int,
    pageOffsetFraction: Float,
    onTabSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabCount = MAIN_TABS.size
    var containerWidthPx by remember { mutableFloatStateOf(0f) }

    val currentFraction = (currentPage + pageOffsetFraction).coerceIn(0f, (tabCount - 1).toFloat())
    val activeTabHighlight = (currentFraction + 0.5f).toInt().coerceIn(0, tabCount - 1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Outer Floating Liquid Glass Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f),
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = CircleShape
                )
                .onGloballyPositioned { coordinates ->
                    containerWidthPx = coordinates.size.width.toFloat()
                }
        ) {
            // Liquid Sliding Glass Pill Indicator - Using Lambda Offset for 60fps zero-recomposition animation
            if (containerWidthPx > 0) {
                val density = LocalDensity.current
                val paddingPx = with(density) { 5.dp.toPx() }
                val tabWidthPx = containerWidthPx / tabCount
                val pillWidthPx = tabWidthPx - (paddingPx * 2)
                val pillWidthDp = with(density) { pillWidthPx.toDp() }

                Box(
                    modifier = Modifier
                        .offset {
                            val pillLeftPx = (currentFraction * tabWidthPx) + paddingPx
                            IntOffset(x = pillLeftPx.roundToInt(), y = with(density) { 5.dp.roundToPx() })
                        }
                        .width(pillWidthDp)
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                )
            }

            // Tab Content Items
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MAIN_TABS.forEachIndexed { index, tab ->
                    val isSelected = activeTabHighlight == index
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onTabSelected(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

