package com.example.saleapp.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        val KEY_USER_ROLE = stringPreferencesKey(Constants.KEY_USER_ROLE)
        val KEY_CURRENT_PAYMENT_ID = intPreferencesKey("current_payment_id")
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

    fun getUserRole(): String? = runBlocking {
        dataStore.data.map { it[KEY_USER_ROLE] }.first()
    }

    suspend fun saveUserRole(role: String) {
        dataStore.edit { it[KEY_USER_ROLE] = role }
    }

    suspend fun saveCurrentPaymentId(paymentId: Int) {
        dataStore.edit { it[KEY_CURRENT_PAYMENT_ID] = paymentId }
    }

    fun getCurrentPaymentId(): Int = runBlocking {
        dataStore.data.map { it[KEY_CURRENT_PAYMENT_ID] ?: 0 }.first()
    }

    suspend fun clearCurrentPaymentId() {
        dataStore.edit { it.remove(KEY_CURRENT_PAYMENT_ID) }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}

