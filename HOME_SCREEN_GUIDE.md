# Home Screen Implementation Guide

## Overview

PickleTV now has a Google TV-style home screen that allows users to browse free content before playing videos in the keystone player. The home screen features:

- **Top Navigation Menu**: Browse Content, Sign In, Settings
- **Content Grid**: Video thumbnails organized by category
- **Streaming Support**: Videos can be streamed from URLs or played locally
- **Fallback Content**: Demo videos available when network is unavailable

## Architecture

### New Components

1. **HomeActivity.kt** - Main launcher activity with Compose UI
2. **data/VideoItem.kt** - Data models for video content
3. **data/ContentRepository.kt** - Fetches content from server or provides fallback
4. **ui/components/VideoThumbnailCard.kt** - Reusable video card component

### Navigation Flow

```
HomeActivity (Launcher)
    ├─→ Browse Content (Default tab)
    │   └─→ Select Video → MainActivity (plays video)
    ├─→ Sign In → WelcomeActivity
    └─→ Settings (TBD)
```

### Content Discovery

Content is fetched from a JSON manifest file hosted at:
```
https://tv.dilly.cloud/content/content.json
```

If the network request fails, the app falls back to demo content defined in `ContentRepository.kt`.

## Content Manifest Format

Upload this `content.json` file to `https://tv.dilly.cloud/content/`:

```json
{
  "videos": [
    {
      "id": "unique-id",
      "title": "Video Title",
      "description": "Video description",
      "thumbnailUrl": "https://tv.dilly.cloud/content/thumbnail.jpg",
      "videoUrl": "https://tv.dilly.cloud/content/video.mp4",
      "duration": "2:30",
      "category": "Nature"
    }
  ]
}
```

### Field Descriptions

- **id**: Unique identifier for the video
- **title**: Display title shown on the card
- **description**: Optional description (not currently displayed)
- **thumbnailUrl**: URL to JPG thumbnail image
  - Can use `https://` URLs for remote images
  - Can use `asset://filename.jpg` for local assets
- **videoUrl**: URL to MP4 video file
  - `https://` or `http://` for streaming
  - `local://filename.mp4` for local app files
- **duration**: Display duration (e.g., "2:30")
- **category**: Category for grouping (e.g., "Nature", "Demo", "Sports")

## Video Playback Strategy

### Streaming (Recommended)

Videos are streamed directly from URLs using ExoPlayer's built-in buffering:

```kotlin
videoUrl = "https://tv.dilly.cloud/content/video.mp4"
```

**Advantages:**
- No storage required
- ExoPlayer handles adaptive buffering
- Always plays latest version

### Local Playback

Videos can reference local files:

```kotlin
videoUrl = "local://h-6.mp4"
```

The app searches these locations:
1. `/sdcard/Android/data/com.example.pickletv/cache/`
2. App cache directory
3. App files directory
4. External files directory

## Hosting Content

### Option 1: Static Web Server

1. Create a directory structure:
```
content/
  ├── content.json
  ├── video1.mp4
  ├── video1.jpg
  ├── video2.mp4
  └── video2.jpg
```

2. Upload to `https://tv.dilly.cloud/content/`

3. Ensure CORS headers are set:
```
Access-Control-Allow-Origin: *
```

### Option 2: CDN (Recommended for Production)

Use a CDN like Cloudflare, AWS CloudFront, or similar for better performance:

1. Upload videos to CDN
2. Update `content.json` with CDN URLs
3. Host `content.json` at `https://tv.dilly.cloud/content/content.json`

### Thumbnail Guidelines

- **Format**: JPG or PNG
- **Dimensions**: 1280x720 (16:9 aspect ratio)
- **File size**: < 200KB for fast loading
- **Content**: Representative frame from video

## Adding Content

### Step 1: Prepare Files

1. Create video file (MP4, H.264 codec recommended)
2. Generate thumbnail image (16:9 aspect ratio)
3. Upload both to server

### Step 2: Update Manifest

Add entry to `content.json`:

```json
{
  "id": "new-video-1",
  "title": "New Video Title",
  "description": "Description here",
  "thumbnailUrl": "https://tv.dilly.cloud/content/new-video-1.jpg",
  "videoUrl": "https://tv.dilly.cloud/content/new-video-1.mp4",
  "duration": "3:15",
  "category": "Nature"
}
```

### Step 3: Test

Restart the app to fetch the updated manifest.

## Fallback/Demo Content

If `content.json` cannot be fetched, the app shows demo content defined in `ContentRepository.getDemoContent()`:

```kotlin
private fun getDemoContent(): ContentManifest {
    return ContentManifest(
        videos = listOf(
            VideoItem(
                id = "demo-1",
                title = "Demo Video",
                thumbnailUrl = "https://tv.dilly.cloud/content/demo-thumb.jpg",
                videoUrl = "local://h-6.mp4",
                category = "Demo"
            )
        )
    )
}
```

To update demo content, edit this method in `ContentRepository.kt`.

## UI Customization

### Changing Colors

Edit theme in `ui/theme/Color.kt` or modify inline colors in HomeActivity:

```kotlin
// Top nav background
.background(Color(0xFF0A0A0A))

// Card background
CardDefaults.colors(
    containerColor = Color(0xFF1A1A1A),
    focusedContainerColor = Color(0xFF2A2A2A)
)
```

### Card Dimensions

Modify in `VideoThumbnailCard.kt`:

```kotlin
Card(
    modifier = modifier
        .width(220.dp)  // Card width
        .height(150.dp) // Card height
)
```

### Grid Spacing

Modify in `CategorySection`:

```kotlin
TvLazyRow(
    horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between cards
)
```

## Dependencies Added

```toml
# gradle/libs.versions.toml
coil = "2.5.0"
retrofit = "2.9.0"
okhttp = "4.12.0"
kotlinxSerialization = "1.6.0"

# Libraries
coil-compose
retrofit
retrofit-kotlinx-serialization
okhttp
okhttp-logging
kotlinx-serialization-json
```

## Network Permissions

Already included in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Testing

### 1. Test with Demo Content

1. Run the app without network
2. Should see demo content from fallback

### 2. Test with Server Content

1. Ensure `content.json` is accessible at URL
2. Run app with network
3. Should load content from server

### 3. Test Video Playback

1. Select a video thumbnail
2. Should launch MainActivity with keystone player
3. Video should play with all keystone controls available (see KEYBOARD_CONTROLS.md)

### 4. Test Navigation

1. Use D-pad to navigate between cards
2. Cards should scale on focus
3. Test "Sign In" and "Settings" tabs

## Troubleshooting

### Content Not Loading

1. Check network connectivity
2. Verify `content.json` URL is accessible: `curl https://tv.dilly.cloud/content/content.json`
3. Check logcat for errors: `adb logcat | grep ContentRepository`

### Videos Not Playing

1. Check video URL is accessible
2. Verify video codec (H.264 recommended)
3. Check MainActivity logs: `adb logcat | grep MainActivity`

### Images Not Loading

1. Verify thumbnail URLs are accessible
2. Check Coil logs for image loading errors
3. Ensure CORS headers are set on server

### D-pad Navigation Issues

1. Ensure app is built for TV (leanback features enabled)
2. Test on actual Android TV device or TV emulator
3. Check focus indicators are visible

## Future Enhancements

### Short Term
- [ ] Add search functionality
- [ ] Implement Settings screen
- [ ] Add video progress tracking
- [ ] Implement "Continue Watching" section

### Medium Term
- [ ] Add user authentication
- [ ] Implement favorites/watchlist
- [ ] Add video categories filtering
- [ ] Support live streaming

### Long Term
- [ ] Offline download support
- [ ] Multiple user profiles
- [ ] Parental controls
- [ ] Recommendations engine

## Content Management Workflow

### Recommended Process

1. **Create Content**
   - Record/edit video
   - Export as MP4 (H.264, 1080p or 720p)
   - Generate thumbnail from key frame

2. **Upload to Server**
   ```bash
   scp video.mp4 server:/path/to/content/
   scp thumbnail.jpg server:/path/to/content/
   ```

3. **Update Manifest**
   - Edit `content.json` locally
   - Add new video entry
   - Upload: `scp content.json server:/path/to/content/`

4. **Test**
   - Restart PickleTV app
   - Verify new content appears
   - Test playback

## Server Setup Example

### Nginx Configuration

```nginx
server {
    listen 80;
    server_name tv.dilly.cloud;
    
    location /content/ {
        alias /var/www/content/;
        add_header Access-Control-Allow-Origin *;
        add_header Cache-Control "public, max-age=3600";
    }
}
```

### Apache Configuration

```apache
<Directory "/var/www/content">
    Header set Access-Control-Allow-Origin "*"
    Header set Cache-Control "public, max-age=3600"
</Directory>
```

## Summary

The new home screen provides a user-friendly way to browse and select content before playback. The system is flexible, supporting both streaming and local playback, with automatic fallback to demo content when needed.

**Key Benefits:**
- ✅ Browse content without sign-in required
- ✅ Stream videos efficiently with ExoPlayer
- ✅ Easy content management via JSON manifest
- ✅ Graceful fallback when network unavailable
- ✅ Full keystone correction in playback (existing feature)

**Next Steps:**
1. Upload `content.json` to your server
2. Add video files and thumbnails to server
3. Test the app
4. Customize UI as needed

