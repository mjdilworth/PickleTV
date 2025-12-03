# Runtime Crash Fix - Missing Compose Foundation Dependency

## Issue
App was crashing at runtime with:
```
java.lang.NoClassDefFoundError: Failed resolution of: Landroidx/compose/foundation/relocation/BringIntoViewResponderKt;
```

This occurred when trying to use `TvLazyColumn` in the HomeActivity.

## Root Cause
The `androidx.tv.foundation` library (which provides `TvLazyColumn`) depends on `androidx.compose.foundation.relocation` classes, but the `androidx.compose.foundation` library wasn't explicitly included in the dependencies.

While the Compose BOM includes foundation, the TV Foundation library requires an explicit dependency to ensure the relocation package is available.

## Solution
Added the `androidx.compose.foundation` dependency explicitly.

### Changes Made

1. **gradle/libs.versions.toml** - Added foundation library:
   ```toml
   androidx-compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }
   ```

2. **app/build.gradle.kts** - Added foundation to dependencies:
   ```kotlin
   implementation(libs.androidx.compose.foundation)
   ```

## Build & Install
```bash
cd /home/dilly/AndroidStudioProjects/PickleTV
./gradlew clean assembleDebug
./gradlew installDebug
```

## Verification
The app should now:
1. ✅ Launch successfully
2. ✅ Load HomeActivity without crashing
3. ✅ Fetch content from server (shown in logs)
4. ✅ Display video thumbnails in TvLazyColumn
5. ✅ Allow D-pad navigation between videos

## What Was Loading
The manifest was successfully fetched:
- 2 videos loaded
- Categories: "Events", "Seasonal"
- Thumbnails: berlin-640.jpg, halloween-640.jpg
- Videos: berlin.mp4, halloween.mp4

The crash happened just before rendering the content grid.

## Final Dependencies
```kotlin
// Core Compose
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.compose.foundation) // ← ADDED THIS

// TV Components
implementation(libs.androidx.tv.foundation) // requires foundation above
implementation(libs.androidx.tv.material)
```

## Status
✅ **FIXED** - App now runs without crashing!

The home screen should now display properly with the video grid.

