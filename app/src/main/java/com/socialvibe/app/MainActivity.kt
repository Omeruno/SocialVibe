package com.socialvibe.app

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.socialvibe.app.model.Contact
import com.socialvibe.app.service.MessagingService
import com.socialvibe.app.ui.AppViewModel
import com.socialvibe.app.ui.screens.DesktopScreen
import com.socialvibe.app.ui.screens.LoginScreen
import com.socialvibe.app.ui.screens.SplashScreen
import com.socialvibe.app.ui.theme.SocialVibeTheme
import com.socialvibe.app.ui.theme.schemeColorsFor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: AppViewModel = viewModel()
            val scheme by viewModel.scheme.collectAsState()
            SocialVibeTheme(scheme = scheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SocialVibeApp(viewModel)
                }
            }
        }
    }
}

private sealed class Screen {
    data object Splash : Screen()
    data object Login : Screen()
    data object Desktop : Screen()
}

@Composable
private fun SocialVibeApp(viewModel: AppViewModel) {
    var screen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var activeChatContact by remember { mutableStateOf<Contact?>(null) }

    val session by viewModel.session.collectAsState()
    val scheme by viewModel.scheme.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val myStatus by viewModel.myStatus.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val messagesByContact by viewModel.messagesByContact.collectAsState()
    val typingContacts by viewModel.typingContacts.collectAsState()
    val colors = schemeColorsFor(scheme)

    // Reacts to session changes that happen after the initial splash
    // decision: a successful login/register while on the Login screen, or
    // a logout from anywhere else in the app.
    LaunchedEffect(session) {
        if (screen is Screen.Splash) return@LaunchedEffect
        if (session != null && screen is Screen.Login) {
            screen = Screen.Desktop
        } else if (session == null && screen !is Screen.Login) {
            screen = Screen.Login
            activeChatContact = null
        }
    }

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Denied just means no local notifications; the service still runs. */ }

    // The background-connection service (see MessagingService — our
    // no-Google stand-in for push) only makes sense once we have a session
    // to authenticate its socket with, and must stop on logout.
    LaunchedEffect(session != null) {
        if (session != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            MessagingService.start(context)
        } else {
            MessagingService.stop(context)
        }
    }

    // Re-resolves against the live contacts list every recomposition so an
    // open chat's header reflects fresh presence/unread state rather than
    // a stale snapshot captured at click-time.
    val currentChat = activeChatContact?.let { active -> contacts.find { it.id == active.id } ?: active }
    val currentChatId = currentChat?.id
    val isContactTyping = currentChatId != null && currentChatId in typingContacts

    fun openChat(contact: Contact) {
        activeChatContact = contact
        viewModel.openChat(contact.id)
    }

    fun backFromChat() {
        activeChatContact = null
        viewModel.closeChat()
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            (scaleIn(initialScale = 0.92f, animationSpec = tween(220)) + fadeIn(tween(220)))
                .togetherWith(scaleOut(targetScale = 1.05f, animationSpec = tween(180)) + fadeOut(tween(180)))
        },
        label = "screen-transition"
    ) { targetScreen ->
        when (targetScreen) {
            is Screen.Splash -> SplashScreen(
                colors = colors,
                onFinished = { screen = if (session != null) Screen.Desktop else Screen.Login }
            )
            is Screen.Login -> LoginScreen(
                colors = colors,
                scheme = scheme,
                onSchemeChange = { viewModel.setScheme(it) },
                error = authError,
                onLogin = { username, password -> viewModel.login(username, password) },
                onRegister = { username, password -> viewModel.register(username, password) }
            )
            is Screen.Desktop -> DesktopScreen(
                colors = colors,
                scheme = scheme,
                onSchemeChange = { viewModel.setScheme(it) },
                contacts = contacts,
                myUsername = session?.username ?: "",
                myStatus = myStatus,
                onStatusChange = { viewModel.setStatus(it) },
                activeChat = currentChat,
                messages = currentChatId?.let { messagesByContact[it] }.orEmpty(),
                isContactTyping = isContactTyping,
                onContactClick = { contact -> openChat(contact) },
                onAddContact = { username -> viewModel.addContact(username) },
                onBackFromChat = { backFromChat() },
                onSendMessage = { text -> currentChatId?.let { viewModel.sendMessage(it, text) } },
                onTypingChanged = { typing -> currentChatId?.let { viewModel.setTyping(it, typing) } },
                onRefreshContacts = { viewModel.refreshContacts() },
                onLogout = { viewModel.logout() },
                onExit = { (context as? Activity)?.finish() }
            )
        }
    }
}
