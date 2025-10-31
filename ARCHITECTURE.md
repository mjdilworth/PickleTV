# PickleTV - Complete Implementation Details

## Project Structure

```
PickleTV/
├── app/
│   ├── build.gradle.kts                 # Build configuration with ExoPlayer deps
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml      # Permissions: READ_EXTERNAL_STORAGE, INTERNET
│   │   │   ├── java/com/example/pickletv/
│   │   │   │   ├── MainActivity.kt      # Main activity - UI & input handling
│   │   │   │   ├── VideoGLRenderer.kt   # OpenGL renderer with shaders
│   │   │   │   ├── WarpShape.kt         # Data model & persistence
│   │   │   │   ├── vertex_shader.glsl   # Reference shader file
│   │   │   │   └── fragment_shader.glsl # Reference shader file
│   │   │   └── res/
│   │   │       ├── values/strings.xml
│   │   │       └── mipmap-*/ic_launcher.webp
│   └── ...
├── gradle/
│   └── libs.versions.toml               # ExoPlayer 2.19.1
├── build.gradle.kts
├── settings.gradle.kts
├── h-6.mp4                              # Test video file
├── README.md                            # Full documentation
├── IMPLEMENTATION.md                    # Implementation summary
├── QUICKSTART.md                        # Quick start guide
└── ARCHITECTURE.md                      # This file
```

## Core Components

### 1. MainActivity.kt

**Purpose**: Main activity that coordinates video playback and warp adjustments

**Key Responsibilities**:
- Initializes GLSurfaceView and ExoPlayer
- Handles remote control input (DPAD, ENTER, DEL)
- Manages warp shape adjustments
- Persists warp settings via WarpShapeManager

**Key Methods**:
```kotlin
onCreate()                    # Initialize UI and start video
initializeExoPlayer()         # Set up ExoPlayer with GLSurfaceView
setupExoPlayerWithSurface()   # Connect ExoPlayer to OpenGL surface
loadVideo()                   # Load and play video file
onKeyDown()                   # Handle remote input
adjustWarp()                  # Modify warp parameters
```

**Input Mapping**:
```
KEYCODE_DPAD_UP      → WarpAdjustment.TOP_UP
KEYCODE_DPAD_DOWN    → WarpAdjustment.BOTTOM_DOWN
KEYCODE_DPAD_LEFT    → WarpAdjustment.LEFT_OUT
KEYCODE_DPAD_RIGHT   → WarpAdjustment.RIGHT_OUT
KEYCODE_ENTER/A      → Save warp shape
KEYCODE_DEL          → Reset warp shape
```

### 2. VideoGLRenderer.kt

**Purpose**: Handles all OpenGL rendering and shader pipeline

**Key Responsibilities**:
- Compiles and links vertex/fragment shaders
- Manages vertex buffers and texture coordinates
- Updates warp uniforms each frame
- Renders quad with keystone transformation

**Shader Code** (embedded as constants):

**Vertex Shader**:
```glsl
attribute vec4 position;
attribute vec2 texCoord;

varying vec2 outTexCoord;

uniform mat4 projection;
uniform float topLeft;      // Top-left corner offset
uniform float topRight;     // Top-right corner offset
uniform float bottomLeft;   // Bottom-left corner offset
uniform float bottomRight;  // Bottom-right corner offset

void main() {
    vec4 warpedPos = position;
    
    // Normalize to 0..1 range
    float normalizedY01 = (position.y + 1.0) / 2.0;
    
    // Interpolate left/right offsets based on Y position
    float leftOffset = mix(topLeft, bottomLeft, normalizedY01);
    float rightOffset = mix(topRight, bottomRight, normalizedY01);
    
    // Interpolate final X offset based on X position
    float normalizedX01 = (position.x + 1.0) / 2.0;
    float xOffset = mix(leftOffset, rightOffset, normalizedX01);
    
    // Apply warp
    warpedPos.x = position.x + xOffset;
    
    gl_Position = projection * warpedPos;
    outTexCoord = texCoord;
}
```

**Fragment Shader**:
```glsl
precision mediump float;

varying vec2 outTexCoord;
uniform sampler2D videoTexture;

void main() {
    gl_FragColor = texture2D(videoTexture, outTexCoord);
}
```

**Key Methods**:
```kotlin
onSurfaceCreated()    # Compile shaders, create texture
onSurfaceChanged()    # Set viewport and projection
onDrawFrame()         # Update texture, set uniforms, draw
setupVertexBuffers()  # Create vertex and texture coord buffers
loadShader()          # Compile individual shaders
```

**Warp Scale Factor**: 0.1f
- Converts normalized warp values (-1.0 to 1.0) to shader offset
- Provides reasonable visual adjustment range
- Example: warpValue of 0.5 → shader offset of 0.05

### 3. WarpShape.kt

**Purpose**: Data model and persistence layer for warp adjustments

**Data Class**:
```kotlin
data class WarpShape(
    val topLeft: Float = 0f,      // -1.0 to 1.0
    val topRight: Float = 0f,     // -1.0 to 1.0
    val bottomLeft: Float = 0f,   // -1.0 to 1.0
    val bottomRight: Float = 0f   // -1.0 to 1.0
)
```

**Adjustment Steps**: 0.05f per key press
- Total range: ±1.0 = ±20 key presses per corner
- Smooth, granular control

**Persistence Manager**:
```kotlin
class WarpShapeManager(context: Context) {
    fun saveWarpShape(warpShape: WarpShape)  # Save to SharedPreferences
    fun loadWarpShape(): WarpShape           # Load from SharedPreferences
    fun resetWarpShape()                     # Clear all saved values
}
```

**SharedPreferences Keys**:
- `warp_shape_prefs` (preference file name)
- `topLeft`, `topRight`, `bottomLeft`, `bottomRight` (float values)

## Data Flow

### Startup Flow
```
1. MainActivity.onCreate()
   ↓
2. Create WarpShapeManager
   ↓
3. Load saved warp shape from SharedPreferences
   ↓
4. Create GLSurfaceView
   ↓
5. Create VideoGLRenderer
   ↓
6. Initialize ExoPlayer
   ↓
7. Register SurfaceTexture callback
   ↓
8. onSurfaceCreated() called by GLThread
   ↓
9. Create and compile shaders
   ↓
10. Find and load video file
    ↓
11. Play video to ExoPlayer surface
```

### Warp Adjustment Flow
```
1. User presses DPAD key
   ↓
2. MainActivity.onKeyDown() triggered
   ↓
3. adjustWarp() updates currentWarpShape
   ↓
4. renderer.setWarpShape() called
   ↓
5. glSurfaceView.requestRender() triggers render
   ↓
6. onDrawFrame() reads new warpShape values
   ↓
7. glUniform1f() sets shader uniforms
   ↓
8. Draw quad with keystone transformation
```

### Save Flow
```
1. User presses ENTER key
   ↓
2. MainActivity.onKeyDown() (KEYCODE_ENTER)
   ↓
3. warpShapeManager.saveWarpShape(currentWarpShape)
   ↓
4. Write to SharedPreferences
   ↓
5. Log confirmation
```

## Rendering Pipeline

### Vertex Processing
```
Input Vertices (4-vertex quad)
         ↓
Vertex Shader receives:
- position (x, y, z)
- texCoord (u, v)
- projection matrix
- 4 corner offset uniforms
         ↓
Warp Transformation:
- Normalize coordinates
- Interpolate offsets
- Apply horizontal displacement
         ↓
Transformed Vertices
```

### Fragment Processing
```
Rasterized Fragments
         ↓
Fragment Shader receives:
- outTexCoord (interpolated)
- videoTexture (sampler2D)
         ↓
Texture Lookup:
- Sample video texture at coordinates
         ↓
Output Fragment Color
```

### Full Pipeline
```
Video Frame (from ExoPlayer)
    ↓
SurfaceTexture
    ↓
OpenGL Texture ID
    ↓
Vertex Shader (applies warp)
    ↓
Rasterizer (interpolates)
    ↓
Fragment Shader (samples)
    ↓
Display on Screen
```

## ExoPlayer Integration

### Surface Setup
```kotlin
// 1. VideoGLRenderer creates SurfaceTexture
surfaceTexture = SurfaceTexture(videoTexture)

// 2. Wrap in android.view.Surface
val surface = Surface(surfaceTexture!!)

// 3. Pass to ExoPlayer
exoPlayer.setVideoSurface(surface)

// 4. ExoPlayer decodes video directly to texture
```

### Lifecycle
```
ExoPlayer prepares video
    ↓
onFrameAvailable callback triggered
    ↓
VideoGLRenderer.onDrawFrame() called
    ↓
surfaceTexture.updateTexImage()
    ↓
Texture updated with latest frame
    ↓
Shader renders to display
```

## OpenGL Configuration

### EGL Context
```kotlin
glSurfaceView.setEGLContextClientVersion(2)  // OpenGL ES 2.0
```

### Render Mode
```kotlin
glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
```
- Only renders when `requestRender()` called
- Saves CPU/GPU resources on TV devices
- Triggered on key input

### Viewport and Projection
```kotlin
override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f)
}
```
- Orthographic projection: -1 to +1 (normalized device coordinates)
- Covers full screen
- Convenient for 2D warp transformations

### Vertex Buffer Object (VBO)
```
Vertex Array (4 vertices × 3 floats = 12 floats)
    ↓ FloatBuffer (DirectBuffer)
    ↓
glVertexAttribPointer
    ↓
positionHandle attribute

Texture Coordinate Array (4 vertices × 2 floats = 8 floats)
    ↓ FloatBuffer (DirectBuffer)
    ↓
glVertexAttribPointer
    ↓
texCoordHandle attribute
```

## State Management

### GL State per Frame
```glsl
glClear(GL_COLOR_BUFFER_BIT)              // Clear background
glUseProgram(programHandle)                // Activate shader
glEnableVertexAttribArray(positionHandle)  // Enable position input
glEnableVertexAttribArray(texCoordHandle)  // Enable texcoord input
glUniformMatrix4fv(projection)             // Set projection matrix
glUniform1f(warp uniforms)                 // Set warp values
glActiveTexture(GL_TEXTURE0)               // Activate texture unit
glBindTexture(GL_TEXTURE_2D, videoTexture) // Bind video texture
glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)     // Draw quad
glDisableVertexAttribArray(...)            // Cleanup
```

## Input Handling

### Key Event Processing
```
Remote Key Press
    ↓
MainActivity.onKeyDown(keyCode)
    ↓
Switch on keyCode
    ├─ DPAD_UP      → TOP_UP adjustment
    ├─ DPAD_DOWN    → BOTTOM_DOWN adjustment
    ├─ DPAD_LEFT    → LEFT_OUT adjustment
    ├─ DPAD_RIGHT   → RIGHT_OUT adjustment
    ├─ ENTER/A      → Save to preferences
    └─ DEL          → Reset to default
    ↓
adjustWarp(WarpAdjustment)
    ↓
Create new WarpShape with adjustments
    ↓
renderer.setWarpShape()
    ↓
glSurfaceView.requestRender()
    ↓
onDrawFrame() with new uniforms
```

## Performance Considerations

### Optimization Strategies
1. **Dirty Rendering**: Only update when input changes
2. **DirectBuffer**: FloatBuffer for GPU-efficient memory transfer
3. **Single Shader Program**: No shader switching overhead
4. **Minimal Uniforms**: Only 4 warp values + projection matrix
5. **No Texture Filtering**: Simple GL_LINEAR for performance

### Estimated Performance
- GPU: ~1-2% on typical Android TV
- CPU: <1% between frames
- Memory: ~50MB OpenGL resources
- Battery: Minimal drain with RENDERMODE_WHEN_DIRTY

## Shader Compilation

### Process
```
Shader Source (String)
    ↓
glCreateShader(GL_VERTEX_SHADER / GL_FRAGMENT_SHADER)
    ↓
glShaderSource(shader, source)
    ↓
glCompileShader(shader)
    ↓
Check glGetShaderiv(GL_COMPILE_STATUS)
    ├─ SUCCESS: Continue
    └─ FAILED: Log error via glGetShaderInfoLog()
    ↓
glCreateProgram()
    ↓
glAttachShader(program, vertexShader)
glAttachShader(program, fragmentShader)
    ↓
glLinkProgram(program)
    ↓
Check glGetProgramiv(GL_LINK_STATUS)
    ├─ SUCCESS: Use program
    └─ FAILED: Log error via glGetProgramInfoLog()
    ↓
glGetAttribLocation() for position, texCoord
glGetUniformLocation() for projection, warp values, texture
```

## Error Handling

### Shader Errors
```kotlin
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
```

### Video File Errors
```kotlin
private fun findVideoFile(): File? {
    val possiblePaths = listOf(
        File(filesDir.parentFile, "h-6.mp4"),
        File(filesDir.parentFile?.parentFile, "h-6.mp4"),
        File(getExternalFilesDir(null), "h-6.mp4"),
        File(cacheDir.parentFile, "h-6.mp4"),
        File("/Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4"),
    )
    
    for (path in possiblePaths) {
        if (path.exists()) {
            Log.d("MainActivity", "Found video at: ${path.absolutePath}")
            return path
        }
    }
    
    return null
}
```

## Testing Recommendations

### Unit Tests
- WarpShape data class (immutability, defaults)
- WarpShapeManager (save/load roundtrip)

### Integration Tests
- ExoPlayer surface integration
- Shader compilation on device
- Key input → warp adjustment flow

### Manual Tests
- Video plays on startup
- All DPAD keys adjust correctly
- Save/load persists correctly
- Reset functionality works
- No crashes during extended use

## Future Enhancement Opportunities

1. **Vertical Warp**: Add Y-axis distortion
2. **Rotation**: Add rotation transformation
3. **Scale**: Add zoom capability
4. **Color Correction**: Implement color matrix in fragment shader
5. **Presets**: Save multiple warp profiles
6. **UI Overlay**: Show warp adjustment visualization
7. **Video Controls**: Pause, seek, volume
8. **Network Streaming**: Support HTTP video sources
9. **Multiple Video Support**: Playlist functionality
10. **Gesture Control**: Touch-based warp adjustment

