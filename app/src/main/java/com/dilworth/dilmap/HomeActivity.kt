package com.dilworth.dilmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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

/**
 * Standard button component used across all screens for consistency
 */
@Composable
fun StandardButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color(0xFF4A90E2),
    contentColor: Color = Color.White,
    isLoading: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .height(60.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused || focusState.hasFocus
            }
            .focusable(enabled = enabled && !isLoading)
            .background(
                color = when {
                    !enabled -> containerColor.copy(alpha = 0.5f)
                    isFocused -> containerColor // Full brightness when focused
                    else -> containerColor.copy(alpha = 0.75f) // Dimmer when not focused
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isFocused) 4.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = contentColor,
                strokeWidth = 3.dp
            )
        } else {
            Text(
                text = text,
                fontSize = if (isFocused) 22.sp else 20.sp,
                color = contentColor,
                fontWeight = if (isFocused) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
            )
        }
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { com.dilworth.dilmap.auth.AuthenticationManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var downloadingVideoId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf<DownloadProgress?>(null) }
    var isLoggedIn by remember { mutableStateOf(authManager.isLoggedIn()) }
    var userEmail by remember { mutableStateOf(authManager.getUserEmail()) }

    // Create FocusRequesters for each tab to ensure proper focus restoration
    val tabFocusRequesters = remember { List(5) { FocusRequester() } }

    // Dynamic tabs based on authentication status
    val tabs = if (isLoggedIn) {
        listOf("Browse Content", "Profile", "Settings", "Help", "About")
    } else {
        listOf("Browse Content", "Sign In", "Settings", "Help", "About")
    }

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
                if (isLoggedIn) {
                    // Show Profile screen when logged in
                    ProfileScreen(
                        authManager = authManager,
                        userEmail = userEmail,
                        onSignOut = {
                            isLoggedIn = false
                            userEmail = null
                            selectedTab = 0 // Return to browse content
                        }
                    )
                } else {
                    // Show Sign In screen when not logged in
                    SignInScreen(
                        onSignInSuccess = { email ->
                            Log.d("HomeActivity", "Sign in successful: $email")
                            isLoggedIn = true
                            userEmail = email
                            selectedTab = 0 // Return to browse content
                        },
                        onCancel = {
                            selectedTab = 0 // Return to browse content
                        }
                    )
                }
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val magicLinkService = remember { com.dilworth.dilmap.auth.MagicLinkService.getInstance(context) }
    val authManager = remember { com.dilworth.dilmap.auth.AuthenticationManager.getInstance(context) }

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isPolling by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    val deviceId = remember { magicLinkService.getDeviceId() }

    // Check if already logged in
    LaunchedEffect(Unit) {
        if (authManager.isLoggedIn()) {
            authManager.getUserEmail()?.let { userEmail ->
                onSignInSuccess(userEmail)
            }
        }
    }

    // Polling for authentication status
    LaunchedEffect(isPolling) {
        if (isPolling) {
            while (isPolling) {
                kotlinx.coroutines.delay(4000) // Poll every 4 seconds

                val result = magicLinkService.checkAuthStatus(deviceId)
                result.onSuccess { userInfo ->
                    // User has clicked the magic link!
                    authManager.saveUserSession(userInfo.email, userInfo.userId, userInfo.deviceId)
                    isPolling = false
                    onSignInSuccess(userInfo.email)
                }.onFailure {
                    // Keep polling - user hasn't clicked link yet
                }
            }
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
                .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp))
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            if (!isPolling) {
                Text(
                    text = "Enter your email to receive a sign-in link",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Email field only - no password!
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Email Address",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    BasicTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2A2A2A), shape = RoundedCornerShape(8.dp))
                            .padding(20.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 18.sp
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )
                }

                // Status message
                if (statusMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isError) Color(0xFFF44336) else Color(0xFF4CAF50),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = statusMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StandardButton(
                        text = "Cancel",
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        containerColor = Color(0xFF2A2A2A)
                    )

                    StandardButton(
                        text = "Send Link",
                        onClick = {
                            if (email.isNotEmpty()) {
                                isLoading = true
                                scope.launch {
                                    val result = magicLinkService.requestMagicLink(email)
                                    isLoading = false

                                    result.onSuccess { message ->
                                        statusMessage = null
                                        isError = false
                                        isPolling = true
                                        Log.d("SignInScreen", "Magic link sent to $email, starting polling")
                                    }.onFailure { error ->
                                        statusMessage = error.message ?: "Failed to send magic link"
                                        isError = true
                                        Log.e("SignInScreen", "Failed to send magic link: ${error.message}")
                                    }
                                }
                            } else {
                                statusMessage = "Please enter your email address"
                                isError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && email.isNotEmpty(),
                        containerColor = Color(0xFF4A90E2),
                        isLoading = isLoading
                    )
                }

                // Development: Test Login Button (REMOVE IN PRODUCTION)
                Spacer(modifier = Modifier.height(16.dp))
                StandardButton(
                    text = "TEST LOGIN (Dev Only)",
                    onClick = {
                        authManager.saveUserSession(
                            email = "test@lucindadilworth.com",
                            userId = "test_user_123",
                            deviceId = deviceId
                        )
                        onSignInSuccess("test@lucindadilworth.com")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFFFF9800)
                )
            } else {
                // Polling state - waiting for user to click link
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color(0xFF4A90E2),
                        strokeWidth = 4.dp
                    )

                    Text(
                        text = "Check Your Email",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    Text(
                        text = "We sent a sign-in link to:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )

                    Text(
                        text = email,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4A90E2)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Click the link in your email to sign in.\nWaiting for confirmation...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StandardButton(
                        text = "Cancel",
                        onClick = {
                            isPolling = false
                            statusMessage = null
                        },
                        containerColor = Color(0xFF2A2A2A),
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProfileScreen(
    authManager: com.dilworth.dilmap.auth.AuthenticationManager,
    userEmail: String?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val magicLinkService = remember { com.dilworth.dilmap.auth.MagicLinkService.getInstance(context) }
    val displayEmail = userEmail ?: authManager.getUserEmail() ?: "No email"
    val deviceId = remember { magicLinkService.getDeviceId() }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(550.dp)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile icon/header - smaller
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF4A90E2), shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayEmail.firstOrNull()?.uppercase() ?: "U",
                    fontSize = 28.sp,
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            // Email display - cleaner, no "Signed in as" label
            Text(
                text = displayEmail,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            // Account status - more subtle
            Row(
                modifier = Modifier
                    .background(Color(0xFF2A2A2A), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF4CAF50), shape = androidx.compose.foundation.shape.CircleShape)
                )
                Text(
                    text = "Active",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!showSignOutConfirm) {
                // Log Out button - smaller, centered, subtle styling
                StandardButton(
                    text = "Log Out",
                    onClick = { showSignOutConfirm = true },
                    containerColor = Color(0xFF2A2A2A),
                    modifier = Modifier.width(200.dp)
                )
            } else {
                // Confirmation
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Are you sure you want to log out?",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StandardButton(
                            text = "Cancel",
                            onClick = { showSignOutConfirm = false },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoggingOut,
                            containerColor = Color(0xFF2A2A2A)
                        )

                        StandardButton(
                            text = "Log Out",
                            onClick = {
                                isLoggingOut = true
                                scope.launch {
                                    // Call logout API
                                    val result = magicLinkService.logout(deviceId)
                                    result.onSuccess {
                                        Log.d("ProfileScreen", "Logout API successful")
                                    }.onFailure {
                                        Log.e("ProfileScreen", "Logout API failed: ${it.message}")
                                    }

                                    // Clear local session regardless of API result
                                    authManager.signOut()
                                    isLoggingOut = false
                                    onSignOut()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoggingOut,
                            containerColor = Color(0xFFF44336),
                            isLoading = isLoggingOut
                        )
                    }
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

                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .width(180.dp)
                    ) {
                        StandardButton(
                            text = if (isClearing) "Clearing..." else "Clear Cache",
                            onClick = {
                                isClearing = true
                                onClearCache()
                                cacheSize = 0L
                                clearMessage = "✓ Cache cleared successfully"
                                isClearing = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isClearing,
                            containerColor = Color(0xFF2A2A2A),
                            isLoading = isClearing
                        )
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

                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .width(200.dp)
                    ) {
                        StandardButton(
                            text = if (isResettingKeystone) "Resetting..." else "Reset Keystone",
                            onClick = {
                                isResettingKeystone = true
                                warpShapeManager.resetWarpShape()
                                keystoneMessage = "✓ Keystone reset to default"
                                isResettingKeystone = false
                                Log.d("SettingsScreen", "Keystone positions reset to default")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isResettingKeystone,
                            containerColor = Color(0xFF2A2A2A),
                            isLoading = isResettingKeystone
                        )
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
