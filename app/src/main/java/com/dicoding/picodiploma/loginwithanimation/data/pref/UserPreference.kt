package com.dicoding.picodiploma.loginwithanimation.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dicoding.picodiploma.loginwithanimation.data.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSession(user: UserModel) {
        dataStore.edit { it ->
            it[EMAIL_KEY] = user.email
            it[TOKEN_KEY] = user.token
            it[IS_LOGIN_KEY] = true
        }
    }

    fun getSession(): Flow<UserModel> =
        dataStore.data.map { it ->
            UserModel(
                it[EMAIL_KEY] ?: "",
                it[TOKEN_KEY] ?: "",
                it[IS_LOGIN_KEY] ?: false
            )
        }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val EMAIL_KEY = stringPreferencesKey("email")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreference(dataStore).also { INSTANCE = it }
            }
    }
}