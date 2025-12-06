# dil.map - Android TV Video Player with Keystone Correction

**Developer**: Dilworth Creative LLC  
**Package**: `com.dilworth.dilmap`  
**Platform**: Android TV / Google TV  
**Target Devices**: Any Android TV device (Google TV Streamer, Android TV boxes, projectors with Android TV)

## Overview

dil.map is an Android TV application that streams video content with real-time keystone/trapezoid correction for projector alignment. The app features a Google TV-style home screen with browsable content, video download caching, and persistent keystone settings.

### Key Technologies
- **Jetpack Compose for TV**: Modern UI with TV-optimized navigation
- **ExoPlayer**: Video playback and streaming
- **OpenGL ES**: Real-time keystone correction via GLSL shaders
- **Retrofit + Kotlin Coroutines**: Content manifest fetching
- **SharedPreferences**: Persistent settings storage

### Key Technologies
- **Jetpack Compose for TV**: Modern UI with TV-optimized navigation
- **ExoPlayer**: Video playback and streaming
- **OpenGL ES**: Real-time keystone correction via GLSL shaders
- **Retrofit + Kotlin Coroutines**: Content manifest fetching
- **SharedPreferences**: Persistent settings storage

---

## Features

### 🏠 Home Screen
- **Browse Content**: View video thumbnails organized by category
- **Sign In**: User authentication interface
- **Settings**: Cache management and keystone reset
- **Help**: Built-in remote control guide
- **About**: App version, license, and contact information

### 📺 Video Playback
- Stream videos from `https://tv.dilly.cloud/content`
- Download and cache videos for offline playback
- Resume cached videos instantly
- Download progress indicator

### 🔧 Keystone Correction
Real-time trapezoid adjustment for projector alignment:
- **4-corner independent control**: Adjust each corner separately
- **Whole-image adjustment**: Quick top/bottom/left/right adjustments
- **Fine/coarse modes**: Toggle between 0.05 and 0.20 adjustment steps
- **Persistent settings**: Saves across app restarts
- **Visual feedback**: Corner markers and borders during editing

### 💾 Storage Management
- Automatic video caching
- Clear cache from Settings
- Shows cache size
- Reset keystone to defaults

---

## 🎮 Remote Control Compatibility

### Supported Remotes
The app uses **standard Android TV key codes** and works with:
- ✅ **Google TV Streamer** (2024+)
- ✅ **Android TV remotes** (any manufacturer)
- ✅ **Projector remotes** (e.g., Valerian, Epson, BenQ with Android TV)
- ✅ **Third-party Android TV remotes**
- ✅ **Mobile remote apps** (Google TV app on phone)

**Required buttons** on your remote:
- D-Pad (↑ ↓ ← →) and OK/Center button
- Back button

**Note**: Volume buttons control TV hardware directly and cannot be used for app controls.

### Basic Navigation
- **D-Pad ↑↓←→**: Navigate menus and content
- **OK/Center**: Select item or save settings
- **Back**: Return to previous screen

### Keystone Correction (During Video Playback)

**Interactive Menu System:**
1. **Press D-Pad Center** → Opens keystone adjustment menu
2. **Use D-Pad Up/Down** → Navigate menu options
3. **Press D-Pad Center** → Select menu item

**Menu Options:**
- **Corner 1-4** → Enter adjustment mode for that corner
- **Reset to Default** → Reset all corners to center position
- **Save & Exit** → Save current settings and close menu

**Adjusting Corners:**
1. Select a corner from menu → Enters adjustment mode
2. Use **D-Pad arrows** → Move the selected corner
3. Press **D-Pad Center** → Return to menu
4. Press **Back** → Exit keystone adjustment

### Keystone Workflow
```
1. Play video
2. Press D-Pad Center → Keystone menu opens
3. Navigate to "Corner 1 (Top Left)" → Press Center to select
4. Use D-Pad arrows → Adjust corner position
5. Press D-Pad Center → Return to menu
6. Navigate to "Save & Exit" → Press Center to save and close
```

**Notes**: 
- Menu provides visual feedback with highlighted selection
- Toast messages guide you through each step
- Corners show visual markers during adjustment
- Settings persist across app restarts

---

## 🚀 Building for Google Play

### Development Build
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./gradlew installDebug
```

### Release Build for Google Play Internal Testing
```bash
# Build Android App Bundle (AAB)
./gradlew bundleRelease

# Output location:
# app/build/outputs/bundle/release/app-release.aab
```

### Version Management
Before each release, update in `app/build.gradle.kts`:
```kotlin
versionCode = 2        // Increment for each release
versionName = "1.1.0"  // User-friendly version
```

### Upload to Google Play Console
1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app → **Internal Testing** track
3. Click **Create new release**
4. Upload `app-release.aab`
5. Add release notes
6. Click **Review** → **Start rollout**

---

## 🛠️ Development Setup

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+ (minSdk: 26, targetSdk: 36)
- Google TV Streamer remote or emulator

### Keyboard Shortcuts (Development)
Development keyboard shortcuts for testing:

| Keyboard | Simulates | Function |
|----------|-----------|----------|
| **V** or **Enter** | D-Pad Center | Open keystone menu |
| **Arrow Up/Down** | D-Pad Up/Down | Navigate menu |
| **Enter** | D-Pad Center | Select menu item / Return to menu |
| **Arrow keys** | D-Pad arrows | Adjust corner position |
| **Esc** | Back | Exit/Cancel |
| **Del/R/0** | N/A | Reset keystone (dev only) |

**Note**: The interactive menu system works identically on all Android TV remotes - just press D-Pad Center to open the menu.

---

---

## 📁 Project Structure

```
app/src/main/java/com/dilworth/dilmap/
├── HomeActivity.kt          # Main home screen with tabs
├── MainActivity.kt          # Video player with keystone correction
├── WelcomeActivity.kt       # Initial welcome/launcher screen
├── VideoGLRenderer.kt       # OpenGL ES renderer with GLSL shaders
├── WarpShape.kt            # Keystone data model and persistence
├── data/
│   ├── ContentRepository.kt     # Fetches video manifest
│   ├── VideoDownloadManager.kt  # Handles video caching
│   └── VideoItem.kt            # Video data model
└── ui/
    ├── components/              # Reusable UI components
    └── theme/                   # Compose TV theme

assets/                     # App icons and branding
```

### Content Source
Videos are fetched from: `https://tv.dilly.cloud/content/manifest.json`

Example manifest format:
```json
{
  "videos": [
    {
      "id": "1",
      "title": "Sample Video",
      "description": "Video description",
      "thumbnailUrl": "https://tv.dilly.cloud/content/thumbnail.jpg",
      "videoUrl": "https://tv.dilly.cloud/content/video.mp4",
      "category": "Events"
    }
  ]
}
```

---

---

## 🔬 Technical Details

### Keystone Correction Implementation
- **GLSL Vertex Shader**: Applies trapezoid warp transformation
- **4 Corner Offsets**: Independent X/Y displacement for each corner
- **Interpolation**: Smooth warping across the entire viewport
- **Real-time Updates**: Adjustments applied immediately during playback

### Data Persistence
- **Keystone Settings**: Saved to SharedPreferences as 8 float values
- **Video Cache**: Stored in app's cache directory
- **Settings Survival**: Persists across app restarts and device reboots

### Performance
- **Render Mode**: `WHEN_DIRTY` - only renders when needed (battery efficient)
- **Video Decoding**: Hardware-accelerated via ExoPlayer
- **OpenGL ES 2.0**: Broad device compatibility

---

## 📚 Documentation

- **[REMOTE_KEYBOARD_CONTROLS.md](REMOTE_KEYBOARD_CONTROLS.md)**: Complete control reference with workflows
- **[GOOGLE_TV_STREAMER_TESTING.md](GOOGLE_TV_STREAMER_TESTING.md)**: Development testing guide
- **[UPLOAD_TO_PLAY_STORE.md](UPLOAD_TO_PLAY_STORE.md)**: Google Play Console upload instructions

---

---

## 🐛 Troubleshooting

### Video Not Playing
- Check network connection (videos stream from tv.dilly.cloud)
- View download progress indicator if video is caching
- Check logcat for network errors: `adb logcat | grep "ContentRepository\|VideoDownload"`

### Keystone Adjustment Not Working
- **Enable corner edit mode first**: Press Volume Up (or V key)
- Look for visual indicators (corner markers and borders)
- Keystone only works during video playback, not on home screen
- Check logs: `adb logcat | grep "Google TV Remote\|Keyboard"`

### Settings Not Persisting
- Settings are saved when you press OK/Center during video playback
- Look for toast message: "✓ Keystone position saved"
- Reset from Settings → Keystone Correction → Reset Keystone

### Cache Issues
- Clear cache from Settings tab
- Check cache size in Settings
- Cache location: `{app_data}/cache/video_cache/`

### Help Tab Not Scrolling
- Each section is focusable - use D-Pad Down to navigate between sections
- Scroll indicator at bottom shows when more content available

---

## 📱 Device Compatibility

**Tested On:**
- Google TV Streamer (2024)
- Android TV Emulator (x86, API 33+)

**Requirements:**
- Android TV / Google TV
- API Level 26+ (Android 8.0+)
- OpenGL ES 2.0 support
- Internet connection for content streaming

---

## 📄 License

Copyright © 2025 Dilworth Creative LLC. All rights reserved.

---

## 🆘 Support

For issues or questions:
- Check the **Help** tab in the app for remote control guide
- Check the **About** tab for version and contact information
- Review [REMOTE_KEYBOARD_CONTROLS.md](REMOTE_KEYBOARD_CONTROLS.md) for detailed controls
- See [GOOGLE_TV_STREAMER_TESTING.md](GOOGLE_TV_STREAMER_TESTING.md) for development testing

**Contact:**
- 🌐 Website: [lucindadilworth.com](https://lucindadilworth.com)
- 📧 Email: hello@lucindadilworth.com
- 📱 Instagram: [@dil.worth](https://instagram.com/dil.worth)

---

**Version**: 1.0.1 (Build 2)  
**Last Updated**: December 4, 2025  
**Package**: com.dilworth.dilmap  
**Developer**: Dilworth Creative LLC  
**Website**: [lucindadilworth.com](https://lucindadilworth.com)
