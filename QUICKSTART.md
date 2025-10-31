# PickleTV - Quick Start Guide

## Setup & Build

### Prerequisites
- Android Studio (latest)
- Android SDK 26+ (API Level 26)
- Gradle 8.13.0+
- h-6.mp4 video file

### Steps to Run

1. **Prepare Video File**
   ```bash
   # Copy h-6.mp4 to project root
   cp /path/to/h-6.mp4 /Users/mike/AndroidStudioProjects/PickleTV/
   ```

2. **Sync Gradle**
   - Open project in Android Studio
   - File → Sync Now
   - Wait for Gradle sync to complete

3. **Build APK**
   ```bash
   # Via Android Studio
   Build → Make Project
   
   # Or via command line
   ./gradlew assembleDebug
   ```

4. **Deploy to Device/Emulator**
   ```bash
   # Via Android Studio
   Run → Run 'app'
   
   # Or via adb
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Launch App**
   - Open PickleTV from app launcher
   - Video should auto-play

## Remote Control Usage

### Navigation Keys
```
UP/DOWN/LEFT/RIGHT    → Adjust warp/trapezoid shape
ENTER / A Button      → Save warp adjustments
DELETE / Back         → Reset warp to default
```

### Warp Adjustment Details
- **DPAD UP**: Pulls top corners inward (reduces top width)
- **DPAD DOWN**: Pushes bottom corners outward (increases bottom width)
- **DPAD LEFT**: Pulls left edge outward
- **DPAD RIGHT**: Pulls right edge inward

These adjustments create keystone correction for projector alignment.

## File Locations

### Source Code
```
app/src/main/java/com/example/pickletv/
├── MainActivity.kt              - Main activity (UI + input)
├── VideoGLRenderer.kt           - OpenGL renderer + shaders
└── WarpShape.kt                 - Data persistence
```

### Configuration
```
app/build.gradle.kts             - Build configuration
gradle/libs.versions.toml        - Dependency versions
app/src/main/AndroidManifest.xml - App manifest
```

### Resources
```
app/src/main/res/values/strings.xml    - String resources
app/src/main/res/mipmap-*/ic_launcher  - App icon
```

## Troubleshooting

### Build Errors

**Issue**: "Unresolved reference 'exoplayer'"
- **Solution**: Run `./gradlew build --refresh-dependencies`

**Issue**: "Task failed with an exception"
- **Solution**: 
  - Check Java version: `java -version` (should be 11+)
  - Clean project: `./gradlew clean`
  - Rebuild: `./gradlew build`

### Runtime Errors

**Issue**: Video file not found
- **Solution**: 
  - Verify h-6.mp4 is in project root
  - Check app logs: `adb logcat | grep MainActivity`
  - Try alternative location: `app/src/main/assets/h-6.mp4`

**Issue**: Black screen (no video)
- **Solution**:
  - Check if device supports OpenGL ES 2.0
  - View shader compile errors in logcat
  - Search for "Program link error" in logs

**Issue**: Warp adjustments not visible
- **Solution**:
  - Verify remote input is working (check logcat)
  - Try touching screen to trigger render
  - Check GLSurfaceView render mode

## Architecture Overview

```
┌─────────────────────────────────────┐
│         MainActivity                │
│  - Handles remote input             │
│  - Manages ExoPlayer lifecycle      │
└────────────┬────────────────────────┘
             │
             ├─────────────────────────┐
             ▼                         ▼
    ┌──────────────────┐      ┌──────────────────┐
    │   GLSurfaceView  │      │    ExoPlayer     │
    │   (OpenGL ES 2)  │◄─────┤  (Video decode)  │
    └──────────────────┘      └──────────────────┘
             │
             ▼
    ┌──────────────────────────────────┐
    │    VideoGLRenderer               │
    │ - Vertex Shader (warp)           │
    │ - Fragment Shader (sampling)     │
    │ - Uniform updates                │
    └──────────────────────────────────┘
             │
             ▼
    ┌──────────────────────────────────┐
    │   SurfaceTexture                 │
    │   (Video rendering surface)      │
    └──────────────────────────────────┘
             │
             ▼
    ┌──────────────────────────────────┐
    │   WarpShape + SharedPreferences  │
    │   (Persistent storage)           │
    └──────────────────────────────────┘
```

## Key Technologies

### ExoPlayer
- Handles video decoding on multiple threads
- Provides SurfaceTexture for OpenGL rendering
- Manages codec selection and resource allocation
- Robust error handling and recovery

### OpenGL ES 2.0
- Vertex shader applies keystone transformation
- Fragment shader samples and renders video
- Orthographic projection for 2D rendering
- Efficient pipeline for TV performance

### Shaders
- **Vertex**: Interpolates warp across viewport
- **Fragment**: Simple texture sampling
- Both compiled and linked at runtime
- Error reporting for debugging

### Persistence
- SharedPreferences stores warp values
- 4 float values: topLeft, topRight, bottomLeft, bottomRight
- Auto-loaded on app startup
- Manual save via ENTER key

## Performance Optimization

### Current Setup
- `RENDERMODE_WHEN_DIRTY`: Renders only on update
- Saves battery on TV devices
- Manual `requestRender()` on key input
- Single thread for GL operations

### Potential Improvements
- Frame rate limiting (30 fps)
- Texture compression
- Higher LOD shaders for older devices
- Hardware acceleration hints

## Developer Notes

### Adding New Features

**To add Y-axis warp:**
1. Add `topY`, `bottomY` uniforms to shader
2. Update WarpShape data class
3. Add key handlers in MainActivity
4. Modify shader vertex transformation

**To add color correction:**
1. Extend fragment shader with color matrix
2. Add color adjustment uniforms
3. Store color settings in SharedPreferences
4. Add UI controls for adjustment

**To add video controls:**
1. Implement PlaybackState in MainActivity
2. Add ExoPlayer.Listener
3. Handle pause/play/seek events
4. Update UI overlay

## Testing Checklist

- [ ] Video plays on app launch
- [ ] DPAD navigation works for warp
- [ ] Warp adjustments visible in real-time
- [ ] Adjustments persist after app restart
- [ ] DELETE key resets to default
- [ ] No shader compile errors (check logcat)
- [ ] App doesn't crash on repeated adjustments
- [ ] Memory usage stable over time
- [ ] Works on multiple device sizes
- [ ] Remote control input responsive

## Support & Debugging

### View Logs
```bash
# All logs
adb logcat

# Filter by app
adb logcat | grep pickletv

# Filter by specific tag
adb logcat VideoGLRenderer:V MainActivity:V

# Save to file
adb logcat > logcat.txt
```

### Common Log Entries
- "SurfaceTexture created" - OpenGL initialized
- "Video loaded and playing" - ExoPlayer ready
- "Warp adjusted" - Remote input processed
- "Warp shape saved" - Settings persisted
- "Program link error" - Shader compilation failed

## References

- [ExoPlayer Documentation](https://exoplayer.dev/)
- [OpenGL ES 2.0 Reference](https://www.khronos.org/opengles/)
- [Android GLSurfaceView](https://developer.android.com/reference/android/opengl/GLSurfaceView)
- [Keystone Correction Basics](https://en.wikipedia.org/wiki/Keystone_effect)

