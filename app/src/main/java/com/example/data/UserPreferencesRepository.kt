package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserProfile(
    val uid: String,
    val name: String,
    val avatarUrl: String?,
    val soundEnabled: Boolean
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AVATAR = stringPreferencesKey("user_avatar")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { preferences ->
        val uid = preferences[PreferencesKeys.USER_ID] ?: UUID.randomUUID().toString()
        val name = preferences[PreferencesKeys.USER_NAME] ?: ""
        val avatar = preferences[PreferencesKeys.USER_AVATAR]
        val sound = preferences[PreferencesKeys.SOUND_ENABLED] ?: true
        UserProfile(uid = uid, name = name, avatarUrl = avatar, soundEnabled = sound)
    }

    suspend fun saveProfile(uid: String? = null, name: String, avatarUrl: String? = null) {
        context.dataStore.edit { preferences ->
            if (uid != null) {
                preferences[PreferencesKeys.USER_ID] = uid
            } else if (!preferences.contains(PreferencesKeys.USER_ID)) {
                preferences[PreferencesKeys.USER_ID] = UUID.randomUUID().toString()
            }
            preferences[PreferencesKeys.USER_NAME] = name
            if (avatarUrl != null) {
                preferences[PreferencesKeys.USER_AVATAR] = avatarUrl
            }
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun clearData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
