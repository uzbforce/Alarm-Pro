package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

@Composable
fun AppleTimePicker(
    hour: Int, // 0..23
    minute: Int, // 0..59
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAm = hour < 12
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hours Wheel (1..12)
        WheelSpinner(
            items = (1..12).toList(),
            selectedItem = displayHour,
            onItemSelected = { selectedH ->
                val newHour24 = if (isAm) {
                    if (selectedH == 12) 0 else selectedH
                } else {
                    if (selectedH == 12) 12 else selectedH + 12
                }
                if (newHour24 != hour) {
                    onTimeChanged(newHour24, minute)
                }
            },
            formatLabel = { String.format("%02d", it) },
            modifier = Modifier.weight(1f)
        )

        Text(
            text = ":",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Minutes Wheel (0..59)
        WheelSpinner(
            items = (0..59).toList(),
            selectedItem = minute,
            onItemSelected = { selectedM ->
                if (selectedM != minute) {
                    onTimeChanged(hour, selectedM)
                }
            },
            formatLabel = { String.format("%02d", it) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // AM / PM Wheel
        WheelSpinner(
            items = listOf("AM", "PM"),
            selectedItem = if (isAm) "AM" else "PM",
            onItemSelected = { selectedAmPm ->
                val isNewAm = selectedAmPm == "AM"
                if (isNewAm != isAm) {
                    val h12 = if (hour % 12 == 0) 12 else hour % 12
                    val newHour24 = if (isNewAm) {
                        if (h12 == 12) 0 else h12
                    } else {
                        if (h12 == 12) 12 else h12 + 12
                    }
                    onTimeChanged(newHour24, minute)
                }
            },
            formatLabel = { it },
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> WheelSpinner(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    formatLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    val itemHeight = 46.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    // Multiplier for infinite continuous wheel effect
    val baseMultiplier = 1000
    val virtualSize = baseMultiplier * items.size
    val targetItemIndex = items.indexOf(selectedItem).let { if (it < 0) 0 else it }
    
    // Initial position in middle chunk
    val initialVirtualIndex = (baseMultiplier / 2) * items.size + targetItemIndex

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialVirtualIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Calculate current selected index smoothly
    val selectedVirtualIndex by remember {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            if (offset > itemHeightPx / 2) firstIndex + 1 else firstIndex
        }
    }

    // Sync state changes with parent when scrolling settles or index changes
    LaunchedEffect(listState) {
        snapshotFlow { selectedVirtualIndex }
            .distinctUntilChanged()
            .collect { vIndex ->
                if (items.isNotEmpty()) {
                    val actualItem = items[vIndex % items.size]
                    if (actualItem != selectedItem) {
                        onItemSelected(actualItem)
                    }
                }
            }
    }

    // Sync external selectedItem changes to listState when not actively scrolling
    LaunchedEffect(selectedItem) {
        if (!listState.isScrollInProgress) {
            val currentActual = items[selectedVirtualIndex % items.size]
            if (currentActual != selectedItem) {
                val newTargetVirtual = (selectedVirtualIndex / items.size) * items.size + targetItemIndex
                listState.scrollToItem(newTargetVirtual)
            }
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * 3)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Center selection bar frame with rounded background
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(itemHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
        )

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight)
        ) {
            items(virtualSize) { index ->
                val actualItem = items[index % items.size]

                // Visual distance calculation relative to central selection
                val distance = abs(index - selectedVirtualIndex)
                val scale = when (distance) {
                    0 -> 1.15f
                    1 -> 0.88f
                    else -> 0.72f
                }
                val alpha = when (distance) {
                    0 -> 1.0f
                    1 -> 0.55f
                    else -> 0.25f
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .scale(scale)
                        .alpha(alpha),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatLabel(actualItem),
                        fontSize = 22.sp,
                        fontWeight = if (distance == 0) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (distance == 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

