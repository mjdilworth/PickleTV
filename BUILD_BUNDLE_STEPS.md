# Creating Your First App Bundle - Step by Step

## 📋 Prerequisites

- ✅ Google Play Developer account ($25 one-time fee)
- ✅ App listed in Google Play Console
- ✅ PickleTV app built and tested locally

---

## 🔐 Step 1: Generate Signing Key (First Time Only)

This creates a certificate that proves the app is from you.

### Command:
```bash
keytool -genkey -v -keystore /home/dilly/AndroidStudioProjects/PickleTV/release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias pickletv_key
```

### When Prompted, Enter:

```
Enter keystore password: [create a strong password, 16+ characters]
Re-enter new password: [repeat the password]
What is your first and last name? [Your Name]
What is the name of your organizational unit? [PickleTV]
What is the name of your organization? [PickleTV Inc]
What is the name of your City or Locality? [Your City]
What is the name of your State or Province? [Your State]
What is the two-letter country code for this unit? [US]
Is CN=Your Name, OU=PickleTV, O=PickleTV Inc, L=Your City, ST=Your State, C=US correct? [yes]
```

### Result:
```
✅ Creates: /home/dilly/AndroidStudioProjects/PickleTV/release.keystore
```

⚠️ **IMPORTANT**: Save your password somewhere safe! You'll need it every time you build.

---

## 📝 Step 2: Create Signing Configuration File

This file tells Gradle where to find your signing key.

### Open Terminal:
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
```

### Create File:
```bash
cat > keystore.properties << 'EOF'
storeFile=release.keystore
storePassword=YOUR_PASSWORD_HERE
keyAlias=pickletv_key
keyPassword=YOUR_PASSWORD_HERE
EOF
```

### Replace `YOUR_PASSWORD_HERE` with the password from Step 1

### Result:
```
✅ Creates: /home/dilly/AndroidStudioProjects/PickleTV/keystore.properties
```

⚠️ **This file is ALREADY in .gitignore** - never commit it!

---

## 🔢 Step 3: Update Version Number

Before each release, you MUST increase the version code.

### Edit File:
```bash
nano /home/dilly/AndroidStudioProjects/PickleTV/app/build.gradle.kts
```

### Find These Lines:
```kotlin
defaultConfig {
    applicationId = "com.example.pickletv"
    minSdk = 26
    targetSdk = 36
    versionCode = 1          ← CHANGE THIS
    versionName = "1.0"      ← CHANGE THIS (optional)
}
```

### Change To:
```kotlin
versionCode = 2              ← Increment (1 → 2 → 3 ...)
versionName = "1.0.1"        ← Optional (1.0, 1.1, 2.0, etc.)
```

### Save:
- Press `Ctrl+X`, then `Y`, then `Enter` (if using nano)

---

## 📦 Step 4: Build the App Bundle

### Quick Method (Recommended):
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./build_bundle.sh
```

### Direct Method:
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./gradlew bundleRelease
```

### Watch For:
```
> Task :app:bundleRelease
BUILD SUCCESSFUL in 2m 30s

✅ BUILD SUCCESSFUL!
📍 App Bundle Location:
   /home/dilly/AndroidStudioProjects/PickleTV/app/build/outputs/bundle/release/app-release.aab
```

### Result:
```
✅ Creates: app/build/outputs/bundle/release/app-release.aab
```

---

## 🌐 Step 5: Upload to Google Play Console

### 5.1 Open Play Console:
```
https://play.google.com/console
```

### 5.2 Sign In:
- Use your Google account

### 5.3 Select Your App:
- Click on "PickleTV"

### 5.4 Go to Testing:
- Left sidebar → **Testing** → **Internal Testing**

### 5.5 Create Release:
- Click **Create new release**

### 5.6 Upload AAB File:
- Click **Upload new APK or AAB**
- Select: `app/build/outputs/bundle/release/app-release.aab`

### 5.7 Add Release Notes:
```
Example release notes:

Version 1.0 - Initial Release

Features:
✅ Google TV-style home screen
✅ Video browsing and content selection
✅ Automatic download with progress indicators
✅ Local caching for offline playback
✅ Keystone/trapezoid correction
✅ Settings with cache management
✅ Full remote control support

Improvements:
- D-pad navigation optimized for TV
- Network resilience with fallback content
- Fast thumbnail loading with Coil
- ExoPlayer for adaptive video streaming

Known Issues:
- None

Testing Notes:
- Test on Android TV or Fire TV device
- Check keystone controls (D-pad, Menu, etc.)
- Monitor cache size in Settings
```

### 5.8 Review Release:
- Click **Review release**
- Verify all information is correct

### 5.9 Rollout to Internal Testing:
- Click **Start rollout to internal testing**
- Confirm

### Result:
```
✅ Release is now live!
   Status: Live on Internal Testing
```

---

## 👥 Step 6: Invite Testers

### 6.1 Go to Testers:
- Still in **Internal Testing**
- Under "Testers," click **Manage testers**

### 6.2 Add Tester Emails:
- Add emails of testers (must be Google accounts)
- Can add up to 100 testers

### 6.3 Send Test Link:
- Copy the **Testing Link**
- Send to testers via email

### 6.4 Testers Install App:
Testers will:
1. Click the test link
2. Tap "Become a tester"
3. Open Google Play Store
4. Search "PickleTV"
5. Install

---

## ✅ Verification Checklist

After uploading, verify:

- [ ] Release shows as "Live" in Internal Testing
- [ ] Test link is active
- [ ] Testers received email invitation
- [ ] AAB file uploaded successfully
- [ ] Release notes are visible
- [ ] Version code increased from previous

---

## 📱 Testing Tips

### What to Test:
1. Home screen loads with video grid
2. Videos download when selected
3. Download progress shows percentage
4. Cached videos play instantly (✓ icon)
5. D-pad controls adjust keystone
6. Menu button enters corner edit mode
7. Settings shows cache size
8. Clear Cache button works with feedback
9. Sign In tab displays form properly
10. Network failure shows fallback content

### How to Report Issues:
- Describe what happened
- Include device model and Android version
- Mention app version (1.0, 1.1, etc.)
- Screenshot if possible
- Steps to reproduce

---

## 🎯 Common Issues & Fixes

### Issue: "Keystore password incorrect"
```
Error: Failed to read key from keystore
```
**Fix**: Verify password in keystore.properties matches

### Issue: "App crashes on startup"
**Fix**: Check logcat for errors:
```bash
adb logcat | grep -i "crash\|error" | head -20
```

### Issue: "Version code already used"
**Fix**: Increment versionCode higher (1 → 2 → 3...)

### Issue: "Download doesn't start"
**Fix**: Check network connection, verify video URL accessible

### Issue: "Cache not clearing"
**Fix**: Check file permissions, verify cache directory exists

---

## 🎉 Success!

Your app is now available for internal testing!

Testers can:
- Download from their Google Play Store
- Test all features
- Report bugs via reviews
- Provide feedback

Next: Collect feedback and prepare for production release!

---

## 📚 Next Steps

1. **Monitor feedback** from testers
2. **Fix any reported bugs**
3. **Increment version code** for next release
4. **Rebuild and upload** new version
5. **Eventually**: Rollout to Closed Beta (5,000 testers)
6. **Finally**: Open Testing (public beta)
7. **Production**: Release to all users

---

**Total Time**: 
- First setup: ~10 minutes
- Build & upload: ~5 minutes
- Subsequent releases: ~3 minutes

Good luck! 🚀

