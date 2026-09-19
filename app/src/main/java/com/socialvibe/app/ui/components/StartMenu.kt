package com.socialvibe.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.model.UserStatus
import com.socialvibe.app.ui.theme.AppScheme
import com.socialvibe.app.ui.theme.BorderSoft
import com.socialvibe.app.ui.theme.InkMuted
import com.socialvibe.app.ui.theme.InkStrong
import com.socialvibe.app.ui.theme.SchemeColors
import com.socialvibe.app.ui.theme.StatusBusy
import com.socialvibe.app.ui.theme.SurfaceAlt
import com.socialvibe.app.ui.theme.SurfaceLight
import com.socialvibe.app.ui.theme.colorForStatus
import com.socialvibe.app.ui.theme.schemeColorsFor

private enum class SettingRow { STATUS, APPEARANCE, NOTIFICATIONS, PRIVACY, ABOUT }

// Every setting the app has lives here — deliberately, per the brief:
// this replaces a separate Settings tab/screen entirely.
@Composable
fun StartMenu(
    visible: Boolean,
    myName: String,
    myStatus: UserStatus,
    onStatusChange: (UserStatus) -> Unit,
    scheme: AppScheme,
    colors: SchemeColors,
    onSchemeChange: (AppScheme) -> Unit,
    onContacts: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onExit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedRow by remember { mutableStateOf<SettingRow?>(null) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 370.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceLight.copy(alpha = 0.97f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(colors.deep, colors.mid)))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(name = myName, size = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(myName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    Text(myStatus.label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(10.dp)) {
                    SectionLabel("Quick actions")
                    MenuItem(icon = "👥", iconBg = Color(0xFFEAFBE9), title = "Contacts", sub = "Your people") {
                        onDismiss(); onContacts()
                    }
                    MenuItem(icon = "🔄", iconBg = Color(0xFFE7F0FF), title = "Refresh", sub = "Sync from server") {
                        onDismiss(); onRefresh()
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Black.copy(alpha = 0.03f))
                        .padding(10.dp)
                ) {
                    SectionLabel("Settings")

                    MenuItem(
                        icon = "🟢",
                        iconBg = Color(0xFFEEF0FF),
                        title = "Profile & Status",
                        sub = myStatus.label,
                        onClick = { expandedRow = toggled(expandedRow, SettingRow.STATUS) }
                    )
                    AnimatedVisibility(expandedRow == SettingRow.STATUS) {
                        Row(
                            modifier = Modifier.padding(start = 40.dp, top = 2.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UserStatus.values().forEach { option ->
                                StatusChip(option = option, selected = option == myStatus, onClick = { onStatusChange(option) })
                            }
                        }
                    }

                    MenuItem(
                        icon = "🎨",
                        iconBg = Color(0xFFF1E9FF),
                        title = "Appearance",
                        sub = scheme.label,
                        onClick = { expandedRow = toggled(expandedRow, SettingRow.APPEARANCE) }
                    )
                    AnimatedVisibility(expandedRow == SettingRow.APPEARANCE) {
                        Row(
                            modifier = Modifier.padding(start = 40.dp, top = 2.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppScheme.values().forEach { option ->
                                SchemeSwatch(option = option, selected = option == scheme, onClick = { onSchemeChange(option) })
                            }
                        }
                    }

                    MenuItem(
                        icon = "🔔",
                        iconBg = Color(0xFFE7F7F5),
                        title = "Notifications",
                        sub = "No FCM, ever",
                        onClick = { expandedRow = toggled(expandedRow, SettingRow.NOTIFICATIONS) }
                    )
                    InfoNote(
                        expandedRow == SettingRow.NOTIFICATIONS,
                        "A background connection on your phone delivers messages — no Google services involved, by design."
                    )

                    MenuItem(
                        icon = "🛡️",
                        iconBg = Color(0xFFFFEEF0),
                        title = "Privacy & Security",
                        sub = "Your server, your data",
                        onClick = { expandedRow = toggled(expandedRow, SettingRow.PRIVACY) }
                    )
                    InfoNote(
                        expandedRow == SettingRow.PRIVACY,
                        "Messages go straight to your own server. Nothing passes through a third party."
                    )

                    MenuItem(
                        icon = "ℹ️",
                        iconBg = Color(0xFFF4F4F4),
                        title = "About",
                        sub = "SocialVibe Lite",
                        onClick = { expandedRow = toggled(expandedRow, SettingRow.ABOUT) }
                    )
                    InfoNote(
                        expandedRow == SettingRow.ABOUT,
                        "SocialVibe · Lite Edition — a private messenger with its own backend, styled after Windows XP."
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLight)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FooterButton(
                    label = "Exit",
                    bg = SurfaceAlt,
                    fg = InkStrong,
                    modifier = Modifier.weight(1f),
                    onClick = { onDismiss(); onExit() }
                )
                FooterButton(
                    label = "Log Out",
                    bg = Color(0xFFFFE9E5),
                    fg = StatusBusy,
                    modifier = Modifier.weight(1f),
                    onClick = { onDismiss(); onLogout() }
                )
            }
        }
    }
}

private fun toggled(current: SettingRow?, target: SettingRow): SettingRow? = if (current == target) null else target

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold,
        color = InkMuted,
        modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun MenuItem(icon: String, iconBg: Color, title: String, sub: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = InkStrong)
            Text(sub, fontSize = 10.sp, color = InkMuted)
        }
    }
}

@Composable
private fun InfoNote(visible: Boolean, text: String) {
    AnimatedVisibility(visible) {
        Text(
            text = text,
            fontSize = 10.5.sp,
            color = InkMuted,
            lineHeight = 14.sp,
            modifier = Modifier.padding(start = 40.dp, end = 8.dp, top = 2.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun StatusChip(option: UserStatus, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) InkStrong else SurfaceLight)
            .border(if (selected) 0.dp else 1.dp, BorderSoft, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(colorForStatus(option)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(option.label, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else InkStrong)
    }
}

@Composable
private fun SchemeSwatch(option: AppScheme, selected: Boolean, onClick: () -> Unit) {
    val swatchColors = schemeColorsFor(option)
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(swatchColors.deep, swatchColors.light)))
            .border(if (selected) 2.dp else 0.dp, InkStrong, CircleShape)
            .clickable { onClick() }
    )
}

@Composable
private fun FooterButton(label: String, bg: Color, fg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}
