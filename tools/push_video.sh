#!/usr/bin/env bash
set -euo pipefail

# Resolve repo root (supports running from any subdir)
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

# Load config with overridable defaults
CONFIG_FILE="${REPO_ROOT}/tools/config.env"
if [[ -f "${CONFIG_FILE}" ]]; then
  # shellcheck disable=SC1090
  source "${CONFIG_FILE}"
fi

PACKAGE_NAME=${PACKAGE_NAME:-com.example.pickletv}
VIDEO_FILE_NAME=${VIDEO_FILE_NAME:-h-6.mp4}
FALLBACK_ENABLE_GENERATION=${FALLBACK_ENABLE_GENERATION:-1}
FALLBACK_FILE_NAME=${FALLBACK_FILE_NAME:-h-6_720p30.mp4}
FALLBACK_DURATION_SEC=${FALLBACK_DURATION_SEC:-10}
FALLBACK_SIZE=${FALLBACK_SIZE:-1280x720}
FALLBACK_FPS=${FALLBACK_FPS:-30}
DEST_DIR=${DEST_DIR:-"/sdcard/Android/data/${PACKAGE_NAME}/cache"}
DEST_PATH=${DEST_PATH:-"${DEST_DIR}/${VIDEO_FILE_NAME}"}

VIDEO_FILE="${VIDEO_FILE:-}" # allow absolute override via env
if [[ -z "${VIDEO_FILE}" ]]; then
  # default to repo-root relative file name
  VIDEO_FILE="${REPO_ROOT}/${VIDEO_FILE_NAME}"
fi
FALLBACK_FILE="${REPO_ROOT}/${FALLBACK_FILE_NAME}"

log() { printf '%s\n' "$*"; }
err() { printf 'ERROR: %s\n' "$*" 1>&2; }

log "=========================================="
log "PickleTV Debug Video Setup"
log "Repo: ${REPO_ROOT}"
log "Package: ${PACKAGE_NAME}"
log "Video: ${VIDEO_FILE}"
log "Device dest: ${DEST_PATH}"
log "=========================================="

# Check adb
if ! command -v adb >/dev/null 2>&1; then
  err "adb not found in PATH (install Android platform-tools)"
  exit 1
fi

# Ensure a device is connected
if ! adb get-state 1>/dev/null 2>&1; then
  # fallback check via 'adb devices'
  DEVICE_COUNT=$(adb devices | awk '/\tdevice$/{c++} END{print c+0}')
  if [[ "${DEVICE_COUNT}" -eq 0 ]]; then
    err "No connected devices/emulators. Start one and retry."
    exit 1
  fi
fi

# Find or generate video
if [[ ! -f "${VIDEO_FILE}" ]]; then
  log "Primary video not found: ${VIDEO_FILE}"
  if [[ "${FALLBACK_ENABLE_GENERATION}" == "1" ]]; then
    if [[ ! -f "${FALLBACK_FILE}" ]]; then
      log "Generating fallback test clip at ${FALLBACK_FILE} (${FALLBACK_SIZE}@${FALLBACK_FPS}, ${FALLBACK_DURATION_SEC}s)"
      if ! command -v ffmpeg >/dev/null 2>&1; then
        err "ffmpeg not installed. Install with: brew install ffmpeg (macOS) or sudo apt-get install ffmpeg (Ubuntu)"
        exit 1
      fi
      ffmpeg -v error -y \
        -f lavfi -i "testsrc=size=${FALLBACK_SIZE}:rate=${FALLBACK_FPS}" \
        -f lavfi -i "sine=frequency=440:sample_rate=48000" \
        -c:v libx264 -profile:v baseline -level 3.1 -pix_fmt yuv420p -preset veryfast -crf 20 \
        -c:a aac -b:a 128k -t "${FALLBACK_DURATION_SEC}" \
        "${FALLBACK_FILE}"
    fi
    VIDEO_FILE="${FALLBACK_FILE}"
  else
    err "Video file missing and generation disabled. Set FALLBACK_ENABLE_GENERATION=1 or provide ${VIDEO_FILE}"
    exit 1
  fi
fi

# Prepare device dir and push
log "Ensuring device directory: ${DEST_DIR}"
adb shell mkdir -p "${DEST_DIR}"
log "Pushing: ${VIDEO_FILE} -> ${DEST_PATH}"
adb push "${VIDEO_FILE}" "${DEST_PATH}"

# Verify and set permissions
if adb shell test -f "${DEST_PATH}"; then
  SIZE=$(adb shell ls -lh "${DEST_PATH}" | awk '{print $5}')
  log "Pushed OK (size ${SIZE})"
  adb shell chmod 644 "${DEST_PATH}" || true
else
  err "Push verification failed: ${DEST_PATH} not found on device"
  exit 1
fi

log "Done."

