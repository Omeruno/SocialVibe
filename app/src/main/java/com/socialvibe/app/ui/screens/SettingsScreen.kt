package com.socialvibe.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.ui.components.XpTitleBar
import com.socialvibe.app.ui.theme.XpSilver

@Composable
fun SettingsScreen() {
    var classicSounds by remember { mutableStateOf(true) }
    var showEmoticons by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(XpSilver)) {
        XpTitleBar(title = "Settings")
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "SocialVibe", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "version 0.1 — lite edition", fontSize = 12.sp)
            Spacer(modifier = Modifier.height(20.dp))
            SettingRow(label = "Classic ICQ sounds", checked = classicSounds, onCheckedChange = { classicSounds = it })
            SettingRow(label = "Show emoticons", checked = showEmoticons, onCheckedChange = { showEmoticons = it })
        }
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label)
    }
}
