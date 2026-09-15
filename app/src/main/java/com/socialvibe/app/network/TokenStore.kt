package com.socialvibe.app.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

data class Session(val userId: String, val username: String, val accessToken: String, val refreshToken: String)

private object Keys {
    val USER_ID = stringPreferencesKey("user_id")
    val USERNAME = stringPreferencesKey("username")
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
}

class TokenStore(private val context: Context) {
    val session: Flow<Session?> = context.sessionDataStore.data.map { prefs ->
        val userId = prefs[Keys.USER_ID] ?: return@map null
        val username = prefs[Keys.USERNAME] ?: return@map null
        val accessToken = prefs[Keys.ACCESS_TOKEN] ?: return@map null
        val refreshToken = prefs[Keys.REFRESH_TOKEN] ?: return@map null
        Session(userId, username, accessToken, refreshToken)
    }

    suspend fun save(session: Session) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.USER_ID] = session.userId
            prefs[Keys.USERNAME] = session.username
            prefs[Keys.ACCESS_TOKEN] = session.accessToken
            prefs[Keys.REFRESH_TOKEN] = session.refreshToken
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}
