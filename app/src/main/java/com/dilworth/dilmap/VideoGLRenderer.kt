package com.dilworth.dilmap

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLES11Ext
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

const val VERTEX_SHADER_CODE = """
    attribute vec4 position;
    attribute vec2 texCoord;
    
    varying vec2 outTexCoord;
    
    uniform mat4 projection;
    uniform mat4 texMatrix;
    uniform float topLeftX;
    uniform float topLeftY;
    uniform float topRightX;
    uniform float topRightY;
    uniform float bottomLeftX;
    uniform float bottomLeftY;
    uniform float bottomRightX;
    uniform float bottomRightY;
    
    void main() {
        vec4 warpedPos = position;
        
        // Normalized texture coordinates (0-1)
        float u = (texCoord.x);  // 0 to 1 across width
        float v = (1.0 - texCoord.y);  // 0 to 1 across height (flip Y)
        
        // Bilinear interpolation between corners
        // Top edge: interpolate between topLeft and topRight
        float topX = mix(topLeftX, topRightX, u);
        float topY = mix(topLeftY, topRightY, u);
        
        // Bottom edge: interpolate between bottomLeft and bottomRight
        float bottomX = mix(bottomLeftX, bottomRightX, u);
        float bottomY = mix(bottomLeftY, bottomRightY, u);
        
        // Interpolate between top and bottom based on v
        float finalX = mix(topX, bottomX, v);
        float finalY = mix(topY, bottomY, v);
        
        warpedPos.x = finalX;
        warpedPos.y = finalY;
        
        gl_Position = projection * warpedPos;
        // Apply SurfaceTexture transform to correct orientation/crop
        outTexCoord = (texMatrix * vec4(texCoord, 0.0, 1.0)).xy;
    }
"""

const val FRAGMENT_SHADER_CODE = """
    #extension GL_OES_EGL_image_external : require
    precision mediump float;
    
    varying vec2 outTexCoord;
    
    uniform samplerExternalOES videoTexture;
    
    void main() {
        gl_FragColor = texture2D(videoTexture, outTexCoord);
    }
"""

class VideoGLRenderer : GLSurfaceView.Renderer {
    private var videoTexture = 0
    private var programHandle = 0
    private var vertexBuffer: FloatBuffer? = null
    private var texCoordBuffer: FloatBuffer? = null

    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private val onSurfaceTextureAvailable = mutableListOf<(Surface) -> Unit>()

    private var onFrameAvailableRenderRequest: (() -> Unit)? = null

    private var warpShape = WarpShape()
    private val projectionMatrix = FloatArray(16)
    private val texTransformMatrix = FloatArray(16)

    // Attribute/uniform handles
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var projectionHandle = 0
    private var texMatrixHandle = 0
    private var videoTextureHandle = 0

    // Corner coordinate uniforms (X, Y for each corner)
    private var topLeftXHandle = 0
    private var topLeftYHandle = 0
    private var topRightXHandle = 0
    private var topRightYHandle = 0
    private var bottomLeftXHandle = 0
    private var bottomLeftYHandle = 0
    private var bottomRightXHandle = 0
    private var bottomRightYHandle = 0

    // Corner highlighting
    private var cornerHighlightProvider: (() -> Pair<Boolean, Corner>)? = null

    // Small program for drawing corner markers
    private var markerProgram = 0
    private var markerPosHandle = 0
    private var markerColorHandle = 0
    private var markerProjHandle = 0

    private val warpScale = 0.1f

    fun setWarpShape(newWarpShape: WarpShape) {
        this.warpShape = newWarpShape
    }

    fun registerSurfaceTextureCallback(callback: (Surface) -> Unit) {
        onSurfaceTextureAvailable.add(callback)
    }

    fun setOnFrameAvailableRenderRequest(callback: () -> Unit) {
        onFrameAvailableRenderRequest = callback
    }

    fun setCornerHighlightProvider(provider: () -> Pair<Boolean, Corner>) {
        cornerHighlightProvider = provider
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set background color
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        // Create shader program
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        programHandle = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)

            // Verify link
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                Log.e(
                    "VideoGLRenderer",
                    "Program link error: ${GLES20.glGetProgramInfoLog(it)}"
                )
            }
        }

        // After creating main program, create a tiny program for markers
        markerProgram = GLES20.glCreateProgram().also { prog ->
            val vsh = loadShader(
                GLES20.GL_VERTEX_SHADER,
                "attribute vec4 aPos; uniform mat4 uProj; void main(){ gl_Position = uProj * aPos; }"
            )
            val fsh = loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                "precision mediump float; uniform vec4 uColor; void main(){ gl_FragColor = uColor; }"
            )
            GLES20.glAttachShader(prog, vsh)
            GLES20.glAttachShader(prog, fsh)
            GLES20.glLinkProgram(prog)
            markerPosHandle = GLES20.glGetAttribLocation(prog, "aPos")
            markerColorHandle = GLES20.glGetUniformLocation(prog, "uColor")
            markerProjHandle = GLES20.glGetUniformLocation(prog, "uProj")
        }

        // Get attribute/uniform locations
        positionHandle = GLES20.glGetAttribLocation(programHandle, "position")
        texCoordHandle = GLES20.glGetAttribLocation(programHandle, "texCoord")
        projectionHandle = GLES20.glGetUniformLocation(programHandle, "projection")
        texMatrixHandle = GLES20.glGetUniformLocation(programHandle, "texMatrix")
        videoTextureHandle = GLES20.glGetUniformLocation(programHandle, "videoTexture")

        // Get corner coordinate uniform locations
        topLeftXHandle = GLES20.glGetUniformLocation(programHandle, "topLeftX")
        topLeftYHandle = GLES20.glGetUniformLocation(programHandle, "topLeftY")
        topRightXHandle = GLES20.glGetUniformLocation(programHandle, "topRightX")
        topRightYHandle = GLES20.glGetUniformLocation(programHandle, "topRightY")
        bottomLeftXHandle = GLES20.glGetUniformLocation(programHandle, "bottomLeftX")
        bottomLeftYHandle = GLES20.glGetUniformLocation(programHandle, "bottomLeftY")
        bottomRightXHandle = GLES20.glGetUniformLocation(programHandle, "bottomRightX")
        bottomRightYHandle = GLES20.glGetUniformLocation(programHandle, "bottomRightY")

        // Create vertex buffers
        setupVertexBuffers()

        // Create external OES texture for video
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        videoTexture = textures[0]

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoTexture)
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // Create surface texture bound to the external OES texture
        surfaceTexture = SurfaceTexture(videoTexture).apply {
            setOnFrameAvailableListener {
                // Request a render when a new decoded frame is available
                onFrameAvailableRenderRequest?.invoke()
            }
        }

        surface = Surface(surfaceTexture)
        surface?.let { s ->
            onSurfaceTextureAvailable.forEach { it(s) }
        }
        Log.d("VideoGLRenderer", "SurfaceTexture created")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        // Set up orthographic projection
        Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        surfaceTexture?.updateTexImage()
        surfaceTexture?.getTransformMatrix(texTransformMatrix)

        GLES20.glUseProgram(programHandle)

        // Set up vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle)
        vertexBuffer?.let {
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, it)
        }

        GLES20.glEnableVertexAttribArray(texCoordHandle)
        texCoordBuffer?.let {
            GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, it)
        }

        // Set up uniforms
        GLES20.glUniformMatrix4fv(projectionHandle, 1, false, projectionMatrix, 0)
        GLES20.glUniformMatrix4fv(texMatrixHandle, 1, false, texTransformMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoTexture)
        GLES20.glUniform1i(videoTextureHandle, 0)

        // Set warp parameters - convert offsets to absolute corner positions
        // Corners start at -1, 1 (TL) to 1, -1 (BR)
        val scaledTLX = warpShape.topLeftX * warpScale
        val scaledTLY = warpShape.topLeftY * warpScale
        val scaledTRX = warpShape.topRightX * warpScale
        val scaledTRY = warpShape.topRightY * warpScale
        val scaledBLX = warpShape.bottomLeftX * warpScale
        val scaledBLY = warpShape.bottomLeftY * warpScale
        val scaledBRX = warpShape.bottomRightX * warpScale
        val scaledBRY = warpShape.bottomRightY * warpScale

        GLES20.glUniform1f(topLeftXHandle, -1f + scaledTLX)
        GLES20.glUniform1f(topLeftYHandle, 1f + scaledTLY)
        GLES20.glUniform1f(topRightXHandle, 1f + scaledTRX)
        GLES20.glUniform1f(topRightYHandle, 1f + scaledTRY)
        GLES20.glUniform1f(bottomLeftXHandle, -1f + scaledBLX)
        GLES20.glUniform1f(bottomLeftYHandle, -1f + scaledBLY)
        GLES20.glUniform1f(bottomRightXHandle, 1f + scaledBRX)
        GLES20.glUniform1f(bottomRightYHandle, -1f + scaledBRY)

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Draw overlays in edit mode
        cornerHighlightProvider?.invoke()?.let { (enabled, selected) ->
            if (enabled) {
                drawWarpBorder()
                drawCornerMarkers(selected)
            }
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun setupVertexBuffers() {
        // Define vertices of a full-screen quad
        val vertices = floatArrayOf(
            -1f, 1f, 0f,    // Top-left
            -1f, -1f, 0f,   // Bottom-left
            1f, 1f, 0f,     // Top-right
            1f, -1f, 0f     // Bottom-right
        )

        val texCoords = floatArrayOf(
            0f, 1f,  // Top-left
            0f, 0f,  // Bottom-left
            1f, 1f,  // Top-right
            1f, 0f   // Bottom-right
        )

        // Allocate and fill vertex buffer
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }

        // Allocate and fill texture coordinate buffer
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCoords)
                position(0)
            }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e("VideoGLRenderer", "Shader compile error: ${GLES20.glGetShaderInfoLog(shader)}")
            }
        }
    }

    private fun drawCornerMarkers(selected: Corner) {
        // Corner positions using actual warped coordinates
        val size = 0.03f
        val selectedSize = 0.08f  // Larger circle for selected corner

        // Get actual corner positions with warp applied
        val scaledTLX = warpShape.topLeftX * warpScale
        val scaledTLY = warpShape.topLeftY * warpScale
        val scaledTRX = warpShape.topRightX * warpScale
        val scaledTRY = warpShape.topRightY * warpScale
        val scaledBLX = warpShape.bottomLeftX * warpScale
        val scaledBLY = warpShape.bottomLeftY * warpScale
        val scaledBRX = warpShape.bottomRightX * warpScale
        val scaledBRY = warpShape.bottomRightY * warpScale

        val corners = arrayOf(
            floatArrayOf(-1f + scaledTLX, 1f + scaledTLY),   // TL
            floatArrayOf(1f + scaledTRX, 1f + scaledTRY),    // TR
            floatArrayOf(-1f + scaledBLX, -1f + scaledBLY),  // BL
            floatArrayOf(1f + scaledBRX, -1f + scaledBRY)    // BR
        )
        val order = arrayOf(Corner.TOP_LEFT, Corner.TOP_RIGHT, Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT)

        GLES20.glUseProgram(markerProgram)
        // Upload projection for marker program
        GLES20.glUniformMatrix4fv(markerProjHandle, 1, false, projectionMatrix, 0)

        for (i in corners.indices) {
            val isSelected = order[i] == selected
            val (cx, cy) = corners[i]

            if (isSelected) {
                // Draw large bright circle around selected corner
                drawCircle(cx, cy, selectedSize, 0f, 1f, 1f, 1f)  // Bright cyan
            }

            // Draw small cross marker
            val markerSize = if (isSelected) size * 1.5f else size
            val verts = floatArrayOf(
                cx - markerSize, cy, 0f,  cx + markerSize, cy, 0f,
                cx, cy - markerSize, 0f,  cx, cy + markerSize, 0f
            )
            val buf = ByteBuffer.allocateDirect(verts.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buf.put(verts).position(0)

            GLES20.glEnableVertexAttribArray(markerPosHandle)
            GLES20.glVertexAttribPointer(markerPosHandle, 3, GLES20.GL_FLOAT, false, 12, buf)

            if (isSelected) {
                // Bright cyan for selected, larger line width
                GLES20.glUniform4f(markerColorHandle, 0f, 1f, 1f, 1f)
                GLES20.glLineWidth(4f)
            } else {
                // Yellow for normal
                GLES20.glUniform4f(markerColorHandle, 1f, 1f, 0f, 1f)
                GLES20.glLineWidth(2f)
            }

            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 4)
            GLES20.glDisableVertexAttribArray(markerPosHandle)
        }
    }

    private fun drawCircle(cx: Float, cy: Float, radius: Float, r: Float, g: Float, b: Float, a: Float) {
        val segments = 16
        val verts = mutableListOf<Float>()

        for (i in 0..segments) {
            val angle = 2f * 3.14159265f * i / segments
            val x = cx + radius * kotlin.math.cos(angle.toDouble()).toFloat()
            val y = cy + radius * kotlin.math.sin(angle.toDouble()).toFloat()
            verts.add(x)
            verts.add(y)
            verts.add(0f)
        }

        val buf = ByteBuffer.allocateDirect(verts.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buf.put(verts.toFloatArray()).position(0)

        GLES20.glUniform4f(markerColorHandle, r, g, b, a)
        GLES20.glLineWidth(2.5f)
        GLES20.glEnableVertexAttribArray(markerPosHandle)
        GLES20.glVertexAttribPointer(markerPosHandle, 3, GLES20.GL_FLOAT, false, 12, buf)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, segments + 1)
        GLES20.glDisableVertexAttribArray(markerPosHandle)
    }

    private fun drawWarpBorder() {
        // Draw border using actual corner positions with warp applied
        val scaledTLX = warpShape.topLeftX * warpScale
        val scaledTLY = warpShape.topLeftY * warpScale
        val scaledTRX = warpShape.topRightX * warpScale
        val scaledTRY = warpShape.topRightY * warpScale
        val scaledBLX = warpShape.bottomLeftX * warpScale
        val scaledBLY = warpShape.bottomLeftY * warpScale
        val scaledBRX = warpShape.bottomRightX * warpScale
        val scaledBRY = warpShape.bottomRightY * warpScale

        val xTL = -1f + scaledTLX
        val yTL = 1f + scaledTLY
        val xTR = 1f + scaledTRX
        val yTR = 1f + scaledTRY
        val xBR = 1f + scaledBRX
        val yBR = -1f + scaledBRY
        val xBL = -1f + scaledBLX
        val yBL = -1f + scaledBLY

        val verts = floatArrayOf(
            xTL, yTL, 0f,
            xTR, yTR, 0f,
            xBR, yBR, 0f,
            xBL, yBL, 0f
        )
        val buf = ByteBuffer.allocateDirect(verts.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buf.put(verts).position(0)

        GLES20.glUseProgram(markerProgram)
        GLES20.glUniformMatrix4fv(markerProjHandle, 1, false, projectionMatrix, 0)
        GLES20.glUniform4f(markerColorHandle, 0f, 1f, 0f, 1f) // green border
        GLES20.glLineWidth(3f)
        GLES20.glEnableVertexAttribArray(markerPosHandle)
        GLES20.glVertexAttribPointer(markerPosHandle, 3, GLES20.GL_FLOAT, false, 12, buf)
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4)
        GLES20.glDisableVertexAttribArray(markerPosHandle)
    }
}
