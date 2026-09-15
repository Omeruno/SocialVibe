package com.socialvibe.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.socialvibe.app.ui.theme.XpBubbleMine
import com.socialvibe.app.ui.theme.XpBubbleTheirs
import com.socialvibe.app.ui.theme.XpSilver
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
private val autoReplies = listOf("haha true", "lol nice", "one sec", "for real?", "same here", "brb")

@Composable
fun ChatScreen(contact: Contact, initialMessages: List<Message>, onBack: () -> Unit) {
    var messages by remember(contact.id) { mutableStateOf(initialMessages) }
    var input by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    BackHandler(onBack = onBack)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(isTyping) {
        if (isTyping) {
            delay(1400)
            messages = messages + Message(
                id = "reply-${messages.size}",
                text = autoReplies[Random.nextInt(autoReplies.size)],
                isFromMe = false
            )
            isTyping = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(XpSilver)) {
        XpTitleBar(title = contact.name, onBack = onBack)
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { message -> MessageBubble(message) }
            if (isTyping) {
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
                        messages = messages + Message(
                            id = "local-${messages.size}",
                            text = input,
                            isFromMe = true
                        )
                        input = ""
                        isTyping = true
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
                text = timeFormatter.format(Date(message.timestamp)),
                fontSize = 10.sp,
                color = Color.DarkGray
            )
        }
    }
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
