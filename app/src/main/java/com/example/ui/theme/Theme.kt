package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = M3ExpressiveDarkPrimary,
    onPrimary = M3ExpressiveDarkOnPrimary,
    primaryContainer = M3ExpressiveDarkPrimaryContainer,
    onPrimaryContainer = M3ExpressiveDarkOnPrimaryContainer,
    secondary = M3ExpressiveDarkSecondary,
    onSecondary = M3ExpressiveDarkOnSecondary,
    secondaryContainer = M3ExpressiveDarkSecondaryContainer,
    onSecondaryContainer = M3ExpressiveDarkOnSecondaryContainer,
    tertiary = M3ExpressiveDarkTertiary,
    onTertiary = M3ExpressiveDarkOnTertiary,
    tertiaryContainer = M3ExpressiveDarkTertiaryContainer,
    onTertiaryContainer = M3ExpressiveDarkOnTertiaryContainer,
    background = M3ExpressiveDarkBackground,
    onBackground = M3ExpressiveDarkOnBackground,
    surface = M3ExpressiveDarkSurface,
    onSurface = M3ExpressiveDarkOnSurface,
    surfaceVariant = M3ExpressiveDarkSurfaceVariant,
    onSurfaceVariant = M3ExpressiveDarkOnSurfaceVariant,
    surfaceContainerLowest = M3ExpressiveDarkSurfaceContainerLowest,
    surfaceContainerLow = M3ExpressiveDarkSurfaceContainerLow,
    surfaceContainer = M3ExpressiveDarkSurfaceContainer,
    surfaceContainerHigh = M3ExpressiveDarkSurfaceContainerHigh,
    surfaceContainerHighest = M3ExpressiveDarkSurfaceContainerHighest,
    outline = M3ExpressiveDarkOutline,
    outlineVariant = M3ExpressiveDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = M3ExpressiveLightPrimary,
    onPrimary = M3ExpressiveLightOnPrimary,
    primaryContainer = M3ExpressiveLightPrimaryContainer,
    onPrimaryContainer = M3ExpressiveLightOnPrimaryContainer,
    secondary = M3ExpressiveLightSecondary,
    onSecondary = M3ExpressiveLightOnSecondary,
    secondaryContainer = M3ExpressiveLightSecondaryContainer,
    onSecondaryContainer = M3ExpressiveLightOnSecondaryContainer,
    tertiary = M3ExpressiveLightTertiary,
    onTertiary = M3ExpressiveLightOnTertiary,
    tertiaryContainer = M3ExpressiveLightTertiaryContainer,
    onTertiaryContainer = M3ExpressiveLightOnTertiaryContainer,
    background = M3ExpressiveLightBackground,
    onBackground = M3ExpressiveLightOnBackground,
    surface = M3ExpressiveLightSurface,
    onSurface = M3ExpressiveLightOnSurface,
    surfaceVariant = M3ExpressiveLightSurfaceVariant,
    onSurfaceVariant = M3ExpressiveLightOnSurfaceVariant,
    surfaceContainerLowest = M3ExpressiveLightSurfaceContainerLowest,
    surfaceContainerLow = M3ExpressiveLightSurfaceContainerLow,
    surfaceContainer = M3ExpressiveLightSurfaceContainer,
    surfaceContainerHigh = M3ExpressiveLightSurfaceContainerHigh,
    surfaceContainerHighest = M3ExpressiveLightSurfaceContainerHighest,
    outline = M3ExpressiveLightOutline,
    outlineVariant = M3ExpressiveLightOutlineVariant
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "DARK",
    dynamicColor: Boolean = false, // Use expressive palette for distinct design
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        "SYSTEM" -> isSystemDark
        else -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

