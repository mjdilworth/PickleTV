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
import android.view.MotionEvent
import android.view.Surface
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import java.io.File
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    private lateinit var exoPlayer: Player
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: VideoGLRenderer
    private lateinit var warpShapeManager: WarpShapeManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentWarpShape = WarpShape()
    private val warpAdjustmentStep = 0.05f

    private var cornerEditMode: Boolean = false
    private var selectedCorner: Corner = Corner.TOP_LEFT

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

    private fun logKeyEvent(phase: String, keyCode: Int, event: KeyEvent) {
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
            "$phase code=$keyCode($codeName), scan=${event.scanCode}, repeat=${event.repeatCount}, meta=${event.metaState}, from=[$srcs], $devInfo"
        )
    }

    private fun logMotionEvent(event: MotionEvent): Boolean {
        if (!inputLoggingEnabled) return false
        val src = event.source
        val isJoyLike = (src and (InputDevice.SOURCE_JOYSTICK or InputDevice.SOURCE_GAMEPAD)) != 0
        if (!isJoyLike) return false
        val axes = intArrayOf(
            MotionEvent.AXIS_X, MotionEvent.AXIS_Y,
            MotionEvent.AXIS_Z, MotionEvent.AXIS_RZ,
            MotionEvent.AXIS_RX, MotionEvent.AXIS_RY,
            MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_HAT_Y,
            MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_RTRIGGER,
            MotionEvent.AXIS_GAS, MotionEvent.AXIS_BRAKE
        )
        val sb = StringBuilder("Axes:")
        var any = false
        for (a in axes) {
            val v = event.getAxisValue(a)
            if (kotlin.math.abs(v) > 0.1f) {
                sb.append(" ").append(MotionEvent.axisToString(a)).append("=").append(String.format(Locale.US, "%.2f", v))
                any = true
            }
        }
        if (any) {
            val dev = event.device
            Log.d(
                "InputLogger",
                "MOTION src=$src, devId=${dev?.id} name=${dev?.name} — ${sb}"
            )
        }
        return any
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let { logKeyEvent("DOWN", keyCode, it) }

        // Development-only keyboard aliases
        if (isDebugBuild && event != null && handleDevKeyAliases(keyCode, event)) {
            return true
        }

        return when (keyCode) {
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

    private fun handleDevKeyAliases(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            // SPACE toggles play/pause AND toggles edit mode accordingly
            KeyEvent.KEYCODE_SPACE -> {
                togglePlayPauseAndEditMode()
                return true
            }
            // P toggles corner edit mode
            KeyEvent.KEYCODE_P -> {
                cornerEditMode = !cornerEditMode
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Corner edit mode: $cornerEditMode, selected=$selectedCorner")
                return true
            }
            // N advances selected corner; Shift+Tab handled in main handler above
            KeyEvent.KEYCODE_N -> {
                selectedCorner = selectedCorner.next()
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Selected corner: $selectedCorner")
                return true
            }
            // Direct corner selection with number keys 1..4
            KeyEvent.KEYCODE_1 -> {
                selectedCorner = Corner.TOP_LEFT
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Selected corner: $selectedCorner")
                return true
            }
            KeyEvent.KEYCODE_2 -> {
                selectedCorner = Corner.TOP_RIGHT
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Selected corner: $selectedCorner")
                return true
            }
            KeyEvent.KEYCODE_3 -> {
                selectedCorner = Corner.BOTTOM_RIGHT
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Selected corner: $selectedCorner")
                return true
            }
            KeyEvent.KEYCODE_4 -> {
                selectedCorner = Corner.BOTTOM_LEFT
                glSurfaceView.requestRender()
                Log.d("MainActivity", "[DEV] Selected corner: $selectedCorner")
                return true
            }
            // WASD fine adjustments when in corner edit mode
            KeyEvent.KEYCODE_W -> {
                if (cornerEditMode) { adjustCorner(selectedCorner, dy = -warpAdjustmentStep); return true }
            }
            KeyEvent.KEYCODE_S -> {
                if (cornerEditMode) { adjustCorner(selectedCorner, dy = warpAdjustmentStep); return true }
            }
            KeyEvent.KEYCODE_A -> {
                if (cornerEditMode) { adjustCorner(selectedCorner, dx = -warpAdjustmentStep); return true }
            }
            KeyEvent.KEYCODE_D -> {
                if (cornerEditMode) { adjustCorner(selectedCorner, dx = warpAdjustmentStep); return true }
            }
        }
        return false
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

    private fun togglePlayPause() {
        try {
            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        } catch (e: Exception) {
            Log.w("MainActivity", "togglePlayPause failed: ${e.message}")
        }
    }

    private fun adjustWarp(adjustment: WarpAdjustment) {
        currentWarpShape = when (adjustment) {
            WarpAdjustment.TOP_UP -> currentWarpShape.copy(
                topLeft = currentWarpShape.topLeft - warpAdjustmentStep,
                topRight = currentWarpShape.topRight + warpAdjustmentStep
            )
            WarpAdjustment.BOTTOM_DOWN -> currentWarpShape.copy(
                bottomLeft = currentWarpShape.bottomLeft + warpAdjustmentStep,
                bottomRight = currentWarpShape.bottomRight - warpAdjustmentStep
            )
            WarpAdjustment.LEFT_OUT -> currentWarpShape.copy(
                topLeft = currentWarpShape.topLeft + warpAdjustmentStep,
                bottomLeft = currentWarpShape.bottomLeft + warpAdjustmentStep
            )
            WarpAdjustment.RIGHT_OUT -> currentWarpShape.copy(
                topRight = currentWarpShape.topRight - warpAdjustmentStep,
                bottomRight = currentWarpShape.bottomRight - warpAdjustmentStep
            )
        }

        renderer.setWarpShape(currentWarpShape)
        glSurfaceView.requestRender()

        Log.d("MainActivity", "Warp adjusted: $adjustment, shape: $currentWarpShape")
    }

    private fun adjustCorner(corner: Corner, dx: Float = 0f, dy: Float = 0f) {
        // Our warp offsets are horizontal per-edge; approximate vertical with opposing edges
        currentWarpShape = when (corner) {
            Corner.TOP_LEFT -> currentWarpShape.copy(
                topLeft = currentWarpShape.topLeft + dx,
                // emulate vertical by tilting top edge inward/outward
                topRight = currentWarpShape.topRight - dy
            )
            Corner.TOP_RIGHT -> currentWarpShape.copy(
                topRight = currentWarpShape.topRight + dx,
                topLeft = currentWarpShape.topLeft + dy
            )
            Corner.BOTTOM_LEFT -> currentWarpShape.copy(
                bottomLeft = currentWarpShape.bottomLeft + dx,
                bottomRight = currentWarpShape.bottomRight + dy
            )
            Corner.BOTTOM_RIGHT -> currentWarpShape.copy(
                bottomRight = currentWarpShape.bottomRight + dx,
                bottomLeft = currentWarpShape.bottomLeft - dy
            )
        }
        renderer.setWarpShape(currentWarpShape)
        glSurfaceView.requestRender()
        Log.d("MainActivity", "Corner adjusted: $corner, dx=$dx, dy=$dy -> $currentWarpShape")
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
