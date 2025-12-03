package com.pickletv.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.*
import com.pickletv.app.data.ContentRepository
import com.pickletv.app.data.VideoItem
import com.pickletv.app.data.VideoDownloadManager
import com.pickletv.app.data.DownloadProgress
import com.pickletv.app.ui.components.CategorySection
import com.pickletv.app.ui.theme.PickleTVTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay

class HomeActivity : ComponentActivity() {

    private lateinit var downloadManager: VideoDownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        downloadManager = VideoDownloadManager.getInstance(this)

        setContent {
            PickleTVTheme {
                HomeScreen(
                    onVideoSelected = { videoItem ->
                        playVideo(videoItem)
                    },
                    onSignInClicked = {
                        // Launch WelcomeActivity for sign in
                        val intent = Intent(this, WelcomeActivity::class.java)
                        startActivity(intent)
                    },
                    onSettingsClicked = {
                        // TODO: Implement settings screen
                        Log.d(TAG, "Settings clicked")
                    },
                    downloadManager = downloadManager
                )
            }
        }
    }

    private fun playVideo(videoItem: VideoItem) {
        Log.d(TAG, "Playing video: ${videoItem.title}")

        // Check if video is cached
        val cachedPath = downloadManager.getCachedVideoPath(videoItem.videoUrl)
        val videoUrl = if (cachedPath != null) {
            Log.d(TAG, "Using cached video: $cachedPath")
            "file://$cachedPath"
        } else {
            videoItem.videoUrl
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("VIDEO_URL", videoUrl)
            putExtra("VIDEO_TITLE", videoItem.title)
            putExtra("MODE", "BROWSE")
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}

@Composable
fun HomeScreen(
    onVideoSelected: (VideoItem) -> Unit,
    onSignInClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    downloadManager: VideoDownloadManager
) {
    val scope = rememberCoroutineScope()
    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var downloadingVideoId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf<DownloadProgress?>(null) }

    val tabs = listOf("Browse Content", "Sign In", "Settings")

    // Load content on first composition
    LaunchedEffect(Unit) {
        scope.launch {
            val result = ContentRepository.getInstance().fetchContentManifest()
            result.onSuccess { manifest ->
                videos = manifest.videos
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top navigation bar - always visible
        TopNavigationBar(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { index ->
                selectedTab = index
            }
        )

        // Main content area - changes based on selected tab
        when {
            isLoading && selectedTab == 0 -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            selectedTab == 0 -> {
                ContentBrowserScreen(
                    videos = videos,
                    onVideoSelected = { videoItem ->
                        // Start download if not cached
                        if (!downloadManager.isVideoCached(videoItem.videoUrl)) {
                            downloadingVideoId = videoItem.id
                            scope.launch {
                                downloadManager.downloadVideo(videoItem.id, videoItem.videoUrl)
                                    .collectLatest { progress ->
                                        downloadProgress = progress
                                        if (progress.isComplete) {
                                            // Play video after download
                                            onVideoSelected(videoItem)
                                            downloadingVideoId = null
                                            downloadProgress = null
                                        }
                                    }
                            }
                        } else {
                            // Play immediately if cached
                            onVideoSelected(videoItem)
                        }
                    },
                    downloadManager = downloadManager,
                    downloadingVideoId = downloadingVideoId,
                    downloadProgress = downloadProgress
                )
            }
            selectedTab == 1 -> {
                SignInScreen(
                    onSignInSuccess = { username ->
                        Log.d("HomeActivity", "Sign in successful: $username")
                        selectedTab = 0 // Return to browse content
                    },
                    onCancel = {
                        selectedTab = 0 // Return to browse content
                    }
                )
            }
            selectedTab == 2 -> {
                SettingsScreen(
                    downloadManager = downloadManager,
                    onClearCache = {
                        scope.launch {
                            downloadManager.clearCache()
                            Log.d("HomeActivity", "Cache cleared")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TopNavigationBar(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App title/logo
        Text(
            text = "PickleTV",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(end = 24.dp)
        )

        // Navigation tabs using TabRow
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.wrapContentWidth(),
            separator = { Spacer(modifier = Modifier.width(8.dp)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onFocus = { onTabSelected(index) },
                    onClick = { onTabSelected(index) }
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selectedTab == index) Color.White else Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContentBrowserScreen(
    videos: List<VideoItem>,
    onVideoSelected: (VideoItem) -> Unit,
    downloadManager: VideoDownloadManager,
    downloadingVideoId: String?,
    downloadProgress: DownloadProgress?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Download progress indicator
        if (downloadingVideoId != null && downloadProgress != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⬇️ Downloading...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${downloadProgress.percentage}%",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color(0xFF3A3A3A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(downloadProgress.percentage / 100f)
                                .height(4.dp)
                                .background(Color(0xFF4A90E2))
                        )
                    }
                }
            }
        }

        TvLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // Group videos by category
            val categorizedVideos = videos.groupBy { it.category }

            categorizedVideos.forEach { (category, categoryVideos) ->
                item {
                    CategorySection(
                        title = category,
                        videos = categoryVideos,
                        onVideoClick = onVideoSelected,
                        downloadManager = downloadManager,
                        downloadingVideoId = downloadingVideoId
                    )
                }
            }

            // If no videos available, show message
            if (videos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No content available. Check your network connection.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SignInScreen(
    onSignInSuccess: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .background(Color(0xFF1A1A1A), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            // Username field
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                androidx.compose.foundation.text.BasicTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A2A2A), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .padding(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                    singleLine = true
                )
            }

            // Password field
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                androidx.compose.foundation.text.BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A2A2A), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .padding(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (username.isNotEmpty() && password.isNotEmpty()) {
                            onSignInSuccess(username)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sign In")
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    downloadManager: VideoDownloadManager,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cacheSize by remember { mutableStateOf(0L) }
    var clearMessage by remember { mutableStateOf<String?>(null) }
    var isClearing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cacheSize = downloadManager.getCacheSize()
    }

    // Auto-dismiss message after 3 seconds
    LaunchedEffect(clearMessage) {
        if (clearMessage != null) {
            kotlinx.coroutines.delay(3000)
            clearMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(600.dp)
                .background(Color(0xFF1A1A1A), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "• Video Quality: Auto",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Text(
                text = "• Keystone Correction: Enabled",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Text(
                text = "• Content Server: tv.dilly.cloud",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cache management section
            Text(
                text = "Storage Management",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cached Videos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Text(
                        text = VideoDownloadManager.formatSize(cacheSize),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = {
                        isClearing = true
                        onClearCache()
                        cacheSize = 0L
                        clearMessage = "✓ Cache cleared successfully"
                        isClearing = false
                    },
                    modifier = Modifier.padding(start = 16.dp),
                    enabled = !isClearing
                ) {
                    Text(if (isClearing) "Clearing..." else "Clear Cache")
                }
            }

            Text(
                text = "Clearing cache will remove all downloaded videos. They will need to be downloaded again on playback.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Feedback message
            if (clearMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (clearMessage!!.startsWith("✓"))
                                Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = clearMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
