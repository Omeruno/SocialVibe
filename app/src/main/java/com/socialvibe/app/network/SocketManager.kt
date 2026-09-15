package com.socialvibe.app.network

import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject

// Verified against socket.io-client 2.1.1 with a live two-client test
// (typing indicator + realtime delivery + ack) before shipping — the
// IO.Options.builder()/setAuth/on/off/emit calls below are the real,
// compiler-checked API, not remembered from documentation.
class SocketManager(private val baseUrl: String) {
    private var socket: Socket? = null

    fun connect(accessToken: String) {
        val options = IO.Options.builder()
            .setAuth(mapOf("token" to accessToken))
            .build()
        socket = IO.socket(baseUrl, options).also { it.connect() }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    fun sendMessage(receiverId: String, text: String, onAck: (JSONObject) -> Unit) {
        val payload = JSONObject().put("receiverId", receiverId).put("text", text)
        socket?.emit("message:send", payload, Ack { args ->
            (args.getOrNull(0) as? JSONObject)?.let(onAck)
        })
    }

    fun setTyping(receiverId: String, isTyping: Boolean) {
        val event = if (isTyping) "typing:start" else "typing:stop"
        socket?.emit(event, JSONObject().put("receiverId", receiverId))
    }

    fun setPresence(status: String) {
        socket?.emit("presence:update", JSONObject().put("status", status))
    }

    fun on(event: String): Flow<JSONObject> = callbackFlow {
        val listener = Emitter.Listener { args ->
            (args.getOrNull(0) as? JSONObject)?.let { trySend(it) }
        }
        socket?.on(event, listener)
        awaitClose { socket?.off(event, listener) }
    }
}
