# PickleTV - Complete Documentation

## 🚀 Quick Start

### Home Screen
- **Browse Content**: Select and play videos from your server
- **Sign In**: Access sign-in form (overlay on home screen)
- **Settings**: Manage cache, view app info

### Video Playback with Keystone Correction
- **D-Pad arrows**: Adjust image shape
- **OK/Center button**: Save adjustments
- **Menu button**: Toggle corner edit mode
- **Channel ↑/↓**: Cycle through corners (in edit mode)

### Downloads & Cache
- Videos auto-download on first playback
- Download progress shows with ⬇️ icon
- Cached videos show ✓ icon and play instantly
- Settings > Storage Management: Clear cache with one click

---

## 📱 Remote Controls (TV Remote)

| Button | Function |
|--------|----------|
| D-Pad ↑↓←→ | Adjust keystone shape |
| OK/Center | Save adjustments |
| Menu | Toggle corner edit mode |
| Channel ↑ | Next corner |
| Channel ↓ | Previous corner |
| Info/Guide | Toggle fine/coarse adjustment |
| B Button | Reset to default |
| Back | Exit video |

---

## ⌨️ Keyboard Controls (Debug Builds)

| Key | Function |
|-----|----------|
| P / Space | Play/pause + edit mode toggle |
| ↑↓←→ | Adjust keystone (same as D-pad) |
| 1/3/7/9 | Select corner (top-left/top-right/bottom-left/bottom-right) |
| N / Tab | Cycle to next corner |
| [ / ] | Decrease/increase adjustment step size |
| 0 / R / Del | Reset to default |
| Enter | Save adjustments |
| E / C | Toggle corner edit mode |

---

## 📦 Creating App Bundle for Google Play

### Step 1: Generate Signing Key (First Time Only)

```bash
keytool -genkey -v -keystore /home/dilly/AndroidStudioProjects/PickleTV/release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias pickletv_key
```

Create a strong password and save it.

### Step 2: Create Signing Configuration

```bash
cd /home/dilly/AndroidStudioProjects/PickleTV

cat > keystore.properties << 'EOF'
storeFile=release.keystore
storePassword=YOUR_PASSWORD
keyAlias=pickletv_key
keyPassword=YOUR_PASSWORD
EOF
```

Replace `YOUR_PASSWORD` with your password from Step 1.

### Step 3: Update Version Code

Edit `app/build.gradle.kts`:
```kotlin
versionCode = 1  // Increment each release: 1 → 2 → 3
versionName = "1.0"
```

### Step 4: Build Bundle

```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./build_bundle.sh
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### Step 5: Upload to Google Play Console

1. Go to https://play.google.com/console
2. Select PickleTV
3. Testing → Internal Testing → Create new release
4. Upload the `.aab` file
5. Add release notes
6. Start rollout

### Security Notes

⚠️ **Never commit these files:**
- `release.keystore` - Signing certificate
- `keystore.properties` - Credentials

Both are protected by `.gitignore`.

---

## 🎥 Content Management

### Content Manifest Format

Your server should have `https://tv.dilly.cloud/content/content.json`:

```json
{
  "videos": [
    {
      "id": "video-1",
      "title": "Video Title",
      "description": "Video description",
      "thumbnailUrl": "https://tv.dilly.cloud/content/thumb.jpg",
      "videoUrl": "https://tv.dilly.cloud/content/video.mp4",
      "duration": 3600,
      "category": "Nature"
    }
  ]
}
```

### Video URL Options

- `https://example.com/video.mp4` - Stream from URL
- `local://filename.mp4` - Play from app cache

### Thumbnail URL Options

- `https://example.com/thumb.jpg` - Remote image
- `asset://filename.jpg` - Local asset (shows placeholder)

---

## 💾 Download & Cache System

### How It Works

1. User selects video
2. App checks if cached locally
3. If not cached: Download starts automatically
4. Progress bar shows percentage at top
5. When complete: Video plays automatically
6. Video now shows ✓ icon (cached)

### Cache Management

- **View size**: Settings → Storage Management
- **Clear cache**: Click "Clear Cache" button
- **Feedback**: Success message appears and auto-dismisses
- **Cache location**: App internal cache directory

---

## 🏠 Home Screen Features

### Browse Content Tab
- Grid of video thumbnails organized by category
- Shows download status with icons:
  - No icon = Not cached, will download
  - ⬇️ = Currently downloading
  - ✓ = Cached locally

### Sign In Tab
- Username field
- Password field (masked)
- Cancel & Sign In buttons
- Overlay style (menu always visible)

### Settings Tab
- Video Quality: Auto
- Keystone Correction: Enabled
- Content Server: tv.dilly.cloud
- Storage Management: Cache size + Clear button

---

## 🔧 Technical Details

### Dependencies
- **Compose UI**: Material3, TV Foundation, TV Material
- **Video**: ExoPlayer with OpenGL keystone correction
- **Networking**: OkHttp for downloads
- **Images**: Coil for async thumbnail loading
- **Serialization**: kotlinx-serialization for JSON
- **Caching**: App internal cache directory

### Architecture
- **HomeActivity**: Launcher with content browsing
- **MainActivity**: Video player with keystone correction
- **VideoDownloadManager**: Handles downloads & caching
- **ContentRepository**: Fetches content manifest from server

### App Specifications
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Android 15)
- **Namespace**: com.pickletv.app
- **Current Version**: 1.0

---

## ✅ Testing Checklist

- [ ] App launches to home screen
- [ ] Content loads from server (or shows fallback)
- [ ] D-pad navigation works between video cards
- [ ] Video selection starts download
- [ ] Progress bar shows percentage
- [ ] Keystone controls adjust image shape
- [ ] OK/Center button saves adjustments
- [ ] Menu button enables corner edit mode
- [ ] Settings cache clear works with feedback
- [ ] Cached videos play instantly
- [ ] Sign In form appears without hiding menu

---

## 🐛 Troubleshooting

### App won't build
```bash
./gradlew clean assembleDebug
```

### Video won't download
- Check network connection
- Verify URL is accessible: `curl https://tv.dilly.cloud/content/content.json`

### Keystone not working
- Press P/Space to enter edit mode (keyboard)
- Press Menu to enable corner edit mode (remote)
- Use D-pad to adjust

### Cache not clearing
- Go to Settings → Storage Management
- Click Clear Cache
- Check success message

---

## 📚 Additional Files

- **README.md** - Project overview
- **KEYBOARD_CONTROLS.md** - Detailed keyboard mappings
- **REMOTE_KEYBOARD_CONTROLS.md** - Combined remote & keyboard guide
- **DOWNLOAD_CACHE_GUIDE.md** - Download system details
- **HOME_SCREEN_GUIDE.md** - Home screen implementation details

---

## 🎯 Feature Summary

✅ **Complete PickleTV App**:
- Google TV-style home screen
- Content browsing with video grid
- Auto-download with progress indicators
- Local caching system
- Settings with cache management
- Full keystone correction
- Sign-in functionality
- D-pad & keyboard controls
- Network resilience

---

**Version**: 1.0  
**Last Updated**: December 3, 2025  
**Status**: Ready for Google Play Testing

