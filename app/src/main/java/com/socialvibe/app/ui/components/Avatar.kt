package com.socialvibe.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private val avatarPalette = listOf(
    Color(0xFF5B7FDE), Color(0xFFDE8A5B), Color(0xFF5BDE99),
    Color(0xFFDE5B94), Color(0xFF9C5BDE), Color(0xFFDEC55B)
)

@Composable
fun Avatar(name: String, size: Dp = 36.dp, modifier: Modifier = Modifier) {
    val color = avatarPalette[abs(name.hashCode()) % avatarPalette.size]
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value / 2).sp
        )
    }
}
