# 🚀 Quick Debug Setup - One Command Run

## The Easy Way

Just run this one command from the project root:

```bash
cd /Users/mike/AndroidStudioProjects/PickleTV
./setup_debug_video.sh
```

That's it! ✅

## What It Does

The script automatically:
1. ✓ Checks if h-6.mp4 exists in project root
2. ✓ Verifies adb is installed
3. ✓ Checks for connected Android devices
4. ✓ Pushes h-6.mp4 to `/sdcard/Download/h-6.mp4`
5. ✓ Verifies the file arrived correctly
6. ✓ Sets proper permissions
7. ✓ Shows success message

## Example Output

```
==========================================
PickleTV Video Debug Setup
==========================================

✓ Found video file: /Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4

✓ adb is available

Checking for connected devices...
✓ Found 1 connected device(s)

Connected devices:
  - emulator-5554

Pushing video file to device...
  Source: /Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4
  Destination: /sdcard/Download/h-6.mp4

h-6.mp4: 1 file pushed. 5.2 MB in 2.345s (2.2 MB/s)

✓ Video file successfully pushed!

Verifying file on device...
✓ File verified on device (Size: 5.2M)

Ensuring file has correct permissions...
✓ Permissions set

==========================================
✅ Setup Complete!
==========================================

You can now:
  1. Run your app in Android Studio
  2. The video will be found at: /sdcard/Download/h-6.mp4
  3. Check logcat to verify it's playing

To view logs in real-time:
  adb logcat | grep MainActivity
```

## When To Run This

- **First time debugging** - Run once before testing
- **After changing video file** - Run again to push new file
- **After factory reset device** - Run again to restore file
- **Every debug session** - No harm running repeatedly, it's fast

## Troubleshooting

### Script says "adb not found"
```bash
export PATH=$PATH:~/Library/Android/sdk/platform-tools
./setup_debug_video.sh
```

### Script says "No devices connected"
- Start Android emulator, OR
- Connect physical Android device via USB
- Run: `adb devices` to verify

### Script says "h-6.mp4 not found"
- Make sure h-6.mp4 is in project root:
  ```bash
  ls -la /Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4
  ```

## Manual Alternative (if script doesn't work)

```bash
adb push /Users/mike/AndroidStudioProjects/PickleTV/h-6.mp4 /sdcard/Download/h-6.mp4
```

## Next Steps

After running the script:

1. **Run your app in Android Studio**
2. **Open Android Studio Logcat** (View → Tool Windows → Logcat)
3. **Watch for these messages:**
   ```
   D/MainActivity: ✓ Found video at: /sdcard/Download/h-6.mp4
   D/MainActivity: Video loaded and playing: /sdcard/Download/h-6.mp4
   ```
4. **Video should play!** 🎬

---

**TL;DR:** Just run `./setup_debug_video.sh` and you're done! ✅

