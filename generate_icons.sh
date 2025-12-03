#!/bin/bash

# Script to generate Android app icons from LD_logo.png
# This creates all required mipmap sizes for Android

SOURCE_IMAGE="assets/LD_logo.png"
RES_DIR="app/src/main/res"

# Check if source image exists
if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "Error: Source image not found at $SOURCE_IMAGE"
    exit 1
fi

echo "Generating app icons from $SOURCE_IMAGE..."

# Create mipmap directories if they don't exist
mkdir -p "$RES_DIR/mipmap-mdpi"
mkdir -p "$RES_DIR/mipmap-hdpi"
mkdir -p "$RES_DIR/mipmap-xhdpi"
mkdir -p "$RES_DIR/mipmap-xxhdpi"
mkdir -p "$RES_DIR/mipmap-xxxhdpi"

# Generate launcher icons (rounded square background recommended)
echo "Creating mipmap-mdpi (48x48)..."
convert "$SOURCE_IMAGE" -resize 48x48 -gravity center -extent 48x48 "$RES_DIR/mipmap-mdpi/ic_launcher.png"

echo "Creating mipmap-hdpi (72x72)..."
convert "$SOURCE_IMAGE" -resize 72x72 -gravity center -extent 72x72 "$RES_DIR/mipmap-hdpi/ic_launcher.png"

echo "Creating mipmap-xhdpi (96x96)..."
convert "$SOURCE_IMAGE" -resize 96x96 -gravity center -extent 96x96 "$RES_DIR/mipmap-xhdpi/ic_launcher.png"

echo "Creating mipmap-xxhdpi (144x144)..."
convert "$SOURCE_IMAGE" -resize 144x144 -gravity center -extent 144x144 "$RES_DIR/mipmap-xxhdpi/ic_launcher.png"

echo "Creating mipmap-xxxhdpi (192x192)..."
convert "$SOURCE_IMAGE" -resize 192x192 -gravity center -extent 192x192 "$RES_DIR/mipmap-xxxhdpi/ic_launcher.png"

# Generate TV banner (1280x720 - required for Android TV)
echo "Creating TV banner (1280x720)..."
mkdir -p "$RES_DIR/drawable-xhdpi"
convert "$SOURCE_IMAGE" -resize 1280x720 -gravity center -extent 1280x720 -background white "$RES_DIR/drawable-xhdpi/banner.png"

echo ""
echo "✓ Icon generation complete!"
echo ""
echo "Generated files:"
echo "  - mipmap-mdpi/ic_launcher.png (48x48)"
echo "  - mipmap-hdpi/ic_launcher.png (72x72)"
echo "  - mipmap-xhdpi/ic_launcher.png (96x96)"
echo "  - mipmap-xxhdpi/ic_launcher.png (144x144)"
echo "  - mipmap-xxxhdpi/ic_launcher.png (192x192)"
echo "  - drawable-xhdpi/banner.png (1280x720)"
echo ""
echo "Note: The TV banner may need manual adjustment for best appearance."
echo "You can edit it at: $RES_DIR/drawable-xhdpi/banner.png"

