package com.example.pickletv

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
import android.widget.FrameLayout
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

    private var cornerEditMode: Boolean = false
    private var selectedCorner: Corner = Corner.TOP_LEFT
    private var overlayGridVisible: Boolean = false
    private var showStepSizeIndicator: Boolean = false

    // Adjustment step sizes for different modes
    private var warpAdjustmentStep = 0.05f
    private var largeAdjustmentStep = 0.20f  // For fast adjustments

    private var inputLoggingEnabled: Boolean = true
    private var isDebugBuild: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isDebugBuild = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        Log.d("MainActivity", "onCreate called")

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
        setContentView(container)

        // Initialize ExoPlayer
        initializeExoPlayer()
    }

    private fun initializeExoPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

        // Register surface texture callback to provide Surface to ExoPlayer
        renderer.registerSurfaceTextureCallback { surface ->
            setupExoPlayerWithSurface(surface)
        }

        // Update warp shape in renderer
        renderer.setWarpShape(currentWarpShape)
    }

    private fun setupExoPlayerWithSurface(surface: Surface) {
        // Post to main thread to avoid "Player is accessed on the wrong thread" error
        mainHandler.post {
            try {
                // Find the video file
                val videoFile = findVideoFile()
                if (videoFile != null && videoFile.exists()) {
                    loadVideo(surface, videoFile.absolutePath)
                } else {
                    Log.e("MainActivity", "Video file not found: h-6.mp4")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting up ExoPlayer: ${e.message}", e)
            }
        }
    }

    private fun findVideoFile(): File? {
        val fileName = BuildConfig.DEBUG_VIDEO_FILE_NAME
        val possiblePaths = mutableListOf<File>()

        // 1. App-specific external cache directory (where the script pushes to)
        externalCacheDir?.let { possiblePaths.add(File(it, fileName)) }

        // 2. App cache directory
        possiblePaths.add(File(cacheDir, fileName))

        // 3. App files directory
        possiblePaths.add(File(filesDir, fileName))

        // 4. App-specific external files directory
        getExternalFilesDir(null)?.let { possiblePaths.add(File(it, fileName)) }

        // 5. App files parent directory (for dev builds)
        filesDir.parentFile?.let { possiblePaths.add(File(it, fileName)) }

        Log.d("MainActivity", "Searching for video in ${possiblePaths.size} locations:")
        for (path in possiblePaths) {
            Log.d("MainActivity", "  - Checking: ${path.absolutePath}")
            if (path.exists() && path.isFile) {
                Log.d("MainActivity", "✓ Found video at: ${path.absolutePath}")
                return path
            }
        }

        Log.w("MainActivity", "Video file ${fileName} not found in any location")
        Log.w("MainActivity", "Please run: ./tools/push_video.sh")
        return null
    }

    private fun loadVideo(surface: Surface, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e("MainActivity", "Video file does not exist: $filePath")
                return
            }

            val videoUri = Uri.fromFile(file)
            val mediaItem = MediaItem.fromUri(videoUri)

            // All ExoPlayer calls on main thread
            exoPlayer.setVideoSurface(surface)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            Log.d("MainActivity", "Video loaded and playing: $filePath")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading video: ${e.message}", e)
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

        // Development-only keyboard aliases and enhanced mappings (CHECK FIRST!)
        if (isDebugBuild && event != null) {
            val devResult = handleDevKeyboardMappings(keyCode, event)
            if (devResult) {
                return true  // Dev handler handled it
            }
        }

        return when (keyCode) {
            // ====== LEGACY REMOTE/BUTTON MAPPINGS (Production) ======

            // Toggle verbose input logging
            KeyEvent.KEYCODE_F1 -> {
                inputLoggingEnabled = !inputLoggingEnabled
                Log.d("InputLogger", "inputLoggingEnabled=$inputLoggingEnabled")
                true
            }

            // Toggle corner edit mode (show/hide corner highlights)
            KeyEvent.KEYCODE_C -> {
                cornerEditMode = !cornerEditMode
                glSurfaceView.requestRender()
                Log.d("MainActivity", "Corner edit mode: $cornerEditMode, selected=$selectedCorner")
                true
            }

            // Cycle selected corner (Tab key)
            KeyEvent.KEYCODE_TAB -> {
                if (event?.isShiftPressed == true) {
                    selectedCorner = prevCorner(selectedCorner)
                } else {
                    selectedCorner = selectedCorner.next()
                }
                glSurfaceView.requestRender()
                Log.d("MainActivity", "Selected corner: $selectedCorner")
                true
            }

            // DPAD adjusts either whole warp (legacy) or selected corner when in corner mode
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (cornerEditMode) adjustCorner(selectedCorner, dy = -warpAdjustmentStep) else adjustWarp(WarpAdjustment.TOP_UP)
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (cornerEditMode) adjustCorner(selectedCorner, dy = warpAdjustmentStep) else adjustWarp(WarpAdjustment.BOTTOM_DOWN)
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (cornerEditMode) adjustCorner(selectedCorner, dx = -warpAdjustmentStep) else adjustWarp(WarpAdjustment.LEFT_OUT)
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (cornerEditMode) adjustCorner(selectedCorner, dx = warpAdjustmentStep) else adjustWarp(WarpAdjustment.RIGHT_OUT)
                true
            }

            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_BUTTON_A -> {
                // Save warp shape on confirm
                warpShapeManager.saveWarpShape(currentWarpShape)
                Log.d("MainActivity", "Warp shape saved: $currentWarpShape")
                true
            }

            KeyEvent.KEYCODE_DEL -> {
                // Reset warp shape
                currentWarpShape = WarpShape()
                renderer.setWarpShape(currentWarpShape)
                glSurfaceView.requestRender()
                Log.d("MainActivity", "Warp shape reset")
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun handleDevKeyboardMappings(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            // ====== PLAY / PAUSE CONTROLS ======
            KeyEvent.KEYCODE_P -> {
                togglePlayPauseAndEditMode()
                true
            }
            KeyEvent.KEYCODE_SPACE -> {
                togglePlayPauseAndEditMode()
                true
            }

            // ====== DIRECTIONAL CONTROLS (Arrow Keys) ======
            // Arrow keys map to DPAD for corner adjustment
            // UP/DOWN = vertical movement, LEFT/RIGHT = horizontal movement
            KeyEvent.KEYCODE_DPAD_UP -> {
                Log.d("MainActivity", "[KEY] DPAD_UP pressed, cornerEditMode=$cornerEditMode, selectedCorner=$selectedCorner")
                if (cornerEditMode) {
                    adjustCorner(selectedCorner, dy = warpAdjustmentStep)  // UP = positive Y
                } else {
                    adjustWarp(WarpAdjustment.TOP_UP)
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                Log.d("MainActivity", "[KEY] DPAD_DOWN pressed, cornerEditMode=$cornerEditMode, selectedCorner=$selectedCorner")
                if (cornerEditMode) {
                    adjustCorner(selectedCorner, dy = -warpAdjustmentStep)  // DOWN = negative Y
                } else {
                    adjustWarp(WarpAdjustment.BOTTOM_DOWN)
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                Log.d("MainActivity", "[KEY] DPAD_LEFT pressed, cornerEditMode=$cornerEditMode, selectedCorner=$selectedCorner")
                if (cornerEditMode) {
                    adjustCorner(selectedCorner, dx = -warpAdjustmentStep)
                } else {
                    adjustWarp(WarpAdjustment.LEFT_OUT)
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                Log.d("MainActivity", "[KEY] DPAD_RIGHT pressed, cornerEditMode=$cornerEditMode, selectedCorner=$selectedCorner")
                if (cornerEditMode) {
                    adjustCorner(selectedCorner, dx = warpAdjustmentStep)
                } else {
                    adjustWarp(WarpAdjustment.RIGHT_OUT)
                }
                true
            }

            // ====== CORNER SELECTION & CYCLING ======
            KeyEvent.KEYCODE_TAB -> {
                if (event?.isShiftPressed == true) {
                    selectedCorner = prevCorner(selectedCorner)
                } else {
                    selectedCorner = selectedCorner.next()
                }
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Selected corner: $selectedCorner")
                true
            }

            // N advances selected corner
            KeyEvent.KEYCODE_N -> {
                selectedCorner = selectedCorner.next()
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Selected corner: $selectedCorner")
                true
            }

            // Direct corner selection with number keys 1,2,3,4
            // 1 = TOP_LEFT, 2 = TOP_RIGHT, 3 = BOTTOM_RIGHT, 4 = BOTTOM_LEFT
            KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> {
                selectedCorner = Corner.TOP_LEFT
                cornerEditMode = true
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Snap: TOP_LEFT (1) selected")
                true
            }
            KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> {
                selectedCorner = Corner.TOP_RIGHT
                cornerEditMode = true
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Snap: TOP_RIGHT (2) selected")
                true
            }
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> {
                selectedCorner = Corner.BOTTOM_RIGHT
                cornerEditMode = true
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Snap: BOTTOM_RIGHT (3) selected")
                true
            }
            KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> {
                selectedCorner = Corner.BOTTOM_LEFT
                cornerEditMode = true
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Snap: BOTTOM_LEFT (4) selected")
                true
            }

            // 0 = Center helper (reset all)
            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> {
                currentWarpShape = WarpShape()
                renderer.setWarpShape(currentWarpShape)
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Snap: Reset to center")
                true
            }

            // ====== STEP SIZE CONTROLS (] and [ keys) ======
            // Right bracket ] increases step size
            KeyEvent.KEYCODE_RIGHT_BRACKET -> {
                warpAdjustmentStep = (warpAdjustmentStep + 0.05f).coerceAtMost(0.50f)
                largeAdjustmentStep = warpAdjustmentStep * 4
                showStepSizeIndicator = true
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Step size increased: $warpAdjustmentStep")
                true
            }

            // Left bracket [ decreases step size
            KeyEvent.KEYCODE_LEFT_BRACKET -> {
                warpAdjustmentStep = (warpAdjustmentStep - 0.05f).coerceAtLeast(0.01f)
                largeAdjustmentStep = warpAdjustmentStep * 4
                showStepSizeIndicator = true
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Step size decreased: $warpAdjustmentStep")
                true
            }

            // ====== WHOLE SHAPE MOVEMENT (Page Up/Down) ======
            // Page Up = Move shape up (vertical adjustment for all corners)
            KeyEvent.KEYCODE_PAGE_UP -> {
                currentWarpShape = currentWarpShape.copy(
                    topLeftY = currentWarpShape.topLeftY + largeAdjustmentStep,
                    topRightY = currentWarpShape.topRightY + largeAdjustmentStep,
                    bottomLeftY = currentWarpShape.bottomLeftY + largeAdjustmentStep,
                    bottomRightY = currentWarpShape.bottomRightY + largeAdjustmentStep
                )
                renderer.setWarpShape(currentWarpShape)
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Moved shape up: $currentWarpShape")
                true
            }

            // Page Down = Move shape down
            KeyEvent.KEYCODE_PAGE_DOWN -> {
                currentWarpShape = currentWarpShape.copy(
                    topLeftY = currentWarpShape.topLeftY - largeAdjustmentStep,
                    topRightY = currentWarpShape.topRightY - largeAdjustmentStep,
                    bottomLeftY = currentWarpShape.bottomLeftY - largeAdjustmentStep,
                    bottomRightY = currentWarpShape.bottomRightY - largeAdjustmentStep
                )
                renderer.setWarpShape(currentWarpShape)
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Moved shape down: $currentWarpShape")
                true
            }

            // ====== OVERLAY GRID / HUD TOGGLE (M key) ======
            KeyEvent.KEYCODE_M -> {
                overlayGridVisible = !overlayGridVisible
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Overlay grid: $overlayGridVisible")
                true
            }

            // ====== EXIT EDIT MODE (Escape key) ======
            KeyEvent.KEYCODE_ESCAPE -> {
                cornerEditMode = false
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Exited edit mode")
                true
            }

            // ====== VOLUME +/- FOR HORIZONTAL FRAME MOVEMENT ======
            // Volume Up = Move entire frame right
            KeyEvent.KEYCODE_VOLUME_UP -> {
                currentWarpShape = currentWarpShape.copy(
                    topLeftX = currentWarpShape.topLeftX + warpAdjustmentStep,
                    topRightX = currentWarpShape.topRightX + warpAdjustmentStep,
                    bottomLeftX = currentWarpShape.bottomLeftX + warpAdjustmentStep,
                    bottomRightX = currentWarpShape.bottomRightX + warpAdjustmentStep
                )
                renderer.setWarpShape(currentWarpShape)
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Moved frame right: $currentWarpShape")
                true
            }

            // Volume Down = Move entire frame left
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                currentWarpShape = currentWarpShape.copy(
                    topLeftX = currentWarpShape.topLeftX - warpAdjustmentStep,
                    topRightX = currentWarpShape.topRightX - warpAdjustmentStep,
                    bottomLeftX = currentWarpShape.bottomLeftX - warpAdjustmentStep,
                    bottomRightX = currentWarpShape.bottomRightX - warpAdjustmentStep
                )
                renderer.setWarpShape(currentWarpShape)
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Moved frame left: $currentWarpShape")
                true
            }

            // ====== EDIT MODE TOGGLE (E key) ======
            KeyEvent.KEYCODE_E -> {
                cornerEditMode = !cornerEditMode
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Edit mode: $cornerEditMode")
                true
            }

            // ====== SAVE / CONFIRM (Enter or Space already handled above) ======
            KeyEvent.KEYCODE_ENTER -> {
                warpShapeManager.saveWarpShape(currentWarpShape)
                Log.d("MainActivity", "[DEV] Warp shape saved: $currentWarpShape")
                true
            }

            // ====== RESET (R key) ======
            KeyEvent.KEYCODE_R -> {
                currentWarpShape = WarpShape()
                renderer.setWarpShape(currentWarpShape)
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Reset warp shape")
                true
            }

            // ====== FINE ADJUSTMENTS (Shift + Arrow keys for smaller steps) ======
            else -> {
                // Only handle Shift modifiers for fine adjustments on non-DPAD keys
                false
            }
        }
    }

    private fun togglePlayPauseAndEditMode() {
        try {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                cornerEditMode = true
                Log.d("MainActivity", "[DEV] Paused + entered edit mode")
            } else {
                exoPlayer.play()
                cornerEditMode = false
                Log.d("MainActivity", "[DEV] Playing + exited edit mode")
            }
            glSurfaceView.requestRender()
        } catch (e: Exception) {
            Log.w("MainActivity", "togglePlayPauseAndEditMode failed: ${e.message}")
        }
    }

    private fun prevCorner(c: Corner): Corner = when (c) {
        Corner.TOP_LEFT -> Corner.BOTTOM_LEFT
        Corner.BOTTOM_LEFT -> Corner.BOTTOM_RIGHT
        Corner.BOTTOM_RIGHT -> Corner.TOP_RIGHT
        Corner.TOP_RIGHT -> Corner.TOP_LEFT
    }

    private fun adjustWarp(adjustment: WarpAdjustment) {
        currentWarpShape = when (adjustment) {
            WarpAdjustment.TOP_UP -> currentWarpShape.copy(
                topLeftX = currentWarpShape.topLeftX - warpAdjustmentStep,
                topRightX = currentWarpShape.topRightX + warpAdjustmentStep
            )
            WarpAdjustment.BOTTOM_DOWN -> currentWarpShape.copy(
                bottomLeftX = currentWarpShape.bottomLeftX + warpAdjustmentStep,
                bottomRightX = currentWarpShape.bottomRightX - warpAdjustmentStep
            )
            WarpAdjustment.LEFT_OUT -> currentWarpShape.copy(
                topLeftX = currentWarpShape.topLeftX + warpAdjustmentStep,
                bottomLeftX = currentWarpShape.bottomLeftX + warpAdjustmentStep
            )
            WarpAdjustment.RIGHT_OUT -> currentWarpShape.copy(
                topRightX = currentWarpShape.topRightX - warpAdjustmentStep,
                bottomRightX = currentWarpShape.bottomRightX - warpAdjustmentStep
            )
        }

        renderer.setWarpShape(currentWarpShape)
        glSurfaceView.requestRender()

        Log.d("MainActivity", "Warp adjusted: $adjustment, shape: $currentWarpShape")
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

enum class WarpAdjustment {
    TOP_UP, BOTTOM_DOWN, LEFT_OUT, RIGHT_OUT
}
