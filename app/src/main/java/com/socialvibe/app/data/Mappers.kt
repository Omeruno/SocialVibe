package com.socialvibe.app.data

import com.socialvibe.app.model.Contact
import com.socialvibe.app.model.Message
import com.socialvibe.app.network.ContactInfo
import com.socialvibe.app.network.MessageInfo
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Deliberately not java.time: minSdk 24 predates it (API 26+) and this
// project has no core-library-desugaring set up, so java.time.Instant
// would crash at runtime on Android 7/7.1 devices despite compiling fine.
private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun parseIsoToEpochMillis(iso: String): Long =
    runCatching { isoFormat.parse(iso)?.time }.getOrNull() ?: System.currentTimeMillis()

fun ContactInfo.toContact(unreadCount: Int = 0): Contact = Contact(
    id = id,
    name = username,
    statusMessage = statusMessage,
    isOnline = status == "ONLINE",
    unreadCount = unreadCount
)

fun MessageInfo.toMessage(currentUserId: String): Message = Message(
    id = id,
    text = text,
    isFromMe = senderId == currentUserId,
    timestamp = parseIsoToEpochMillis(createdAt)
)
