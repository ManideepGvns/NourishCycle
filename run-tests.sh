#!/usr/bin/env zsh
# ─────────────────────────────────────────────────────────────────────────────
# run-tests.sh
#
# Runs NourishCycle unit tests (JVM-only, no device needed).
# Source the dev env first or let this script load it automatically.
#
# Usage:
#   ./run-tests.sh               # run all unit tests
#   ./run-tests.sh Greeting      # run only GreetingTest
#   ./run-tests.sh Preferences   # run only PreferencesRepositoryTest
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

TOOLS_DIR="$HOME/dev-tools"
ENV_FILE="$TOOLS_DIR/env.sh"

# Auto-load env if JAVA_HOME is not already set
if [ -z "${JAVA_HOME:-}" ]; then
  if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
    echo "✓ Dev environment loaded from $ENV_FILE"
  else
    echo "✗ Dev environment not found. Run setup-dev-env.sh first."
    exit 1
  fi
fi

echo "  JAVA_HOME:    $JAVA_HOME"
echo "  ANDROID_HOME: ${ANDROID_HOME:-not set}"
echo "  java version: $(java -version 2>&1 | head -1)"
echo ""

SCRIPT_DIR="${0:A:h}"
cd "$SCRIPT_DIR"

# ── Unit tests (JVM, fast, no emulator required) ──────────────────────────────
FILTER="${1:-}"

if [ -n "$FILTER" ]; then
  echo "→ Running unit tests matching *${FILTER}* …"
  ./gradlew testDebugUnitTest --tests "com.wefit.nourishcycle.*${FILTER}*" \
    --info 2>&1 | grep -E "PASS|FAIL|ERROR|tests were|> Task|exception|BUILD" || true
else
  echo "→ Running ALL unit tests …"
  ./gradlew testDebugUnitTest \
    --info 2>&1 | grep -E "PASS|FAIL|ERROR|tests were|> Task|exception|BUILD" || true
fi

# Show the HTML report location
REPORT="$SCRIPT_DIR/app/build/reports/tests/testDebugUnitTest/index.html"
if [ -f "$REPORT" ]; then
  echo ""
  echo "📊 Full HTML report: $REPORT"
  echo "   Open it with: open $REPORT"
fi
