# Google TV Streamer Remote - Quick Testing Guide

## 🎮 Keyboard Simulation of Google TV Streamer Remote

Use these keyboard keys to simulate the Google TV Streamer remote during development:

| Keyboard Key | Simulates | Function |
|--------------|-----------|----------|
| **↑ ↓ ← →** | D-Pad | Adjust keystone shape |
| **Enter** | OK/Center | Save adjustments |
| **V** | Volume Up | Toggle corner edit mode |
| **B** | Volume Down | Next corner |
| **M** | Mute | Toggle fine/coarse adjustment |
| **Esc** | Back | Exit video |

## 🧪 Test Workflow

1. **Play a video**
2. **Press V** → Enable corner edit mode (corners appear)
3. **Press B** → Cycle to next corner
4. **Use arrow keys** → Adjust selected corner
5. **Press M** → Toggle between fine (0.05) and coarse (0.20) steps
6. **Press Enter** → Save
7. **Press Esc** → Exit video

## ✅ What Changed

### Removed (not on Google TV Streamer):
- ❌ Menu button → Replaced by Volume Up
- ❌ Channel ↑/↓ → Replaced by Volume Down (cycles one direction)
- ❌ Info/Guide → Replaced by Mute
- ❌ B button reset → Removed

### Added (Google TV Streamer):
- ✅ Volume Up → Toggle corner edit mode
- ✅ Volume Down → Cycle corners (one direction only)
- ✅ Mute → Toggle adjustment speed

### Keyboard Simulation:
- ✅ V key → Simulates Volume Up
- ✅ B key → Simulates Volume Down  
- ✅ M key → Simulates Mute

### Legacy Shortcuts (still work):
- ✅ E/C keys → Also toggle corner edit mode
- ✅ N key → Also cycles corners
- ✅ 0/R/Del → Reset (keyboard only)
- ✅ Numpad 1,3,7,9 → Direct corner selection

## 📝 Testing Checklist

- [ ] V key toggles corner edit mode
- [ ] B key cycles through corners (TL→TR→BR→BL→TL)
- [ ] M key toggles adjustment step size
- [ ] Arrow keys adjust image/corner
- [ ] Enter saves adjustments
- [ ] Settings persist across app restarts
- [ ] Volume Up works on actual Google TV Streamer remote
- [ ] Volume Down works on actual Google TV Streamer remote
- [ ] Mute works on actual Google TV Streamer remote

## 🎯 Quick Commands

```bash
# Build and install
cd /home/dilly/AndroidStudioProjects/PickleTV
./gradlew installDebug

# Check logs
adb logcat | grep "Google TV Remote\|Keyboard"
```

## 📱 Actual Google TV Streamer Remote Testing

When testing with the real Google TV Streamer remote:

1. **Volume Up** → Should enable corner edit mode (look for visual indicators)
2. **Volume Down** → Should cycle through corners (cyan highlight moves)
3. **Mute** → Should toggle adjustment speed (check logs for confirmation)
4. **D-Pad** → Should adjust keystone shape
5. **OK** → Should save settings

Look for log messages like:
- `Google TV Remote: Corner edit mode: true`
- `Google TV Remote: Next corner: TOP_RIGHT`
- `Google TV Remote: Adjustment step toggled to 0.20`

---

**Last Updated**: December 4, 2025
**Target Device**: Google TV Streamer (2024+)
**Package**: com.dilworth.dilmap

