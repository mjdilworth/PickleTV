package com.dilworth.dilmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.*
import com.dilworth.dilmap.data.ContentRepository
import com.dilworth.dilmap.data.VideoItem
import com.dilworth.dilmap.data.VideoDownloadManager
import com.dilworth.dilmap.data.DownloadProgress
import com.dilworth.dilmap.ui.components.CategorySection
import com.dilworth.dilmap.ui.theme.PickleTVTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay

class HomeActivity : ComponentActivity() {

    private lateinit var downloadManager: VideoDownloadManager
    private lateinit var warpShapeManager: WarpShapeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        downloadManager = VideoDownloadManager.getInstance(this)
        warpShapeManager = WarpShapeManager(this)

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
                    downloadManager = downloadManager,
                    warpShapeManager = warpShapeManager
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
            // Pass the absolute path directly - MainActivity will handle it
            cachedPath
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
    downloadManager: VideoDownloadManager,
    warpShapeManager: WarpShapeManager
) {
    val scope = rememberCoroutineScope()
    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var downloadingVideoId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf<DownloadProgress?>(null) }

    // Create FocusRequesters for each tab to ensure proper focus restoration
    val tabFocusRequesters = remember { List(5) { FocusRequester() } }

    val tabs = listOf("Browse Content", "Sign In", "Settings", "Help", "About")

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
            },
            focusRequesters = tabFocusRequesters
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
                    warpShapeManager = warpShapeManager,
                    onClearCache = {
                        scope.launch {
                            downloadManager.clearCache()
                            Log.d("HomeActivity", "Cache cleared")
                        }
                    }
                )
            }
            selectedTab == 3 -> {
                HelpScreen()
            }
            selectedTab == 4 -> {
                AboutScreen()
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
    focusRequesters: List<FocusRequester>,
    modifier: Modifier = Modifier
) {
    // Track the last key event to distinguish horizontal vs vertical navigation
    var lastKeyCode by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 48.dp)
            .onPreviewKeyEvent { keyEvent ->
                // Track key presses to detect navigation direction
                if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                    lastKeyCode = keyEvent.nativeKeyEvent.keyCode
                }
                false // Don't consume the event
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App title/logo
        Text(
            text = "dil.map",
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
                    onFocus = {
                        if (index == selectedTab) {
                            // Same tab getting focus - already selected, do nothing
                        } else if (lastKeyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT ||
                                   lastKeyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                            // Horizontal navigation (LEFT/RIGHT keys) - change tab
                            onTabSelected(index)
                        } else {
                            // Vertical navigation (UP from content) - restore focus to correct tab
                            focusRequesters[selectedTab].requestFocus()
                        }
                        lastKeyCode = null // Reset after handling
                    },
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.focusRequester(focusRequesters[index])
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selectedTab == index) Color.Black else Color.Gray,
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
    warpShapeManager: WarpShapeManager,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cacheSize by remember { mutableStateOf(0L) }
    var clearMessage by remember { mutableStateOf<String?>(null) }
    var isClearing by remember { mutableStateOf(false) }
    var keystoneMessage by remember { mutableStateOf<String?>(null) }
    var isResettingKeystone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cacheSize = downloadManager.getCacheSize()
    }

    // Auto-dismiss cache message after 3 seconds
    LaunchedEffect(clearMessage) {
        if (clearMessage != null) {
            kotlinx.coroutines.delay(3000)
            clearMessage = null
        }
    }

    // Auto-dismiss keystone message after 3 seconds
    LaunchedEffect(keystoneMessage) {
        if (keystoneMessage != null) {
            kotlinx.coroutines.delay(3000)
            keystoneMessage = null
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TvLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Settings header and info
            item {
                Column(
                    modifier = Modifier
                        .width(600.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp) // Reduced padding
                    )

                    Text(
                        text = "• Video Quality: Auto",
                        style = MaterialTheme.typography.bodyMedium, // Changed from bodyLarge
                        color = Color.White
                    )

                    Text(
                        text = "• Keystone Correction: Enabled",
                        style = MaterialTheme.typography.bodyMedium, // Changed from bodyLarge
                        color = Color.White
                    )

                    Text(
                        text = "• Content Server: tv.dilly.cloud",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            // Cache management section
            item {
                Column(
                    modifier = Modifier
                        .width(600.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Storage Management",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
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

                    androidx.tv.material3.Button(
                        onClick = {
                            isClearing = true
                            onClearCache()
                            cacheSize = 0L
                            clearMessage = "✓ Cache cleared successfully"
                            isClearing = false
                        },
                        modifier = Modifier.padding(start = 16.dp),
                        enabled = !isClearing,
                        scale = androidx.tv.material3.ButtonDefaults.scale(
                            focusedScale = 1.15f
                        ),
                        colors = androidx.tv.material3.ButtonDefaults.colors(
                            containerColor = Color(0xFF2A2A2A),
                            contentColor = Color.White,
                            focusedContainerColor = Color(0xFF4A90E2),
                            focusedContentColor = Color.White
                        )
                    ) {
                        Text(if (isClearing) "Clearing..." else "Clear Cache")
                    }
                }

                Text(
                    text = "Clearing cache will remove all downloaded videos. They will need to be downloaded again on playback.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp) // Reduced padding
                )

                // Feedback message for cache
                if (clearMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (clearMessage!!.startsWith("✓"))
                                    Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = RoundedCornerShape(4.dp)
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

            // Keystone Correction Reset section
            item {
                Column(
                    modifier = Modifier
                        .width(600.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Keystone Correction",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reset Keystone Position",
                            style = MaterialTheme.typography.bodyMedium, // Changed from bodyLarge
                            color = Color.White
                        )
                        Text(
                            text = "Returns all corner positions to default (centered)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    androidx.tv.material3.Button(
                        onClick = {
                            isResettingKeystone = true
                            warpShapeManager.resetWarpShape()
                            keystoneMessage = "✓ Keystone reset to default"
                            isResettingKeystone = false
                            Log.d("SettingsScreen", "Keystone positions reset to default")
                        },
                        modifier = Modifier.padding(start = 16.dp),
                        enabled = !isResettingKeystone,
                        scale = androidx.tv.material3.ButtonDefaults.scale(
                            focusedScale = 1.15f
                        ),
                        colors = androidx.tv.material3.ButtonDefaults.colors(
                            containerColor = Color(0xFF2A2A2A),
                            contentColor = Color.White,
                            focusedContainerColor = Color(0xFF4A90E2),
                            focusedContentColor = Color.White
                        )
                    ) {
                        Text(if (isResettingKeystone) "Resetting..." else "Reset Keystone")
                    }
                }

                // Feedback message for keystone
                if (keystoneMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (keystoneMessage!!.startsWith("✓"))
                                    Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = keystoneMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HelpScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        TvLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Remote Control Guide",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Remote Controls",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A90E2),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Basic Navigation
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.Start
                ) {
                    ControlGroup(
                        title = "Basic Navigation",
                        controls = listOf(
                            "D-Pad ↑↓←→" to "Navigate menus and content",
                            "OK/Center" to "Select item or save settings",
                            "Back" to "Return to previous screen"
                        )
                    )
                }
            }

            // Video Playback
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.Start
                ) {
                    ControlGroup(
                        title = "Video Playback",
                        controls = listOf(
                            "OK/Center" to "Play/Pause video",
                            "Back" to "Exit video and return to home"
                        )
                    )
                }
            }

            // Keystone Correction
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.Start
                ) {
                    ControlGroup(
                        title = "Keystone Correction (During Video)",
                        controls = listOf(
                            "D-Pad Center" to "Open keystone adjustment menu",
                            "D-Pad Up/Down" to "Navigate menu options",
                            "D-Pad Center (on menu item)" to "Select option",
                            "D-Pad arrows (in adjust mode)" to "Move selected corner",
                            "Back" to "Exit keystone menu"
                        )
                    )
                }
            }

            // Workflow Example
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Keystone Adjustment Workflow",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A90E2)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2A2A2A), shape = RoundedCornerShape(4.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkflowStep("1", "Play a video")
                        WorkflowStep("2", "Press D-Pad Center → Menu opens")
                        WorkflowStep("3", "Navigate to a corner → Press Center")
                        WorkflowStep("4", "Use D-Pad arrows → Adjust corner")
                        WorkflowStep("5", "Press D-Pad Center → Back to menu")
                        WorkflowStep("6", "Select 'Save & Exit' → Press Center")
                    }
                }
            }

            // Tips
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tips",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A90E2),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TipItem("Press D-Pad Center during video to open keystone menu")
                    TipItem("Menu provides visual guide - no need to memorize controls")
                    TipItem("Settings persist across app restarts")
                    TipItem("Use Settings → Reset Keystone to restore defaults")
                    TipItem("Downloaded videos show a ✓ icon and play instantly")
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⬇ Scroll down for more information",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        TvLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Info
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "dil.map",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )

                    Text(
                        text = "Video Player with Keystone Correction",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF90CAF9)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            // License Information
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "License",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Copyright © 2025 Dilworth Creative LLC",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Text(
                        text = "All rights reserved.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            // Contact Information
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Contact",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🌐 Website:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF90CAF9),
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "lucindadilworth.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📧 Email:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF90CAF9),
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "hello@lucindadilworth.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📱 Instagram:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF90CAF9),
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "@dil.worth",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }

            // Developer Info
            item {
                Column(
                    modifier = Modifier
                        .width(700.dp)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                        .padding(32.dp)
                        .focusable(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Developer",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Dilworth Creative LLC",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )

                    Text(
                        text = "Package: com.dilworth.dilmap",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Scroll indicator at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⬇ Scroll down for more information",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ControlGroup(title: String, controls: List<Pair<String, String>>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        controls.forEach { (button, description) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "• $button:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF90CAF9),
                    modifier = Modifier.width(140.dp)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WorkflowStep(step: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF4A90E2), shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TipItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "💡",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB0B0B0)
        )
    }
}
