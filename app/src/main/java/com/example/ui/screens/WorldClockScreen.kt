package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.WorldClockEntity
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

val POPULAR_CITIES = listOf(
    WorldClockEntity(cityName = "London", countryName = "United Kingdom", timeZoneId = "Europe/London"),
    WorldClockEntity(cityName = "New York", countryName = "United States", timeZoneId = "America/New_York"),
    WorldClockEntity(cityName = "Tokyo", countryName = "Japan", timeZoneId = "Asia/Tokyo"),
    WorldClockEntity(cityName = "Paris", countryName = "France", timeZoneId = "Europe/Paris"),
    WorldClockEntity(cityName = "Sydney", countryName = "Australia", timeZoneId = "Australia/Sydney"),
    WorldClockEntity(cityName = "Dubai", countryName = "United Arab Emirates", timeZoneId = "Asia/Dubai"),
    WorldClockEntity(cityName = "Singapore", countryName = "Singapore", timeZoneId = "Asia/Singapore"),
    WorldClockEntity(cityName = "Hong Kong", countryName = "China", timeZoneId = "Asia/Hong_Kong"),
    WorldClockEntity(cityName = "Berlin", countryName = "Germany", timeZoneId = "Europe/Berlin"),
    WorldClockEntity(cityName = "Seoul", countryName = "South Korea", timeZoneId = "Asia/Seoul"),
    WorldClockEntity(cityName = "Istanbul", countryName = "Türkiye", timeZoneId = "Europe/Istanbul"),
    WorldClockEntity(cityName = "Los Angeles", countryName = "United States", timeZoneId = "America/Los_Angeles"),
    WorldClockEntity(cityName = "Toronto", countryName = "Canada", timeZoneId = "America/Toronto"),
    WorldClockEntity(cityName = "Mumbai", countryName = "India", timeZoneId = "Asia/Kolkata"),
    WorldClockEntity(cityName = "Delhi", countryName = "India", timeZoneId = "Asia/Kolkata"),
    WorldClockEntity(cityName = "Cairo", countryName = "Egypt", timeZoneId = "Africa/Cairo"),
    WorldClockEntity(cityName = "Bangkok", countryName = "Thailand", timeZoneId = "Asia/Bangkok"),
    WorldClockEntity(cityName = "Auckland", countryName = "New Zealand", timeZoneId = "Pacific/Auckland"),
    WorldClockEntity(cityName = "Honolulu", countryName = "Hawaii, US", timeZoneId = "Pacific/Honolulu"),
    WorldClockEntity(cityName = "Beijing", countryName = "China", timeZoneId = "Asia/Shanghai"),
    WorldClockEntity(cityName = "Shanghai", countryName = "China", timeZoneId = "Asia/Shanghai"),
    WorldClockEntity(cityName = "Chicago", countryName = "United States", timeZoneId = "America/Chicago"),
    WorldClockEntity(cityName = "San Francisco", countryName = "United States", timeZoneId = "America/Los_Angeles"),
    WorldClockEntity(cityName = "Rome", countryName = "Italy", timeZoneId = "Europe/Rome"),
    WorldClockEntity(cityName = "Madrid", countryName = "Spain", timeZoneId = "Europe/Madrid"),
    WorldClockEntity(cityName = "Amsterdam", countryName = "Netherlands", timeZoneId = "Europe/Amsterdam"),
    WorldClockEntity(cityName = "Zurich", countryName = "Switzerland", timeZoneId = "Europe/Zurich"),
    WorldClockEntity(cityName = "Vienna", countryName = "Austria", timeZoneId = "Europe/Vienna"),
    WorldClockEntity(cityName = "Stockholm", countryName = "Sweden", timeZoneId = "Europe/Stockholm"),
    WorldClockEntity(cityName = "Dublin", countryName = "Ireland", timeZoneId = "Europe/Dublin"),
    WorldClockEntity(cityName = "Moscow", countryName = "Russia", timeZoneId = "Europe/Moscow"),
    WorldClockEntity(cityName = "Riyadh", countryName = "Saudi Arabia", timeZoneId = "Asia/Riyadh"),
    WorldClockEntity(cityName = "Jakarta", countryName = "Indonesia", timeZoneId = "Asia/Jakarta"),
    WorldClockEntity(cityName = "Kuala Lumpur", countryName = "Malaysia", timeZoneId = "Asia/Kuala_Lumpur"),
    WorldClockEntity(cityName = "Manila", countryName = "Philippines", timeZoneId = "Asia/Manila"),
    WorldClockEntity(cityName = "São Paulo", countryName = "Brazil", timeZoneId = "America/Sao_Paulo"),
    WorldClockEntity(cityName = "Buenos Aires", countryName = "Argentina", timeZoneId = "America/Argentina/Buenos_Aires"),
    WorldClockEntity(cityName = "Mexico City", countryName = "Mexico", timeZoneId = "America/Mexico_City"),
    WorldClockEntity(cityName = "Vancouver", countryName = "Canada", timeZoneId = "America/Vancouver"),
    WorldClockEntity(cityName = "Johannesburg", countryName = "South Africa", timeZoneId = "Africa/Johannesburg"),
    WorldClockEntity(cityName = "Nairobi", countryName = "Kenya", timeZoneId = "Africa/Nairobi"),
    WorldClockEntity(cityName = "Athens", countryName = "Greece", timeZoneId = "Europe/Athens"),
    WorldClockEntity(cityName = "Doha", countryName = "Qatar", timeZoneId = "Asia/Qatar"),
    WorldClockEntity(cityName = "Tashkent", countryName = "Uzbekistan", timeZoneId = "Asia/Tashkent"),
    WorldClockEntity(cityName = "Anchorage", countryName = "Alaska, US", timeZoneId = "America/Anchorage"),
    WorldClockEntity(cityName = "Melbourne", countryName = "Australia", timeZoneId = "Australia/Melbourne"),
    WorldClockEntity(cityName = "Santiago", countryName = "Chile", timeZoneId = "America/Santiago"),
    WorldClockEntity(cityName = "Lima", countryName = "Peru", timeZoneId = "America/Lima"),
    WorldClockEntity(cityName = "Prague", countryName = "Czech Republic", timeZoneId = "Europe/Prague")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(
    worldClocks: List<WorldClockEntity>,
    onAddWorldClock: (WorldClockEntity) -> Unit,
    onDeleteWorldClock: (WorldClockEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(Instant.now()) }

    // Live clock ticker
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Instant.now()
            delay(1000)
        }
    }

    val localZone = ZoneId.systemDefault()
    val localZdt = ZonedDateTime.ofInstant(currentTime, localZone)

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
                        text = "World Clock",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Local Time: ${localZdt.format(DateTimeFormatter.ofPattern("hh:mm a"))} (${localZone.id})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.AddLocation, contentDescription = "Add City")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (worldClocks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No cities added yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Tap + to track time around the globe",
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
                    items(worldClocks, key = { it.id }) { clock ->
                        WorldClockCard(
                            clock = clock,
                            now = currentTime,
                            localZone = localZone,
                            onDelete = { onDeleteWorldClock(clock) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddCityDialog(
                existingClocks = worldClocks,
                onDismiss = { showAddDialog = false },
                onCitySelected = { city ->
                    onAddWorldClock(city)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun WorldClockCard(
    clock: WorldClockEntity,
    now: Instant,
    localZone: ZoneId,
    onDelete: () -> Unit
) {
    val targetZone = try {
        ZoneId.of(clock.timeZoneId)
    } catch (e: Exception) {
        ZoneId.systemDefault()
    }

    val targetZdt = ZonedDateTime.ofInstant(now, targetZone)
    val localZdt = ZonedDateTime.ofInstant(now, localZone)

    val hour = targetZdt.hour
    val isDay = hour in 6..18

    // Calculate hour difference relative to local
    val diffHours = (targetZdt.offset.totalSeconds - localZdt.offset.totalSeconds) / 3600
    val diffText = when {
        diffHours > 0 -> "+$diffHours hrs"
        diffHours < 0 -> "$diffHours hrs"
        else -> "Same time"
    }

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm")
    val amPmFormatter = DateTimeFormatter.ofPattern("a")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = if (isDay) Color(0xFFFFB300) else Color(0xFF7E57C2),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = clock.cityName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${clock.countryName} • $diffText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = targetZdt.format(dateFormatter),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mini Analog Clock
                MiniAnalogClock(
                    hour = targetZdt.hour,
                    minute = targetZdt.minute,
                    second = targetZdt.second,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = targetZdt.format(timeFormatter),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = targetZdt.format(amPmFormatter),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete City",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniAnalogClock(
    hour: Int,
    minute: Int,
    second: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Face
        drawCircle(
            color = outlineColor.copy(alpha = 0.2f),
            radius = radius,
            center = center
        )

        // Hour Hand
        val hourAngle = Math.toRadians(((hour % 12 + minute / 60f) * 30 - 90).toDouble())
        val hourLength = radius * 0.5f
        val hourEnd = Offset(
            center.x + (hourLength * cos(hourAngle)).toFloat(),
            center.y + (hourLength * sin(hourAngle)).toFloat()
        )
        drawLine(
            color = primaryColor,
            start = center,
            end = hourEnd,
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Minute Hand
        val minAngle = Math.toRadians((minute * 6 - 90).toDouble())
        val minLength = radius * 0.75f
        val minEnd = Offset(
            center.x + (minLength * cos(minAngle)).toFloat(),
            center.y + (minLength * sin(minAngle)).toFloat()
        )
        drawLine(
            color = primaryColor,
            start = center,
            end = minEnd,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Center Dot
        drawCircle(color = primaryColor, radius = 3f, center = center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCityDialog(
    existingClocks: List<WorldClockEntity>,
    onDismiss: () -> Unit,
    onCitySelected: (WorldClockEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = remember(searchQuery) {
        POPULAR_CITIES.filter {
            it.cityName.contains(searchQuery, ignoreCase = true) ||
                    it.countryName.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add World City",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search city or country...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredCities) { city ->
                        val isAlreadyAdded = existingClocks.any { it.cityName == city.cityName }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAlreadyAdded)
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            onClick = {
                                if (!isAlreadyAdded) {
                                    onCitySelected(city)
                                }
                            },
                            enabled = !isAlreadyAdded,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(city.cityName, fontWeight = FontWeight.Bold)
                                    Text(city.countryName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                                if (isAlreadyAdded) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                } else {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}
