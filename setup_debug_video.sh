#!/usr/bin/env bash
# Legacy wrapper - uses portable tools/push_video.sh
set -euo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
"${SCRIPT_DIR}/tools/push_video.sh" "$@"

