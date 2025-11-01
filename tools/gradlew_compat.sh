#!/usr/bin/env bash
set -euo pipefail
# Ensure Gradle runs with a JDK compatible with Android Gradle Plugin/Kotlin (17 or 21)

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

pick_jdk() {
  # 1) Respect explicit env override
  if [[ -n "${JAVA_HOME:-}" ]]; then
    echo "${JAVA_HOME}"
    return 0
  fi
  if [[ -n "${ANDROID_STUDIO_JDK:-}" ]]; then
    echo "${ANDROID_STUDIO_JDK}"
    return 0
  fi

  # 2) Android Studio embedded JBR (macOS typical)
  for app in \
    "/Applications/Android Studio.app" \
    "/Applications/Android Studio Preview.app" \
    "/Applications/AndroidStudio.app" \
    "/Applications/AndroidStudioPreview.app"
  do
    if [[ -d "$app/Contents/jbr/Contents/Home" ]]; then
      echo "$app/Contents/jbr/Contents/Home"
      return 0
    fi
  done

  # 3) macOS java_home (prefer 17, then 21)
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    if J=$(/usr/libexec/java_home -v 17 2>/dev/null); then echo "$J"; return 0; fi
    if J=$(/usr/libexec/java_home -v 21 2>/dev/null); then echo "$J"; return 0; fi
  fi

  # 4) Common Linux paths
  for dir in \
    "/usr/lib/jvm/java-17-openjdk-amd64" \
    "/usr/lib/jvm/java-17-openjdk" \
    "/usr/lib/jvm/temurin-17-jdk-amd64" \
    "/usr/lib/jvm/temurin-21-jdk-amd64" \
    "/usr/lib/jvm/java-21-openjdk-amd64" \
    "/usr/lib/jvm/java-21-openjdk"
  do
    if [[ -x "$dir/bin/java" ]]; then echo "$dir"; return 0; fi
  done

  return 1
}

JDK_HOME=""
if ! JDK_HOME="$(pick_jdk)"; then
  echo "ERROR: Could not locate a compatible JDK (17 or 21)." >&2
  echo "- Install Temurin 17: brew install temurin@17 (macOS) or apt install temurin-17-jdk (Ubuntu)" >&2
  echo "- Or set ANDROID_STUDIO_JDK to Android Studio's embedded JBR path" >&2
  exit 1
fi

export JAVA_HOME="$JDK_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

exec "$REPO_ROOT/gradlew" "$@"

