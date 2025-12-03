package com.pickletv.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoItem(
    val id: String,
    val title: String,
    val description: String = "",
    val thumbnailUrl: String,
    val videoUrl: String,
    @SerialName("duration")
    private val _duration: Int = 0,
    val category: String = "Uncategorized"
) {
    // Convert seconds to MM:SS format
    val duration: String
        get() = if (_duration > 0) {
            val minutes = _duration / 60
            val seconds = _duration % 60
            String.format("%d:%02d", minutes, seconds)
        } else ""
}

@Serializable
data class ContentManifest(
    val videos: List<VideoItem>
)

