package com.l8r2gether.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun LtTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LtColorScheme,
        typography = LtTypography,
        shapes = LtShapes,
        content = content,
    )
}
