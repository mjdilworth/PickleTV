package com.dilworth.dilmap.auth

import android.content.Context
import android.util.Log

/**
 * Testing utilities for authentication
 * Use these methods to simulate authentication flows during development
 */
object AuthTestUtils {

    private const val TAG = "AuthTestUtils"

    /**
     * Simulate a successful magic link authentication for testing
     * This mimics what would happen when a user clicks a magic link
     */
    fun simulateSuccessfulLogin(
        context: Context,
        email: String = "test@lucindadilworth.com",
        userId: String = "test_user_123"
    ) {
        val authManager = AuthenticationManager.getInstance(context)
        val magicLinkService = MagicLinkService.getInstance(context)
        val deviceId = magicLinkService.getDeviceId()

        authManager.saveUserSession(
            email = email,
            userId = userId,
            deviceId = deviceId
        )

        Log.d(TAG, "Simulated successful login for: $email")
    }

    /**
     * Clear all authentication data for testing
     */
    fun clearAuth(context: Context) {
        val authManager = AuthenticationManager.getInstance(context)
        authManager.signOut()
        Log.d(TAG, "Authentication cleared")
    }

    /**
     * Check current authentication status
     */
    fun printAuthStatus(context: Context) {
        val authManager = AuthenticationManager.getInstance(context)
        Log.d(TAG, "=== Authentication Status ===")
        Log.d(TAG, "Is Logged In: ${authManager.isLoggedIn()}")
        Log.d(TAG, "Email: ${authManager.getUserEmail() ?: "Not logged in"}")
        Log.d(TAG, "User ID: ${authManager.getUserId() ?: "None"}")
        Log.d(TAG, "Device ID: ${authManager.getDeviceId() ?: "None"}")
        Log.d(TAG, "===========================")
    }
}

