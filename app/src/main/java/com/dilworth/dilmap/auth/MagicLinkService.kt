package com.dilworth.dilmap.auth

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.dilworth.dilmap.config.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Service for handling magic link authentication
 * Sends authentication requests to the server with email and device ID
 */
class MagicLinkService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.Timeouts.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.Timeouts.READ_TIMEOUT, TimeUnit.SECONDS)
        .build()


    /**
     * Get the unique device identifier for this Android TV device
     */
    fun getDeviceId(): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device ID: ${e.message}")
            "unknown-device"
        }
    }

    /**
     * Request a magic link to be sent to the provided email address
     * @param email The user's email address
     * @return Result indicating success or failure with message
     */
    suspend fun requestMagicLink(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isValidEmail(email)) {
                return@withContext Result.failure(Exception("Please enter a valid email address"))
            }

            val deviceId = getDeviceId()
            val deviceModel = android.os.Build.MODEL
            val deviceManufacturer = android.os.Build.MANUFACTURER

            val jsonBody = JSONObject().apply {
                put("email", email)
                put("deviceId", deviceId)
                put("deviceModel", deviceModel)
                put("deviceManufacturer", deviceManufacturer)
                put("platform", "android-tv")
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(ApiConfig.Auth.MAGIC_LINK)
                .post(requestBody)
                .build()

            Log.d(TAG, "Requesting magic link for email: $email, deviceId: $deviceId")

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    Log.d(TAG, "Magic link request successful")
                    Result.success("Magic link sent! Check your email to complete sign in.")
                } else {
                    val errorMsg = try {
                        JSONObject(responseBody).optString("message", "Failed to send magic link")
                    } catch (e: Exception) {
                        "Failed to send magic link (${response.code})"
                    }
                    Log.e(TAG, "Magic link request failed: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting magic link", e)
            Result.failure(Exception("Network error: ${e.message ?: "Unknown error"}"))
        }
    }

    /**
     * Check if user has completed authentication via magic link
     * Used for polling after sending magic link
     * @param deviceId The device ID to check
     * @return Result with user info if authenticated, or failure if not yet authenticated
     */
    suspend fun checkAuthStatus(deviceId: String): Result<UserInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${ApiConfig.Auth.STATUS}?deviceId=$deviceId")
                .get()
                .build()

            Log.d(TAG, "Checking auth status for deviceId: $deviceId")

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val authenticated = json.optBoolean("authenticated", false)

                    // First check if authenticated field is true
                    if (!authenticated) {
                        Log.d(TAG, "Device not authenticated yet (authenticated=false)")
                        return@withContext Result.failure(Exception("Not authenticated yet"))
                    }

                    // Now validate the email and userId fields
                    val email = json.optString("email", "")
                    val userId = json.optString("userId", "")

                    if (email.isBlank() || email == "null" || userId.isBlank() || userId == "null") {
                        Log.w(TAG, "Server says authenticated but returned invalid data: email='$email', userId='$userId'")
                        return@withContext Result.failure(Exception("Invalid authentication data from server"))
                    }

                    val userInfo = UserInfo(
                        email = email,
                        userId = userId,
                        deviceId = deviceId
                    )
                    Log.d(TAG, "Authentication confirmed for ${userInfo.email}")
                    Result.success(userInfo)
                } else {
                    // HTTP error - this is normal during polling
                    Result.failure(Exception("Not authenticated yet"))
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Auth status check: not authenticated yet")
            Result.failure(Exception("Not authenticated yet"))
        }
    }

    /**
     * Verify a magic link token
     * @param token The token from the magic link
     * @return Result with user info or error
     */
    suspend fun verifyMagicLink(token: String): Result<UserInfo> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId()

            val request = Request.Builder()
                .url("${ApiConfig.Auth.VERIFY}?token=$token&deviceId=$deviceId")
                .get()
                .build()

            Log.d(TAG, "Verifying magic link token")

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val userInfo = UserInfo(
                        email = json.getString("email"),
                        userId = json.getString("userId"),
                        deviceId = deviceId
                    )
                    Log.d(TAG, "Magic link verified successfully for ${userInfo.email}")
                    Result.success(userInfo)
                } else {
                    val errorMsg = try {
                        JSONObject(responseBody).optString("message", "Invalid or expired magic link")
                    } catch (e: Exception) {
                        "Invalid or expired magic link"
                    }
                    Log.e(TAG, "Magic link verification failed: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying magic link", e)
            Result.failure(Exception("Verification error: ${e.message ?: "Unknown error"}"))
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Logout user from the server
     * @param deviceId The device ID to logout
     * @return Result indicating success or failure
     */
    suspend fun logout(deviceId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${ApiConfig.Auth.LOGOUT}?deviceId=$deviceId")
                .post("".toRequestBody(null))
                .build()

            Log.d(TAG, "Logging out deviceId: $deviceId")

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "Logout successful")
                    Result.success("Logged out successfully")
                } else {
                    Log.e(TAG, "Logout failed: ${response.code}")
                    Result.failure(Exception("Logout failed"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            Result.failure(Exception("Logout error: ${e.message ?: "Unknown error"}"))
        }
    }

    companion object {
        private const val TAG = "MagicLinkService"

        @Volatile
        private var instance: MagicLinkService? = null

        fun getInstance(context: Context): MagicLinkService {
            return instance ?: synchronized(this) {
                instance ?: MagicLinkService(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * User information returned after successful authentication
 */
data class UserInfo(
    val email: String,
    val userId: String,
    val deviceId: String
)

