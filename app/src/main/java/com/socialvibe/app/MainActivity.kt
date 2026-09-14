package com.socialvibe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.socialvibe.app.data.MockData
import com.socialvibe.app.model.Contact
import com.socialvibe.app.ui.screens.ChatScreen
import com.socialvibe.app.ui.screens.ContactListScreen
import com.socialvibe.app.ui.theme.SocialVibeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SocialVibeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SocialVibeApp()
                }
            }
        }
    }
}

private sealed class Screen {
    data object Contacts : Screen()
    data class Chat(val contact: Contact) : Screen()
}

@Composable
private fun SocialVibeApp() {
    var screen by remember { mutableStateOf<Screen>(Screen.Contacts) }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            (scaleIn(initialScale = 0.92f, animationSpec = tween(220)) + fadeIn(tween(220)))
                .togetherWith(scaleOut(targetScale = 1.05f, animationSpec = tween(180)) + fadeOut(tween(180)))
        },
        label = "screen-transition"
    ) { targetScreen ->
        when (targetScreen) {
            is Screen.Contacts -> ContactListScreen(
                onContactClick = { contact -> screen = Screen.Chat(contact) }
            )
            is Screen.Chat -> ChatScreen(
                contact = targetScreen.contact,
                initialMessages = MockData.conversations[targetScreen.contact.id] ?: emptyList(),
                onBack = { screen = Screen.Contacts }
            )
        }
    }
}
