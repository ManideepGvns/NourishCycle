#!/usr/bin/env zsh
# ─────────────────────────────────────────────────────────────────────────────
# run-instrumented-tests.sh
#
# Runs instrumented tests (DietDao Room + Navigation UI tests).
# Requires a connected Android device or running emulator.
#
# Usage:
#   ./run-instrumented-tests.sh            # run all instrumented tests
#   ./run-instrumented-tests.sh DietDao    # run only DietDaoTest
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

TOOLS_DIR="$HOME/dev-tools"
ENV_FILE="$TOOLS_DIR/env.sh"

if [ -z "${JAVA_HOME:-}" ]; then
  if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
    echo "✓ Dev environment loaded"
  else
    echo "✗ Dev environment not found. Run setup-dev-env.sh first."
    exit 1
  fi
fi

echo "  JAVA_HOME:    $JAVA_HOME"
echo "  ANDROID_HOME: ${ANDROID_HOME:-not set}"
echo ""

SCRIPT_DIR="${0:A:h}"
cd "$SCRIPT_DIR"

# Verify adb sees a device
if ! adb devices 2>/dev/null | grep -q "device$"; then
  echo "✗ No Android device/emulator connected."
  echo "  Connect a device via USB (with USB debugging on) or start an emulator."
  echo "  You can use the Android Emulator downloaded via:"
  echo "    sdkmanager 'system-images;android-35;google_apis;arm64-v8a' emulator"
  exit 1
fi

DEVICE=$(adb devices | grep "device$" | head -1 | awk '{print $1}')
echo "→ Target device: $DEVICE"

FILTER="${1:-}"
if [ -n "$FILTER" ]; then
  echo "→ Running instrumented tests matching *${FILTER}* …"
  ./gradlew connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class="com.wefit.nourishcycle.${FILTER}Test" \
    2>&1 | grep -E "PASS|FAIL|ERROR|tests were|> Task|exception|BUILD" || true
else
  echo "→ Running ALL instrumented tests …"
  ./gradlew connectedDebugAndroidTest \
    2>&1 | grep -E "PASS|FAIL|ERROR|tests were|> Task|exception|BUILD" || true
fi

REPORT="$SCRIPT_DIR/app/build/reports/androidTests/connected/debug/index.html"
if [ -f "$REPORT" ]; then
  echo ""
  echo "📊 Full HTML report: $REPORT"
  echo "   Open it with: open $REPORT"
fi
