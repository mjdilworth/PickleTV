# PickleTV App Bundle - Complete Setup Guide

## 📋 What You Need to Do

You're ready to create an app bundle for Google Play internal testing! Here's the complete process:

---

## 🚀 Three Simple Steps

### Step 1️⃣: Create Signing Key (First Time Only - 2 minutes)

```bash
keytool -genkey -v -keystore /home/dilly/AndroidStudioProjects/PickleTV/release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias pickletv_key
```

When asked for passwords, create a strong one (16+ characters with uppercase, numbers, symbols).

### Step 2️⃣: Create Signing Configuration (First Time Only - 1 minute)

```bash
cd /home/dilly/AndroidStudioProjects/PickleTV

cat > keystore.properties << 'EOF'
storeFile=release.keystore
storePassword=YOUR_PASSWORD
keyAlias=pickletv_key
keyPassword=YOUR_PASSWORD
EOF
```

Replace `YOUR_PASSWORD` with the password from Step 1.

### Step 3️⃣: Build & Upload (Every Release - 3 minutes)

**Before building, update version number in `app/build.gradle.kts`:**
```kotlin
versionCode = 1  // Change to 2, 3, 4... for each release
versionName = "1.0"
```

**Then build:**
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./build_bundle.sh
```

**Upload to Play Console:**
1. Go to https://play.google.com/console
2. Select PickleTV app
3. Testing → Internal Testing → Create new release
4. Upload file from: `app/build/outputs/bundle/release/app-release.aab`
5. Add release notes
6. Start rollout

---

## 📚 Documentation Files

| File | Purpose | Read When |
|------|---------|-----------|
| **BUNDLE_QUICK_REF.md** | Quick reference card | Quick lookup |
| **BUILD_BUNDLE_STEPS.md** | Detailed step-by-step | First time setup |
| **BUILD_BUNDLE_GUIDE.md** | Technical details | Deep dive needed |
| **GOOGLE_PLAY_TESTING_GUIDE.md** | Complete guide | Full context |

---

## 🔧 Files Created for You

```
✅ build_bundle.sh
   └─ Helper script to build bundle automatically

✅ Gradle configuration
   └─ app/build.gradle.kts (updated with signing support)

✅ Documentation
   ├─ BUNDLE_QUICK_REF.md
   ├─ BUILD_BUNDLE_STEPS.md
   ├─ BUILD_BUNDLE_GUIDE.md
   └─ GOOGLE_PLAY_TESTING_GUIDE.md

✅ Security
   └─ .gitignore (updated to protect keystore files)
```

---

## 🎯 What's Included in Your App Bundle

✅ **Full PickleTV App**:
- Google TV-style home screen
- Content browsing with video grid
- Automatic download with progress indicators
- Local caching system
- Settings with cache management
- Full keystone correction controls
- Sign-in functionality
- D-pad remote navigation

✅ **Ready for Testing**:
- No hardcoded test data
- Real content from your server
- Proper error handling
- Network resilience

---

## ⚠️ Important Security Notes

**Files to NEVER Commit:**
- `release.keystore` - Your signing certificate
- `keystore.properties` - Your credentials

**Already Protected:**
- Both files are in `.gitignore`
- Cannot accidentally commit them

**Backup Your Keystore:**
```bash
cp release.keystore ~/backup/pickletv_key_backup.keystore
chmod 600 ~/backup/pickletv_key_backup.keystore
```

---

## 📊 Version Management

Each release needs a new version code:

```kotlin
// Current Release (Upload First)
versionCode = 1
versionName = "1.0"

// Next Release
versionCode = 2
versionName = "1.0.1"

// Future Release
versionCode = 3
versionName = "1.1"
```

**Rules:**
- Version code MUST increase (required by Play Store)
- Version name is optional (for users to see)

---

## ✅ Pre-Build Checklist

Before running `./build_bundle.sh`:

- [ ] Keystore file exists: `release.keystore`
- [ ] Keystore properties exists: `keystore.properties`
- [ ] Version code updated in `app/build.gradle.kts`
- [ ] App tested locally: `./gradlew installDebug`
- [ ] All features working (home, cache, keystone, settings)
- [ ] No build errors in debug build

---

## 🎯 Next: Upload to Google Play

After building:

1. ✅ Get your `.aab` file from `app/build/outputs/bundle/release/`
2. ✅ Go to https://play.google.com/console
3. ✅ Select your PickleTV app
4. ✅ Testing → Internal Testing
5. ✅ Create new release
6. ✅ Upload the AAB file
7. ✅ Add release notes
8. ✅ Start rollout
9. ✅ Share test link with testers

---

## 🐛 Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| "keystore not found" | Run Step 1 to create it |
| "keystore.properties not found" | Run Step 2 to create it |
| "password incorrect" | Verify password in keystore.properties |
| "Build failed" | Check app builds with `./gradlew installDebug` first |
| "Upload rejected" | Increment versionCode higher |

---

## 📱 For Your Testers

Once uploaded to Internal Testing, testers will:

1. Receive email with test link
2. Click link and "Become a tester"
3. Open Google Play Store
4. Search "PickleTV"
5. Install
6. Test features

They can then:
- Download videos and test caching
- Adjust keystone with remote
- Clear cache and verify it works
- Report any issues via Play Store reviews

---

## 🎉 You're All Set!

Everything is configured and ready. Just:

1. Create signing key (Step 1)
2. Create signing config (Step 2)
3. Build & upload (Step 3)

**That's it!** Your app will be live for internal testing.

---

## 📖 For More Details

Read the full guides:
- **First time?** → Read `BUILD_BUNDLE_STEPS.md`
- **Quick reference?** → See `BUNDLE_QUICK_REF.md`
- **Need help?** → Check `GOOGLE_PLAY_TESTING_GUIDE.md`

---

**Ready to go!** 🚀

Questions? Check the troubleshooting section or read the detailed guides.

