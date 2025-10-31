# 🎯 PickleTV Debug Automation - Complete Setup

## What You Get

I've created **automated scripts** to make debugging super easy. No more manual `adb push` commands!

## Files Created

1. **`setup_debug_video.sh`** - Main setup script (automatic video push)
2. **`DEBUG_SETUP.md`** - Quick reference guide
3. **`add_to_zshrc.sh`** - Optional convenience aliases

## Quick Start (Recommended)

### Option A: One-Time Setup
```bash
cd /Users/mike/AndroidStudioProjects/PickleTV
./setup_debug_video.sh
```

### Option B: Add Convenient Aliases (Optional)
```bash
# Add these to ~/.zshrc for quick commands
cat add_to_zshrc.sh >> ~/.zshrc
source ~/.zshrc

# Then you can use:
pickletv-setup        # Run setup
pickletv-logs         # View logs
pickletv-debug        # Setup + logs
```

## Step-by-Step Debugging Workflow

### First Time Setup
```bash
1. cd /Users/mike/AndroidStudioProjects/PickleTV
2. ./setup_debug_video.sh
3. Verify output says "✅ Setup Complete!"
```

### Every Time You Debug
```bash
1. Connect Android device/emulator
2. Run: ./setup_debug_video.sh
3. Open Android Studio
4. Run app: Build → Run 'app'
5. Watch Logcat for:
   - "✓ Found video at: /sdcard/Download/h-6.mp4"
   - "Video loaded and playing"
```

## What the Script Checks

The `setup_debug_video.sh` script automatically verifies:

| Check | What It Does | Example |
|-------|-------------|---------|
| Video Exists | Looks for h-6.mp4 in project root | ❌ or ✓ |
| ADB Available | Checks if adb command exists | ❌ or ✓ |
| Device Connected | Verifies phone/emulator is ready | ❌ or ✓ |
| File Pushed | Copies h-6.mp4 to device | ✓ (5.2 MB in 2.3s) |
| File Verified | Confirms file arrived | ✓ (Size: 5.2M) |
| Permissions Set | Makes file readable | ✓ |

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

✓ File verified on device (Size: 5.2M)

✓ Permissions set

==========================================
✅ Setup Complete!
==========================================
```

## Optional: Add Aliases to ~/.zshrc

Add these lines to `~/.zshrc` for quick commands:

```bash
# PickleTV shortcuts
alias pickletv-setup="cd /Users/mike/AndroidStudioProjects/PickleTV && ./setup_debug_video.sh"
alias pickletv-logs="adb logcat | grep MainActivity"
alias pickletv-debug="cd /Users/mike/AndroidStudioProjects/PickleTV && ./setup_debug_video.sh && adb logcat | grep MainActivity"
```

Then you can just type:
```bash
pickletv-setup        # Setup video
pickletv-logs         # Watch logs
pickletv-debug        # Setup + logs
```

## Common Commands

```bash
# Push video and verify
./setup_debug_video.sh

# View logs in real-time
adb logcat | grep MainActivity

# Clear logs before testing
adb logcat -c

# List connected devices
adb devices

# Stop the app
adb shell am force-stop com.example.pickletv

# Check if file exists on device
adb shell ls -lh /sdcard/Download/h-6.mp4

# Remove old video
adb shell rm /sdcard/Download/h-6.mp4
```

## Troubleshooting

### "Command not found: adb"
```bash
export PATH=$PATH:~/Library/Android/sdk/platform-tools
./setup_debug_video.sh
```

### "No devices connected"
- Start Android emulator in Android Studio, OR
- Connect physical device with USB debugging enabled
- Run: `adb devices`

### Script shows "permission denied"
```bash
chmod +x /Users/mike/AndroidStudioProjects/PickleTV/setup_debug_video.sh
./setup_debug_video.sh
```

### Video still not found in app
1. Run the script
2. Verify output says "✅ Setup Complete!"
3. Check Android Studio logcat:
   ```
   D/MainActivity: ✓ Found video at: /sdcard/Download/h-6.mp4
   ```
4. If different path, update the code

## What's Automated

Before these scripts, debugging required:
```bash
adb push h-6.mp4 /sdcard/Download/h-6.mp4
```

Now it's:
```bash
./setup_debug_video.sh
```

Plus you get:
- ✓ Automatic error checking
- ✓ Device verification
- ✓ File validation
- ✓ Permission setup
- ✓ Helpful error messages
- ✓ Clear success indication

## Next Steps

1. **Run the setup script:**
   ```bash
   ./setup_debug_video.sh
   ```

2. **Open Android Studio and run the app**

3. **Watch the logcat for "Video loaded and playing"**

4. **Video should play! 🎬**

---

**That's it!** The script handles everything automatically. Just run it once before each debugging session! ✅

