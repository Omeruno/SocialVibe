package com.socialvibe.app.network

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.toAuthResult(): AuthResult {
    val user = getJSONObject("user")
    return AuthResult(
        userId = user.getString("id"),
        username = user.getString("username"),
        accessToken = getString("accessToken"),
        refreshToken = getString("refreshToken")
    )
}

fun JSONObject.toUserInfo() = UserInfo(
    id = getString("id"),
    username = getString("username"),
    status = optString("status", "OFFLINE"),
    statusMessage = optString("statusMessage", "")
)

fun JSONObject.toContactInfo() = ContactInfo(
    id = getString("id"),
    username = getString("username"),
    status = optString("status", "OFFLINE"),
    statusMessage = optString("statusMessage", "")
)

fun JSONArray.toContactList(): List<ContactInfo> =
    (0 until length()).map { getJSONObject(it).toContactInfo() }

fun JSONObject.toMessageInfo() = MessageInfo(
    id = getString("id"),
    senderId = getString("senderId"),
    receiverId = getString("receiverId"),
    text = getString("text"),
    createdAt = getString("createdAt"),
    deliveredAt = if (isNull("deliveredAt")) null else getString("deliveredAt"),
    readAt = if (isNull("readAt")) null else getString("readAt")
)

fun JSONArray.toMessageList(): List<MessageInfo> =
    (0 until length()).map { getJSONObject(it).toMessageInfo() }
