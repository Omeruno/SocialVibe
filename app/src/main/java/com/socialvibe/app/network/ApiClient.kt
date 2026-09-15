package com.socialvibe.app.network

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Verified against a real running instance of the SocialVibe backend
// (see backend/README.md) before this ever shipped in the app — every
// endpoint here was exercised with a live server, not guessed from the
// route source alone.
class ApiClient(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val accessTokenProvider: () -> String?
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun urlFor(path: String, query: Map<String, String> = emptyMap()) =
        "$baseUrl$path".toHttpUrl().newBuilder().apply {
            query.forEach { (key, value) -> addQueryParameter(key, value) }
        }.build()

    private suspend fun request(
        method: String,
        path: String,
        query: Map<String, String> = emptyMap(),
        body: JSONObject? = null,
        authorized: Boolean = true
    ): JSONObject = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder().url(urlFor(path, query))
        if (authorized) {
            accessTokenProvider()?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }
        }
        when (method) {
            "GET" -> requestBuilder.get()
            "POST" -> requestBuilder.post((body ?: JSONObject()).toString().toRequestBody(jsonMediaType))
            else -> error("Unsupported method $method")
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val text = response.body?.string().orEmpty()
            val json = if (text.isNotBlank()) JSONObject(text) else JSONObject()
            if (!response.isSuccessful) {
                throw ApiException(response.code, json.optString("error", "HTTP ${response.code}"))
            }
            json
        }
    }

    suspend fun register(username: String, password: String): AuthResult =
        request(
            "POST", "/auth/register",
            body = JSONObject().put("username", username).put("password", password),
            authorized = false
        ).toAuthResult()

    suspend fun login(username: String, password: String): AuthResult =
        request(
            "POST", "/auth/login",
            body = JSONObject().put("username", username).put("password", password),
            authorized = false
        ).toAuthResult()

    suspend fun refresh(refreshToken: String): TokenPair {
        val json = request(
            "POST", "/auth/refresh",
            body = JSONObject().put("refreshToken", refreshToken),
            authorized = false
        )
        return TokenPair(json.getString("accessToken"), json.getString("refreshToken"))
    }

    suspend fun me(): UserInfo =
        request("GET", "/auth/me").getJSONObject("user").toUserInfo()

    suspend fun getContacts(): List<ContactInfo> =
        request("GET", "/contacts").getJSONArray("contacts").toContactList()

    suspend fun addContact(username: String): ContactInfo =
        request("POST", "/contacts", body = JSONObject().put("username", username))
            .getJSONObject("contact").toContactInfo()

    suspend fun getMessages(contactId: String, since: String? = null, limit: Int = 50): List<MessageInfo> {
        val query = buildMap {
            put("limit", limit.toString())
            since?.let { put("since", it) }
        }
        return request("GET", "/messages/$contactId", query = query)
            .getJSONArray("messages").toMessageList()
    }

    suspend fun sendMessage(contactId: String, text: String): MessageInfo =
        request("POST", "/messages/$contactId", body = JSONObject().put("text", text))
            .getJSONObject("message").toMessageInfo()
}
