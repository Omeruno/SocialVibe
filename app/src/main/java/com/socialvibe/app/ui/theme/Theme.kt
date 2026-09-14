package com.socialvibe.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SocialVibeColorScheme = lightColorScheme(
    primary = XpBlueTitle,
    secondary = XpGreenOnline,
    background = XpSilver,
    surface = XpSilverDark,
    onPrimary = Color.White,
    onBackground = XpTextDark,
    onSurface = XpTextDark
)

@Composable
fun SocialVibeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SocialVibeColorScheme,
        typography = Typography,
        content = content
    )
}
