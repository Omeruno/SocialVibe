package com.socialvibe.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.data.MockData
import com.socialvibe.app.model.Contact
import com.socialvibe.app.ui.components.XpTitleBar
import com.socialvibe.app.ui.theme.XpGrayOffline
import com.socialvibe.app.ui.theme.XpGreenOnline
import com.socialvibe.app.ui.theme.XpSilver

@Composable
fun ContactListScreen(onContactClick: (Contact) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(XpSilver)) {
        XpTitleBar(title = "SocialVibe — Contacts")
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(MockData.contacts) { contact ->
                ContactRow(contact = contact, onClick = { onContactClick(contact) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (contact.isOnline) XpGreenOnline else XpGrayOffline)
        )
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
