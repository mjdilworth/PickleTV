#!/bin/bash
# Verification script for PickleTV development setup
set -e
echo "╔════════════════════════════════════════════════════════════╗"
echo "║         PickleTV Development Setup Verification            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'
PASSED=0
FAILED=0
check_file() {
    local file=$1
    local description=$2
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $description"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} $description (missing: $file)"
        ((FAILED++))
    fi
}
check_dir() {
    local dir=$1
    local description=$2
    if [ -d "$dir" ]; then
        echo -e "${GREEN}✓${NC} $description"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} $description (missing: $dir)"
        ((FAILED++))
    fi
}
check_command() {
    local cmd=$1
    local description=$2
    if command -v "$cmd" &> /dev/null; then
        echo -e "${GREEN}✓${NC} $description"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} $description ($cmd not found)"
        ((FAILED++))
    fi
}
PROJECT_ROOT=$(cd "$(dirname "$0")" && pwd)
echo "📋 PROJECT STRUCTURE"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_file "$PROJECT_ROOT/app/build.gradle.kts" "Build configuration"
check_file "$PROJECT_ROOT/local.properties" "Local properties (SDK path)"
check_file "$PROJECT_ROOT/h-6.mp4" "Video file"
check_file "$PROJECT_ROOT/push_video_simple.sh" "Video push script"
check_dir "$PROJECT_ROOT/app/src/main/java/com/example/pickletv" "Source code directory"
check_dir "$PROJECT_ROOT/gradle" "Gradle directory"
echo ""
echo "📄 SOURCE FILES"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_file "$PROJECT_ROOT/app/src/main/java/com/example/pickletv/MainActivity.kt" "Main activity"
check_file "$PROJECT_ROOT/app/src/main/java/com/example/pickletv/VideoGLRenderer.kt" "GL renderer"
check_file "$PROJECT_ROOT/app/src/main/java/com/example/pickletv/WarpShape.kt" "Warp data model"
check_file "$PROJECT_ROOT/app/src/main/java/com/example/pickletv/Corner.kt" "Corner enum"
echo ""
echo "📚 DOCUMENTATION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_file "$PROJECT_ROOT/KEYBOARD_CONTROLS.md" "Keyboard reference"
check_file "$PROJECT_ROOT/QUICK_REFERENCE.md" "Quick start guide"
check_file "$PROJECT_ROOT/README.md" "Project README"
check_file "$PROJECT_ROOT/ARCHITECTURE.md" "Architecture document"
check_file "$PROJECT_ROOT/DEVELOPMENT_COMPLETE.md" "Development status"
echo ""
echo "🔧 TOOLS & DEPENDENCIES"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_command "java" "Java compiler"
check_command "gradle" "Gradle"
check_dir "$HOME/Android/Sdk" "Android SDK"
check_dir "$HOME/Android/Sdk/platform-tools" "ADB tools"
echo ""
echo "📦 BUILD ARTIFACTS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_file "$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk" "Debug APK"
echo ""
echo "════════════════════════════════════════════════════════════"
echo "SUMMARY"
echo "════════════════════════════════════════════════════════════"
TOTAL=$((PASSED + FAILED))
echo "Passed: ${GREEN}${PASSED}${NC}"
echo "Failed: ${RED}${FAILED}${NC}"
echo "Total:  $TOTAL"
echo ""
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ Setup verification PASSED${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. ./push_video_simple.sh       (Push video to device)"
    echo "  2. ./gradlew installDebug       (Install app)"
    echo "  3. Use keyboard controls        (See QUICK_REFERENCE.md)"
    exit 0
else
    echo -e "${RED}✗ Setup verification FAILED${NC}"
    echo "Please check the missing items above"
    exit 1
fi
