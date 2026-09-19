package com.socialvibe.app.data

import android.content.Context
import com.socialvibe.app.model.Contact
import com.socialvibe.app.model.Message
import com.socialvibe.app.model.UserStatus
import com.socialvibe.app.network.ApiClient
import com.socialvibe.app.network.ApiException
import com.socialvibe.app.network.AuthResult
import com.socialvibe.app.network.NetworkConfig
import com.socialvibe.app.network.Session
import com.socialvibe.app.network.SocketManager
import com.socialvibe.app.network.TokenStore
import com.socialvibe.app.network.toMessageInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class IncomingMessage(val contactId: String, val message: Message)

class ChatRepository(context: Context) {
    private val appContext = context.applicationContext
    private val tokenStore = TokenStore(appContext)
    private val socket = SocketManager(NetworkConfig.BASE_URL)
    private val api = ApiClient(NetworkConfig.BASE_URL) { currentAccessToken }

    @Volatile private var currentAccessToken: String? = null
    @Volatile private var currentRefreshToken: String? = null
    @Volatile var currentUserId: String? = null
        private set

    val schemeName: Flow<String?> = tokenStore.schemeName

    suspend fun saveSchemeName(name: String) = tokenStore.saveSchemeName(name)

    suspend fun restoreSession(): Session? {
        val session = tokenStore.session.first()
        if (session != null) {
            currentAccessToken = session.accessToken
            currentRefreshToken = session.refreshToken
            currentUserId = session.userId
        }
        return session
    }

    suspend fun register(username: String, password: String): Result<Session> =
        runCatching { applySession(api.register(username, password)) }

    suspend fun login(username: String, password: String): Result<Session> =
        runCatching { applySession(api.login(username, password)) }

    private suspend fun applySession(result: AuthResult): Session {
        currentAccessToken = result.accessToken
        currentRefreshToken = result.refreshToken
        currentUserId = result.userId
        val session = Session(result.userId, result.username, result.accessToken, result.refreshToken)
        tokenStore.save(session)
        return session
    }

    suspend fun logout() {
        socket.disconnect()
        currentAccessToken = null
        currentRefreshToken = null
        currentUserId = null
        tokenStore.clear()
    }

    fun connectRealtime() {
        currentAccessToken?.let { socket.connect(it) }
    }

    fun disconnectRealtime() {
        socket.disconnect()
    }

    // A single 401 usually means the short-lived access token expired
    // mid-session; refresh once with the stored refresh token and retry
    // exactly once rather than bouncing the user back to the login screen.
    private suspend fun <T> withAutoRefresh(block: suspend () -> T): T =
        try {
            block()
        } catch (e: ApiException) {
            val refreshToken = currentRefreshToken
            if (e.statusCode == 401 && refreshToken != null) {
                val refreshed = api.refresh(refreshToken)
                currentAccessToken = refreshed.accessToken
                currentRefreshToken = refreshed.refreshToken
                tokenStore.updateTokens(refreshed.accessToken, refreshed.refreshToken)
                block()
            } else {
                throw e
            }
        }

    // Enriches each contact with a real last-message preview + timestamp
    // (fetched in parallel) instead of a static bio line — the backend's
    // history endpoint sorts oldest-first for pagination, so "most recent"
    // means fetching a page and taking the tail client-side. Each of these
    // per-contact fetches skips the auto-refresh wrapper deliberately: if
    // several ran concurrently and all hit an expired token, they'd race
    // to redeem the same single-use refresh token and all but one would
    // fail (see withAutoRefresh) — better to just skip a preview than to
    // risk that cascade.
    suspend fun loadContacts(): Result<List<Contact>> = runCatching {
        val baseContacts = withAutoRefresh { api.getContacts() }.map { it.toContact() }
        coroutineScope {
            baseContacts.map { contact ->
                async {
                    val last = runCatching { api.getMessages(contact.id, limit = 50) }.getOrNull()?.lastOrNull()
                    if (last != null) {
                        contact.copy(
                            statusMessage = last.text,
                            lastMessageAt = parseIsoToEpochMillis(last.createdAt)
                        )
                    } else {
                        contact
                    }
                }
            }.awaitAll()
        }
    }

    suspend fun addContact(username: String): Result<Contact> = runCatching {
        withAutoRefresh { api.addContact(username) }.toContact()
    }

    suspend fun loadMessages(contactId: String, since: String? = null): Result<List<Message>> = runCatching {
        val myId = currentUserId ?: error("Not logged in")
        withAutoRefresh { api.getMessages(contactId, since) }.map { it.toMessage(myId) }
    }

    suspend fun sendMessageRest(contactId: String, text: String): Result<Message> = runCatching {
        val myId = currentUserId ?: error("Not logged in")
        withAutoRefresh { api.sendMessage(contactId, text) }.toMessage(myId)
    }

    // Primary send path: goes over the already-open socket so delivery is
    // instant, with the REST endpoint (sendMessageRest) as a fallback for
    // callers if this throws because the socket isn't connected.
    suspend fun sendMessageRealtime(contactId: String, text: String): Message =
        suspendCancellableCoroutine { continuation ->
            val myId = currentUserId ?: run {
                continuation.resumeWithException(IllegalStateException("Not logged in"))
                return@suspendCancellableCoroutine
            }
            socket.sendMessage(contactId, text) { ack ->
                val messageJson = ack.optJSONObject("message")
                if (messageJson != null) {
                    continuation.resume(messageJson.toMessageInfo().toMessage(myId))
                } else {
                    continuation.resumeWithException(IllegalStateException(ack.optString("error", "Send failed")))
                }
            }
        }

    fun incomingMessages(): Flow<IncomingMessage> = socket.on("message:new").map { json ->
        val myId = currentUserId.orEmpty()
        val info = json.toMessageInfo()
        val contactId = if (info.senderId == myId) info.receiverId else info.senderId
        IncomingMessage(contactId, info.toMessage(myId))
    }

    fun typingUpdates(): Flow<Pair<String, Boolean>> = socket.on("typing:update").map {
        it.getString("fromUserId") to it.getBoolean("isTyping")
    }

    fun presenceUpdates(): Flow<Pair<String, UserStatus>> = socket.on("presence:update").map {
        it.getString("userId") to UserStatus.valueOf(it.getString("status"))
    }

    // (contactId who read our messages, epoch millis of everything up to
    // and including that moment) — the ViewModel uses this to flip our
    // already-sent messages in that thread over to "read" instantly.
    fun readReceipts(): Flow<Pair<String, Long>> = socket.on("message:read").map {
        it.getString("by") to parseIsoToEpochMillis(it.getString("at"))
    }

    fun markRead(contactId: String) = socket.markRead(contactId)

    fun setTyping(contactId: String, isTyping: Boolean) = socket.setTyping(contactId, isTyping)

    fun setPresence(status: UserStatus) = socket.setPresence(status.name)
}
