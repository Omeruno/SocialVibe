package com.socialvibe.app.model

data class Message(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
