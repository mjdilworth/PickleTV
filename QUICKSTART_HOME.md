# PickleTV - Quick Start Guide

## 🚀 What's New

Your PickleTV app now has a **Google TV-style home screen** for browsing content!

```
┌─────────────────────────────────────────────────────────┐
│  PickleTV  │  Browse Content  │  Sign In  │  Settings  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Nature                                                 │
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐       │
│  │ Video1 │  │ Video2 │  │ Video3 │  │ Video4 │  →    │
│  └────────┘  └────────┘  └────────┘  └────────┘       │
│                                                         │
│  Demo                                                   │
│  ┌────────┐  ┌────────┐                                │
│  │ Demo1  │  │ Demo2  │                                │
│  └────────┘  └────────┘                                │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## 📋 Setup Steps

### 1. Sync Gradle (First Time)
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./gradlew build
```

### 2. Prepare Your Content

Create a `content.json` file:
```json
{
  "videos": [
    {
      "id": "video-1",
      "title": "My First Video",
      "description": "Video description",
      "thumbnailUrl": "https://tv.dilly.cloud/content/thumb1.jpg",
      "videoUrl": "https://tv.dilly.cloud/content/video1.mp4",
      "duration": "2:30",
      "category": "Nature"
    }
  ]
}
```

### 3. Upload to Server

Upload these files to `https://tv.dilly.cloud/content/`:
- `content.json` (required)
- `video1.mp4` (your video file)
- `thumb1.jpg` (thumbnail image)

**Quick Upload:**
```bash
./upload_content.sh
```

Or manually:
```bash
scp content.json your-server:/var/www/html/content/
scp video1.mp4 your-server:/var/www/html/content/
scp thumb1.jpg your-server:/var/www/html/content/
```

### 4. Test the App

```bash
./gradlew installDebug
adb shell am start -n com.example.pickletv/.HomeActivity
```

## 🎮 Usage

### Navigation
- **D-pad**: Navigate between video cards
- **Enter/OK**: Select video to play
- **Back**: Return to home screen

### Playing Videos
- Select any video thumbnail
- Video plays in MainActivity with keystone correction
- Use all existing keystone controls (see KEYBOARD_CONTROLS.md)

## 📁 File Locations

### New Files Created
```
✅ HomeActivity.kt - Main launcher
✅ data/VideoItem.kt - Data models
✅ data/ContentRepository.kt - Content fetcher
✅ ui/components/VideoThumbnailCard.kt - UI components
✅ content.json - Sample manifest
✅ HOME_SCREEN_GUIDE.md - Full documentation
✅ HOME_SCREEN_README.md - Implementation summary
✅ upload_content.sh - Upload helper script
```

### Modified Files
```
📝 AndroidManifest.xml - HomeActivity is launcher
📝 MainActivity.kt - Supports URL playback
📝 app/build.gradle.kts - New dependencies
📝 gradle/libs.versions.toml - Library versions
```

## 🌐 Content URLs

### Video URL Formats
- **Streaming**: `https://tv.dilly.cloud/content/video.mp4`
- **Local**: `local://h-6.mp4`

### Thumbnail URL Formats
- **Remote**: `https://tv.dilly.cloud/content/thumb.jpg`
- **Asset**: `asset://montblancscene4.jpg` (placeholder shown)

## ✅ Testing Checklist

- [ ] Gradle sync completed successfully
- [ ] App installs without errors
- [ ] HomeActivity launches on app start
- [ ] Content loads (or demo content shows)
- [ ] Can navigate with D-pad
- [ ] Video plays when selected
- [ ] Keystone controls work
- [ ] Can return to home screen

## 🔧 Troubleshooting

### Build Fails
```bash
./gradlew clean build
```

### Content Not Loading
```bash
# Test URL
curl https://tv.dilly.cloud/content/content.json

# Check logs
adb logcat | grep ContentRepository
```

### Videos Not Playing
```bash
# Check MainActivity logs
adb logcat | grep MainActivity

# Verify video codec
ffprobe video.mp4
```

## 📊 App Flow

```
App Launch
    ↓
HomeActivity
    ├─ Browse Content (default)
    │   ├─ Show demo content (if network fails)
    │   └─ Show server content (if successful)
    │       └─ Select Video
    │           └─ MainActivity (keystone player)
    │               ├─ Stream from URL
    │               └─ Play local file
    ├─ Sign In
    │   └─ WelcomeActivity
    └─ Settings
        └─ (TBD)
```

## 🎯 Key Features

✅ **Google TV-style interface**
✅ **Browse without sign-in**
✅ **Stream from URLs**
✅ **Automatic fallback**
✅ **TV-optimized navigation**
✅ **Keystone correction preserved**
✅ **Easy content management**

## 📚 Documentation

- **HOME_SCREEN_GUIDE.md** - Complete implementation guide
- **HOME_SCREEN_README.md** - Implementation summary
- **KEYBOARD_CONTROLS.md** - All keystone controls
- **content.json** - Sample manifest template

## 🚦 Next Steps

1. **Test Demo Content**
   - Build and run the app
   - Should see demo videos

2. **Add Your Content**
   - Edit content.json
   - Upload to server
   - Restart app

3. **Customize**
   - Adjust colors in HomeActivity.kt
   - Modify card sizes in VideoThumbnailCard.kt
   - Update categories as needed

## 💡 Tips

- Start with 1-2 test videos
- Use 16:9 thumbnails (1280x720)
- Keep videos under 500MB for streaming
- Test on actual Android TV device
- Use H.264 codec for compatibility

## ❓ Questions?

Check the documentation:
- Full guide: `HOME_SCREEN_GUIDE.md`
- This summary: `HOME_SCREEN_README.md`
- Controls: `KEYBOARD_CONTROLS.md`

---

**Ready to Go!** 🎬

Build the app and start browsing content!

