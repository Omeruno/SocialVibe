package com.socialvibe.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialvibe.app.model.Contact
import com.socialvibe.app.model.Message
import com.socialvibe.app.model.UserStatus
import com.socialvibe.app.ui.components.Avatar
import com.socialvibe.app.ui.components.AnimatedSky
import com.socialvibe.app.ui.components.StartMenu
import com.socialvibe.app.ui.components.Taskbar
import com.socialvibe.app.ui.components.WindowChrome
import com.socialvibe.app.ui.theme.AppScheme
import com.socialvibe.app.ui.theme.SchemeColors

// The "rest of the environment": one window (Contacts or an open Chat)
// floating over an animated sky, a taskbar with a real Start button, and
// the Start Menu as the single home for every setting — no separate
// Settings tab, no per-screen status dropdown.
@Composable
fun DesktopScreen(
    colors: SchemeColors,
    scheme: AppScheme,
    onSchemeChange: (AppScheme) -> Unit,
    contacts: List<Contact>,
    myUsername: String,
    myStatus: UserStatus,
    onStatusChange: (UserStatus) -> Unit,
    activeChat: Contact?,
    messages: List<Message>,
    isContactTyping: Boolean,
    onContactClick: (Contact) -> Unit,
    onAddContact: (String) -> Unit,
    onBackFromChat: () -> Unit,
    onSendMessage: (String) -> Unit,
    onTypingChanged: (Boolean) -> Unit,
    onRefreshContacts: () -> Unit,
    onLogout: () -> Unit,
    onExit: () -> Unit
) {
    var startMenuOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = activeChat != null, onBack = onBackFromChat)

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedSky(colors = colors)

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.widthIn(max = 480.dp).fillMaxSize()) {
                    if (activeChat != null) {
                        WindowChrome(
                            colors = colors,
                            title = activeChat.name,
                            subtitle = when {
                                isContactTyping -> "typing..."
                                activeChat.isOnline -> "online"
                                else -> "offline"
                            },
                            onBack = onBackFromChat,
                            leading = { Avatar(name = activeChat.name, size = 26.dp) }
                        ) {
                            ChatScreen(
                                colors = colors,
                                messages = messages,
                                isContactTyping = isContactTyping,
                                onSendMessage = onSendMessage,
                                onTypingChanged = onTypingChanged
                            )
                        }
                    } else {
                        WindowChrome(colors = colors, title = "SocialVibe — Contacts") {
                            ContactListScreen(
                                contacts = contacts,
                                onContactClick = onContactClick,
                                onAddContact = onAddContact
                            )
                        }
                    }
                }
            }

            Taskbar(
                myStatus = myStatus,
                activeChatLabel = activeChat?.let { "Chat — ${it.name}" },
                onStartClick = { startMenuOpen = !startMenuOpen }
            )
        }

        if (startMenuOpen) {
            val scrimInteractionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = scrimInteractionSource,
                        indication = null
                    ) { startMenuOpen = false }
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(start = 10.dp, bottom = 64.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            StartMenu(
                visible = startMenuOpen,
                myName = myUsername,
                myStatus = myStatus,
                onStatusChange = onStatusChange,
                scheme = scheme,
                colors = colors,
                onSchemeChange = onSchemeChange,
                onContacts = onBackFromChat,
                onRefresh = onRefreshContacts,
                onLogout = onLogout,
                onExit = onExit,
                onDismiss = { startMenuOpen = false }
            )
        }
    }
}
