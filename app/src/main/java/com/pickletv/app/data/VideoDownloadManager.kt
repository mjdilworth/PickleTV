package com.pickletv.app.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

data class DownloadProgress(
    val videoId: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val isComplete: Boolean,
    val error: String? = null
) {
    val percentage: Int get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0
}

class VideoDownloadManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .build()

    private val cacheDir = File(context.cacheDir, "video_cache").apply {
        if (!exists()) mkdirs()
    }

    /**
     * Check if video is already cached locally
     */
    fun isVideoCached(videoUrl: String): Boolean {
        val cachedFile = getCachedVideoFile(videoUrl)
        return cachedFile.exists() && cachedFile.length() > 0
    }

    /**
     * Get the local path for a cached video
     */
    fun getCachedVideoPath(videoUrl: String): String? {
        val cachedFile = getCachedVideoFile(videoUrl)
        return if (cachedFile.exists()) cachedFile.absolutePath else null
    }

    /**
     * Download video with progress tracking
     */
    fun downloadVideo(videoId: String, videoUrl: String): Flow<DownloadProgress> = flow {
        try {
            val cachedFile = getCachedVideoFile(videoUrl)

            // Check if already downloaded
            if (cachedFile.exists() && cachedFile.length() > 0) {
                emit(DownloadProgress(videoId, cachedFile.length(), cachedFile.length(), true))
                return@flow
            }

            val request = Request.Builder()
                .url(videoUrl)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    emit(DownloadProgress(videoId, 0, 0, false, "HTTP ${response.code}"))
                    return@flow
                }

                val totalBytes = response.body?.contentLength() ?: -1
                var downloadedBytes = 0L

                response.body?.byteStream()?.use { input ->
                    FileOutputStream(cachedFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            // Emit progress every 100KB
                            if (downloadedBytes % 102400 == 0L) {
                                emit(DownloadProgress(videoId, downloadedBytes, totalBytes, false))
                            }
                        }

                        // Final progress
                        emit(DownloadProgress(videoId, downloadedBytes, totalBytes, true))
                        Log.d(TAG, "Download complete: $videoUrl -> ${cachedFile.absolutePath}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}", e)
            emit(DownloadProgress(videoId, 0, 0, false, e.message))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get cached file for a video URL
     */
    private fun getCachedVideoFile(videoUrl: String): File {
        val fileName = urlToFileName(videoUrl)
        return File(cacheDir, fileName)
    }

    /**
     * Convert URL to safe filename
     */
    private fun urlToFileName(url: String): String {
        // Use MD5 hash to create a unique but consistent filename
        val md5 = MessageDigest.getInstance("MD5")
        val hashBytes = md5.digest(url.toByteArray())
        val hash = hashBytes.joinToString("") { "%02x".format(it) }

        // Extract extension from URL if possible
        val extension = url.substringAfterLast('.', "mp4").take(4)

        return "$hash.$extension"
    }

    /**
     * Get total cache size
     */
    fun getCacheSize(): Long {
        return cacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }

    /**
     * Clear all cached videos
     */
    fun clearCache(): Boolean {
        return try {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            Log.d(TAG, "Cache cleared successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache: ${e.message}", e)
            false
        }
    }

    /**
     * Delete specific cached video
     */
    fun deleteCachedVideo(videoUrl: String): Boolean {
        return try {
            val cachedFile = getCachedVideoFile(videoUrl)
            val deleted = cachedFile.delete()
            Log.d(TAG, "Deleted cached video: $videoUrl -> $deleted")
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting cached video: ${e.message}", e)
            false
        }
    }

    /**
     * Get list of all cached videos
     */
    fun getCachedVideos(): List<File> {
        return cacheDir.listFiles()?.filter { it.isFile } ?: emptyList()
    }

    companion object {
        private const val TAG = "VideoDownloadManager"

        @Volatile
        private var INSTANCE: VideoDownloadManager? = null

        fun getInstance(context: Context): VideoDownloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VideoDownloadManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Format bytes to human-readable size
         */
        fun formatSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
                else -> "${bytes / (1024 * 1024 * 1024)} GB"
            }
        }
    }
}

