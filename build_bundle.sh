#!/bin/bash

# PickleTV - App Bundle Builder
# This script automates the process of building an app bundle for Google Play

set -e

PROJECT_ROOT="/home/dilly/AndroidStudioProjects/PickleTV"
KEYSTORE_PROPS="$PROJECT_ROOT/keystore.properties"
KEYSTORE_FILE="$PROJECT_ROOT/release.keystore"

echo "======================================="
echo "PickleTV - App Bundle Builder"
echo "======================================="
echo ""

# Check if keystore exists
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "❌ Signing keystore not found: $KEYSTORE_FILE"
    echo ""
    echo "To create a signing key, run:"
    echo "  keytool -genkey -v -keystore $KEYSTORE_FILE \\"
    echo "    -keyalg RSA -keysize 2048 -validity 10000 \\"
    echo "    -alias pickletv_key"
    echo ""
    exit 1
fi

# Check if keystore.properties exists
if [ ! -f "$KEYSTORE_PROPS" ]; then
    echo "❌ Keystore properties file not found: $KEYSTORE_PROPS"
    echo ""
    echo "Create keystore.properties with your signing credentials:"
    echo "  cat > $KEYSTORE_PROPS << 'EOF'"
    echo "storeFile=release.keystore"
    echo "storePassword=YOUR_PASSWORD"
    echo "keyAlias=pickletv_key"
    echo "keyPassword=YOUR_PASSWORD"
    echo "EOF"
    echo ""
    exit 1
fi

# Change to project directory
cd "$PROJECT_ROOT"

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build the bundle
echo "📦 Building release app bundle..."
./gradlew bundleRelease

# Check if build was successful
OUTPUT_PATH="$PROJECT_ROOT/app/build/outputs/bundle/release/app-release.aab"

if [ -f "$OUTPUT_PATH" ]; then
    SIZE=$(du -h "$OUTPUT_PATH" | cut -f1)

    echo ""
    echo "======================================="
    echo "✅ BUILD SUCCESSFUL!"
    echo "======================================="
    echo ""
    echo "📍 App Bundle Location:"
    echo "   $OUTPUT_PATH"
    echo ""
    echo "📊 Bundle Size: $SIZE"
    echo ""
    echo "📋 Next Steps:"
    echo "   1. Open Google Play Console"
    echo "      https://play.google.com/console"
    echo ""
    echo "   2. Select your app (com.pickletv.app)"
    echo ""
    echo "   3. Go to Testing > Internal Testing"
    echo ""
    echo "   4. Click 'Create new release'"
    echo ""
    echo "   5. Upload the AAB file:"
    echo "      $OUTPUT_PATH"
    echo ""
    echo "   6. Add release notes"
    echo ""
    echo "   7. Click 'Review release'"
    echo ""
    echo "   8. Click 'Start rollout to internal testing'"
    echo ""
    echo "======================================="

    # Optional: Copy to temp location for easy access
    BACKUP_PATH="/tmp/PickleTV-$(date +%Y%m%d-%H%M%S).aab"
    cp "$OUTPUT_PATH" "$BACKUP_PATH"
    echo "📋 Backup copy: $BACKUP_PATH"
else
    echo ""
    echo "❌ BUILD FAILED"
    echo "App bundle not found at: $OUTPUT_PATH"
    echo ""
    echo "Check the build output above for errors."
    exit 1
fi

