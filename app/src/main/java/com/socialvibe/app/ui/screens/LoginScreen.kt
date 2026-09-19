package com.socialvibe.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
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
import com.socialvibe.app.ui.components.AnimatedSky
import com.socialvibe.app.ui.components.BrandFlag
import com.socialvibe.app.ui.components.GlassTextField
import com.socialvibe.app.ui.components.GoButton
import com.socialvibe.app.ui.theme.AppScheme
import com.socialvibe.app.ui.theme.SchemeColors
import com.socialvibe.app.ui.theme.StatusBusy
import com.socialvibe.app.ui.theme.schemeColorsFor

private enum class LoginTile { SIGN_IN, REGISTER }

// The XP Welcome Screen, recreated: same fixed logon-bitmap-style gradient
// (now gently animated), the classic tile-expands-to-reveal-password
// interaction, and the round green "go" button. Adapted for a real
// server-backed login: a tile expands to username+password rather than
// just a password field, since there's no local "known account" here.
@Composable
fun LoginScreen(
    colors: SchemeColors,
    scheme: AppScheme,
    onSchemeChange: (AppScheme) -> Unit,
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf<LoginTile?>(null) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        if (error != null) busy = false
    }

    fun openTile(tile: LoginTile) {
        expanded = if (expanded == tile) null else tile
        username = ""
        password = ""
        busy = false
    }

    fun submit() {
        if (username.isBlank() || password.isBlank() || busy) return
        busy = true
        when (expanded) {
            LoginTile.SIGN_IN -> onLogin(username, password)
            LoginTile.REGISTER -> onRegister(username, password)
            null -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedSky(colors = colors)

        Column(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandFlag()
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("SocialVibe", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                    Text(
                        "LITE EDITION",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 360.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.13f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                        .padding(22.dp)
                ) {
                    Text(
                        "To begin, sign in or create an account",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LoginTileRow(
                        label = "Sign in",
                        sub = "Existing account",
                        glyph = "🔑",
                        expanded = expanded == LoginTile.SIGN_IN,
                        onClick = { openTile(LoginTile.SIGN_IN) }
                    )
                    AnimatedVisibility(
                        visible = expanded == LoginTile.SIGN_IN,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        UnlockFields(username, { username = it }, password, { password = it }, busy, colors, ::submit)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LoginTileRow(
                        label = "Create account",
                        sub = "New to SocialVibe",
                        glyph = "+",
                        ghost = true,
                        expanded = expanded == LoginTile.REGISTER,
                        onClick = { openTile(LoginTile.REGISTER) }
                    )
                    AnimatedVisibility(
                        visible = expanded == LoginTile.REGISTER,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        UnlockFields(username, { username = it }, password, { password = it }, busy, colors, ::submit)
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = StatusBusy, fontSize = 11.5.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "SocialVibe · Lite Edition",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppScheme.values().forEach { s ->
                        SchemeDot(scheme = s, selected = s == scheme, onClick = { onSchemeChange(s) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginTileRow(
    label: String,
    sub: String,
    glyph: String,
    expanded: Boolean,
    onClick: () -> Unit,
    ghost: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = if (expanded) 0.16f else 0.08f))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .then(
                    if (ghost) {
                        Modifier.border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    } else {
                        Modifier.background(Color(0xFF5B7FDE))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (ghost) 20.sp else 17.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
            Text(sub, color = Color.White.copy(alpha = 0.7f), fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun UnlockFields(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    busy: Boolean,
    colors: SchemeColors,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        GlassTextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = "Username",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Password",
                isPassword = true,
                modifier = Modifier.weight(1f)
            )
            GoButton(busy = busy, colors = colors, onClick = onSubmit)
        }
    }
}

@Composable
private fun SchemeDot(scheme: AppScheme, selected: Boolean, onClick: () -> Unit) {
    val dotColors = schemeColorsFor(scheme)
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(dotColors.deep, dotColors.light)))
            .border(if (selected) 2.dp else 0.dp, Color.White, CircleShape)
            .clickable { onClick() }
    )
}
