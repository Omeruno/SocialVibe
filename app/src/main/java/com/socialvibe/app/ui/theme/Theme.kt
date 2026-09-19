package com.socialvibe.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SocialVibeTheme(scheme: AppScheme = AppScheme.BLUE, content: @Composable () -> Unit) {
    val colors = schemeColorsFor(scheme)
    val materialColors = lightColorScheme(
        primary = colors.mid,
        secondary = colors.go,
        background = SurfaceAlt,
        surface = SurfaceLight,
        onPrimary = Color.White,
        onBackground = InkStrong,
        onSurface = InkStrong
    )
    MaterialTheme(
        colorScheme = materialColors,
        typography = Typography,
        content = content
    )
}
