package com.socialvibe.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.model.Contact
import com.socialvibe.app.ui.components.Avatar
import com.socialvibe.app.ui.components.GlassTextField
import com.socialvibe.app.ui.components.pressScale
import com.socialvibe.app.ui.theme.InkMuted
import com.socialvibe.app.ui.theme.InkStrong
import com.socialvibe.app.ui.theme.StatusOffline
import com.socialvibe.app.ui.theme.StatusOnline
import com.socialvibe.app.ui.theme.SurfaceAlt
import com.socialvibe.app.ui.theme.SurfaceLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val previewTimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

// Just the window body now — WindowChrome (in DesktopScreen) owns the
// title bar, and the Start Menu owns status/appearance/logout, so this
// is purely: search, sectioned rows, add-by-username.
@Composable
fun ContactListScreen(
    contacts: List<Contact>,
    onContactClick: (Contact) -> Unit,
    onAddContact: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val filtered = remember(contacts, query) {
        if (query.isBlank()) contacts else contacts.filter { it.name.contains(query, ignoreCase = true) }
    }
    val online = remember(filtered) { filtered.filter { it.isOnline } }
    val offline = remember(filtered) { filtered.filter { !it.isOnline } }
    val nothingFound = query.isNotBlank() && online.isEmpty() && offline.isEmpty()

    Column(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
        GlassTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = "🔍  Search or add by username...",
            backgroundColor = SurfaceAlt,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .focusRequester(focusRequester)
        )

        if (nothingFound) {
            AddContactPrompt(username = query, onAddContact = { onAddContact(query); query = "" })
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (online.isNotEmpty()) {
                    item { SectionLabel("Online (${online.size})") }
                    items(online, key = { it.id }) { contact ->
                        ContactRow(contact = contact, onClick = { onContactClick(contact) })
                    }
                }
                if (offline.isNotEmpty()) {
                    item { SectionLabel("Offline (${offline.size})") }
                    items(offline, key = { it.id }) { contact ->
                        ContactRow(contact = contact, onClick = { onContactClick(contact) })
                    }
                }
                item { Spacer(modifier = Modifier.size(64.dp)) }
            }

            Fab(onClick = { focusRequester.requestFocus() }, modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp))
        }
    }
}

@Composable
private fun AddContactPrompt(username: String, onAddContact: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "No contact named \"$username\" yet.",
            fontSize = 12.sp,
            color = InkMuted,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onAddContact) { Text("Add") }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        letterSpacing = 0.06.sp,
        color = InkMuted,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Avatar(name = contact.name, size = 46.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(SurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (contact.isOnline) StatusOnline else StatusOffline)
                )
            }
        }
        Spacer(modifier = Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = InkStrong)
                if (contact.lastMessageAt != null) {
                    Text(
                        text = previewTimeFormatter.format(Date(contact.lastMessageAt)),
                        fontSize = 11.sp,
                        color = InkMuted
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.statusMessage,
                    fontSize = 12.5.sp,
                    color = InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (contact.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    UnreadBadge(contact.unreadCount)
                }
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(StatusOnline),
        contentAlignment = Alignment.Center
    ) {
        Text(text = count.toString(), color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Fab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(52.dp)
            .pressScale(interactionSource)
            .clip(CircleShape)
            .background(StatusOnline)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}
