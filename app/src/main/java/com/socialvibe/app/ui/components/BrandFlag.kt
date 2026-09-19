package com.socialvibe.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// SocialVibe's own mark: four colored tiles in a 2x2 grid, evoking the
// shape-grammar of the real Windows flag without reusing it — same idea,
// our own colors (all four already used elsewhere in the app's palette).
@Composable
fun BrandFlag(size: Dp = 38.dp, modifier: Modifier = Modifier) {
    val gap = size * 0.08f
    val tile = (size - gap) / 2
    Column(modifier = modifier.size(size)) {
        Row {
            FlagTile(Color(0xFFFF8A3D), tile)
            Spacer(modifier = Modifier.width(gap))
            FlagTile(Color(0xFF4CC552), tile)
        }
        Spacer(modifier = Modifier.height(gap))
        Row {
            FlagTile(Color(0xFF5B7FDE), tile)
            Spacer(modifier = Modifier.width(gap))
            FlagTile(Color(0xFFDE5B94), tile)
        }
    }
}

@Composable
private fun FlagTile(color: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.18f))
            .background(color)
    )
}
