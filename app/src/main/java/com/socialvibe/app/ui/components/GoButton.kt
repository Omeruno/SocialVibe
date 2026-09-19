package com.socialvibe.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.ui.theme.SchemeColors

// The one non-negotiable XP logon detail: the round green "go" button.
// Text glyph instead of an icon font/vector on purpose — see XpTitleBar's
// back arrow from the first pass of this app for why (avoids depending on
// material-icons-core, which isn't guaranteed present in this project).
@Composable
fun GoButton(
    busy: Boolean,
    colors: SchemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(44.dp)
            .pressScale(interactionSource)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFFB6F3BD), colors.go, colors.goDeep)))
            .clickable(interactionSource = interactionSource, indication = null, enabled = !busy) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text = "→", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}
