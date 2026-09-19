package com.socialvibe.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.socialvibe.app.model.UserStatus

// The three real Windows XP "Luna" color schemes, recreated 1:1 by hex —
// this is the whole visual identity of the app, not a decoration on top
// of it. "go"/"goDeep" are the logon screen's green arrow button, which
// stayed green across all three real schemes.
enum class AppScheme(val label: String) {
    BLUE("Blue"),
    OLIVE("Olive Green"),
    SILVER("Silver")
}

data class SchemeColors(
    val deep: Color,
    val mid: Color,
    val light: Color,
    val glow: Color,
    val go: Color,
    val goDeep: Color
)

val BlueScheme = SchemeColors(
    deep = Color(0xFF0A246A),
    mid = Color(0xFF2A6FDB),
    light = Color(0xFF3D95FF),
    glow = Color(0xFFBFE0FF),
    go = Color(0xFF4CC552),
    goDeep = Color(0xFF2E9E3C)
)

val OliveScheme = SchemeColors(
    deep = Color(0xFF45521F),
    mid = Color(0xFF7A8C4A),
    light = Color(0xFFABC073),
    glow = Color(0xFFEAF3CE),
    go = Color(0xFF8FCB3E),
    goDeep = Color(0xFF5E9B22)
)

val SilverScheme = SchemeColors(
    deep = Color(0xFF333A4D),
    mid = Color(0xFF6B7286),
    light = Color(0xFFA3ABC2),
    glow = Color(0xFFEDEFF6),
    go = Color(0xFF4CC552),
    goDeep = Color(0xFF2E9E3C)
)

fun schemeColorsFor(scheme: AppScheme): SchemeColors = when (scheme) {
    AppScheme.BLUE -> BlueScheme
    AppScheme.OLIVE -> OliveScheme
    AppScheme.SILVER -> SilverScheme
}

// Modern, scheme-independent surface tokens — the deliberate other half of
// the fusion: retro chrome around flat, neutral, current-day surfaces.
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceAlt = Color(0xFFF4F5F8)
val InkStrong = Color(0xFF16213D)
val InkMuted = Color(0xFF7A8194)
val BorderSoft = Color(0xFFE7E9EF)

val StatusOnline = Color(0xFF4CC552)
val StatusAway = Color(0xFFE8B400)
val StatusBusy = Color(0xFFE8574A)
val StatusOffline = Color(0xFF9AA0AE)

fun colorForStatus(status: UserStatus): Color = when (status) {
    UserStatus.ONLINE -> StatusOnline
    UserStatus.AWAY -> StatusAway
    UserStatus.BUSY -> StatusBusy
    UserStatus.OFFLINE -> StatusOffline
}

val avatarPalette = listOf(
    Color(0xFF5B7FDE), Color(0xFFDE8A5B), Color(0xFF5BDE99),
    Color(0xFFDE5B94), Color(0xFF9C5BDE), Color(0xFFDEC55B)
)
