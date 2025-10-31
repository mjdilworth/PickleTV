˘˘#!/bin/bash
# PickleTV Video Debug Setup Script
# This script pushes h-6.mp4 to the Android device for debugging

set -e  # Exit on error

PROJECT_ROOT="/Users/mike/AndroidStudioProjects/PickleTV"
VIDEO_FILE="$PROJECT_ROOT/h-6.mp4"
DEVICE_PATH="/sdcard/Android/data/com.example.pickletv/cache/h-6.mp4"
PACKAGE_NAME="com.example.pickletv"

echo "=========================================="
echo "PickleTV Video Debug Setup"
echo "=========================================="
echo ""

# Check if video file exists
if [ ! -f "$VIDEO_FILE" ]; then
    echo "❌ ERROR: Video file not found at $VIDEO_FILE"
    echo "Please ensure h-6.mp4 is in the project root directory"
    exit 1
fi

echo "✓ Found video file: $VIDEO_FILE"
echo ""

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ ERROR: adb not found in PATH"
    echo "Please install Android SDK or add it to PATH:"
    echo "  export PATH=\$PATH:~/Library/Android/sdk/platform-tools"
    exit 1
fi

echo "✓ adb is available"
echo ""

# Check for connected devices
echo "Checking for connected devices..."
DEVICE_COUNT=$(adb devices | grep -c "device$")

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "❌ ERROR: No Android devices/emulators connected"
    echo "Please:"
    echo "  1. Connect an Android device via USB, OR"
    echo "  2. Start an Android emulator"
    echo ""
    echo "To list devices:"
    echo "  adb devices"
    exit 1
fi

echo "✓ Found $DEVICE_COUNT connected device(s)"
echo ""

# List connected devices
echo "Connected devices:"
adb devices | grep "device$" | awk '{print "  - " $1}'
echo ""

# Create the cache directory on device
echo "Creating app cache directory..."
adb shell mkdir -p "/sdcard/Android/data/$PACKAGE_NAME/cache"
echo "✓ Directory created"
echo ""

# Push the video file
echo "Pushing video file to device..."
echo "  Source: $VIDEO_FILE"
echo "  Destination: $DEVICE_PATH"
echo ""

if adb push "$VIDEO_FILE" "$DEVICE_PATH" 2>&1 | tail -5; then
    echo ""
    echo "✓ Video file successfully pushed!"
else
    echo ""
    echo "❌ Failed to push video file"
    exit 1
fi

echo ""

# Verify the file
echo "Verifying file on device..."
if adb shell test -f "$DEVICE_PATH"; then
    FILE_SIZE=$(adb shell ls -lh "$DEVICE_PATH" | awk '{print $5}')
    echo "✓ File verified on device (Size: $FILE_SIZE)"
else
    echo "❌ File verification failed"
    exit 1
fi

echo ""

# Grant permissions - app cache dir is already readable by app
echo "Setting file permissions..."
adb shell chmod 644 "$DEVICE_PATH"
echo "✓ Permissions set"

echo ""
echo "=========================================="
echo "✅ Setup Complete!"
echo "=========================================="
echo ""
echo "You can now:"
echo "  1. Run your app in Android Studio"
echo "  2. The video will be found at: $DEVICE_PATH"
echo "  3. Check logcat to verify it's playing"
echo ""
echo "To view logs in real-time:"
echo "  adb logcat | grep MainActivity"
echo ""

