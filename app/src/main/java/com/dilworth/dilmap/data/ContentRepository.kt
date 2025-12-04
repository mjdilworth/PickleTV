package com.dilworth.dilmap.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class ContentRepository(private val baseUrl: String = "https://tv.dilly.cloud/content/") {

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
                    thumbnailUrl = "https://tv.dilly.cloud/content/demo-thumb.jpg",
                    videoUrl = "local://h-6.mp4", // Special URL for local files
                    _duration = 30,
                    category = "Demo"
                ),
                VideoItem(
                    id = "montblanc-1",
                    title = "Mont Blanc Scene",
                    description = "Scenic footage from Mont Blanc",
                    thumbnailUrl = "asset://montblancscene4.jpg", // Use local asset
                    videoUrl = "https://tv.dilly.cloud/content/montblanc.mp4",
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

