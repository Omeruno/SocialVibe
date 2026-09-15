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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.model.Contact
import com.socialvibe.app.model.Message
import com.socialvibe.app.ui.components.XpTitleBar
import com.socialvibe.app.ui.components.pressScale
import com.socialvibe.app.ui.theme.XpBlueTitle
import com.socialvibe.app.ui.theme.XpBubbleMine
import com.socialvibe.app.ui.theme.XpBubbleTheirs
import com.socialvibe.app.ui.theme.XpSilver
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

@Composable
fun ChatScreen(
    contact: Contact,
    messages: List<Message>,
    isContactTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onTypingChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    BackHandler(onBack = onBack)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Debounced typing signal: fires "typing" on every keystroke, then
    // "stopped typing" after 2s of inactivity — restarted on each new
    // keystroke via LaunchedEffect's key-based cancellation.
    LaunchedEffect(input) {
        if (input.isNotEmpty()) {
            onTypingChanged(true)
            delay(2000)
            onTypingChanged(false)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(XpSilver)) {
        XpTitleBar(title = contact.name, onBack = onBack)
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages, key = { it.id }) { message -> MessageBubble(message) }
            if (isContactTyping) {
                item { TypingIndicator() }
            }
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
            val sendInteractionSource = remember { MutableInteractionSource() }
            Button(
                modifier = Modifier.pressScale(sendInteractionSource),
                interactionSource = sendInteractionSource,
                onClick = {
                    if (input.isNotBlank()) {
                        onSendMessage(input)
                        input = ""
                        onTypingChanged(false)
                    }
                }
            ) {
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
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = message.text)
            Text(
                text = timeFormatter.format(Date(message.timestamp)) + readMarker(message),
                fontSize = 10.sp,
                color = if (message.isFromMe && message.isRead) XpBlueTitle else Color.DarkGray
            )
        }
    }
}

private fun readMarker(message: Message): String {
    if (!message.isFromMe) return ""
    return if (message.isRead) "  ✓✓" else "  ✓"
}

@Composable
private fun TypingIndicator() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(XpBubbleTheirs)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = "typing...")
        }
    }
}
