# Google TV Streamer Debugging Guide

## Updated Decoder Fallback Implementation ✅

The app now has a **properly implemented** hardware-to-software decoder fallback that actually forces software decoding when hardware fails.

### What Changed:

**BEFORE (Didn't Work):**
- Used `EXTENSION_RENDERER_MODE_PREFER` which doesn't force software decoding
- ExoPlayer could still try to use hardware decoders
- Fallback didn't actually change decoder selection

**NOW (Fixed):**
- Uses `MediaCodecSelector` to **filter codec list**
- Only allows software decoders (names starting with `OMX.google.`, `c2.android.`, or containing `sw`)
- Actually forces ExoPlayer to use software decoding
- Detects specific `MediaCodecVideoDecoderException` and `MediaCodecDecoderException`

## Testing on Google TV Streamer

### Step 1: Connect via ADB

```bash
# Enable Developer Options on Google TV Streamer:
# Settings → System → About → Android TV OS build (click 7 times)

# Enable Network Debugging:
# Settings → System → Developer Options → Network debugging → ON

# Find IP address:
# Settings → Network & Internet → Your network → Advanced

# Connect from computer:
adb connect <GOOGLE_TV_IP>:5555

# Example:
adb connect 192.168.1.100:5555
```

### Step 2: Install the APK

```bash
# Install latest version
adb install -r /path/to/app-debug.apk

# OR use gradle (if connected):
cd /home/dilly/AndroidStudioProjects/PickleTV
./gradlew installDebug
```

### Step 3: Watch Logs in Real-Time

```bash
# Clear old logs
adb logcat -c

# Watch playback logs
adb logcat | grep -E "MainActivity|ExoPlayer|MediaCodec"

# OR save to file
adb logcat > google_tv_debug.log
```

### Step 4: Play the Video

1. Navigate to Browse Content
2. Select "Trains alive in Mallorca" (train-b0.mp4)
3. Watch the logs and on-screen toasts

## Expected Behavior

### Scenario 1: Hardware Decoder Works
```
D/MainActivity: === VIDEO FILE DIAGNOSTICS ===
D/MainActivity: File size: 1234567 bytes
D/MainActivity: Creating ExoPlayer with HARDWARE decoder preference (default)
D/MainActivity: Playback state changed: BUFFERING
D/MainActivity: Playback state changed: READY
D/MainActivity: === VIDEO SIZE CHANGED ===
D/MainActivity: Resolution: 1920x1080
```
**Result**: Video plays immediately ✅

### Scenario 2: Hardware Fails, Software Succeeds
```
D/MainActivity: Creating ExoPlayer with HARDWARE decoder preference (default)
E/MainActivity: === PLAYBACK ERROR ===
E/MainActivity: Error type: decoder_init_failed
E/MainActivity: MediaCodecVideoDecoderException detected
W/MainActivity: 🔄 Hardware decoder failed - attempting SOFTWARE decoder fallback...
D/MainActivity: === RETRYING WITH SOFTWARE DECODER ===
D/MainActivity: Creating ExoPlayer with SOFTWARE decoder (forced via MediaCodecSelector)
D/MainActivity: Using software decoder: OMX.google.h264.decoder
D/MainActivity: Playback state changed: READY
D/MainActivity: === VIDEO SIZE CHANGED ===
```
**Result**: Toast "Trying software decoder...", then video plays ✅

### Scenario 3: Both Decoders Fail
```
E/MainActivity: === PLAYBACK ERROR ===
E/MainActivity: MediaCodecVideoDecoderException detected  
W/MainActivity: 🔄 Hardware decoder failed - attempting SOFTWARE decoder fallback...
D/MainActivity: Creating ExoPlayer with SOFTWARE decoder (forced via MediaCodecSelector)
E/MainActivity: === PLAYBACK ERROR ===
E/MainActivity: ❌ Final error: Video playback failed (both hardware & software decoders tried)
```
**Result**: Toast "both hardware & software decoders tried" → Need to re-encode video ❌

## Common Issues on Google TV Streamer

### Issue 1: H.265/HEVC Codec
**Problem**: Google TV Streamer may have limited H.265 hardware support

**Solution**: The software decoder should handle it

**Check video codec:**
```bash
ffprobe train-b0.mp4 | grep "codec_name"
```

If it says `hevc` or `h265`, that's likely the issue.

### Issue 2: High Bitrate
**Problem**: Video bitrate too high for streaming

**Check bitrate:**
```bash
ffprobe -v error -select_streams v:0 -show_entries stream=bit_rate -of default=noprint_wrappers=1:nokey=1 train-b0.mp4
```

If over 20,000,000 (20 Mbps), may cause buffering issues.

### Issue 3: Resolution Too High
**Problem**: 4K videos may struggle

**Check resolution:**
```bash
ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 train-b0.mp4
```

### Issue 4: Keystone Rendering Issues
**Problem**: OpenGL rendering may conflict with video playback

**Test**: Try playing without keystone adjustments first

## Re-encode Video for Maximum Compatibility

If both decoders fail, re-encode the video:

```bash
# Most compatible format (H.264 baseline)
ffmpeg -i train-b0.mp4 \
  -c:v libx264 -profile:v baseline -level 3.1 \
  -pix_fmt yuv420p \
  -vf "scale=1920:1080:flags=lanczos" \
  -b:v 5M -maxrate 8M -bufsize 2M \
  -c:a aac -b:a 192k -ar 48000 \
  -movflags +faststart \
  train-compatible.mp4

# Higher quality (H.264 high profile)
ffmpeg -i train-b0.mp4 \
  -c:v libx264 -profile:v high -level 4.0 \
  -preset slow -crf 23 \
  -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  -movflags +faststart \
  train-hq.mp4
```

**Settings explained:**
- `-profile:v baseline` - Most compatible (works on all devices)
- `-level 3.1` or `4.0` - Compatibility level
- `-pix_fmt yuv420p` - Required pixel format
- `-b:v 5M` - Video bitrate (5 Mbps = good quality, streams well)
- `-maxrate 8M -bufsize 2M` - Rate control for streaming
- `-c:a aac` - AAC audio (universal support)
- `-movflags +faststart` - Optimize for streaming

## Debugging Commands

### Check Available Decoders on Device
```bash
adb shell dumpsys media.player | grep -i codec
```

### Check Video File on Device
```bash
# List cached videos
adb shell "run-as com.dilworth.dilmap ls -lh /data/data/com.dilworth.dilmap/cache/video_cache/"

# Pull video for analysis
adb pull /data/data/com.dilworth.dilmap/cache/video_cache/HASH.mp4 ./downloaded-from-tv.mp4
ffprobe downloaded-from-tv.mp4
```

### Monitor Memory Usage
```bash
adb shell dumpsys meminfo com.dilworth.dilmap
```

### Check CPU Usage During Playback
```bash
adb shell top | grep dilworth
```

## What to Report if Still Failing

If the video still doesn't play after the fix, please provide:

1. **Full logcat output:**
   ```bash
   adb logcat > tv_full_log.txt
   # Play video, wait for error
   # Ctrl+C to stop
   ```

2. **Video file info:**
   ```bash
   ffprobe -v quiet -print_format json -show_streams train-b0.mp4 > video_info.json
   ```

3. **Device info:**
   ```bash
   adb shell getprop ro.product.model
   adb shell getprop ro.build.version.release
   ```

4. **Screenshot of error** (if visible)

5. **Look for these specific lines in logs:**
   - `Using software decoder: XXX` - Shows which decoder was selected
   - `MediaCodecVideoDecoderException` - Shows exact codec failure
   - `Resolution: WxH` - Shows if decoder could initialize

## Performance Notes

**Software Decoder Impact:**
- CPU usage: ~40-60% (vs 10-20% for hardware)
- May cause slight warmth/heat
- Battery drain increased (if portable)
- But will play videos that hardware can't!

**For Best Performance:**
- Re-encode to H.264 baseline
- Use hardware decoder
- Enjoy smooth playback 🎬

