package com.dilworth.dilmap

import android.content.pm.ApplicationInfo
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.InputDevice
import android.view.Surface
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import java.io.File

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    private lateinit var exoPlayer: Player
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: VideoGLRenderer
    private lateinit var warpShapeManager: WarpShapeManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentWarpShape = WarpShape()

    // Keystone menu system
    private var keystoneMenuVisible: Boolean = false
    private var selectedMenuIndex: Int = 0
    private var cornerEditMode: Boolean = false
    private var selectedCorner: Corner = Corner.TOP_LEFT
    private lateinit var keystoneMenuOverlay: LinearLayout
    private val menuTextViews = mutableListOf<TextView>()
    private val menuOptions = listOf(
        "Corner 1 (Top Left)",
        "Corner 2 (Top Right)",
        "Corner 3 (Bottom Left)",
        "Corner 4 (Bottom Right)",
        "Reset to Default",
        "Save & Exit"
    )

    // Adjustment settings
    private var warpAdjustmentStep = 0.05f

    private var inputLoggingEnabled: Boolean = true
    private var isDebugBuild: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isDebugBuild = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // Handle mode from WelcomeActivity
        val mode = intent.getStringExtra("MODE")
        val email = intent.getStringExtra("EMAIL")

        Log.d("MainActivity", "onCreate called - Mode: $mode, Email: $email")

        warpShapeManager = WarpShapeManager(this)
        currentWarpShape = warpShapeManager.loadWarpShape()

        // Create main container
        val container = FrameLayout(this)
        container.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // Initialize GLSurfaceView
        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        renderer = VideoGLRenderer()
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        renderer.setOnFrameAvailableRenderRequest { glSurfaceView.requestRender() }
        renderer.setCornerHighlightProvider { cornerEditMode to selectedCorner }

        container.addView(glSurfaceView)

        // Create keystone menu overlay (initially hidden)
        createKeystoneMenuOverlay(container)

        setContentView(container)

        // Initialize ExoPlayer
        initializeExoPlayer()
    }

    private fun createKeystoneMenuOverlay(container: FrameLayout) {
        // Create semi-transparent background overlay
        keystoneMenuOverlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xCC000000.toInt()) // Semi-transparent black
            setPadding(60, 60, 60, 60)
            visibility = android.view.View.GONE

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        // Add title
        TextView(this).apply {
            text = "KEYSTONE ADJUSTMENT"
            textSize = 28f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, 40)
            gravity = Gravity.CENTER
            keystoneMenuOverlay.addView(this)
        }

        // Add menu options
        menuOptions.forEachIndexed { index, option ->
            val textView = TextView(this).apply {
                text = option
                textSize = 22f
                setTextColor(0xFFCCCCCC.toInt())
                setPadding(40, 20, 40, 20)
                gravity = Gravity.CENTER

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 10, 0, 10)
                }
            }
            menuTextViews.add(textView)
            keystoneMenuOverlay.addView(textView)
        }

        container.addView(keystoneMenuOverlay)
        updateMenuHighlight()
    }

    private fun updateMenuHighlight() {
        menuTextViews.forEachIndexed { index, textView ->
            if (index == selectedMenuIndex) {
                textView.setBackgroundColor(0xFF4A90E2.toInt()) // Blue highlight
                textView.setTextColor(0xFFFFFFFF.toInt()) // White text
                textView.textSize = 24f
            } else {
                textView.setBackgroundColor(0x00000000.toInt()) // Transparent
                textView.setTextColor(0xFFCCCCCC.toInt()) // Gray text
                textView.textSize = 22f
            }
        }
    }

    private fun showKeystoneMenu() {
        keystoneMenuVisible = true
        keystoneMenuOverlay.visibility = android.view.View.VISIBLE
        selectedMenuIndex = 0
        updateMenuHighlight()
        Log.d("MainActivity", "Keystone menu opened")
    }

    private fun hideKeystoneMenu() {
        keystoneMenuVisible = false
        keystoneMenuOverlay.visibility = android.view.View.GONE
        cornerEditMode = false
        glSurfaceView.requestRender()
        Log.d("MainActivity", "Keystone menu closed")
    }

    private fun handleMenuSelection() {
        when (selectedMenuIndex) {
            0, 1, 2, 3 -> { // Corner selection
                selectedCorner = Corner.values()[selectedMenuIndex]
                cornerEditMode = true
                keystoneMenuVisible = false
                keystoneMenuOverlay.visibility = android.view.View.GONE
                glSurfaceView.requestRender()
                Toast.makeText(
                    this,
                    "Adjusting ${menuOptions[selectedMenuIndex]}\nUse D-Pad arrows to move\nPress Center to return to menu",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("MainActivity", "Selected corner: $selectedCorner")
            }
            4 -> { // Reset to Default
                currentWarpShape = WarpShape()
                renderer.setWarpShape(currentWarpShape)
                glSurfaceView.requestRender()
                Toast.makeText(this, "✓ Keystone reset to default", Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", "Keystone reset to default")
            }
            5 -> { // Save & Exit
                warpShapeManager.saveWarpShape(currentWarpShape)
                Toast.makeText(this, "✓ Keystone saved", Toast.LENGTH_SHORT).show()
                hideKeystoneMenu()
                Log.d("MainActivity", "Keystone saved and exited")
            }
        }
    }

    private var playbackAttempts = 0
    private var currentVideoUrl: String? = null
    private var currentSurface: Surface? = null

    private fun initializeExoPlayer() {
        // Start with hardware decoding preference (default)
        // Software fallback will be triggered on error
        exoPlayer = createExoPlayerWithDecoder(preferSoftwareDecoder = false)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

        // Register surface texture callback to provide Surface to ExoPlayer
        renderer.registerSurfaceTextureCallback { surface ->
            setupExoPlayerWithSurface(surface)
        }

        // Update warp shape in renderer
        renderer.setWarpShape(currentWarpShape)
    }

    private fun createExoPlayerWithDecoder(preferSoftwareDecoder: Boolean): ExoPlayer {
        val renderersFactory = com.google.android.exoplayer2.DefaultRenderersFactory(this).apply {
            if (preferSoftwareDecoder) {
                // Force software decoding by using a custom MediaCodecSelector
                // that filters out hardware codecs
                setMediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
                    val allCodecs = com.google.android.exoplayer2.mediacodec.MediaCodecUtil
                        .getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder)

                    // Filter to only software decoders (names starting with "OMX.google." or "c2.android.")
                    val softwareCodecs = allCodecs.filter { codecInfo ->
                        val name = codecInfo.name.lowercase()
                        name.startsWith("omx.google.") ||
                        name.startsWith("c2.android.") ||
                        name.contains("sw")  // Software decoder indicator
                    }

                    if (softwareCodecs.isNotEmpty()) {
                        Log.d("MainActivity", "Using software decoder: ${softwareCodecs[0].name}")
                        softwareCodecs
                    } else {
                        Log.w("MainActivity", "No software decoders found, using default")
                        allCodecs
                    }
                }
                Log.d("MainActivity", "Creating ExoPlayer with SOFTWARE decoder (forced via MediaCodecSelector)")
            } else {
                // Use default hardware decoder preference
                Log.d("MainActivity", "Creating ExoPlayer with HARDWARE decoder preference (default)")
            }
        }

        return ExoPlayer.Builder(this)
            .setRenderersFactory(renderersFactory)
            .build()
    }

    private fun setupExoPlayerWithSurface(surface: Surface) {
        // Post to main thread to avoid "Player is accessed on the wrong thread" error
        mainHandler.post {
            try {
                // Check if a video URL was provided via intent
                val videoUrl = intent.getStringExtra("VIDEO_URL")

                if (!videoUrl.isNullOrEmpty()) {
                    // Handle video URL from HomeActivity
                    if (videoUrl.startsWith("local://")) {
                        // Local file - extract path
                        val localPath = videoUrl.removePrefix("local://")
                        loadLocalVideo(surface, localPath)
                    } else if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://")) {
                        // Network URL - stream directly
                        loadVideoFromUrl(surface, videoUrl)
                    } else if (videoUrl.startsWith("/")) {
                        // Absolute file path (from cache)
                        loadLocalVideo(surface, videoUrl)
                    } else {
                        Log.e("MainActivity", "Unknown video URL format: $videoUrl")
                    }
                } else {
                    // Fallback to default video
                    loadLocalVideo(surface, BuildConfig.DEBUG_VIDEO_FILE_NAME)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting up video: ${e.message}", e)
            }
        }
    }

    private fun loadLocalVideo(surface: Surface, fileName: String) {
        try {
            // Check if file exists at the path
            val videoFile = File(fileName)

            if (videoFile.exists()) {
                Log.d("MainActivity", "=== VIDEO FILE DIAGNOSTICS ===")
                Log.d("MainActivity", "File path: ${videoFile.absolutePath}")
                Log.d("MainActivity", "File size: ${videoFile.length()} bytes (${formatBytes(videoFile.length())})")
                Log.d("MainActivity", "File readable: ${videoFile.canRead()}")
                Log.d("MainActivity", "File extension: ${videoFile.extension}")

                // Log device information
                Log.d("MainActivity", "=== DEVICE INFORMATION ===")
                Log.d("MainActivity", "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                Log.d("MainActivity", "Android version: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
                Log.d("MainActivity", "Hardware: ${android.os.Build.HARDWARE}")

                val videoUri = Uri.fromFile(videoFile)
                val mediaItem = MediaItem.fromUri(videoUri)

                exoPlayer.setVideoSurface(surface)

                // Store for potential retry
                currentVideoUrl = videoFile.absolutePath
                currentSurface = surface

                // Add detailed event listeners
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val stateName = when (playbackState) {
                            Player.STATE_IDLE -> "IDLE"
                            Player.STATE_BUFFERING -> "BUFFERING"
                            Player.STATE_READY -> "READY"
                            Player.STATE_ENDED -> "ENDED"
                            else -> "UNKNOWN"
                        }
                        Log.d("MainActivity", "Playback state changed: $stateName")

                        // Reset attempts counter on successful playback
                        if (playbackState == Player.STATE_READY) {
                            playbackAttempts = 0
                        }
                    }

                    override fun onPlayerError(error: com.google.android.exoplayer2.PlaybackException) {
                        Log.e("MainActivity", "=== PLAYBACK ERROR ===")
                        Log.e("MainActivity", "Error type: ${error.errorCode}")
                        Log.e("MainActivity", "Error message: ${error.message}")
                        Log.e("MainActivity", "Error cause: ${error.cause?.message}")
                        Log.e("MainActivity", "Error cause type: ${error.cause?.javaClass?.simpleName}")
                        Log.e("MainActivity", "Playback attempt: ${playbackAttempts + 1}")

                        // Check if this is a decoder-related error
                        val isDecoderError = when {
                            // Check for specific MediaCodec decoder exceptions
                            error.cause is com.google.android.exoplayer2.mediacodec.MediaCodecDecoderException -> {
                                Log.e("MainActivity", "MediaCodecDecoderException detected")
                                true
                            }
                            error.cause is com.google.android.exoplayer2.video.MediaCodecVideoDecoderException -> {
                                Log.e("MainActivity", "MediaCodecVideoDecoderException detected")
                                true
                            }
                            // Check error codes
                            error.errorCode == com.google.android.exoplayer2.PlaybackException.ERROR_CODE_DECODING_FAILED -> {
                                Log.e("MainActivity", "ERROR_CODE_DECODING_FAILED")
                                true
                            }
                            error.errorCode == com.google.android.exoplayer2.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {
                                Log.e("MainActivity", "ERROR_CODE_DECODER_INIT_FAILED")
                                true
                            }
                            // Check error messages
                            error.message?.contains("decoder", ignoreCase = true) == true ||
                            error.message?.contains("codec", ignoreCase = true) == true -> {
                                Log.e("MainActivity", "Decoder/codec keyword in error message")
                                true
                            }
                            error.cause?.message?.contains("decoder", ignoreCase = true) == true ||
                            error.cause?.message?.contains("codec", ignoreCase = true) == true -> {
                                Log.e("MainActivity", "Decoder/codec keyword in error cause")
                                true
                            }
                            else -> {
                                Log.e("MainActivity", "Not a decoder error")
                                false
                            }
                        }

                        // Implement fallback: try software decoder if hardware failed
                        if (isDecoderError && playbackAttempts == 0) {
                            Log.w("MainActivity", "🔄 Hardware decoder failed - attempting SOFTWARE decoder fallback...")
                            playbackAttempts++

                            mainHandler.post {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Hardware decoder failed. Trying software decoder...",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Retry with software decoder
                                retryWithSoftwareDecoder()
                            }
                        } else {
                            // Already tried software or not a decoder error - show error
                            val errorMsg = if (playbackAttempts > 0) {
                                "Video playback failed (both hardware & software decoders tried)"
                            } else {
                                "Video playback error: ${error.message}"
                            }

                            Log.e("MainActivity", "❌ Final error: $errorMsg")

                            mainHandler.post {
                                Toast.makeText(
                                    this@MainActivity,
                                    errorMsg,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    override fun onVideoSizeChanged(videoSize: com.google.android.exoplayer2.video.VideoSize) {
                        Log.d("MainActivity", "=== VIDEO SIZE CHANGED ===")
                        Log.d("MainActivity", "Resolution: ${videoSize.width}x${videoSize.height}")
                        Log.d("MainActivity", "Pixel aspect ratio: ${videoSize.pixelWidthHeightRatio}")
                    }
                })

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
                Log.d("MainActivity", "✓ Video playback initiated")
            } else {
                Log.e("MainActivity", "Video file does not exist: ${videoFile.absolutePath}")
                mainHandler.post {
                    Toast.makeText(
                        this,
                        "Error: Video file not found",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading local video: ${e.message}", e)
            mainHandler.post {
                Toast.makeText(
                    this,
                    "Error loading video: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    private fun retryWithSoftwareDecoder() {
        try {
            Log.d("MainActivity", "=== RETRYING WITH SOFTWARE DECODER ===")

            // Release old player
            exoPlayer.release()

            // Create new player with software decoder preference
            exoPlayer = createExoPlayerWithDecoder(preferSoftwareDecoder = true)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            // Retry loading the video
            currentSurface?.let { surface ->
                currentVideoUrl?.let { url ->
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        loadVideoFromUrl(surface, url)
                    } else {
                        loadLocalVideo(surface, url)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error retrying with software decoder: ${e.message}", e)
        }
    }

    private fun loadVideoFromUrl(surface: Surface, url: String) {
        try {
            Log.d("MainActivity", "Loading video from URL: $url")
            val videoUri = Uri.parse(url)
            val mediaItem = MediaItem.fromUri(videoUri)

            // All ExoPlayer calls on main thread
            exoPlayer.setVideoSurface(surface)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            Log.d("MainActivity", "Video streaming from: $url")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error streaming video: ${e.message}", e)
        }
    }

    private fun logKeyEvent(keyCode: Int, event: KeyEvent) {
        if (!inputLoggingEnabled) return
        val codeName = KeyEvent.keyCodeToString(keyCode)
        val src = event.source
        val srcs = buildList {
            if ((src and InputDevice.SOURCE_KEYBOARD) != 0) add("KEYBOARD")
            if ((src and InputDevice.SOURCE_DPAD) != 0) add("DPAD")
            if ((src and InputDevice.SOURCE_GAMEPAD) != 0) add("GAMEPAD")
            if ((src and InputDevice.SOURCE_JOYSTICK) != 0) add("JOYSTICK")
        }.joinToString("|")
        val dev = event.device
        val devInfo = if (dev != null) "devId=${dev.id}, name=${dev.name}, vendor=${dev.vendorId}, product=${dev.productId}" else "dev=null"
        Log.d(
            "InputLogger",
            "DOWN code=$keyCode($codeName), scan=${event.scanCode}, repeat=${event.repeatCount}, meta=${event.metaState}, from=[$srcs], $devInfo"
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let { logKeyEvent(keyCode, it) }

        // Handle keystone menu navigation FIRST
        if (keystoneMenuVisible) {
            return when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    selectedMenuIndex = (selectedMenuIndex - 1 + menuOptions.size) % menuOptions.size
                    updateMenuHighlight()
                    true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    selectedMenuIndex = (selectedMenuIndex + 1) % menuOptions.size
                    updateMenuHighlight()
                    true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    handleMenuSelection()
                    true
                }
                KeyEvent.KEYCODE_BACK -> {
                    hideKeystoneMenu()
                    true
                }
                else -> true // Consume all keys when menu is visible
            }
        }

        // Handle corner adjustment mode (when adjusting a specific corner)
        if (cornerEditMode) {
            return when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    adjustCorner(selectedCorner, dy = warpAdjustmentStep)
                    true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    adjustCorner(selectedCorner, dy = -warpAdjustmentStep)
                    true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    adjustCorner(selectedCorner, dx = -warpAdjustmentStep)
                    true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    adjustCorner(selectedCorner, dx = warpAdjustmentStep)
                    true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    // Return to menu
                    showKeystoneMenu()
                    true
                }
                KeyEvent.KEYCODE_BACK -> {
                    // Exit corner adjustment
                    hideKeystoneMenu()
                    true
                }
                else -> super.onKeyDown(keyCode, event)
            }
        }

        // Normal playback mode - D-Pad Center opens keystone menu
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                showKeystoneMenu()
                true
            }

            // Development keyboard shortcuts
            KeyEvent.KEYCODE_V -> {
                // V key = open menu (for keyboard testing)
                showKeystoneMenu()
                true
            }

            KeyEvent.KEYCODE_F1 -> {
                inputLoggingEnabled = !inputLoggingEnabled
                Log.d("InputLogger", "inputLoggingEnabled=$inputLoggingEnabled")
                true
            }

            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_0 -> {
                // Reset warp shape (keyboard only)
                currentWarpShape = WarpShape()
                renderer.setWarpShape(currentWarpShape)
                glSurfaceView.requestRender()
                Toast.makeText(this, "Keystone reset to default", Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", "Warp shape reset")
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun adjustCorner(corner: Corner, dx: Float = 0f, dy: Float = 0f) {
        // Directly adjust the X and Y coordinates of the selected corner
        // dx = horizontal movement (left/right)
        // dy = vertical adjustment (up/down)
        Log.d("MainActivity", ">>> ADJUSTING CORNER: $corner, dx=$dx, dy=$dy")

        currentWarpShape = when (corner) {
            Corner.TOP_LEFT -> currentWarpShape.copy(
                topLeftX = currentWarpShape.topLeftX + dx,
                topLeftY = currentWarpShape.topLeftY + dy
            )
            Corner.TOP_RIGHT -> currentWarpShape.copy(
                topRightX = currentWarpShape.topRightX + dx,
                topRightY = currentWarpShape.topRightY + dy
            )
            Corner.BOTTOM_LEFT -> currentWarpShape.copy(
                bottomLeftX = currentWarpShape.bottomLeftX + dx,
                bottomLeftY = currentWarpShape.bottomLeftY + dy
            )
            Corner.BOTTOM_RIGHT -> currentWarpShape.copy(
                bottomRightX = currentWarpShape.bottomRightX + dx,
                bottomRightY = currentWarpShape.bottomRightY + dy
            )
        }
        renderer.setWarpShape(currentWarpShape)
        glSurfaceView.requestRender()
        Log.d("MainActivity", "<<< Corner adjusted: $corner, shape: $currentWarpShape")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
        try {
            exoPlayer.pause()
            glSurfaceView.onPause()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onPause: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
        try {
            glSurfaceView.onResume()
            exoPlayer.play()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onResume: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
        try {
            exoPlayer.release()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error releasing ExoPlayer: ${e.message}")
        }
    }
}

