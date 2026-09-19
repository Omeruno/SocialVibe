package com.socialvibe.app.model

data class Contact(
    val id: String,
    val name: String,
    val statusMessage: String,
    val isOnline: Boolean,
    val unreadCount: Int = 0,
    val lastMessageAt: Long? = null
)
