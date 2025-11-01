package com.example.pickletv

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
    uniform float topLeft;
    uniform float topRight;
    uniform float bottomLeft;
    uniform float bottomRight;
    
    void main() {
        vec4 warpedPos = position;
        
        float normalizedX = position.x;
        float normalizedY = position.y;
        
        float normalizedY01 = (normalizedY + 1.0) / 2.0;
        
        float leftOffset = mix(topLeft, bottomLeft, normalizedY01);
        float rightOffset = mix(topRight, bottomRight, normalizedY01);
        
        float normalizedX01 = (normalizedX + 1.0) / 2.0;
        float xOffset = mix(leftOffset, rightOffset, normalizedX01);
        
        warpedPos.x = position.x + xOffset;
        
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
    private var topLeftHandle = 0
    private var topRightHandle = 0
    private var bottomLeftHandle = 0
    private var bottomRightHandle = 0

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
        topLeftHandle = GLES20.glGetUniformLocation(programHandle, "topLeft")
        topRightHandle = GLES20.glGetUniformLocation(programHandle, "topRight")
        bottomLeftHandle = GLES20.glGetUniformLocation(programHandle, "bottomLeft")
        bottomRightHandle = GLES20.glGetUniformLocation(programHandle, "bottomRight")

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

        // Set warp parameters
        GLES20.glUniform1f(topLeftHandle, warpShape.topLeft * warpScale)
        GLES20.glUniform1f(topRightHandle, warpShape.topRight * warpScale)
        GLES20.glUniform1f(bottomLeftHandle, warpShape.bottomLeft * warpScale)
        GLES20.glUniform1f(bottomRightHandle, warpShape.bottomRight * warpScale)

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
        // Corner positions in NDC matching the quad
        val size = 0.03f
        val corners = arrayOf(
            floatArrayOf(-1f, 1f),   // TL
            floatArrayOf(1f, 1f),    // TR
            floatArrayOf(-1f, -1f),  // BL
            floatArrayOf(1f, -1f)    // BR
        )
        val order = arrayOf(Corner.TOP_LEFT, Corner.TOP_RIGHT, Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT)

        GLES20.glUseProgram(markerProgram)
        // Upload projection for marker program
        GLES20.glUniformMatrix4fv(markerProjHandle, 1, false, projectionMatrix, 0)

        for (i in corners.indices) {
            val isSelected = order[i] == selected
            val (cx, cy) = corners[i]
            // Make a small cross marker
            val verts = floatArrayOf(
                cx - size, cy, 0f,  cx + size, cy, 0f,
                cx, cy - size, 0f,  cx, cy + size, 0f
            )
            val buf = ByteBuffer.allocateDirect(verts.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buf.put(verts).position(0)

            GLES20.glEnableVertexAttribArray(markerPosHandle)
            GLES20.glVertexAttribPointer(markerPosHandle, 3, GLES20.GL_FLOAT, false, 12, buf)
            // Yellow for normal, Cyan for selected
            GLES20.glUniform4f(markerColorHandle, if (isSelected) 0f else 1f, 1f, if (isSelected) 1f else 0f, 1f)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 4)
            GLES20.glDisableVertexAttribArray(markerPosHandle)
        }
    }

    private fun drawWarpBorder() {
        // Compute warped corner positions matching the vertex shader logic
        // Top edge uses bottom offsets per current mix() formula, bottom uses top offsets
        val xTL = -1f + (warpShape.bottomLeft * warpScale)
        val yTL = 1f
        val xTR = 1f + (warpShape.bottomRight * warpScale)
        val yTR = 1f
        val xBR = 1f + (warpShape.topRight * warpScale)
        val yBR = -1f
        val xBL = -1f + (warpShape.topLeft * warpScale)
        val yBL = -1f

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
