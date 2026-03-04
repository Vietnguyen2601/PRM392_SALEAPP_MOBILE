package com.example.saleapp.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sale_app_prefs")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.dataStore

    companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey(Constants.KEY_AUTH_TOKEN)
        val KEY_USER_ID = stringPreferencesKey(Constants.KEY_USER_ID)
        val KEY_USER_EMAIL = stringPreferencesKey(Constants.KEY_USER_EMAIL)
        val KEY_IS_LOGGED_IN = booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)
    }

    fun getAuthToken(): String? = runBlocking {
        dataStore.data.map { it[KEY_AUTH_TOKEN] }.first()
    }

    suspend fun saveAuthToken(token: String) {
        dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    fun getUserId(): String? = runBlocking {
        dataStore.data.map { it[KEY_USER_ID] }.first()
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { it[KEY_USER_ID] = userId }
    }

    fun getUserEmail(): String? = runBlocking {
        dataStore.data.map { it[KEY_USER_EMAIL] }.first()
    }

    suspend fun saveUserEmail(email: String) {
        dataStore.edit { it[KEY_USER_EMAIL] = email }
    }

    fun isLoggedIn(): Boolean = runBlocking {
        dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }.first()
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { it[KEY_IS_LOGGED_IN] = loggedIn }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}

