package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberColorScheme =
  darkColorScheme(
    primary = MatrixGreenPrimary,
    onPrimary = CyberBlack,
    primaryContainer = MatrixGreenDark,
    onPrimaryContainer = MatrixNeonGreen,
    secondary = MatrixGreenSecondary,
    onSecondary = CyberBlack,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = MatrixGreenPrimary,
    tertiary = CyberCyan,
    onTertiary = CyberBlack,
    background = CyberBlack,
    onBackground = CyberTextPrimary,
    surface = CyberDarkSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberCardBorder,
    outlineVariant = MatrixGreenDark,
    error = CyberRed,
    onError = CyberBlack,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = CyberColorScheme,
    typography = Typography,
    content = content,
  )
}

