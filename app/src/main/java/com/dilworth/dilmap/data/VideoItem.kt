package com.dilworth.dilmap.data

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

@Serializable
data class UserContentResponse(
    val items: List<UserContentItem>
)

@Serializable
data class UserContentItem(
    @SerialName("content_id")
    val contentId: String,
    val title: String,
    val description: String = "",
    @SerialName("video_url")
    val videoUrl: String,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String,
    @SerialName("duration_secs")
    val durationSecs: Int = 0,
    @SerialName("is_user_content")
    val isUserContent: Boolean = true
) {
    fun toVideoItem(): VideoItem {
        return VideoItem(
            id = contentId,
            title = title,
            description = description,
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
            _duration = durationSecs,
            category = "My Content"
        )
    }
}
