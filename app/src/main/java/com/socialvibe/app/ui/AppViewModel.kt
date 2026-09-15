package com.socialvibe.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.socialvibe.app.data.ChatRepository
import com.socialvibe.app.model.Contact
import com.socialvibe.app.model.Message
import com.socialvibe.app.model.UserStatus
import com.socialvibe.app.network.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application.applicationContext)

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _myStatus = MutableStateFlow(UserStatus.ONLINE)
    val myStatus: StateFlow<UserStatus> = _myStatus.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _messagesByContact = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messagesByContact: StateFlow<Map<String, List<Message>>> = _messagesByContact.asStateFlow()

    private val _typingContacts = MutableStateFlow<Set<String>>(emptySet())
    val typingContacts: StateFlow<Set<String>> = _typingContacts.asStateFlow()

    private var activeChatContactId: String? = null

    init {
        viewModelScope.launch {
            val restored = repository.restoreSession()
            _session.value = restored
            if (restored != null) onSessionEstablished()
        }
    }

    private fun onSessionEstablished() {
        repository.connectRealtime()
        refreshContacts()

        viewModelScope.launch {
            repository.incomingMessages().collect { (contactId, message) ->
                _messagesByContact.update { current ->
                    current + (contactId to (current[contactId].orEmpty() + message))
                }
                if (contactId != activeChatContactId && !message.isFromMe) {
                    bumpUnread(contactId)
                }
            }
        }
        viewModelScope.launch {
            repository.typingUpdates().collect { (fromUserId, isTyping) ->
                _typingContacts.update { current -> if (isTyping) current + fromUserId else current - fromUserId }
            }
        }
        viewModelScope.launch {
            repository.presenceUpdates().collect { (userId, status) ->
                _contacts.update { list ->
                    list.map { if (it.id == userId) it.copy(isOnline = status == UserStatus.ONLINE) else it }
                }
            }
        }
    }

    private fun bumpUnread(contactId: String) {
        _contacts.update { list ->
            list.map { if (it.id == contactId) it.copy(unreadCount = it.unreadCount + 1) else it }
        }
    }

    private fun refreshContacts() {
        viewModelScope.launch {
            repository.loadContacts().onSuccess { loaded -> _contacts.value = loaded }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            repository.login(username, password)
                .onSuccess { session -> _session.value = session; onSessionEstablished() }
                .onFailure { _authError.value = it.message ?: "Login failed" }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            repository.register(username, password)
                .onSuccess { session -> _session.value = session; onSessionEstablished() }
                .onFailure { _authError.value = it.message ?: "Registration failed" }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _session.value = null
            _contacts.value = emptyList()
            _messagesByContact.value = emptyMap()
            _typingContacts.value = emptySet()
            activeChatContactId = null
        }
    }

    fun addContact(username: String) {
        viewModelScope.launch {
            repository.addContact(username).onSuccess { contact ->
                _contacts.update { it + contact }
            }
        }
    }

    fun openChat(contactId: String) {
        activeChatContactId = contactId
        _contacts.update { list -> list.map { if (it.id == contactId) it.copy(unreadCount = 0) else it } }
        viewModelScope.launch {
            repository.loadMessages(contactId).onSuccess { history ->
                _messagesByContact.update { it + (contactId to history) }
            }
        }
    }

    fun closeChat() {
        activeChatContactId = null
    }

    fun sendMessage(contactId: String, text: String) {
        viewModelScope.launch {
            val sent = runCatching { repository.sendMessageRealtime(contactId, text) }
                .recoverCatching { repository.sendMessageRest(contactId, text).getOrThrow() }
                .getOrNull() ?: return@launch

            _messagesByContact.update { current ->
                current + (contactId to (current[contactId].orEmpty() + sent))
            }
        }
    }

    fun setTyping(contactId: String, isTyping: Boolean) {
        repository.setTyping(contactId, isTyping)
    }

    fun setStatus(status: UserStatus) {
        _myStatus.value = status
        repository.setPresence(status)
    }
}
