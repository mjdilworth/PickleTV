# PickleTV - Google Play Internal Testing Guide

## 🚀 Quick Start (5 minutes)

### Step 1: Generate Signing Key (One-time)

```bash
keytool -genkey -v -keystore /home/dilly/AndroidStudioProjects/PickleTV/release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias pickletv_key
```

**When prompted, enter:**
- Keystore password: `(create a strong password, save it!)`
- Key password: `(same as keystore password)`
- First/Last name: Your name
- Organization: PickleTV
- City: Your city
- State: Your state
- Country code: US

✅ Creates: `release.keystore` in project root

### Step 2: Create Signing Configuration

```bash
cat > /home/dilly/AndroidStudioProjects/PickleTV/keystore.properties << 'EOF'
storeFile=release.keystore
storePassword=YOUR_PASSWORD_HERE
keyAlias=pickletv_key
keyPassword=YOUR_PASSWORD_HERE
EOF
```

Replace `YOUR_PASSWORD_HERE` with the password from Step 1.

**⚠️ WARNING**: These files contain sensitive information:
- Never commit to Git
- Never share with others
- Keep backups in a safe location
- Already in `.gitignore` for protection

### Step 3: Build the App Bundle

**Option A: Use helper script**
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./build_bundle.sh
```

**Option B: Direct gradle command**
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./gradlew bundleRelease
```

Output file: `app/build/outputs/bundle/release/app-release.aab`

### Step 4: Upload to Google Play Console

1. Go to: https://play.google.com/console
2. Sign in with your Google account
3. Select your app (or create a new one)
4. Navigate to: **Testing** → **Internal Testing**
5. Click: **Create new release**
6. Upload the `.aab` file from Step 3
7. Fill in release notes (e.g., "Internal testing build - features: home screen, caching, keystone correction")
8. Click: **Review release**
9. Click: **Start rollout to internal testing**
10. Internal testers can now download and test!

---

## 📱 Setting Up Internal Testers

### In Google Play Console:

1. Go to **Testing** → **Internal Testing**
2. Under "Testers," click **Manage testers**
3. Add tester email addresses (Google accounts)
4. Send them the **internal test link**

### For Testers:

Testers will receive an email with a link to:
1. Open the link on their Android TV device
2. Tap "Become a tester"
3. Open Google Play Store
4. Search for "PickleTV"
5. Download and install

---

## 🔒 Security & Best Practices

### Keystore Management

✅ **DO:**
- Keep `release.keystore` safe (backup to secure location)
- Keep `keystore.properties` in `.gitignore` (already done)
- Use strong passwords (16+ characters with mix of uppercase, numbers, symbols)
- Store password in password manager

❌ **DON'T:**
- Commit keystore files to Git
- Share keystore/passwords via email or chat
- Use weak passwords
- Store passwords in plain text files

### Version Management

Before each release, update in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    // ...
    versionCode = 2      // Increment each release
    versionName = "1.1"  // User-visible version
}
```

**Version code**: Must increase with each release (Play Store requirement)
**Version name**: User-facing version (e.g., "1.0", "1.1", "2.0")

### Backup Your Keystore

Create a secure backup:
```bash
cp /home/dilly/AndroidStudioProjects/PickleTV/release.keystore ~/backup/pickletv_signing.keystore
chmod 600 ~/backup/pickletv_signing.keystore
```

---

## 📋 Checklist Before Building

- [ ] Keystore file exists: `release.keystore`
- [ ] Keystore properties file exists: `keystore.properties`
- [ ] Updated version code in `app/build.gradle.kts`
- [ ] Tested app locally: `./gradlew installDebug`
- [ ] All features working (home screen, caching, keystone, etc.)
- [ ] No build warnings or errors

---

## 🐛 Troubleshooting

### "Invalid keystore or keystore password"
```
Error: Failed to read key [pickletv_key] from keystore
```
**Solution:** 
- Verify password in `keystore.properties` matches
- Check keystore file isn't corrupted
- Regenerate if needed

### "Cannot read keystore.properties"
```
Error: No such file or directory
```
**Solution:**
- Create `keystore.properties` as shown in Step 2
- Ensure it's in project root: `/home/dilly/AndroidStudioProjects/PickleTV/`

### "Certificate not found"
```
Error: Certificate doesn't contain the correct keyalias
```
**Solution:**
- Check alias matches: `keyAlias=pickletv_key`
- Verify in keystore: `keytool -list -v -keystore release.keystore`

### "Bundle upload rejected by Play Store"
Possible causes:
- Version code not increased
- Signing certificate doesn't match previous releases
- App uses restricted APIs

**Solution:**
- Check Play Console error message
- Review release notes
- Verify signing certificate is correct

---

## 📊 Build Output Example

```
🧹 Cleaning previous builds...

BUILD SUCCESSFUL in 45s

📦 Building release app bundle...

:app:assembleReleaseUniversal UP-TO-DATE
:app:bundleReleaseResources
:app:bundleRelease

BUILD SUCCESSFUL in 2m 30s

=========================================
✅ BUILD SUCCESSFUL!
=========================================

📍 App Bundle Location:
   /home/dilly/AndroidStudioProjects/PickleTV/app/build/outputs/bundle/release/app-release.aab

📊 Bundle Size: 45.2 MB

Next Steps:
   1. Open Google Play Console
   2. Select PickleTV
   3. Go to Testing > Internal Testing
   4. Click 'Create new release'
   5. Upload the AAB file
   6. Add release notes
   7. Click 'Review release'
   8. Click 'Start rollout to internal testing'
```

---

## 📈 What's Included in This Release

✅ **Features:**
- Google TV-style home screen with menu
- Content browsing and video selection
- Automatic download with progress indicators
- Caching system for offline playback
- Settings with cache management
- Sign-in screen
- Full keystone correction controls (remote + keyboard)

✅ **Content:**
- Sample Berlin and Halloween videos from server
- Real-time video thumbnail loading

✅ **Quality:**
- TV-optimized UI (D-pad navigation)
- Proper error handling
- Network resilience with fallback content
- Keystone adjustments persist across sessions

---

## 🎯 Testing Focus Areas

For internal testers, focus on:

1. **Home Screen**
   - Browse content displays correctly
   - D-pad navigation works smoothly
   - Video thumbnails load properly

2. **Video Downloads**
   - Download starts when video selected
   - Progress bar shows percentage
   - Video plays automatically when done
   - Cached videos show ✓ icon

3. **Keystone Correction**
   - D-pad adjusts image shape
   - OK button saves adjustments
   - Menu button toggles corner edit mode
   - Adjustments persist after restart

4. **Cache Management**
   - Settings shows cache size
   - Clear Cache button works
   - Success message appears
   - Cache is actually cleared

5. **Edge Cases**
   - Network disconnection handling
   - Video retry after failure
   - App restart with cached videos

---

## 📞 Support

### Common Questions

**Q: How long does internal testing take?**
A: Usually 15-20 minutes for Google Play to process the release.

**Q: Can I have multiple testers?**
A: Yes, add up to 100 testers in Internal Testing.

**Q: Can I rollback a release?**
A: Yes, go to Internal Testing and "Start new release" with previous version.

**Q: What's the difference between Internal, Closed, and Open Testing?**
- **Internal**: You + up to 100 testers
- **Closed**: Up to 5,000 testers (requires specific user group)
- **Open**: Anyone can test (public beta)

**Q: How do testers provide feedback?**
- Play Store review comments
- In-app feedback form (not yet implemented)
- Direct email/messaging

### Debug Logs

Get detailed logs from testers:
```bash
adb logcat | grep -E "PickleTV|HomeActivity|MainActivity|DownloadManager"
```

---

## 🔄 Future Release Process

1. Make code changes
2. Update `versionCode` and `versionName`
3. Test locally: `./gradlew installDebug`
4. Build bundle: `./build_bundle.sh`
5. Upload to Play Console
6. Add release notes
7. Rollout to testing track
8. Collect feedback
9. Fix issues or prepare for production release

---

## 📚 Additional Resources

- **Google Play Console Help**: https://support.google.com/googleplay/android-developer
- **Android App Bundles**: https://developer.android.com/guide/app-bundle
- **Internal Testing Guide**: https://support.google.com/googleplay/android-developer/answer/3131213

---

**Version**: 1.0  
**Last Updated**: December 3, 2025  
**App Version**: 1.0 (versionCode: 1)

Ready to test PickleTV on Google Play! 🎉

