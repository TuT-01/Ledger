package com.bking.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "profile")

@Singleton
class ProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val profile: Flow<UserProfile> = context.profileDataStore.data.map { preferences ->
        UserProfile(
            email = preferences[Keys.EMAIL].orEmpty(),
            displayName = preferences[Keys.DISPLAY_NAME].orEmpty()
        )
    }

    suspend fun register(email: String, displayName: String = "") {
        context.profileDataStore.edit { preferences ->
            preferences[Keys.EMAIL] = email.trim()
            preferences[Keys.DISPLAY_NAME] = displayName.trim()
        }
    }

    private object Keys {
        val EMAIL = stringPreferencesKey("email")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
    }
}

data class UserProfile(
    val email: String = "",
    val displayName: String = ""
) {
    val isRegistered: Boolean = email.isNotBlank()
}

