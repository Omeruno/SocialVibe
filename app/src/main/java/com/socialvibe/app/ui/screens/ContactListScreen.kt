package com.socialvibe.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.model.Contact
import com.socialvibe.app.model.UserStatus
import com.socialvibe.app.ui.components.Avatar
import com.socialvibe.app.ui.components.ColorDot
import com.socialvibe.app.ui.components.XpTitleBar
import com.socialvibe.app.ui.components.pressScale
import com.socialvibe.app.ui.theme.XpGrayOffline
import com.socialvibe.app.ui.theme.XpGreenOnline
import com.socialvibe.app.ui.theme.XpSilver
import com.socialvibe.app.ui.theme.colorForStatus

@Composable
fun ContactListScreen(
    contacts: List<Contact>,
    myStatus: UserStatus,
    onStatusChange: (UserStatus) -> Unit,
    onContactClick: (Contact) -> Unit,
    onAddContact: (String) -> Unit,
    onLogout: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(contacts, query) {
        if (query.isBlank()) contacts else contacts.filter { it.name.contains(query, ignoreCase = true) }
    }
    val online = remember(filtered) { filtered.filter { it.isOnline } }
    val offline = remember(filtered) { filtered.filter { !it.isOnline } }
    val nothingFound = query.isNotBlank() && online.isEmpty() && offline.isEmpty()

    Column(modifier = Modifier.fillMaxSize().background(XpSilver)) {
        XpTitleBar(title = "SocialVibe — Contacts")
        MyProfileRow(status = myStatus, onStatusChange = onStatusChange, onLogout = onLogout)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            placeholder = { Text("Search or add by username...") },
            singleLine = true
        )
        if (nothingFound) {
            AddContactPrompt(username = query, onAddContact = { onAddContact(query); query = "" })
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            if (online.isNotEmpty()) {
                item { SectionHeader(text = "Online (${online.size})") }
                items(online) { contact ->
                    ContactRow(contact = contact, onClick = { onContactClick(contact) })
                    HorizontalDivider()
                }
            }
            if (offline.isNotEmpty()) {
                item { SectionHeader(text = "Offline (${offline.size})") }
                items(offline) { contact ->
                    ContactRow(contact = contact, onClick = { onContactClick(contact) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AddContactPrompt(username: String, onAddContact: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "No contact named \"$username\" yet.", fontSize = 12.sp, modifier = Modifier.weight(1f))
        TextButton(onClick = onAddContact) {
            Text("Add")
        }
    }
}

@Composable
private fun MyProfileRow(status: UserStatus, onStatusChange: (UserStatus) -> Unit, onLogout: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { menuExpanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorDot(color = colorForStatus(status), size = 12.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "You", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = status.label, fontSize = 12.sp)
            }
            Text(text = "▾", fontWeight = FontWeight.Bold)
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            UserStatus.values().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onStatusChange(option)
                        menuExpanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Log out") },
                onClick = {
                    menuExpanded = false
                    onLogout()
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Avatar(name = contact.name)
            ColorDot(
                color = if (contact.isOnline) XpGreenOnline else XpGrayOffline,
                size = 10.dp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = contact.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = contact.statusMessage, fontSize = 12.sp)
        }
        if (contact.unreadCount > 0) {
            UnreadBadge(count = contact.unreadCount)
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(XpGreenOnline),
        contentAlignment = Alignment.Center
    ) {
        Text(text = count.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
