# App Bundle - Quick Reference

## 🚀 Build Process (Summary)

```bash
# 1. ONE-TIME: Create signing key
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias pickletv_key

# 2. ONE-TIME: Create keystore.properties
echo "storeFile=release.keystore" > keystore.properties
echo "storePassword=YOUR_PASSWORD" >> keystore.properties
echo "keyAlias=pickletv_key" >> keystore.properties
echo "keyPassword=YOUR_PASSWORD" >> keystore.properties

# 3. EVERY RELEASE: Update version
# Edit app/build.gradle.kts:
# versionCode = 2
# versionName = "1.1"

# 4. EVERY RELEASE: Build bundle
cd /home/dilly/AndroidStudioProjects/PickleTV
./build_bundle.sh

# 5. Output file
# app/build/outputs/bundle/release/app-release.aab
```

## 📍 File Locations

| File | Location | Purpose | Secret? |
|------|----------|---------|---------|
| release.keystore | Project root | Signing certificate | YES ✓ |
| keystore.properties | Project root | Signing credentials | YES ✓ |
| app-release.aab | `app/build/outputs/bundle/release/` | Upload to Play Store | NO |

## 🔑 Security

✅ Protected by `.gitignore`:
- `keystore.properties`
- `*.keystore`
- `*.jks`
- `*.p12`

Never commit or share these files!

## 📱 Upload Steps

1. `https://play.google.com/console`
2. Select app
3. **Testing** → **Internal Testing**
4. **Create new release**
5. Upload `.aab` file
6. Add release notes
7. **Review release**
8. **Start rollout**

## 📊 Version Management

Every release needs:
- `versionCode++` (must increase, 1→2→3...)
- `versionName` optional (1.0, 1.1, 2.0...)

Edit in: `app/build.gradle.kts`

## ✅ Pre-Build Checklist

- [ ] Keystore exists
- [ ] keystore.properties exists
- [ ] Version code updated
- [ ] App tested locally
- [ ] No build warnings

## 🐛 Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `Invalid keystore password` | Wrong password | Check keystore.properties |
| `Cannot read keystore.properties` | File missing | Create as shown above |
| `Certificate not found` | Wrong alias | Verify `pickletv_key` |
| `Version code error` | Code not increased | Increment versionCode |

## 🎯 Build Time

- Clean build: ~2-3 minutes
- Incremental: ~30 seconds
- Upload to Play Store: ~15-20 minutes

## 📈 Release Checklist

Before uploading to Play Store:
1. ✅ Build successful locally
2. ✅ No build warnings
3. ✅ Tested on emulator/device
4. ✅ Version code increased
5. ✅ AAB file generated
6. ✅ Release notes prepared

## 🎉 You're Ready!

```bash
./build_bundle.sh
# → app-release.aab ready
# → Upload to Play Console
# → Share test link with testers
```

For details, see: `GOOGLE_PLAY_TESTING_GUIDE.md`

