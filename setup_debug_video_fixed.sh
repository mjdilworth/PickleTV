#!/bin/bash
set -e
PROJECT_ROOT="/Users/mike/AndroidStudioProjects/PickleTV"
VIDEO_FILE="$PROJECT_ROOT/h-6.mp4"
FALLBACK_FILE="$PROJECT_ROOT/h-6_720p30.mp4"
DEVICE_PATH="/sdcard/Android/data/com.example.pickletv/cache/h-6.mp4"
PACKAGE_NAME="com.example.pickletv"

echo "=========================================="
echo "PickleTV Video Debug Setup"
echo "=========================================="
echo ""

if [ ! -f "$VIDEO_FILE" ]; then
    echo "Primary video not found at $VIDEO_FILE"
    if [ -f "$FALLBACK_FILE" ]; then
        echo "Using fallback file: $FALLBACK_FILE"
        VIDEO_FILE="$FALLBACK_FILE"
    else
        echo "Generating fallback 720p30 baseline test clip (requires ffmpeg)..."
        if ! command -v ffmpeg &> /dev/null; then
            echo "ERROR: ffmpeg not installed. Install via Homebrew: brew install ffmpeg"
            exit 1
        fi
        ffmpeg -f lavfi -i testsrc=size=1280x720:rate=30 -f lavfi -i sine=frequency=440:sample_rate=48000 \
          -c:v libx264 -profile:v baseline -level 3.1 -pix_fmt yuv420p -preset veryfast -crf 18 \
          -c:a aac -b:a 128k -t 10 "$FALLBACK_FILE"
        VIDEO_FILE="$FALLBACK_FILE"
    fi
fi

echo "Found video file: $VIDEO_FILE"
echo ""

if ! command -v adb &> /dev/null; then
    echo "ERROR: adb not found in PATH"
    exit 1
fi

echo "adb is available"
echo ""

echo "Checking for connected devices..."
DEVICE_COUNT=$(adb devices | grep -c "device$" || true)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "ERROR: No Android devices/emulators connected"
    exit 1
fi

echo "Found $DEVICE_COUNT connected device(s)"
echo ""

adb shell mkdir -p "/sdcard/Android/data/$PACKAGE_NAME/cache"
echo "Directory created"
echo ""

echo "Pushing video file to device..."
echo "Source: $VIDEO_FILE"
echo "Destination: $DEVICE_PATH"
echo ""

adb push "$VIDEO_FILE" "$DEVICE_PATH"

echo ""
echo "Verifying file on device..."
if adb shell test -f "$DEVICE_PATH"; then
    echo "File verified on device"
else
    echo "File verification failed"
    exit 1
fi

echo ""
echo "Setting file permissions..."
adb shell chmod 644 "$DEVICE_PATH"
echo "Permissions set"

echo ""
echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "Run your app in Android Studio now"
echo "Video will be found at: $DEVICE_PATH"
echo ""
echo "View logs:"
echo "adb logcat | grep MainActivity"
echo ""
