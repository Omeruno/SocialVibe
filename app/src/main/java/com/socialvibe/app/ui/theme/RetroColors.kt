package com.socialvibe.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.socialvibe.app.model.UserStatus

// Windows XP "Luna" palette + ICQ accent colors
val XpBlueDark = Color(0xFF0A246A)
val XpBlueTitle = Color(0xFF2A6FDB)
val XpBlueLight = Color(0xFF3D95FF)
val XpSilver = Color(0xFFECE9D8)
val XpSilverDark = Color(0xFFD4D0C8)
val XpGreenOnline = Color(0xFF4CC552)
val XpGrayOffline = Color(0xFF9A9A9A)
val XpStatusAway = Color(0xFFE8B400)
val XpStatusBusy = Color(0xFFE8574A)
val XpBubbleMine = Color(0xFFB7D6FF)
val XpBubbleTheirs = Color(0xFFFFFFFF)
val XpTextDark = Color(0xFF1A1A1A)

fun colorForStatus(status: UserStatus): Color = when (status) {
    UserStatus.ONLINE -> XpGreenOnline
    UserStatus.AWAY -> XpStatusAway
    UserStatus.BUSY -> XpStatusBusy
    UserStatus.OFFLINE -> XpGrayOffline
}
