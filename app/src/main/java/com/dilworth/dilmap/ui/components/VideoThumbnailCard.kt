package com.dilworth.dilmap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dilworth.dilmap.data.VideoItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoThumbnailCard(
    videoItem: VideoItem,
    onClick: () -> Unit,
    isCached: Boolean = false,
    isDownloading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(220.dp)
            .height(150.dp),
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = 1.1f
        ),
        colors = CardDefaults.colors(
            containerColor = Color(0xFF1A1A1A),
            focusedContainerColor = Color(0xFF2A2A2A)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Thumbnail image
            if (videoItem.thumbnailUrl.startsWith("asset://")) {
                // Load from local assets
                val assetName = videoItem.thumbnailUrl.removePrefix("asset://")
                // For now, show placeholder - we'll need to properly handle asset loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = videoItem.title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Load from URL
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(videoItem.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = videoItem.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(android.R.drawable.ic_menu_gallery)
                )
            }

            // Download/Cache indicator (top-right corner)
            if (isCached || isDownloading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0xCC000000), shape = CircleShape)
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (isDownloading) "⬇️" else "✓",
                        color = if (isDownloading) Color(0xFF4A90E2) else Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp
                    )
                }
            }

            // Gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomStart)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = videoItem.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (videoItem.duration.isNotEmpty()) {
                        Text(
                            text = videoItem.duration,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategorySection(
    title: String,
    videos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    downloadManager: com.dilworth.dilmap.data.VideoDownloadManager,
    downloadingVideoId: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
        )

        TvLazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(videos.size) { index ->
                val video = videos[index]
                VideoThumbnailCard(
                    videoItem = video,
                    onClick = { onVideoClick(video) },
                    isCached = downloadManager.isVideoCached(video.videoUrl),
                    isDownloading = video.id == downloadingVideoId
                )
            }
        }
    }
}
