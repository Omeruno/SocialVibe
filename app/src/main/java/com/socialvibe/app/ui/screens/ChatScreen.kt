package com.socialvibe.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.socialvibe.app.model.Message
import com.socialvibe.app.ui.components.GlassTextField
import com.socialvibe.app.ui.components.pressScale
import com.socialvibe.app.ui.theme.InkMuted
import com.socialvibe.app.ui.theme.InkStrong
import com.socialvibe.app.ui.theme.SchemeColors
import com.socialvibe.app.ui.theme.SurfaceAlt
import com.socialvibe.app.ui.theme.SurfaceLight
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

// Just the window body — WindowChrome (in DesktopScreen) owns the header
// now, with the contact's name/avatar/typing status.
@Composable
fun ChatScreen(
    colors: SchemeColors,
    messages: List<Message>,
    isContactTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onTypingChanged: (Boolean) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var attachNoteVisible by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isContactTyping) {
        val lastIndex = messages.size - 1 + if (isContactTyping) 1 else 0
        if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
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

    LaunchedEffect(attachNoteVisible) {
        if (attachNoteVisible) {
            delay(1600)
            attachNoteVisible = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceAlt)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(messages, key = { it.id }) { message -> MessageBubble(message, colors) }
            if (isContactTyping) {
                item { TypingIndicator() }
            }
        }

        AnimatedVisibility(visible = attachNoteVisible) {
            Text(
                text = "Attachments coming soon",
                fontSize = 11.sp,
                color = InkMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GlyphButton(glyph = "📎") { attachNoteVisible = true }
            GlassTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = "Type a message...",
                backgroundColor = SurfaceAlt,
                modifier = Modifier.weight(1f)
            )
            GlyphButton(glyph = "🙂") { input += "🙂" }
            SendButton(colors = colors) {
                if (input.isNotBlank()) {
                    onSendMessage(input)
                    input = ""
                    onTypingChanged(false)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, colors: SchemeColors) {
    val alignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (message.isFromMe) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp)
    }
    val textColor = if (message.isFromMe) Color.White else InkStrong

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(
                    if (message.isFromMe) {
                        Brush.linearGradient(listOf(colors.light, colors.mid))
                    } else {
                        Brush.linearGradient(listOf(SurfaceLight, SurfaceLight))
                    }
                )
                .padding(horizontal = 13.dp, vertical = 9.dp)
        ) {
            Text(text = message.text, color = textColor, fontSize = 13.5.sp)
            Text(
                text = timeFormatter.format(Date(message.timestamp)) + readMarker(message),
                fontSize = 10.sp,
                color = if (message.isFromMe) Color.White.copy(alpha = 0.75f) else InkMuted
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
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp))
                .background(SurfaceLight)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TypingDot(0)
            TypingDot(1)
            TypingDot(2)
        }
    }
}

@Composable
private fun TypingDot(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = index * 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typing-dot"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .offset(y = offsetY.dp)
            .clip(CircleShape)
            .background(InkMuted)
    )
}

@Composable
private fun GlyphButton(glyph: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, fontSize = 16.sp)
    }
}

@Composable
private fun SendButton(colors: SchemeColors, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(38.dp)
            .pressScale(interactionSource)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(colors.light, colors.mid)))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("→", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
