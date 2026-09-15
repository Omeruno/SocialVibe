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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.socialvibe.app.model.Contact
import com.socialvibe.app.ui.AppViewModel
import com.socialvibe.app.ui.components.BottomDock
import com.socialvibe.app.ui.components.DockTab
import com.socialvibe.app.ui.screens.ChatScreen
import com.socialvibe.app.ui.screens.ContactListScreen
import com.socialvibe.app.ui.screens.LoginScreen
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
    data object Login : Screen()
    data object Home : Screen()
    data class Chat(val contact: Contact) : Screen()
}

@Composable
private fun SocialVibeApp(viewModel: AppViewModel = viewModel()) {
    var screen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var tab by remember { mutableStateOf(DockTab.CONTACTS) }

    val session by viewModel.session.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val myStatus by viewModel.myStatus.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val messagesByContact by viewModel.messagesByContact.collectAsState()
    val typingContacts by viewModel.typingContacts.collectAsState()

    // Reacts to session changes that happen after the initial splash
    // decision: a successful login/register while on the Login screen, or
    // a logout from anywhere else in the app.
    LaunchedEffect(session) {
        if (screen is Screen.Splash) return@LaunchedEffect
        if (session != null && screen is Screen.Login) {
            screen = Screen.Home
        } else if (session == null && screen !is Screen.Login) {
            screen = Screen.Login
        }
    }

    fun openChat(contact: Contact) {
        viewModel.openChat(contact.id)
        screen = Screen.Chat(contact)
    }

    fun goHome() {
        viewModel.closeChat()
        screen = Screen.Home
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
                is Screen.Splash -> SplashScreen(onFinished = {
                    screen = if (session != null) Screen.Home else Screen.Login
                })
                is Screen.Login -> LoginScreen(
                    error = authError,
                    onLogin = { username, password -> viewModel.login(username, password) },
                    onRegister = { username, password -> viewModel.register(username, password) }
                )
                is Screen.Home -> when (tab) {
                    DockTab.CONTACTS -> ContactListScreen(
                        contacts = contacts,
                        myStatus = myStatus,
                        onStatusChange = { viewModel.setStatus(it) },
                        onContactClick = { contact -> openChat(contact) },
                        onAddContact = { username -> viewModel.addContact(username) },
                        onLogout = { viewModel.logout() }
                    )
                    DockTab.SETTINGS -> SettingsScreen()
                }
                is Screen.Chat -> ChatScreen(
                    contact = targetScreen.contact,
                    messages = messagesByContact[targetScreen.contact.id].orEmpty(),
                    isContactTyping = targetScreen.contact.id in typingContacts,
                    onSendMessage = { text -> viewModel.sendMessage(targetScreen.contact.id, text) },
                    onTypingChanged = { isTyping -> viewModel.setTyping(targetScreen.contact.id, isTyping) },
                    onBack = { goHome() }
                )
            }
        }
        if (screen is Screen.Home) {
            BottomDock(selected = tab, onSelect = { tab = it })
        }
    }
}
