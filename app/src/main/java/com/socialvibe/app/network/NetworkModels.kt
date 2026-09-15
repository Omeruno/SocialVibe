package com.socialvibe.app.network

data class TokenPair(val accessToken: String, val refreshToken: String)

data class AuthResult(
    val userId: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String
)

data class UserInfo(val id: String, val username: String, val status: String, val statusMessage: String)

data class ContactInfo(val id: String, val username: String, val status: String, val statusMessage: String)

data class MessageInfo(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val createdAt: String,
    val deliveredAt: String?,
    val readAt: String?
)

class ApiException(val statusCode: Int, message: String) : Exception(message)
