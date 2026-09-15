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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import com.socialvibe.app.data.MockData
import com.socialvibe.app.model.Contact
import com.socialvibe.app.model.UserStatus
import com.socialvibe.app.ui.components.BottomDock
import com.socialvibe.app.ui.components.DockTab
import com.socialvibe.app.ui.screens.ChatScreen
import com.socialvibe.app.ui.screens.ContactListScreen
import com.socialvibe.app.ui.screens.SettingsScreen
import com.socialvibe.app.ui.screens.SplashScreen
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
    data object Splash : Screen()
    data object Home : Screen()
    data class Chat(val contact: Contact) : Screen()
}

@Composable
private fun SocialVibeApp() {
    var screen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var tab by remember { mutableStateOf(DockTab.CONTACTS) }
    var myStatus by remember { mutableStateOf(UserStatus.ONLINE) }
    val contacts = remember { MockData.contacts.toMutableStateList() }

    fun openChat(contact: Contact) {
        val index = contacts.indexOfFirst { it.id == contact.id }
        if (index != -1 && contacts[index].unreadCount > 0) {
            contacts[index] = contacts[index].copy(unreadCount = 0)
        }
        screen = Screen.Chat(contact)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = screen,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                (scaleIn(initialScale = 0.92f, animationSpec = tween(220)) + fadeIn(tween(220)))
                    .togetherWith(scaleOut(targetScale = 1.05f, animationSpec = tween(180)) + fadeOut(tween(180)))
            },
            label = "screen-transition"
        ) { targetScreen ->
            when (targetScreen) {
                is Screen.Splash -> SplashScreen(onFinished = { screen = Screen.Home })
                is Screen.Home -> when (tab) {
                    DockTab.CONTACTS -> ContactListScreen(
                        contacts = contacts,
                        myStatus = myStatus,
                        onStatusChange = { myStatus = it },
                        onContactClick = { contact -> openChat(contact) }
                    )
                    DockTab.SETTINGS -> SettingsScreen()
                }
                is Screen.Chat -> ChatScreen(
                    contact = targetScreen.contact,
                    initialMessages = MockData.conversations[targetScreen.contact.id] ?: emptyList(),
                    onBack = { screen = Screen.Home }
                )
            }
        }
        if (screen is Screen.Home) {
            BottomDock(selected = tab, onSelect = { tab = it })
        }
    }
}
