# PickleTV - Complete Control Reference
## Keystone Correction Controls for TV Remote & Keyboard

This guide covers all available controls for adjusting keystone/trapezoid correction during video playback.

---

## 🎮 TV Remote Controls (Production - Always Available)

### Basic Image Adjustment

| Remote Button | Function | Details |
|--------------|----------|---------|
| **D-Pad ↑** | Pull top inward | Adjusts top-left and top-right corners inward |
| **D-Pad ↓** | Push bottom outward | Adjusts bottom-left and bottom-right corners outward |
| **D-Pad ←** | Push left outward | Adjusts left side (top-left and bottom-left) outward |
| **D-Pad →** | Pull right inward | Adjusts right side (top-right and bottom-right) inward |
| **OK/Center** | Save adjustments | Saves current keystone shape to device storage |

### Advanced Corner Editing

| Remote Button | Function | Details |
|--------------|----------|---------|
| **Menu** | Toggle corner edit mode | Shows/hides corner markers. Enables individual corner adjustment |
| **Channel ↑** | Next corner | Cycles through corners: Top-Left → Top-Right → Bottom-Right → Bottom-Left |
| **Channel ↓** | Previous corner | Cycles backwards through corners |
| **Info/Guide** | Toggle adjustment speed | Switches between fine (0.05) and coarse (0.20) adjustment steps |
| **B Button** | Reset to default | Removes all keystone correction (returns to flat/centered) |
| **Back** | Exit video | Returns to home screen |

### Remote Control Workflow Examples

#### Quick Adjustment (Whole Image)
```
1. Select and play a video
2. Use D-Pad arrows to adjust the image shape
   - ↑ for top, ↓ for bottom, ← for left, → for right
3. Press OK/Center button to save
```

#### Precise Corner-by-Corner Adjustment
```
1. Play video
2. Press Menu button → Enables corner edit mode (corners highlighted)
3. Press Channel ↑ → Select which corner to adjust
4. Use D-Pad arrows → Move that specific corner
5. Press Channel ↑ again → Move to next corner
6. Repeat steps 4-5 for each corner
7. Press OK/Center → Save all adjustments
```

#### Reset and Start Over
```
1. During video playback
2. Press B button → Resets to no distortion
3. Press OK/Center → Saves the reset state
```

---

## ⌨️ Keyboard Controls (Development - Debug Builds Only)

### Play/Pause & Edit Mode

| Key | Function | Details |
|-----|----------|---------|
| **P** | Toggle play/pause + edit mode | Pauses video and enters edit mode; resumes and exits edit mode |
| **Space** | Same as P | Alternative play/pause toggle |
| **E** | Toggle corner edit mode | Shows/hides corner markers without pausing |
| **Esc** | Exit edit mode | Disables corner markers |

### Directional Navigation (Arrow Keys = D-Pad)

| Key | Same as Remote | Function | In Edit Mode |
|-----|---------------|----------|--------------|
| **↑** | D-Pad ↑ | Pull top inward | Move selected corner UP |
| **↓** | D-Pad ↓ | Push bottom outward | Move selected corner DOWN |
| **←** | D-Pad ← | Push left outward | Move selected corner LEFT |
| **→** | D-Pad → | Pull right inward | Move selected corner RIGHT |

### Corner Selection (Numpad)

| Key | Function | Details |
|-----|----------|---------|
| **1** | Select TOP-LEFT corner | Automatically enables edit mode |
| **3** | Select TOP-RIGHT corner | Automatically enables edit mode |
| **7** | Select BOTTOM-LEFT corner | Automatically enables edit mode |
| **9** | Select BOTTOM-RIGHT corner | Automatically enables edit mode |
| **N** | Next corner | Cycles through corners in order |
| **Tab** | Next corner | Alternative to N key |
| **Shift+Tab** | Previous corner | Cycles backwards |

### Quick Reset & Snap

| Key | Function | Details |
|-----|----------|---------|
| **0** | Reset to center | Returns all corners to (0,0,0,0) - no distortion |
| **R** | Reset warp shape | Same as 0 key |
| **Del** | Reset warp shape | Same as 0 and R keys |

### Adjustment Step Size

| Key | Same as Remote | Function | Range |
|-----|---------------|----------|-------|
| **]** | - | Increase step size | Makes adjustments faster (max 0.50) |
| **[** | - | Decrease step size | Makes adjustments finer (min 0.01) |

Default steps:
- Fine adjustment: **0.05** per keypress
- Large adjustment: **0.20** (4x fine)

### Save & Actions

| Key | Same as Remote | Function |
|-----|---------------|----------|
| **Enter** | OK/Center | Save current keystone shape to storage |
| **C** | - | Legacy toggle for corner edit mode |

### Whole Shape Movement (Advanced)

| Key | Function | Details |
|-----|----------|---------|
| **Page Up** | Move shape UP | Tilts entire shape: top inward, bottom outward |
| **Page Down** | Move shape DOWN | Tilts entire shape: top outward, bottom inward |
| **Volume +** | Move frame RIGHT | Shifts entire shape horizontally right |
| **Volume −** | Move frame LEFT | Shifts entire shape horizontally left |

### Display & Debugging

| Key | Function | Details |
|-----|----------|---------|
| **M** | Toggle overlay grid | Shows/hides adjustment grid overlay |
| **F1** | Toggle input logging | Enables/disables verbose keyboard logging in logcat |

---

## 📋 Common Workflows

### Workflow 1: Quick Keystone Correction (TV Remote)

**Goal**: Quickly fix trapezoid distortion for a projector

```
1. Select video from home screen → Video plays
2. D-Pad ↑ (3 times) → Pull top inward
3. D-Pad ↓ (2 times) → Push bottom outward
4. D-Pad → (2 times) → Pull right side in
5. OK button → Save adjustments
```

**Time**: ~30 seconds

---

### Workflow 2: Precise Corner Adjustment (TV Remote)

**Goal**: Perfect alignment for each corner individually

```
1. Play video
2. Menu button → Enable corner edit mode (corners appear)
3. Channel ↑ → Select top-left corner (highlighted in cyan)
4. D-Pad ← (4 times) → Move corner left
5. D-Pad ↑ (2 times) → Move corner up
6. Channel ↑ → Select top-right corner
7. D-Pad → (3 times) → Adjust right edge
8. Channel ↑ → Select bottom-right corner
9. D-Pad → → D-Pad ↓ → Adjust corner position
10. Channel ↑ → Select bottom-left corner
11. D-Pad ← → D-Pad ↓ → Adjust final corner
12. OK button → Save all adjustments
```

**Time**: 1-2 minutes for precise calibration

---

### Workflow 3: Fine-Tuning with Speed Control (TV Remote)

**Goal**: Make very precise micro-adjustments

```
1. Play video
2. Menu → Enable corner edit
3. Info/Guide button → Switch to fine adjustment mode (0.05 step)
4. Channel ↑ → Select corner
5. D-Pad arrows → Make tiny precise adjustments
6. Channel ↑ → Next corner
7. Repeat steps 5-6 for all corners
8. OK → Save
```

---

### Workflow 4: Development Testing (Keyboard)

**Goal**: Rapid testing and calibration during development

```
1. P → Pause and enter edit mode
2. ] ] → Increase step size for faster adjustments
3. 1 → Select top-left corner
4. ←←←← ↑↑ → Adjust corner quickly
5. N → Next corner (top-right)
6. →→→ → Adjust right edge
7. 0 → Reset if needed to start over
8. [ → Decrease step for fine-tuning
9. ↑ → Make tiny adjustment
10. Enter → Save final result
11. P → Resume playback
```

**Time**: 30-60 seconds with keyboard shortcuts

---

## 🎯 Visual Guide

### Corner Selection Order

```
1 (Top-Left)      3 (Top-Right)
    ┌─────────────┐
    │             │
    │   VIDEO     │
    │             │
    └─────────────┘
7 (Bottom-Left)   9 (Bottom-Right)

Channel ↑: 1 → 3 → 9 → 7 → 1 (cycles)
Channel ↓: 7 → 9 → 3 → 1 → 7 (reverse)
```

### D-Pad Adjustment Direction (Normal Mode)

```
        ↑ (Pull top inward)
        
← (Left out)    → (Right in)

        ↓ (Bottom outward)
```

### Corner Edit Mode Indicators

- **Yellow markers**: Normal corners
- **Cyan marker**: Currently selected corner
- **Green border**: Shows the warped shape outline

---

## 💾 Persistence

### Automatic Saving
- Adjustments are **automatically saved** when you press OK/Enter
- Settings persist across app restarts
- Each video starts with the last saved keystone correction

### Manual Reset
- Press **0**, **R**, or **Del** (keyboard) or **B button** (remote)
- Press OK/Enter to save the reset state
- Restart app to reload previous saved settings if you didn't save the reset

---

## 🔧 Technical Details

### Adjustment Values
- Stored as 4 floats: `topLeft`, `topRight`, `bottomLeft`, `bottomRight`
- Values represent normalized displacement (-1.0 to 1.0)
- Scaled by adjustment step in shader for visual effect
- Default step: **0.05** per keypress

### Storage Location
- Settings saved to: `SharedPreferences` (Android internal storage)
- Key: `warp_shape`
- Survives app updates and device restarts
- Can be cleared via app data settings

---

## 🆘 Troubleshooting

### Issue: Adjustments not saving
**Solution**: Make sure to press OK/Enter button after making changes

### Issue: Image is too distorted
**Solution**: Press B button (remote) or 0 key (keyboard) to reset to default

### Issue: Corner edit mode won't enable
**Solution**: Press Menu button (remote) or E/C key (keyboard)

### Issue: Adjustments are too large/small
**Solution**: Press Info/Guide button (remote) or [ ] keys (keyboard) to change step size

### Issue: Can't see which corner is selected
**Solution**: Make sure corner edit mode is enabled (Menu button or E key)

---

## 📱 Remote Button Reference (Common TV Remotes)

Different TV remotes may have slightly different button labels:

| Our Name | Alternative Names | Common Symbols |
|----------|------------------|----------------|
| OK/Center | Select, Enter | ⏺, ✓ |
| Menu | Settings, Options | ☰, ⚙ |
| Channel ↑ | CH+, Page Up | ▲, + |
| Channel ↓ | CH-, Page Down | ▼, - |
| Info | Display, i | ℹ️, 📋 |
| Guide | EPG | 📺 |
| B Button | Cancel, Clear | ⭕, ✕ |
| Back | Return, Exit | ◀, ← |

---

## 🎓 Tips & Best Practices

1. **Start with whole-image adjustment** (D-Pad only) before switching to corner edit mode
2. **Use coarse adjustments first**, then switch to fine mode for precision
3. **Save frequently** - Press OK/Center after each major adjustment
4. **Test at full brightness** - Keystone correction is easier to see with bright video content
5. **Adjust with content playing** - Easier to see alignment with actual video, not black screen
6. **Use channel buttons** to quickly compare corner positions
7. **Reset and try again** if you get lost - B button resets everything

---

## Summary

### TV Remote (All Users)
- **Basic**: D-Pad arrows + OK button
- **Advanced**: Menu, Channel ↑↓, Info/Guide, B button
- **Most common**: D-Pad + OK for quick adjustments

### Keyboard (Developers Only)
- **Full control**: All features accessible
- **Faster workflow**: Numpad corner selection, step size control
- **Debugging**: Input logging, overlay grid

Both control schemes save to the same settings and work identically - choose based on your input device!

---

**Last Updated**: December 2025  
**App Version**: 1.0  
**Platform**: Android TV / Fire TV

