package com.socialvibe.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.ui.theme.XpBlueDark
import com.socialvibe.app.ui.theme.XpBlueLight
import com.socialvibe.app.ui.theme.XpBlueTitle
import com.socialvibe.app.ui.theme.XpSilver
import com.socialvibe.app.ui.theme.XpStatusBusy
import com.socialvibe.app.ui.theme.XpTextDark

@Composable
fun LoginScreen(
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(XpBlueDark, XpBlueTitle, XpBlueLight))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(XpSilver)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "SocialVibe", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = XpBlueDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isRegisterMode) "Create an account" else "Welcome back",
                fontSize = 13.sp,
                color = XpTextDark
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = XpStatusBusy, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isRegisterMode) onRegister(username, password) else onLogin(username, password)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegisterMode) "Create account" else "Sign in")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                Text(if (isRegisterMode) "Already have an account? Sign in" else "No account? Create one")
            }
        }
    }
}
