# PickleTV - Video Player with Keystone Adjustment

## Overview

PickleTV is an Android TV application that plays local or downloaded videos with real-time keystone/trapezoid adjustment capability. The app uses:

- **ExoPlayer**: For video playback
- **GLSurfaceView**: For OpenGL rendering
- **GLSL Shaders**: For keystone/trapezoid warp transformation
- **SharedPreferences**: For persistent warp shape storage

## Features

### 1. Video Playback
- Plays local MP4 files (tested with `h-6.mp4`)
- Uses ExoPlayer for robust video decoding and playback
- Renders video via OpenGL for shader pipeline support

### 2. Keystone/Trapezoid Adjustment
- **DPAD UP**: Pulls top of image inward (top-left and top-right adjustment)
- **DPAD DOWN**: Pushes bottom of image outward (bottom-left and bottom-right adjustment)
- **DPAD LEFT**: Pulls left side outward
- **DPAD RIGHT**: Pulls right side inward
- **ENTER/BUTTON_A**: Save warp shape to persistent storage
- **DEL**: Reset warp shape to default (no distortion)

### 3. Warp Shape Persistence
- Warp adjustments are automatically saved to SharedPreferences
- Shape is restored when app restarts
- Manual save via ENTER/BUTTON_A button

## Architecture

### Files

1. **MainActivity.kt**
   - Main activity that sets up the UI and handles input
   - Manages ExoPlayer lifecycle
   - Processes remote/keyboard input for warp adjustments
   - Integrates GLSurfaceView with ExoPlayer

2. **VideoGLRenderer.kt**
   - Implements GLSurfaceView.Renderer
   - Manages OpenGL rendering pipeline
   - Compiles and links vertex and fragment shaders
   - Updates warp uniform values each frame
   - Handles SurfaceTexture for video rendering

3. **WarpShape.kt**
   - `WarpShape` data class: Stores 4 corner offset values
   - `WarpShapeManager`: Handles SharedPreferences persistence
   - Methods: `saveWarpShape()`, `loadWarpShape()`, `resetWarpShape()`

4. **Shaders** (embedded in VideoGLRenderer.kt)
   - **Vertex Shader**: Applies trapezoid warp transformation
     - Takes 4 corner offsets as uniforms
     - Interpolates warp effect across the quad
     - Outputs transformed vertex positions
   
   - **Fragment Shader**: Standard texture sampling
     - Samples from video texture
     - Applies color directly to fragment

## Technical Details

### Shader Pipeline

The vertex shader implements keystone correction by:
1. Normalizing screen coordinates to 0-1 range
2. Calculating left and right offset interpolation based on Y position
3. Interpolating between left/right offset based on X position
4. Applying horizontal offset to vertex positions

This creates a smooth trapezoid warp across the entire viewport.

### Warp Adjustment Values

- Stored as 4 floats: `topLeft`, `topRight`, `bottomLeft`, `bottomRight`
- Values represent normalized displacement (-1.0 to 1.0)
- Scaled by 0.1f in shader for reasonable visual adjustment
- Each key press adjusts by 0.05f step

### ExoPlayer Integration

The SurfaceTexture created by OpenGL is wrapped in an `android.view.Surface` and passed to ExoPlayer:
```kotlin
exoPlayer.setVideoSurface(surface)
```

This allows ExoPlayer to decode video directly to the OpenGL texture.

### Rendering Flow

1. `onSurfaceCreated`: Initialize shaders and textures
2. `onSurfaceChanged`: Set viewport and projection matrix
3. `onDrawFrame`: 
   - Update SurfaceTexture from ExoPlayer
   - Set warp uniforms
   - Draw full-screen quad with warp transformation

## Dependencies

Added to `gradle/libs.versions.toml`:
```toml
exoplayer = "2.19.1"
```

Added to `app/build.gradle.kts`:
```kotlin
implementation(libs.exoplayer.core)
implementation(libs.exoplayer.ui)
```

## Permissions

Required in `AndroidManifest.xml`:
- `android.permission.READ_EXTERNAL_STORAGE` - To read video files
- `android.permission.INTERNET` - For potential streaming

## Usage

### Playing a Video
1. Place `h-6.mp4` in the project root directory
2. Run the app
3. Video will automatically start playing

### Adjusting Warp
Use remote control or keyboard:
- Arrow keys: Adjust trapezoid shape
- Enter/A button: Save adjustments
- Delete/Backspace: Reset to default

### Accessing Saved Warp Shape
Warp shapes are saved in SharedPreferences at:
```
com.example.pickletv_preferences.xml (package-specific prefs)
```

Keys:
- `topLeft`, `topRight`, `bottomLeft`, `bottomRight` (float values)

## OpenGL Details

### Vertex Format
- 3D positions: 4 vertices (full-screen quad)
- 2D texture coordinates: 0-1 range

### Rendering Mode
- `RENDERMODE_WHEN_DIRTY`: Only renders when `requestRender()` is called
- Saves battery on Android TV devices

### Matrix Projection
- Orthographic projection: -1 to 1 in both X and Y
- Near plane: -1, Far plane: 1

## Future Enhancements

Possible improvements:
1. Vertical warp adjustment (Y-axis transformation)
2. Rotation and scale transformations
3. Multiple warp profiles (TV, Projector, etc.)
4. GUI overlay for warp adjustment visualization
5. Video playback controls (pause, seek, volume)
6. Support for remote video sources
7. Advanced color correction shader effects

## Known Limitations

1. Currently only supports horizontal (keystone) warp
2. GL_TEXTURE_2D used instead of GL_TEXTURE_EXTERNAL_OES for compatibility
3. No UI overlays for adjustment visualization
4. Limited to single video file at startup

## Troubleshooting

### Video not playing
- Ensure `h-6.mp4` is in the correct directory
- Check logcat for "Video file not found"
- Verify file permissions

### Warp not appearing
- Check if render is being called (use RENDERMODE_CONTINUOUS for debugging)
- Verify shader compilation (check logcat for "Program link error")
- Ensure warp values are being updated

### Performance issues
- Consider reducing video resolution
- Check for shader compilation errors
- Monitor GPU usage with Android Profiler

# PickleTV
