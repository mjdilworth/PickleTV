# dil.map - Complete Control Reference
## Keystone Correction Controls for Google TV Streamer Remote & Keyboard

This guide covers all available controls for adjusting keystone/trapezoid correction during video playback.

---

## 🎮 Google TV Streamer Remote (Primary Controls)

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
| **Volume Up** | Toggle corner edit mode | Shows/hides corner markers. Enables individual corner adjustment |
| **Volume Down** | Next corner | Cycles through corners: Top-Left → Top-Right → Bottom-Right → Bottom-Left |
| **Mute** | Toggle adjustment speed | Switches between fine (0.05) and coarse (0.20) adjustment steps |
| **Back** | Exit video | Returns to home screen |

### Google TV Streamer Workflow Examples

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
2. Press Volume Up → Enables corner edit mode (corners highlighted)
3. Press Volume Down → Select which corner to adjust
4. Use D-Pad arrows → Move that specific corner
5. Press Volume Down again → Move to next corner
6. Repeat steps 4-5 for each corner
7. Press OK/Center → Save all adjustments
```

#### Toggle Adjustment Speed
```
1. During corner editing
2. Press Mute → Switch to fine adjustment (0.05 steps)
3. Press Mute again → Switch to coarse adjustment (0.20 steps)
4. Use D-Pad for precise control
```

---

## ⌨️ Keyboard Controls (Development - Simulates Google TV Streamer Remote)

### Keyboard Keys Mapped to Remote Buttons

| Keyboard Key | Simulates Remote | Function |
|-------------|------------------|----------|
| **Arrow ↑** | D-Pad ↑ | Pull top inward |
| **Arrow ↓** | D-Pad ↓ | Push bottom outward |
| **Arrow ←** | D-Pad ← | Push left outward |
| **Arrow →** | D-Pad → | Pull right inward |
| **Enter** | OK/Center | Save adjustments |
| **V** | Volume Up | Toggle corner edit mode |
| **B** | Volume Down | Next corner |
| **M** | Mute | Toggle adjustment speed |
| **Esc** | Back | Exit video |

### Additional Development Shortcuts

| Key | Function | Details |
|-----|----------|---------|
| **P** | Toggle play/pause + edit mode | Pauses video and enters edit mode |
| **Space** | Same as P | Alternative play/pause toggle |
| **E** | Toggle corner edit mode | Same as V key (Volume Up) |
| **N** | Next corner | Same as B key (Volume Down) |

### Corner Selection (Numpad - Development Only)

| Key | Function | Details |
|-----|----------|---------|
| **1** | Select TOP-LEFT corner | Automatically enables edit mode |
| **3** | Select TOP-RIGHT corner | Automatically enables edit mode |
| **7** | Select BOTTOM-LEFT corner | Automatically enables edit mode |
| **9** | Select BOTTOM-RIGHT corner | Automatically enables edit mode |
| **Tab** | Next corner | Cycles forward |
| **Shift+Tab** | Previous corner | Cycles backwards |

### Quick Reset & Snap (Development Only)

| Key | Function | Details |
|-----|----------|---------|
| **0** | Reset to center | Returns all corners to (0,0,0,0) - no distortion |
| **R** | Reset warp shape | Same as 0 key |
| **Del** | Reset warp shape | Same as 0 and R keys |

### Adjustment Step Size (Development Only)

| Key | Function | Range |
|-----|----------|-------|
| **]** | Increase step size | Makes adjustments faster (max 0.50) |
| **[** | Decrease step size | Makes adjustments finer (min 0.01) |

Default steps:
- Fine adjustment: **0.05** per keypress
- Large adjustment: **0.20** (4x fine)

### Display & Debugging (Development Only)

| Key | Function | Details |
|-----|----------|---------|
| **F1** | Toggle input logging | Enables/disables verbose keyboard logging in logcat |
| **C** | Toggle corner edit mode | Legacy shortcut |

---

## 📋 Common Workflows

### Workflow 1: Quick Keystone Correction (Google TV Streamer Remote)

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

### Workflow 2: Precise Corner Adjustment (Google TV Streamer Remote)

**Goal**: Perfect alignment for each corner individually

```
1. Play video
2. Volume Up → Enable corner edit mode (corners appear)
3. Volume Down → Select top-left corner (highlighted in cyan)
4. D-Pad ← (4 times) → Move corner left
5. D-Pad ↑ (2 times) → Move corner up
6. Volume Down → Select top-right corner
7. D-Pad → (3 times) → Adjust right edge
8. Volume Down → Select bottom-right corner
9. D-Pad → → D-Pad ↓ → Adjust corner position
10. Volume Down → Select bottom-left corner
11. D-Pad ← → D-Pad ↓ → Adjust final corner
12. OK button → Save all adjustments
```

**Time**: 1-2 minutes for precise calibration

---

### Workflow 3: Fine-Tuning with Speed Control (Google TV Streamer Remote)

**Goal**: Make very precise micro-adjustments

```
1. Play video
2. Volume Up → Enable corner edit
3. Mute button → Switch to fine adjustment mode (0.05 step)
4. Volume Down → Select corner
5. D-Pad arrows → Make tiny precise adjustments
6. Volume Down → Next corner
7. Repeat steps 5-6 for all corners
8. OK → Save
```

---

### Workflow 4: Development Testing (Keyboard - Simulates Remote)

**Goal**: Rapid testing and calibration during development

```
1. P → Pause and enter edit mode
2. V → Enable corner edit (simulates Volume Up)
3. B → Select corner (simulates Volume Down)
4. Arrow keys → Adjust corner
5. B → Next corner
6. M → Toggle fine/coarse (simulates Mute)
7. Enter → Save (simulates OK button)
8. P → Resume playback
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

Volume Down: 1 → 3 → 9 → 7 → 1 (cycles)
Numpad (dev): Direct selection
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
- Press **0**, **R**, or **Del** (keyboard only)
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
**Solution**: Make sure to press OK/Center button after making changes

### Issue: Image is too distorted
**Solution**: Press 0 key (keyboard) to reset to default, then press Enter to save

### Issue: Corner edit mode won't enable
**Solution**: Press Volume Up button (remote) or V/E key (keyboard)

### Issue: Adjustments are too large/small
**Solution**: Press Mute button (remote) or M key (keyboard) to toggle fine/coarse adjustment

### Issue: Can't see which corner is selected
**Solution**: Make sure corner edit mode is enabled (Volume Up or V key)

---

## 📱 Google TV Streamer Remote Button Reference

The Google TV Streamer (2024+) has these buttons:

| Button | Symbol | Usage in dil.map |
|--------|--------|------------------|
| D-Pad | ↑↓←→ | Keystone adjustment |
| OK/Center | ⏺ | Save settings |
| Back | ← | Exit video |
| Home | 🏠 | Return to Android TV home |
| Volume Up | 🔊+ | Toggle corner edit mode |
| Volume Down | 🔉− | Cycle through corners |
| Mute | 🔇 | Toggle adjustment speed |
| Power | ⏻ | Device power |
| Assistant | 🎤 | Google Assistant (unused) |

---

## 🎓 Tips & Best Practices

1. **Start with whole-image adjustment** (D-Pad only) before switching to corner edit mode
2. **Use coarse adjustments first** (default 0.05), then fine-tune with Mute button
3. **Save frequently** - Press OK/Center after each major adjustment
4. **Test at full brightness** - Keystone correction is easier to see with bright video content
5. **Adjust with content playing** - Easier to see alignment with actual video
6. **Cycle through corners** with Volume Down to compare positions
7. **Reset if needed** - Use 0 key (keyboard only) if you get lost

---

## Summary

### Google TV Streamer Remote (All Users)
- **Basic**: D-Pad arrows + OK button
- **Advanced**: Volume Up (edit mode), Volume Down (cycle corners), Mute (speed)
- **Most common**: D-Pad + OK for quick adjustments

### Keyboard (Developers Only)
- **Simulates remote**: V (Vol+), B (Vol−), M (Mute), Enter (OK)
- **Extra features**: Numpad corner selection, step size control ([ ])
- **Debugging**: Input logging (F1)

Both control schemes save to the same settings and work identically!

---

**Last Updated**: December 4, 2025  
**App Version**: 1.0  
**Platform**: Android TV / Google TV
**Remote**: Google TV Streamer (2024+)

