package com.l8r2gether.app.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LtSurface = Color(0xFFFFF8F6)
val LtPrimary = Color(0xFF7B5455)
val LtOnPrimary = Color(0xFFFFF8F6)
val LtSecondary = Color(0xFF5B614F)
val LtOnSecondary = Color(0xFFFFF8F6)
val LtContainerLow = Color(0xFFFDF1ED)
val LtAccent = Color(0xFFD4A5A5)
val LtOnBackground = Color(0xFF5C4545)
val LtOnSurfaceVariant = Color(0xFF7A6565)
val LtRailSelected = Color(0xFFE6C8C4)

val LtColorScheme = lightColorScheme(
    primary = LtPrimary,
    onPrimary = LtOnPrimary,
    secondary = LtSecondary,
    onSecondary = LtOnSecondary,
    tertiary = LtAccent,
    background = LtSurface,
    onBackground = LtOnBackground,
    surface = LtSurface,
    onSurface = LtOnBackground,
    surfaceContainerLow = LtContainerLow,
    onSurfaceVariant = LtOnSurfaceVariant,
)
