# PickleTV - Quick Reference Card

## Minimum Setup for Development

```bash
# 1. Copy video to device
/home/dilly/AndroidStudioProjects/PickleTV/push_video_simple.sh

# 2. Build and run (from Android Studio or)
./gradlew installDebug
```

## Essential Keys

```
P / Space      Pause/Play + Toggle Edit Mode
1              Select TOP-LEFT corner + enter edit mode
2              Select TOP-RIGHT corner + enter edit mode
3              Select BOTTOM-RIGHT corner + enter edit mode
4              Select BOTTOM-LEFT corner + enter edit mode
↑ ↓ ← →        Adjust selected corner
0              RESET to center
R              RESET warp
Enter          SAVE adjustments
```

## Advanced Keys

```
E              Toggle edit mode (without play/pause)
N              Cycle to next corner
Tab            Cycle next / Shift+Tab cycle previous
[ ]            Decrease/increase adjustment step
M              Toggle overlay grid
Esc            Exit edit mode
Page Up/Dn     Move shape up/down
Vol +/-        Move shape left/right
F1             Toggle input logging
```

## Quick Adjustment Workflow

```
1. P              → Enter edit mode (pauses video)
2. 1              → Select top-left corner
3. ← ← ← → → →   → Adjust as needed
4. 2              → Next corner (top-right)
5. ↑ ↓ ← →        → Adjust corner
6. 3              → Next corner (bottom-right)
7. ↑ ↓ ← →        → Adjust corner
8. 4              → Next corner (bottom-left)
9. ↑ ↓ ← →        → Adjust corner
10. Enter         → Save
11. P             → Resume and see result
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Keys not working | Press F1 to enable logging, check app is focused |
| Video not found | Run: `/home/dilly/AndroidStudioProjects/PickleTV/push_video_simple.sh` |
| Changes lost | Press **Enter** BEFORE closing app |
| Extreme distortion | Press **0** to reset, start over |

## File Locations

```
Source:    /home/dilly/AndroidStudioProjects/PickleTV/
Video:     /home/dilly/AndroidStudioProjects/PickleTV/h-6.mp4
On Device: /sdcard/Android/data/com.example.pickletv/cache/h-6.mp4
APK:       app/build/outputs/apk/debug/app-debug.apk
```

## Commands

```bash
# Build
./gradlew build

# Install to device/emulator
./gradlew installDebug

# Push video
./push_video_simple.sh

# View logs
adb logcat | grep -E "MainActivity|InputLogger"

# Full build + install
./gradlew installDebug && ./push_video_simple.sh
```

---

For full documentation see: `KEYBOARD_CONTROLS.md`

