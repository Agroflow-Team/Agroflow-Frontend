package com.agroflow.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AgroFlowLightColorScheme = lightColorScheme(
    primary = AgroFlowGreen,
    onPrimary = AppleTextLight,
    background = AgroFlowBackground,
    onBackground = AppleDarkGrey,
    surface = AgroFlowSurface,
    onSurface = AppleDarkGrey,
    surfaceVariant = AppleLightGrey,
    onSurfaceVariant = AppleTextSecondary,
    outline = AppleBorderGrey,
    error = AppleRed,
    onError = AppleTextLight
)

private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleGreen,
    onPrimary = AppleBlack,
    primaryContainer = AppleGreenDark,
    onPrimaryContainer = AppleGreen,

    secondary = AppleBlue,
    onSecondary = AppleTextLight,
    secondaryContainer = AppleBlueDark,
    onSecondaryContainer = AppleBlue,

    tertiary = ApplePurple,
    onTertiary = AppleTextLight,
    tertiaryContainer = ApplePurpleDark,
    onTertiaryContainer = ApplePurple,

    background = AppleBlack,
    onBackground = AppleTextLight,

    surface = AppleBlack,
    onSurface = AppleTextLight,

    surfaceVariant = AppleDarkGrey,
    onSurfaceVariant = AppleTextSecondary,

    outline = AppleBorderGrey,
    error = AppleRed,
    onError = AppleTextLight
)

@Composable
fun AgroFlowTheme(
    darkTheme: Boolean = false, // Cambiado a false para activar el tema claro beige
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AppleDarkColorScheme else AgroFlowLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}