package com.dilworth.dilmap.data

import android.util.Log
import com.dilworth.dilmap.config.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class ContentRepository(private val baseUrl: String = ApiConfig.CONTENT_BASE_URL) {

    private val client = OkHttpClient.Builder()
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Fetch the content manifest from the server
     * Falls back to demo content if network request fails
     */
    suspend fun fetchContentManifest(): Result<ContentManifest> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${baseUrl}content.json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Failed to fetch manifest: ${response.code}")
                    return@withContext Result.success(getDemoContent())
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    Log.w(TAG, "Empty response body")
                    return@withContext Result.success(getDemoContent())
                }

                Log.d(TAG, "Fetched manifest: $responseBody")
                val manifest = json.decodeFromString<ContentManifest>(responseBody)
                Result.success(manifest)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching content", e)
            // Fallback to demo content
            Result.success(getDemoContent())
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing content manifest", e)
            Result.success(getDemoContent())
        }
    }

    /**
     * Fetch user-specific content from the server
     * @param userId The user's unique ID
     * @return Result with user's ContentManifest or empty list on failure
     */
    suspend fun fetchUserContent(userId: String): Result<ContentManifest> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(ApiConfig.Content.getUserContentUrl(userId))
                .header("accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Failed to fetch user content: ${response.code}")
                    return@withContext Result.success(ContentManifest(videos = emptyList()))
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    Log.w(TAG, "Empty user content response")
                    return@withContext Result.success(ContentManifest(videos = emptyList()))
                }

                Log.d(TAG, "Fetched user content: $responseBody")

                // Parse UserContentResponse which has "items" instead of "videos"
                val userResponse = json.decodeFromString<UserContentResponse>(responseBody)

                // Convert UserContentItems to VideoItems and update URLs
                val videos = userResponse.items.map { item ->
                    val videoItem = item.toVideoItem()
                    videoItem.copy(
                        videoUrl = if (item.videoUrl.startsWith("http")) {
                            item.videoUrl
                        } else {
                            // URLs from server start with /content/user/... so use full base URL
                            "${ApiConfig.API_BASE_URL}${item.videoUrl}"
                        },
                        thumbnailUrl = if (item.thumbnailUrl.startsWith("http") || item.thumbnailUrl.startsWith("asset://")) {
                            item.thumbnailUrl
                        } else {
                            "${ApiConfig.API_BASE_URL}${item.thumbnailUrl}"
                        }
                    )
                }

                Log.d(TAG, "Parsed ${videos.size} user videos")
                Result.success(ContentManifest(videos = videos))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching user content", e)
            Result.success(ContentManifest(videos = emptyList()))
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing user content", e)
            Result.success(ContentManifest(videos = emptyList()))
        }
    }

    /**
     * Get demo/fallback content when network is unavailable
     * This includes the existing h-6.mp4 video and any local assets
     */
    private fun getDemoContent(): ContentManifest {
        return ContentManifest(
            videos = listOf(
                VideoItem(
                    id = "demo-1",
                    title = "Demo Video",
                    description = "Sample keystone-corrected video playback",
                    thumbnailUrl = "${ApiConfig.CONTENT_BASE_URL}demo-thumb.jpg",
                    videoUrl = "local://h-6.mp4", // Special URL for local files
                    _duration = 30,
                    category = "Demo"
                ),
                VideoItem(
                    id = "montblanc-1",
                    title = "Mont Blanc Scene",
                    description = "Scenic footage from Mont Blanc",
                    thumbnailUrl = "asset://montblancscene4.jpg", // Use local asset
                    videoUrl = "${ApiConfig.CONTENT_BASE_URL}montblanc.mp4",
                    _duration = 60,
                    category = "Nature"
                )
            )
        )
    }

    companion object {
        private const val TAG = "ContentRepository"

        @Volatile
        private var INSTANCE: ContentRepository? = null

        fun getInstance(): ContentRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContentRepository().also { INSTANCE = it }
            }
        }
    }
}

