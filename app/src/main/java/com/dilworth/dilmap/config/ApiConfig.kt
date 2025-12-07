package com.dilworth.dilmap.config

/**
 * Central configuration for API endpoints and server URLs
 * All server URLs should be defined here for easy management
 */
object ApiConfig {

    /**
     * Base domain for all services
     */
    const val BASE_DOMAIN = "tv.dilly.cloud"

    /**
     * Base URL for all API calls
     */
    const val API_BASE_URL = "https://$BASE_DOMAIN"

    /**
     * Base URL for content server (videos, thumbnails, manifest)
     */
    const val CONTENT_BASE_URL = "https://$BASE_DOMAIN/content/"

    /**
     * Manifest endpoint
     */
    const val CONTENT_MANIFEST_URL = "${CONTENT_BASE_URL}manifest.json"

    // API Endpoints
    object Auth {
        const val MAGIC_LINK = "$API_BASE_URL/auth/magic-link"
        const val VERIFY = "$API_BASE_URL/auth/verify"
        const val STATUS = "$API_BASE_URL/auth/status"
        const val LOGOUT = "$API_BASE_URL/auth/logout"
    }

    object Content {
        const val USER_CONTENT = "${CONTENT_BASE_URL}user"

        /**
         * Get user content URL
         * @param userId User's unique ID
         * @return URL for user's content manifest
         */
        fun getUserContentUrl(userId: String): String {
            return "$USER_CONTENT?userId=$userId"
        }

        /**
         * Get user file URL
         * @param userId User's unique ID
         * @param filename Name of the file
         * @return URL for user's specific file
         */
        fun getUserFileUrl(userId: String, filename: String): String {
            return "${CONTENT_BASE_URL}user/$userId/$filename"
        }
    }

    // Timeouts (in seconds)
    object Timeouts {
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
    }
}

