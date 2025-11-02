#!/bin/bash
# Simple script to push h-6.mp4 to the emulator/device

set -euo pipefail

ADB="/home/dilly/Android/Sdk/platform-tools/adb"
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
VIDEO_FILE="$PROJECT_ROOT/h-6.mp4"
DEVICE_DEST="/sdcard/Android/data/com.example.pickletv/cache/h-6.mp4"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}PickleTV Video Pusher${NC}"
echo "================================"

# Check if video exists
if [ ! -f "$VIDEO_FILE" ]; then
    echo -e "${RED}✗ Video file not found: $VIDEO_FILE${NC}"
    echo "  Checking for h-6_720p30.mp4..."
    if [ -f "$PROJECT_ROOT/h-6_720p30.mp4" ]; then
        echo -e "${YELLOW}  Found! Copying to h-6.mp4...${NC}"
        cp "$PROJECT_ROOT/h-6_720p30.mp4" "$VIDEO_FILE"
        echo -e "${GREEN}  ✓ Copied${NC}"
    else
        echo -e "${RED}✗ h-6_720p30.mp4 not found either${NC}"
        exit 1
    fi
fi

echo "Video file: $VIDEO_FILE"
echo "Device dest: $DEVICE_DEST"

# Check if device is connected
DEVICES=$($ADB devices | grep -v "List of devices" | grep -v "^$")
if [ -z "$DEVICES" ]; then
    echo -e "${RED}✗ No devices connected${NC}"
    exit 1
fi

DEVICE_ID=$(echo "$DEVICES" | head -1 | awk '{print $1}')
echo "Target device: $DEVICE_ID"
echo ""

# Push the file
echo -e "${YELLOW}Pushing video...${NC}"
$ADB -s "$DEVICE_ID" push "$VIDEO_FILE" "$DEVICE_DEST"

# Verify
echo ""
echo -e "${YELLOW}Verifying...${NC}"
SIZE=$($ADB -s "$DEVICE_ID" shell ls -lh "$DEVICE_DEST" | awk '{print $5}')
if [ -n "$SIZE" ]; then
    echo -e "${GREEN}✓ Video pushed successfully!${NC}"
    echo "  Size on device: $SIZE"
    echo ""
    echo -e "${GREEN}You can now run the app!${NC}"
else
    echo -e "${RED}✗ Verification failed${NC}"
    exit 1
fi

