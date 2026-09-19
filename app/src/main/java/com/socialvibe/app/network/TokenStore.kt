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
    val COLOR_SCHEME = stringPreferencesKey("color_scheme")
}

class TokenStore(private val context: Context) {
    val session: Flow<Session?> = context.sessionDataStore.data.map { prefs ->
        val userId = prefs[Keys.USER_ID] ?: return@map null
        val username = prefs[Keys.USERNAME] ?: return@map null
        val accessToken = prefs[Keys.ACCESS_TOKEN] ?: return@map null
        val refreshToken = prefs[Keys.REFRESH_TOKEN] ?: return@map null
        Session(userId, username, accessToken, refreshToken)
    }

    // A device preference, not tied to who's logged in — kept as a plain
    // string here rather than the ui-layer's AppScheme enum, so this
    // network-adjacent class doesn't need to know that type exists.
    val schemeName: Flow<String?> = context.sessionDataStore.data.map { it[Keys.COLOR_SCHEME] }

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

    suspend fun saveSchemeName(name: String) {
        context.sessionDataStore.edit { prefs -> prefs[Keys.COLOR_SCHEME] = name }
    }

    // Only the session half — the scheme is a device preference that
    // should survive logging out.
    suspend fun clear() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
        }
    }
}
