# Upload to Google Play - Internal Testing

## ✅ Ready for Upload!

**App Bundle Location:**
```
/home/dilly/AndroidStudioProjects/PickleTV/app/build/outputs/bundle/release/app-release.aab
```

**Bundle Size:** 11 MB

**App Details:**
- **Product Name:** dil.map
- **Package Name:** com.dilworth.dilmap
- **Version Code:** 3
- **Version Name:** 1.0.2
- **Developer:** Dilworth Creative LLC
- **Target:** Android TV

---

## 📋 Upload Steps

### 1. Go to Google Play Console
https://play.google.com/console

### 2. Create or Select Your App
- If first time: Click "Create app"
  - App name: **dil.map**
  - Default language: English (United States)
  - App or game: App
  - Free or paid: Free (or Paid)
  - Check declarations and click "Create app"

### 3. Go to Testing → Internal Testing
- Left sidebar → **Testing** → **Internal testing**

### 4. Create New Release
- Click **"Create new release"** button

### 5. Upload App Bundle
- Click **"Upload"**
- Select: `app-release.aab`
- Wait for upload to complete (Google will process it)

### 6. Add Release Notes
Example:
```
Version 1.0.2

Features:
✓ Google TV-style home screen
✓ Video content browsing
✓ Auto-download with progress tracking
✓ Local caching for offline playback
✓ Keystone/projection correction
✓ Settings with cache management
✓ Full D-pad remote control support

Improvements:
- Optimized for Android TV
- Network resilience with fallback content
- Fast thumbnail loading
- Adaptive video streaming

Target: Android TV / Fire TV devices
```

### 7. Review Release
- Click **"Review release"**
- Check all information is correct

### 8. Start Rollout
- Click **"Start rollout to internal testing"**
- Confirm

---

## 👥 Add Testers

### After Release is Live:

1. **Go to "Manage testers"**
   - Under "Testers" section in Internal testing

2. **Add Tester Emails**
   - Add Google account emails (up to 100)
   - Create email list if needed

3. **Copy Test Link**
   - Copy the internal testing link
   - Send to testers

### For Testers:
1. Click the test link on Android TV device
2. Tap "Become a tester"
3. Open Google Play Store
4. Search "dil.map"
5. Install and test

---

## 📱 App Store Listing Requirements

You'll need to complete these sections before publishing:

### Store Listing (Required)
- [x] App name: dil.map
- [x] Short description (80 chars max)
- [x] Full description (4000 chars max)
- [ ] Screenshots (at least 2, TV screenshots required)
  - TV screenshot size: 1920x1080 or 3840x2160
- [ ] Feature graphic: 1024x500 px
- [x] App icon (already have)
- [x] App category: Entertainment or Media & Video
- [x] Content rating questionnaire
- [x] Privacy policy URL: https://tv.dilly.cloud/privacy
- [x] Contact email

### Content Rating
- Complete the questionnaire
- Free, takes 5 minutes
- Required before publishing

### Store Presence
- Select countries: Choose where available
- Pricing: Free or Paid

---

## 🎯 Android TV Specific Requirements

✅ **Already Configured:**
- Leanback feature declared
- Banner icon set
- LEANBACK_LAUNCHER category
- Touchscreen not required
- Supports D-pad navigation
- TV-optimized UI

### TV Screenshots Needed:
- Minimum: 2 screenshots
- Size: 1920x1080 (Full HD) or 3840x2160 (4K)
- Show main features:
  1. Home screen with video grid
  2. Video playing with keystone correction
  3. Settings/cache management (optional)

### How to Capture Screenshots:
On emulator:
```bash
# Take screenshot
~/Android/Sdk/platform-tools/adb shell screencap -p /sdcard/screenshot.png

# Pull to computer
~/Android/Sdk/platform-tools/adb pull /sdcard/screenshot.png ~/Desktop/dilmap-screenshot1.png
```

Or use Android Studio: **Tools → Device Manager → Camera icon**

---

## ⚠️ Important Notes

### Before Internal Testing:
- ✅ App bundle built and signed
- ✅ Version code incremented (3)
- ✅ Package name correct (com.dilworth.dilmap)
- ✅ Copyright added
- ✅ Privacy policy URL set

### Required for Internal Testing:
- Email addresses of testers (Google accounts)
- Release notes
- App bundle upload

### NOT Required for Internal Testing:
- ❌ Store screenshots (can add later)
- ❌ Full store listing (can add later)
- ❌ Content rating (can add later)
- ❌ Production release setup

Internal testing allows you to test with real users before completing the full store listing!

---

## 🚀 Timeline

**Upload & Processing:** ~15-20 minutes
- Upload bundle: 2-5 minutes
- Google processing: 10-15 minutes
- Release goes live: Immediately after approval

**Tester Access:** Immediate
- Testers get email with link
- Can install from Play Store right away

---

## 📊 What Happens Next

1. ✅ Upload bundle to Internal Testing
2. ✅ Add tester emails
3. ✅ Testers install and test app
4. ✅ Collect feedback
5. ✅ Fix bugs if needed
6. ✅ Upload new version (increment versionCode)
7. → Eventually: Move to Closed Beta (optional)
8. → Eventually: Production Release

---

## 🔧 If You Need to Update

### Increment Version:
Edit `app/build.gradle.kts`:
```kotlin
versionCode = 4  // Increment each release
versionName = "1.0.3"
```

### Rebuild:
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./gradlew clean bundleRelease
```

### Upload New Version:
- Internal Testing → Create new release
- Upload new AAB file
- Add release notes
- Start rollout

---

## ✅ You're Ready!

**Your app bundle is ready for Google Play Internal Testing!**

**Next step:** Go to https://play.google.com/console and upload `app-release.aab`

Good luck with your internal testing! 🎉

