package com.socialvibe.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.ui.theme.XpBlueTitle
import com.socialvibe.app.ui.theme.XpSilverDark

enum class DockTab(val label: String, val emoji: String) {
    CONTACTS("Contacts", "👥"),
    SETTINGS("Settings", "⚙️")
}

@Composable
fun BottomDock(selected: DockTab, onSelect: (DockTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(XpSilverDark),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DockTab.values().forEach { tab ->
            DockItem(tab = tab, isSelected = tab == selected, onClick = { onSelect(tab) })
        }
    }
}

@Composable
private fun DockItem(tab: DockTab, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val highlightScale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1f, label = "dock-highlight")

    Box(
        modifier = Modifier
            .pressScale(interactionSource)
            .scale(highlightScale)
            .clip(RoundedCornerShape(10.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .background(if (isSelected) XpBlueTitle else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${tab.emoji}  ${tab.label}",
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
