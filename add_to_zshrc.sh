#!/usr/bin/env bash
set -euo pipefail

# Installer for dev shell aliases (portable, repo-relative)
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="${SCRIPT_DIR}"
ALIAS_FILE_REL="tools/dev_aliases.sh"
ALIAS_FILE="${REPO_ROOT}/${ALIAS_FILE_REL}"

# Create alias file
mkdir -p "${REPO_ROOT}/tools"
cat >"${ALIAS_FILE}" <<'EOF'
# PickleTV dev aliases (source this file)
ptv-root() { cd "$(git rev-parse --show-toplevel 2>/dev/null || pwd)"; }

alias ptv-setup='ptv-root; ./tools/push_video.sh'
alias ptv-logs='adb logcat | grep -E "(MainActivity|ExoPlayer|VideoGLRenderer)"'
alias ptv-clear-logs='adb logcat -c && echo "Logs cleared"'
alias ptv-debug='ptv-root; ./tools/push_video.sh && echo && echo "Waiting for logs (Ctrl+C to stop)..." && sleep 1 && adb logcat | grep -E "(MainActivity|ExoPlayer|VideoGLRenderer)"'
EOF

# Print instructions to add to shell rc (no auto-modify)
RC_FILE="${HOME}/.zshrc"
cat <<EOF
Add the following line to your shell rc (${RC_FILE}) to enable dev aliases:

  source "${ALIAS_FILE}"

Then open a new shell or run:

  source "${RC_FILE}"
EOF
