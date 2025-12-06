package com.dilworth.dilmap.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manages user authentication state and session
 */
class AuthenticationManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save authenticated user information
     */
    fun saveUserSession(email: String, userId: String, deviceId: String) {
        prefs.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_USER_ID, userId)
            putString(KEY_DEVICE_ID, deviceId)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
        Log.d(TAG, "User session saved: $email")
    }

    /**
     * Check if user is currently logged in with valid credentials
     */
    fun isLoggedIn(): Boolean {
        val isLogged = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val email = prefs.getString(KEY_EMAIL, null)

        // Validate that we have a real email, not the string "null"
        if (isLogged && (email == null || email == "null" || email.isBlank())) {
            // Corrupted data - clear it
            signOut()
            return false
        }

        return isLogged
    }

    /**
     * Get logged in user's email
     */
    fun getUserEmail(): String? {
        val email = prefs.getString(KEY_EMAIL, null)
        // Return null if email is the string "null" or blank
        return if (email == "null" || email.isNullOrBlank()) null else email
    }

    /**
     * Get logged in user's ID
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Get device ID
     */
    fun getDeviceId(): String? {
        return prefs.getString(KEY_DEVICE_ID, null)
    }

    /**
     * Get login timestamp
     */
    fun getLoginTime(): Long {
        return prefs.getLong(KEY_LOGIN_TIME, 0)
    }

    /**
     * Sign out the user
     */
    fun signOut() {
        val email = getUserEmail()
        prefs.edit().apply {
            remove(KEY_EMAIL)
            remove(KEY_USER_ID)
            remove(KEY_DEVICE_ID)
            remove(KEY_LOGIN_TIME)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
        Log.d(TAG, "User signed out: $email")
    }

    companion object {
        private const val TAG = "AuthenticationManager"
        private const val PREFS_NAME = "dil_map_auth"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        @Volatile
        private var instance: AuthenticationManager? = null

        fun getInstance(context: Context): AuthenticationManager {
            return instance ?: synchronized(this) {
                instance ?: AuthenticationManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

