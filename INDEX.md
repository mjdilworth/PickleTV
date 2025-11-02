# PickleTV - Complete Documentation Index
## 🎯 Start Here
### For First-Time Users
1. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** ⭐
   - One-page quick start guide
   - Essential keyboard shortcuts
   - Common workflows
   - Troubleshooting tips
   - **Time to read: 5 minutes**
### For Developers
2. **[KEYBOARD_CONTROLS.md](KEYBOARD_CONTROLS.md)** ⭐
   - Complete keyboard mapping reference
   - 40+ key combinations documented
   - Detailed workflow examples
   - Tips & tricks
   - **Time to read: 15 minutes**
### For Technical Review
3. **[ARCHITECTURE.md](ARCHITECTURE.md)**
   - Technical architecture overview
   - Component descriptions
   - Shader pipeline details
   - Implementation details
   - **Time to read: 20 minutes**
---
## 📚 Complete Documentation Library
### Project Overview
- **[README.md](README.md)** - Project description, features, and overview
- **[DEVELOPMENT_COMPLETE.md](DEVELOPMENT_COMPLETE.md)** - Development completion status
- **[IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md)** - Implementation summary
### Getting Started
- **[QUICKSTART.md](QUICKSTART.md)** - Step-by-step setup guide
- **[VIDEO_PLACEMENT.md](VIDEO_PLACEMENT.md)** - Video file configuration
- **[DEBUG_SETUP.md](DEBUG_SETUP.md)** - Debug environment setup
- **[AUTOMATED_SETUP.md](AUTOMATED_SETUP.md)** - Automated setup scripts
### Development Guides
- **[KEYBOARD_CONTROLS.md](KEYBOARD_CONTROLS.md)** - Complete keyboard reference ⭐
- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Quick cheat sheet ⭐
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Technical architecture
### Build & Deployment
- **[build.gradle.kts](app/build.gradle.kts)** - Gradle build configuration
- **[local.properties](local.properties)** - SDK path configuration
- **[BUGFIXES.md](BUGFIXES.md)** - Known issues and fixes
### Build Status
- **[IMPLEMENTATION.md](IMPLEMENTATION.md)** - Implementation checklist
---
## 🎮 Control Systems
### Keyboard Mapping
#### Essential Controls
```
P / Space      Play/Pause + Edit Mode
1,3,7,9        Select corner + Edit
↑↓←→           Adjust
0              Reset to center
R              Reset warp
Enter          Save
```
#### Advanced Controls
```
E              Toggle edit mode
N              Cycle to next corner
Tab/Shift+Tab  Navigate corners
[ ]            Adjust step size (precision)
M              Toggle grid overlay
Esc            Exit edit mode
Page Up/Down   Move vertical
Vol +/-        Move horizontal
F1             Toggle logging
```
### Remote Control Mapping
| Remote Button | Keyboard | Function |
|---|---|---|
| D-Pad ▲▼◀▶ | Arrow Keys/DPAD | Adjust warp |
| OK/Enter | Enter | Save adjustments |
| Play/Pause | P or Space | Play/pause + edit mode |
| Menu/Info | M | Toggle overlay |
| Back | Esc | Exit edit mode |
| Volume +/- | Vol +/- | Move frame |
| Channel +/- | Page Up/Down | Move shape |
| Fast Fwd/Rewind | ]/[ | Adjust step size |
| 1,3,7,9,0 | 1,3,7,9,0 | Snap helpers |
---
## 📊 Project Structure
### Source Code
```
app/src/main/java/com/example/pickletv/
├── MainActivity.kt           (800 lines - Input handling)
├── VideoGLRenderer.kt        (400 lines - OpenGL pipeline)
├── WarpShape.kt             (Data model & persistence)
├── Corner.kt                (Corner enumeration)
├── vertex_shader.glsl       (Warp transformation)
└── fragment_shader.glsl     (Texture sampling)
```
### Configuration
```
app/build.gradle.kts          (Build configuration)
gradle/libs.versions.toml     (Dependencies)
app/src/main/AndroidManifest.xml (Permissions)
local.properties              (SDK path)
```
### Scripts
```
push_video_simple.sh          (Video deployment)
VERIFY_SETUP.sh               (Setup verification)
gradlew                       (Gradle wrapper)
```
### Resources
```
app/src/main/res/
├── mipmap-*/ic_launcher.webp (App icon)
└── values/strings.xml         (Strings)
```
---
## 🚀 Quick Commands
### Build & Run
```bash
# Clean build
./gradlew clean
# Build debug APK
./gradlew assembleDebug
# Build and install
./gradlew installDebug
# Full build
./gradlew build
```
### Device Management
```bash
# Push video to device
./push_video_simple.sh
# List devices
adb devices
# View logs
adb logcat | grep MainActivity
# Start app
adb shell am start -n com.example.pickletv/.MainActivity
# Uninstall
adb uninstall com.example.pickletv
```
### Verification
```bash
# Verify setup
./VERIFY_SETUP.sh
```
---
## 📋 Checklist for New Developers
### Setup (30 minutes)
- [ ] Read `QUICK_REFERENCE.md`
- [ ] Run `./VERIFY_SETUP.sh`
- [ ] Run `./push_video_simple.sh`
- [ ] Run `./gradlew installDebug`
### First Run (10 minutes)
- [ ] Launch app on device/emulator
- [ ] Press `P` to pause and enter edit mode
- [ ] Press `1` to select top-left corner
- [ ] Use arrows to adjust
- [ ] Press `Enter` to save
- [ ] Press `P` to resume
### Learning (1 hour)
- [ ] Read complete `KEYBOARD_CONTROLS.md`
- [ ] Try each control category
- [ ] Experiment with step sizes
- [ ] Test reset functionality
- [ ] Review logging output (F1)
### Development (ongoing)
- [ ] Follow architecture in `ARCHITECTURE.md`
- [ ] Use keyboard shortcuts from `QUICK_REFERENCE.md`
- [ ] Reference complete mappings in `KEYBOARD_CONTROLS.md`
- [ ] Check troubleshooting when issues arise
---
## 🔍 File Quick Reference
| File | Purpose | Read Time |
|------|---------|-----------|
| `QUICK_REFERENCE.md` | Quick start | 5 min |
| `KEYBOARD_CONTROLS.md` | Complete reference | 15 min |
| `README.md` | Overview | 10 min |
| `ARCHITECTURE.md` | Technical details | 20 min |
| `QUICKSTART.md` | Setup guide | 10 min |
| `DEVELOPMENT_COMPLETE.md` | Completion status | 5 min |
---
## 🎯 Common Tasks
### Task: Adjust Warp Shape
See: `QUICK_REFERENCE.md` → Quick Adjustment Workflow
### Task: Select Specific Corner
See: `KEYBOARD_CONTROLS.md` → Corner Selection & Snap Helpers
### Task: Fine-Tune Precision
See: `KEYBOARD_CONTROLS.md` → Step Size Control
### Task: Reset Everything
See: `KEYBOARD_CONTROLS.md` → Common Workflows
### Task: Deploy to Device
See: `QUICKSTART.md` or run `./push_video_simple.sh`
### Task: Understand Architecture
See: `ARCHITECTURE.md` and review source code
---
## 📞 Getting Help
### Question Type | Where to Look
---|---
"How do I...?" | QUICK_REFERENCE.md
"What key does...?" | KEYBOARD_CONTROLS.md (Keyboard Reference Sheet section)
"Why does the code...?" | ARCHITECTURE.md
"How do I set up?" | QUICKSTART.md
"Is something broken?" | BUGFIXES.md
"What's the status?" | DEVELOPMENT_COMPLETE.md
---
## 🔄 Document Relationships
```
START
  ↓
QUICK_REFERENCE.md (5 min)
  ↓
KEYBOARD_CONTROLS.md (15 min)
  ↓
Try it out on device
  ↓
Questions?
  ├→ How-to → KEYBOARD_CONTROLS.md
  ├→ Technical → ARCHITECTURE.md
  ├→ Setup → QUICKSTART.md
  └→ Issues → BUGFIXES.md
```
---
## 📊 Documentation Statistics
- **Total Pages**: 8+ documents
- **Total Words**: 15,000+
- **Code Examples**: 50+
- **Keyboard Mappings**: 40+
- **Workflows**: 10+
- **Troubleshooting Tips**: 20+
---
## ✨ Key Features Documented
### Core Features
- ✅ Video playback with ExoPlayer
- ✅ OpenGL ES 2.0 rendering
- ✅ Real-time warp transformation
- ✅ 4-point corner adjustment
- ✅ Persistent warp storage
### Input Features
- ✅ 40+ keyboard mappings
- ✅ Remote control support
- ✅ Multi-speed adjustments
- ✅ Dynamic step sizing
- ✅ Precise corner selection
### Visual Features
- ✅ Real-time warp preview
- ✅ Corner highlighting
- ✅ Green warp border
- ✅ Color-coded markers
- ✅ Overlay grid option
---
## 🎊 Document Status
| Document | Status | Updated |
|----------|--------|---------|
| QUICK_REFERENCE.md | ✅ Complete | Nov 2, 2025 |
| KEYBOARD_CONTROLS.md | ✅ Complete | Nov 2, 2025 |
| ARCHITECTURE.md | ✅ Complete | Nov 2, 2025 |
| README.md | ✅ Complete | Nov 2, 2025 |
| QUICKSTART.md | ✅ Complete | Nov 2, 2025 |
| DEVELOPMENT_COMPLETE.md | ✅ Complete | Nov 2, 2025 |
---
## 🎓 Learning Path
### Beginner (1 hour)
1. Read `QUICK_REFERENCE.md` (5 min)
2. Run setup verification (5 min)
3. Build and install (10 min)
4. Try basic controls (10 min)
5. Read common workflows (10 min)
6. Try advanced controls (15 min)
### Intermediate (2 hours)
1. Read `KEYBOARD_CONTROLS.md` (30 min)
2. Practice each control category (30 min)
3. Review workflows (15 min)
4. Experiment with edge cases (15 min)
### Advanced (4 hours)
1. Read `ARCHITECTURE.md` (30 min)
2. Study source code (90 min)
3. Understand shader pipeline (30 min)
4. Review persistence layer (30 min)
---
**Version**: 1.0  
**Last Updated**: November 2, 2025  
**Status**: Complete & Ready for Production  
