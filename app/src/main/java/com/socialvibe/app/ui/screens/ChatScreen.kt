package com.socialvibe.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.socialvibe.app.model.Contact
import com.socialvibe.app.model.Message
import com.socialvibe.app.ui.components.XpTitleBar
import com.socialvibe.app.ui.theme.XpBubbleMine
import com.socialvibe.app.ui.theme.XpBubbleTheirs
import com.socialvibe.app.ui.theme.XpSilver

@Composable
fun ChatScreen(contact: Contact, initialMessages: List<Message>, onBack: () -> Unit) {
    var messages by remember(contact.id) { mutableStateOf(initialMessages) }
    var input by remember { mutableStateOf("") }

    BackHandler(onBack = onBack)

    Column(modifier = Modifier.fillMaxSize().background(XpSilver)) {
        XpTitleBar(title = contact.name, onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { message -> MessageBubble(message) }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            Button(onClick = {
                if (input.isNotBlank()) {
                    messages = messages + Message(
                        id = "local-${messages.size}",
                        text = input,
                        isFromMe = true
                    )
                    input = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val alignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isFromMe) XpBubbleMine else XpBubbleTheirs

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = message.text)
        }
    }
}
