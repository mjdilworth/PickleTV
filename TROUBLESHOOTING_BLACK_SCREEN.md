# Troubleshooting: Black Screen on TV (Video Plays on Emulator)

## Automatic Decoder Fallback ✅

**Good News**: The app now has automatic hardware-to-software decoder fallback!

- **First attempt**: Hardware decoder (H.264, some H.265 support)
- **If hardware fails**: Automatically retries with software decoder
- **Software decoder**: Supports more codecs but slower performance

**What you'll see:**
- Toast message: "Trying software decoder..."
- Video should play (may have slight delay or lower performance)
- Check logs to confirm fallback worked

**If fallback doesn't work**: The video codec/format is truly incompatible → Re-encode recommended

## Problem
Video `train-b0.mp4` plays correctly on Android TV emulator but shows **black screen** on actual TV device.

## Common Causes

### 1. **Codec/Format Incompatibility** ⚠️ (Most Likely)
Different devices support different video codecs. Emulators often have broader codec support than actual hardware.

**Check video format:**
```bash
# On your computer, check the video file properties
ffprobe train-b0.mp4

# Look for:
# - Video codec (H.264, H.265/HEVC, VP9, AV1, etc.)
# - Audio codec (AAC, MP3, etc.)
# - Container format (MP4, MKV, etc.)
# - Resolution and bitrate
```

**Android TV Codec Support:**
- ✅ **H.264 (AVC)** - Universally supported
- ⚠️ **H.265 (HEVC)** - Requires hardware support (many older TVs don't support)
- ⚠️ **VP9** - Limited support
- ❌ **AV1** - Very limited support on Android TV

### 2. **Resolution Too High**
Some TVs can't handle 4K or high bitrate videos.

**Recommended specs:**
- Resolution: 1920x1080 (1080p) or lower
- Bitrate: < 10 Mbps
- Frame rate: 30fps or 60fps

### 3. **Hardware Decoding Issues**
TV's hardware decoder may not support the video profile/level.

### 4. **DRM or Protection**
Some content may have protection that blocks playback on certain devices.

### 5. **OpenGL/Rendering Issues**
The keystone correction uses OpenGL - some TVs may have OpenGL compatibility issues.

## Diagnostic Steps

### Step 1: Check Logs on TV
```bash
# Connect to your TV via ADB
adb connect <TV_IP_ADDRESS>

# Watch logs while playing the video
adb logcat | grep -E "MainActivity|ExoPlayer|VideoGLRenderer"
```

**Look for:**
- `=== VIDEO FILE DIAGNOSTICS ===` - File size, path
- `=== DEVICE INFORMATION ===` - TV model, Android version
- `Playback state changed: READY` - Should appear if video loads
- `=== VIDEO SIZE CHANGED ===` - Resolution info
- `=== PLAYBACK ERROR ===` - Any errors

### Step 2: Check Video Format
```bash
# Get detailed video information
ffprobe -v quiet -print_format json -show_format -show_streams train-b0.mp4

# Check codec
ffprobe -v error -select_streams v:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 train-b0.mp4

# Should output: h264 (good) or hevc/h265 (may cause issues)
```

### Step 3: Re-encode Video (If Needed)
If the video uses H.265/HEVC or other unsupported codec:

```bash
# Convert to H.264 with compatible settings
ffmpeg -i train-b0.mp4 -c:v libx264 -profile:v baseline -level 3.1 \
       -c:a aac -b:a 128k -movflags +faststart \
       train-b0-compatible.mp4

# Or for higher quality:
ffmpeg -i train-b0.mp4 -c:v libx264 -profile:v high -level 4.0 \
       -preset slow -crf 23 -c:a aac -b:a 192k -movflags +faststart \
       train-b0-compatible.mp4
```

**Settings explained:**
- `-c:v libx264` - Use H.264 codec (most compatible)
- `-profile:v baseline` - Most compatible profile (or use `high` for better quality)
- `-level 3.1` - Compatible with most devices (or `4.0` for 1080p)
- `-c:a aac` - AAC audio codec
- `-movflags +faststart` - Optimize for streaming

### Step 4: Test Without Keystone
The OpenGL keystone correction might cause issues on some TVs.

**Temporary disable:**
```kotlin
// In MainActivity.kt, comment out:
// renderer.setWarpShape(currentWarpShape)
```

### Step 5: Compare Working vs Non-Working Videos
Check if the working videos (berlin.mp4, halloween.mp4) use different codecs:

```bash
ffprobe berlin-640.jpg  # Check format
ffprobe halloween.mp4   # Compare with train-b0.mp4
```

## Quick Fixes to Try

### Option 1: Force Software Decoding
Add to `MainActivity.kt`:
```kotlin
// In initializeExoPlayer()
val renderersFactory = DefaultRenderersFactory(this)
    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

exoPlayer = ExoPlayer.Builder(this)
    .setRenderersFactory(renderersFactory)
    .build()
```

### Option 2: Check ExoPlayer Version
Update to latest ExoPlayer version in `build.gradle.kts`:
```kotlin
implementation("com.google.android.exoplayer:exoplayer:2.19.1") // or latest
```

### Option 3: Add MediaCodec Logging
```kotlin
// In MainActivity, add this to see which codec is being used
exoPlayer.addAnalyticsListener(object : AnalyticsListener {
    override fun onVideoDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializationDurationMs: Long
    ) {
        Log.d("MainActivity", "Video decoder: $decoderName")
    }
})
```

## Expected Log Output (Working Video)

```
D/MainActivity: === VIDEO FILE DIAGNOSTICS ===
D/MainActivity: File path: /data/user/0/com.dilworth.dilmap/cache/video_cache/xxx.mp4
D/MainActivity: File size: 1234567 bytes (1.2 MB)
D/MainActivity: File readable: true
D/MainActivity: === DEVICE INFORMATION ===
D/MainActivity: Device: YourTV Model
D/MainActivity: Android version: 11 (SDK 30)
D/MainActivity: Playback state changed: BUFFERING
D/MainActivity: Playback state changed: READY
D/MainActivity: === VIDEO SIZE CHANGED ===
D/MainActivity: Resolution: 1920x1080
D/MainActivity: ✓ Video playback initiated
```

## If Black Screen Persists

### Test Pattern Video
Create a simple test video to rule out file corruption:
```bash
# Generate a 10-second test pattern
ffmpeg -f lavfi -i testsrc=duration=10:size=1280x720:rate=30 \
       -c:v libx264 -profile:v baseline -pix_fmt yuv420p \
       test-pattern.mp4
```

Upload this to your server and try playing it.

### Contact Information
If the issue persists after trying these steps, provide:
1. Full logcat output from TV
2. Output of `ffprobe train-b0.mp4`
3. TV model and Android version
4. Whether other videos (berlin.mp4, halloween.mp4) work on the TV

## Most Likely Solution
**Re-encode the video with H.264 baseline profile** - this has the highest compatibility across all Android TV devices.

