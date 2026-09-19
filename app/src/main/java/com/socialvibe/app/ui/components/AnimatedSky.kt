package com.socialvibe.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.socialvibe.app.ui.theme.SchemeColors

// A slowly "breathing" version of the XP logon bitmap: same fixed gradient
// stops, just gently drifting instead of a static image. The stop math
// keeps every stop strictly increasing for drift in [0,1] so the gradient
// never inverts mid-animation.
@Composable
fun AnimatedSky(colors: SchemeColors, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sky-drift")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sky-drift-value"
    )

    val stops = arrayOf(
        0f to colors.deep,
        (0.45f + drift * 0.06f) to colors.mid,
        (0.68f + drift * 0.04f) to colors.glow,
        0.85f to colors.mid,
        1f to colors.deep
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(*stops))
    )
}
