# PickleTV App Bundle - Build Instructions

## Quick Start: Create App Bundle for Google Play

### Step 1: Generate a Signing Key

If you don't have a signing key yet, create one:

```bash
cd /home/dilly/AndroidStudioProjects/PickleTV

# Generate keystore (one-time only)
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias pickletv_key
```

**When prompted, enter:**
- Keystore password: (remember this!)
- Key password: (same as keystore)
- First/last name: Your name
- Organization: PickleTV
- City/Province: Your location
- Country: US (or your country)

This creates `release.keystore` file in the project root.

### Step 2: Create keystore.properties File

Create a file `keystore.properties` in the project root with your signing credentials:

```bash
cat > /home/dilly/AndroidStudioProjects/PickleTV/keystore.properties << 'EOF'
storeFile=release.keystore
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=pickletv_key
keyPassword=YOUR_KEY_PASSWORD
EOF
```

Replace `YOUR_KEYSTORE_PASSWORD` and `YOUR_KEY_PASSWORD` with the passwords you entered above.

### Step 3: Build the App Bundle

```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./gradlew bundleRelease
```

**Output location:**
```
app/build/outputs/bundle/release/app-release.aab
```

### Step 4: Upload to Google Play Console

1. Go to https://play.google.com/console
2. Select your app
3. Go to **Testing** > **Internal Testing**
4. Click **Upload new release**
5. Select the `.aab` file from `app/build/outputs/bundle/release/app-release.aab`
6. Fill in release notes and click **Review release**
7. Click **Start rollout to internal testing**

---

## Advanced: Automated Build Script

Create this script to automate the process:

```bash
#!/bin/bash
# build_bundle.sh

set -e

echo "🔨 Building PickleTV App Bundle..."

cd /home/dilly/AndroidStudioProjects/PickleTV

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build bundle
echo "📦 Building release bundle..."
./gradlew bundleRelease

# Get the output path
OUTPUT_PATH="app/build/outputs/bundle/release/app-release.aab"

if [ -f "$OUTPUT_PATH" ]; then
    echo "✅ App bundle created successfully!"
    echo "📍 Location: $OUTPUT_PATH"
    echo "📊 Size: $(du -h $OUTPUT_PATH | cut -f1)"
    echo ""
    echo "Next steps:"
    echo "1. Go to https://play.google.com/console"
    echo "2. Select your app"
    echo "3. Upload $OUTPUT_PATH to Internal Testing"
else
    echo "❌ Build failed - no AAB file found"
    exit 1
fi
```

Save as `build_bundle.sh` and run:
```bash
chmod +x /home/dilly/AndroidStudioProjects/PickleTV/build_bundle.sh
./build_bundle.sh
```

---

## Troubleshooting

### "keystore.properties not found"
**Solution**: Create the file as described in Step 2

### "Invalid keystore"
**Solution**: Make sure `release.keystore` exists in the project root

### "Build failed - missing signing configuration"
**Solution**: Rebuild with the updated build.gradle.kts

### "AAB file not found after build"
**Solution**: Check build output: `./gradlew bundleRelease --info`

---

## Important Notes

### Security
- **Never commit** `keystore.properties` or `release.keystore` to Git
- Add to `.gitignore`:
  ```
  keystore.properties
  release.keystore
  ```
- Keep your keystore password safe - you'll need it for future updates

### Version Management
Update version before each release in `app/build.gradle.kts`:
```kotlin
versionCode = 2  // Increment each release
versionName = "1.1"  // User-visible version
```

### App Size
- Bundle is optimized per device configuration
- Actual downloaded size varies by device
- Check bundle size in Play Console

---

## What's Included in the Bundle

✅ Full PickleTV app with all features:
- Google TV-style home screen
- Video content browsing & selection
- Download & caching system with progress indicators
- Keystone correction with full remote/keyboard controls
- Settings with cache management
- Sign-in functionality

---

## Next Steps

1. Create keystore (Step 1)
2. Create keystore.properties (Step 2)
3. Build bundle: `./gradlew bundleRelease`
4. Upload to Google Play Console Internal Testing
5. Share download link with testers

---

**For Support**: Check app logs during testing with:
```bash
adb logcat | grep -E "PickleTV|HomeActivity|MainActivity"
```

