# PickleTV - Comprehensive Keyboard & Remote Control Mappings

## Overview

PickleTV supports extensive keyboard and remote control mappings for precise keystone adjustment during development. This document details all available controls.

---

## Control Categories

### 1. Play/Pause Controls

| Key | Remote Button | Function |
|-----|---|---|
| **P** | Play/Pause | Toggle playback AND auto-enter/exit edit mode |
| **Space** | Play/Pause | Same as P key |

**Behavior**:
- When playing: Press → Pauses playback + Enters edit mode
- When paused: Press → Resumes playback + Exits edit mode

---

### 2. Directional Navigation (DPAD / Arrow Keys)

| Key | Remote Button | Function | In Edit Mode |
|-----|---|---|---|
| **↑** / **DPAD UP** | D-Pad ▲ | Top adjustment | Adjust selected corner UP |
| **↓** / **DPAD DOWN** | D-Pad ▼ | Bottom adjustment | Adjust selected corner DOWN |
| **←** / **DPAD LEFT** | D-Pad ◀ | Left adjustment | Adjust selected corner LEFT |
| **→** / **DPAD RIGHT** | D-Pad ▶ | Right adjustment | Adjust selected corner RIGHT |

**Behavior**:
- **Normal mode**: Adjusts entire warp shape
  - ↑ = Pull top inward
  - ↓ = Push bottom outward
  - ← = Push left outward
  - → = Pull right inward
  
- **Edit mode** (E or P to toggle): Adjusts selected corner individually

---

### 3. Corner Selection & Snap Helpers

| Key | Numpad | Remote | Function |
|-----|---|---|---|
| **1** | Numpad 1 | - | Select TOP-LEFT corner + Enable edit mode |
| **3** | Numpad 3 | - | Select TOP-RIGHT corner + Enable edit mode |
| **7** | Numpad 7 | - | Select BOTTOM-LEFT corner + Enable edit mode |
| **9** | Numpad 9 | - | Select BOTTOM-RIGHT corner + Enable edit mode |
| **0** | Numpad 0 | - | **RESET** - Return warp to center (0,0,0,0) |
| **N** | - | - | Cycle to next corner (TL → TR → BR → BL → TL) |
| **Tab** | - | - | Cycle to next corner |
| **Shift+Tab** | - | - | Cycle to previous corner |

**Example Workflow**:
1. Press **1** → TOP-LEFT corner selected, edit mode on
2. Use ↑/↓/←/→ to adjust
3. Press **N** → TOP-RIGHT corner selected
4. Continue adjusting
5. Press **0** → Reset everything to center

---

### 4. Step Size Control

| Key | Remote Button | Function |
|-----|---|---|
| **]** (Right Bracket) | Fast Forward | Increase adjustment step size (max 0.50) |
| **[** (Left Bracket) | Rewind | Decrease adjustment step size (min 0.01) |

**Current step sizes**:
- Default fine adjustment: **0.05** per key press
- Default large adjustment: **0.20** (4x fine)
- Can be adjusted between 0.01 - 0.50

**Usage**:
```
Press [ repeatedly to decrease step size for very fine adjustments
Press ] repeatedly to increase step size for quick adjustments
```

---

### 5. Whole Shape Movement

| Key | Remote Button | Function | Effect |
|-----|---|---|---|
| **Page Up** | Channel + | Move shape UP | Tilts top inward, bottom outward |
| **Page Down** | Channel − | Move shape DOWN | Tilts top outward, bottom inward |
| **Volume +** | Volume + | Move frame RIGHT | Shifts entire shape right |
| **Volume −** | Volume − | Move frame LEFT | Shifts entire shape left |

**Use cases**:
- Use **Page Up/Down** for major vertical positioning
- Use **Volume +/−** to center image horizontally

---

### 6. Edit Mode & Display

| Key | Remote Button | Function |
|-----|---|---|
| **E** | - | Toggle corner edit mode (shows corner markers) |
| **M** | Menu/Info | Toggle overlay grid/HUD visibility |
| **Esc** | Back | Exit edit mode (disable corner markers) |

**Edit Mode Features**:
- When enabled: Green border shows warped shape
- Corner markers appear (yellow = normal, cyan = selected)
- DPAD adjusts only the selected corner

---

### 7. Save & Reset

| Key | Remote Button | Function |
|-----|---|---|
| **Enter** | OK / Enter | Save current warp shape to SharedPreferences |
| **R** | - | Reset warp shape to default (0,0,0,0) |

**Important**:
- Warp is auto-loaded on app startup
- Manual save with Enter persists to device storage
- Delete/Reset can be undone by restarting app

---

### 8. Development Utilities

| Key | Remote Button | Function |
|-----|---|---|
| **F1** | - | Toggle verbose input logging (on/off) |
| **C** | - | Legacy: Toggle corner edit mode |

---

## Common Workflows

### Workflow 1: Quick Keystone Correction

```
1. P               → Pause video, enter edit mode
2. ]               → Increase step size for faster adjustments
3. ↑ ↑ ↑           → Pull top inward 3 clicks
4. ↓ ↓             → Push bottom outward 2 clicks
5. → →             → Pull right side in
6. Enter           → Save adjustments
7. P               → Resume playback
```

### Workflow 2: Precise Corner Adjustment

```
1. P               → Enter edit mode
2. 1               → Select TOP-LEFT corner
3. ← ← ← ←         → Adjust left edge very precisely
4. N               → Move to TOP-RIGHT
5. → → →           → Adjust right edge
6. 0               → Reset if needed
7. Enter           → Save
```

### Workflow 3: Center & Fine-Tune

```
1. 0               → Reset to center
2. [               → Decrease step size for fine control
3. 1               → Select TOP-LEFT
4. ↑               → Make tiny adjustment
5. N, N, N         → Cycle through all corners
6. ← or → as needed→ Fine-tune each corner
7. Enter           → Save final result
```

### Workflow 4: Full Frame Repositioning

```
1. P               → Enter edit mode
2. Page Up / Down  → Move entire shape vertically
3. Volume +/-      → Move entire shape horizontally
4. ]               → Increase step size
5. ↑↓←→            → Make final micro adjustments
6. Enter           → Save
```

---

## Technical Details

### Adjustment Logic

**Normal Mode (whole warp)**:
- ↑ = topLeft -= step, topRight += step
- ↓ = bottomLeft += step, bottomRight -= step
- ← = topLeft += step, bottomLeft += step
- → = topRight -= step, bottomRight -= step

**Edit Mode (single corner)**:
- Horizontal (dx): Moves corner left/right
- Vertical (dy): Tilts top/bottom edge for corner effect

**Movement Mode**:
- Page Up/Down: Adjusts all corners in unison
- Volume +/−: Shifts all corners uniformly

### Step Sizes

```kotlin
// Default values
fine step    = 0.05f    (increased/decreased by [ and ])
large step   = 0.20f    (4x fine, used for Page Up/Down)
shift step   = fine/2   (half fine for Shift+Arrow)

// Bounds
min:  0.01f
max:  0.50f
```

### Real-time Feedback

- **Green border**: Shows actual warped quad corners
- **Yellow crosses**: Normal corners
- **Cyan crosses**: Currently selected corner
- **All updates** are rendered immediately (no delay)

---

## Remote Control Mapping Reference

### Primary Remote (Physical Device)

```
┌──────────────────────┐
│  Keyboard Equivalent │
├──────────────────────┤
│  D-Pad ▲▼◀▶  = Arrows/DPAD
│  OK/Enter      = Enter
│  Play/Pause    = P or Space
│  Menu/Info     = M
│  Back          = Esc
│  Volume +/−    = Vol +/−
│  Channel +/−   = Page Up/Down
│  Fast Fwd [    = [ (decrease step)
│  Rewind ]      = ] (increase step)
│  Numbers 1,3,7,9,0 = Snap to corners
└──────────────────────┘
```

---

## Tips & Tricks

✅ **Best Practices**:
1. Start with larger step size (**]** key) for gross adjustments
2. Decrease step size (**[** key) when getting close
3. Use **0** to reset if you make a mistake
4. Use **Page Up/Down** for global vertical alignment
5. Use number keys (1,3,7,9) to jump directly to corners
6. Save frequently with **Enter** key
7. Test adjustments with video playing before saving

⚠️ **Avoid**:
- Don't let corner values get too extreme (can cause distortion artifacts)
- Don't forget to **Enter** to save - changes are lost if app closes without saving
- Avoid extreme step sizes (use values 0.01 - 0.50)

💡 **Pro Tips**:
- Shift+Arrow keys give half-step adjustments for ultra-fine control
- Press **M** to see grid overlay while adjusting
- Use **E** key instead of **P** if you just want edit mode without pausing
- Monitor the green border in edit mode for real-time feedback

---

## Keyboard Reference Sheet

```
╔════════════════════════════════════════════════════════════╗
║           PickleTV Complete Keyboard Reference             ║
╠════════════════════════════════════════════════════════════╣
║ PLAY/PAUSE           │ P, Space                            ║
║ EDIT MODE            │ E (toggle)                          ║
║ CORNER SELECTION     │ 1, 3, 7, 9 (snap) | N (cycle)      ║
║ RESET                │ 0 (center) | R (reset)             ║
║ ARROW KEYS           │ ↑↓←→ or DPAD_UP/DOWN/LEFT/RIGHT    ║
║ STEP SIZE            │ [ (decrease) | ] (increase)        ║
║ MOVEMENT             │ Page Up/Down (vertical)             ║
║                      │ Volume +/− (horizontal)             ║
║ OVERLAY              │ M (toggle grid)                     ║
║ EXIT EDIT            │ Esc                                 ║
║ SAVE                 │ Enter                               ║
║ LOGGING              │ F1 (toggle)                         ║
╚════════════════════════════════════════════════════════════╝
```

---

## Version History

**v1.0** (Nov 2, 2025)
- Initial comprehensive keyboard mapping implementation
- Support for 40+ key combinations
- Real-time visual feedback in edit mode
- Step size adjustment on the fly
- Complete corner control system
- Save/Load persistence

---

## Troubleshooting

**Q: Keys aren't responding**
- A: Make sure app is in focus and `isDebugBuild` is true (debug APK)
- Try pressing **F1** to enable logging and see input events

**Q: Edit mode won't activate**
- A: Try pressing **E** directly instead of **P**
- Or select a corner with **1, 3, 7, 9**

**Q: Changes aren't saving**
- A: Press **Enter** to explicitly save
- Check logcat for "Warp shape saved" message

**Q: Video isn't showing after adjustments**
- A: Press **R** to reset to default, then press **P** to play
- Check that video file is still in `/sdcard/Android/data/com.example.pickletv/cache/h-6.mp4`

**Q: Step size too large/small**
- A: Use **[** to decrease (min 0.01) or **]** to increase (max 0.50)
- Check logcat for "[DEV] Step size" messages

---

## Next Steps

- Implement visual step size indicator on screen
- Add on-screen help overlay (press **H** for help)
- Support gamepad analog sticks for smooth warp adjustment
- Add preset warp profiles (cinema, curved, etc.)

