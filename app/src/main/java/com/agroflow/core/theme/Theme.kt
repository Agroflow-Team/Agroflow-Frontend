package com.agroflow.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
    darkTheme: Boolean = true, // Force Dark Mode for liquid crystal effect
    dynamicColor: Boolean = false, // Disable material you to keep the iOS look
    content: @Composable () -> Unit
) {
    val colorScheme = AppleDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}