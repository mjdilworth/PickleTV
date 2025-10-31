mar#!/bin/zsh
# Add this to your ~/.zshrc to create a convenient alias

# PickleTV Debug Setup Alias
alias pickletv-setup="cd /Users/mike/AndroidStudioProjects/PickleTV && ./setup_debug_video.sh"

# Optional: Add this for quick logcat viewing
alias pickletv-logs="adb logcat | grep MainActivity"

# Optional: Add this to clear device logs before testing
alias pickletv-clear-logs="adb logcat -c && echo 'Logs cleared'"

# Optional: One command to setup + show logs
alias pickletv-debug="cd /Users/mike/AndroidStudioProjects/PickleTV && ./setup_debug_video.sh && echo '' && echo 'Waiting for logs (press Ctrl+C to stop)...' && sleep 2 && adb logcat | grep MainActivity"

