# Quick Debugging Commands for TV Black Screen Issue

## Automatic Hardware-to-Software Decoder Fallback ✨

**NEW**: The app now automatically falls back to software decoding if hardware decoding fails!

**How it works:**
1. App tries hardware decoder first (faster, better performance)
2. If hardware decoder fails with a codec error → **Automatically retries with software decoder**
3. User sees toast: "Trying software decoder..."
4. Software decoder can handle more codecs but uses more CPU

**Logs you'll see:**
```
E/MainActivity: === PLAYBACK ERROR ===
E/MainActivity: Error type: decoder_init_failed
W/MainActivity: Hardware decoder failed - attempting SOFTWARE decoder fallback...
D/MainActivity: Creating ExoPlayer with SOFTWARE decoder preference
D/MainActivity: === RETRYING WITH SOFTWARE DECODER ===
D/MainActivity: Playback state changed: READY  ← Success!
```

## Connect to Your TV

```bash
# 1. Enable Developer Options on your TV:
#    Settings → About → Build Number (click 7 times)

# 2. Enable ADB Debugging:
#    Settings → Developer Options → USB/Network Debugging → ON

# 3. Find TV IP address:
#    Settings → Network → Status

# 4. Connect from computer:
adb connect <TV_IP_ADDRESS>:5555

# Example:
# adb connect 192.168.1.100:5555
```

## Watch Video Playback Logs

```bash
# Clear old logs and watch in real-time
adb logcat -c && adb logcat | grep -E "MainActivity|ExoPlayer|VideoDownloadManager"

# Just video diagnostics
adb logcat | grep "=== VIDEO"

# Just errors
adb logcat | grep -E "ERROR|FATAL"

# Save logs to file
adb logcat > tv_debug.log
```

## Check Video File on TV

```bash
# List cached videos
adb shell "run-as com.dilworth.dilmap ls -lh /data/data/com.dilworth.dilmap/cache/video_cache/"

# Check specific video
adb shell "run-as com.dilworth.dilmap ls -lh /data/data/com.dilworth.dilmap/cache/video_cache/*.mp4"

# Get file size
adb shell "run-as com.dilworth.dilmap du -h /data/data/com.dilworth.dilmap/cache/video_cache/"
```

## Pull Video from TV for Analysis

```bash
# Pull the cached video file
adb shell "run-as com.dilworth.dilmap cat /data/data/com.dilworth.dilmap/cache/video_cache/FILENAME.mp4" > downloaded-from-tv.mp4

# Check if it's valid
ffprobe downloaded-from-tv.mp4
```

## Check Video Codec on Server

```bash
# Check the original video on your server
curl -I https://tv.dilly.cloud/content/user/7e1819bf-342c-468f-a8d3-0fdb39c8e1fe/train-b0.mp4

# Download and analyze
curl -o train-b0.mp4 https://tv.dilly.cloud/content/user/7e1819bf-342c-468f-a8d3-0fdb39c8e1fe/train-b0.mp4
ffprobe train-b0.mp4

# Get detailed codec info
ffprobe -v quiet -print_format json -show_streams train-b0.mp4 | grep -E "codec_name|width|height|bit_rate"
```

## What to Look For

### In Logs:
```
✅ GOOD:
D/MainActivity: === VIDEO FILE DIAGNOSTICS ===
D/MainActivity: File size: 12345678 bytes (11.7 MB)
D/MainActivity: Playback state changed: READY
D/MainActivity: === VIDEO SIZE CHANGED ===
D/MainActivity: Resolution: 1920x1080

❌ BAD:
E/ExoPlayer: Video decoder error
E/MainActivity: === PLAYBACK ERROR ===
E/ExoPlayer: Codec (OMX.google.h265.decoder) is not supported
```

### In ffprobe Output:
```
✅ GOOD (H.264):
codec_name=h264
codec_long_name=H.264 / AVC / MPEG-4 AVC
profile=High

❌ BAD (H.265 may not be supported):
codec_name=hevc
codec_long_name=H.265 / HEVC
profile=Main
```

## Quick Test

1. **Connect to TV:**
   ```bash
   adb connect YOUR_TV_IP:5555
   ```

2. **Clear cache and logs:**
   ```bash
   adb shell pm clear com.dilworth.dilmap
   adb logcat -c
   ```

3. **Start log monitoring:**
   ```bash
   adb logcat | grep -E "MainActivity|ExoPlayer|Download"
   ```

4. **Play the video on TV and watch logs**

5. **Look for these key messages:**
   - `File size: X bytes` - Confirms download
   - `Playback state changed: READY` - Video loaded
   - `Resolution: WxH` - Video dimensions
   - Any ERROR messages

## If Black Screen Occurs

Check logs for:
1. **File downloaded correctly?**
   ```
   Download complete: https://...
   File size: ... bytes
   ✓ File validation passed
   ```

2. **File playback initiated?**
   ```
   ✓ Video playback initiated
   Playback state changed: READY
   ```

3. **Video size detected?**
   ```
   === VIDEO SIZE CHANGED ===
   Resolution: 1920x1080
   ```

If you see all three ✓ but still black screen → **Codec issue**

If you DON'T see "VIDEO SIZE CHANGED" → **Decoder can't process the video**

## Solution: Re-encode Video

```bash
# Convert to most compatible format
ffmpeg -i train-b0.mp4 \
  -c:v libx264 -profile:v baseline -level 3.1 \
  -pix_fmt yuv420p \
  -c:a aac -b:a 128k \
  -movflags +faststart \
  train-b0-compatible.mp4

# Upload to server:
# Replace the original file or upload as new file
```

## Common TV Models and Issues

**Sony Bravia:**
- Usually supports H.264 and H.265
- May have issues with high bitrate

**Samsung Tizen:**
- Good H.264 support
- Limited H.265 support on older models

**LG webOS:**
- Good H.264 support
- Check bitrate limits

**Generic Android TV:**
- Safest: H.264 baseline profile
- Max resolution: 1920x1080
- Max bitrate: 10 Mbps

