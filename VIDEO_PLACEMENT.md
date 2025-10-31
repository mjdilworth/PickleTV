# Video File Placement Guide for Debugging

## Current Situation
- You have `h-6.mp4` in project root: `/Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4`
- The app can't find it in that location

## Why?
When you deploy the app to Android device/emulator, the project root directory doesn't exist on the device. Files need to be copied to device-accessible locations.

## 3 Solutions (Pick One)

### Solution 1: Copy to Download Directory (Easiest for Testing)
```bash
# Push file to device Downloads folder
adb push /Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4 /sdcard/Download/h-6.mp4

# Or Downloads (alternate)
adb push /Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4 /sdcard/Downloads/h-6.mp4

# Verify it's there
adb shell ls -la /sdcard/Download/
```

Then run your app and it will find it! ✅

### Solution 2: Add to App Assets (Permanent)
This bundles the video with your app:

1. **Create assets folder:**
   ```
   app/src/main/assets/
   ```

2. **Copy video there:**
   ```bash
   cp /Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4 \
      /Users/mike/AndroidStudioProjects/PickleTV/app/src/main/assets/h-6.mp4
   ```

3. **Rebuild the app** in Android Studio

4. **Update code** to load from assets:
   ```kotlin
   private fun loadVideoFromAssets(): File {
       val inputStream = assets.open("h-6.mp4")
       val file = File(cacheDir, "h-6.mp4")
       inputStream.copyTo(file.outputStream())
       return file
   }
   ```

### Solution 3: Use Android's App Data Directory
```bash
adb push /Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4 \
         /sdcard/Android/data/com.example.pickletv/h-6.mp4

# Make directory if it doesn't exist
adb shell mkdir -p /sdcard/Android/data/com.example.pickletv/
adb push /Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4 \
         /sdcard/Android/data/com.example.pickletv/h-6.mp4
```

## Step-by-Step for Solution 1 (Recommended)

```bash
# 1. Connect device/emulator and verify connection
adb devices

# 2. Push the video file
adb push ~/AndroidStudioProjects/PickleTV/h-6.mp4 /sdcard/Download/h-6.mp4

# 3. Verify it was copied
adb shell ls -lh /sdcard/Download/h-6.mp4

# 4. Grant permissions (if needed)
adb shell chmod 644 /sdcard/Download/h-6.mp4

# 5. Run your app in Android Studio
# App will find the file and play it!
```

## What the App Will Log

When you run the app now, in logcat you'll see:

```
D/MainActivity: Searching for video in 7 locations:
D/MainActivity:   - Checking: /data/data/com.example.pickletv/files/h-6.mp4
D/MainActivity:   - Checking: /sdcard/Android/data/com.example.pickletv/files/h-6.mp4
D/MainActivity:   - Checking: /data/data/com.example.pickletv/cache/h-6.mp4
D/MainActivity:   - Checking: /data/data/com.example.pickletv/h-6.mp4
D/MainActivity:   - Checking: /sdcard/Android/data/com.example.pickletv/h-6.mp4
D/MainActivity:   - Checking: /sdcard/Download/h-6.mp4
D/MainActivity: ✓ Found video at: /sdcard/Download/h-6.mp4
D/MainActivity: Video loaded and playing: /sdcard/Download/h-6.mp4
```

## Permissions Required

Make sure your `AndroidManifest.xml` has:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

✅ Your code already has this!

## Troubleshooting

**"adb: command not found"**
```bash
export PATH=$PATH:~/Library/Android/sdk/platform-tools
adb devices
```

**"Permission denied" errors**
```bash
adb shell chmod 777 /sdcard/Download/
adb push h-6.mp4 /sdcard/Download/h-6.mp4
```

**Can't find device**
```bash
adb kill-server
adb start-server
adb devices
```

## Quick Reference

| Method | Command |
|--------|---------|
| Check devices | `adb devices` |
| Push file | `adb push local_path device_path` |
| List files | `adb shell ls -la /path` |
| Remove file | `adb shell rm /path/file` |
| View logs | `adb logcat \| grep MainActivity` |

---

**RECOMMENDED: Use Solution 1 (Copy to /sdcard/Download/)**
- Fastest for testing
- No recompile needed
- Easy to swap different video files

