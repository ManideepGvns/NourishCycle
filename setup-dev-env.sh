#!/usr/bin/env zsh
# ─────────────────────────────────────────────────────────────────────────────
# setup-dev-env.sh
#
# Downloads JDK 21 + Android SDK (command-line tools only) into ~/dev-tools
# without any installer or admin rights.  Run once, then use run-tests.sh.
#
# Usage:
#   chmod +x setup-dev-env.sh
#   ./setup-dev-env.sh
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

TOOLS_DIR="$HOME/dev-tools"
JDK_DIR="$TOOLS_DIR/jdk21"
SDK_DIR="$TOOLS_DIR/android-sdk"

mkdir -p "$TOOLS_DIR"

# ── 1. JDK 21 (Eclipse Temurin, macOS aarch64) ───────────────────────────────
JDK_URL="https://api.adoptium.net/v3/binary/latest/21/ga/mac/aarch64/jdk/hotspot/normal/eclipse?project=jdk"
JDK_TARBALL="$TOOLS_DIR/jdk21.tar.gz"

if [ -d "$JDK_DIR" ]; then
  echo "✓ JDK 21 already present at $JDK_DIR"
else
  echo "→ Downloading JDK 21 (macOS arm64) from Adoptium …"
  curl -fSL "$JDK_URL" -o "$JDK_TARBALL" --progress-bar
  echo "→ Extracting …"
  mkdir -p "$JDK_DIR"
  tar -xzf "$JDK_TARBALL" -C "$JDK_DIR" --strip-components=1
  rm -f "$JDK_TARBALL"
  echo "✓ JDK 21 installed at $JDK_DIR"
fi

export JAVA_HOME="$JDK_DIR/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "  java: $(java -version 2>&1 | head -1)"

# ── 2. Android Command-Line Tools ─────────────────────────────────────────────
# Auto-detect the latest cmdline-tools URL from the Android Studio download page
echo "→ Detecting latest Android command-line tools URL …"
CMDTOOLS_URL=$(curl -fsSL "https://developer.android.com/studio" \
  | grep -oE "https://dl.google.com/android/repository/commandlinetools-mac-[0-9]+_latest.zip" \
  | head -1)

# Fallback to a known-good version if auto-detect fails
if [ -z "$CMDTOOLS_URL" ]; then
  CMDTOOLS_URL="https://dl.google.com/android/repository/commandlinetools-mac-14742923_latest.zip"
  echo "  (using fallback URL)"
fi
echo "  $CMDTOOLS_URL"
CMDTOOLS_ZIP="$TOOLS_DIR/cmdtools.zip"
CMDTOOLS_DIR="$SDK_DIR/cmdline-tools/latest"

if [ -d "$CMDTOOLS_DIR" ]; then
  echo "✓ Android command-line tools already present"
else
  echo "→ Downloading Android SDK command-line tools …"
  curl -fSL "$CMDTOOLS_URL" -o "$CMDTOOLS_ZIP" --progress-bar
  echo "→ Extracting …"
  mkdir -p "$SDK_DIR/cmdline-tools"
  TMPDIR_TOOLS="$TOOLS_DIR/cmdtools-tmp"
  mkdir -p "$TMPDIR_TOOLS"
  unzip -q "$CMDTOOLS_ZIP" -d "$TMPDIR_TOOLS"
  # The zip extracts to a folder called "cmdline-tools"
  mv "$TMPDIR_TOOLS/cmdline-tools" "$CMDTOOLS_DIR"
  rm -rf "$TMPDIR_TOOLS" "$CMDTOOLS_ZIP"
  echo "✓ Android command-line tools installed at $CMDTOOLS_DIR"
fi

export ANDROID_HOME="$SDK_DIR"
export PATH="$CMDTOOLS_DIR/bin:$ANDROID_HOME/platform-tools:$PATH"

# Accept licenses automatically
yes | sdkmanager --sdk_root="$SDK_DIR" --licenses > /dev/null 2>&1 || true

# ── 3. Install required SDK packages ─────────────────────────────────────────
echo "→ Installing Android SDK packages (this may take a few minutes) …"
sdkmanager --sdk_root="$SDK_DIR" \
  "platforms;android-35" \
  "build-tools;35.0.0" \
  "platform-tools"

echo "✓ Android SDK packages installed"

# ── 4. Write local.properties so Gradle finds the SDK ─────────────────────────
SCRIPT_DIR="${0:A:h}"   # directory of this script (zsh)
LOCAL_PROPS="$SCRIPT_DIR/local.properties"
cat > "$LOCAL_PROPS" <<PROPS
sdk.dir=$SDK_DIR
PROPS
echo "✓ Wrote $LOCAL_PROPS"

# ── 5. Print env export commands to source into current shell ─────────────────
ENV_FILE="$TOOLS_DIR/env.sh"
cat > "$ENV_FILE" <<ENV
export JAVA_HOME="$JAVA_HOME"
export ANDROID_HOME="$SDK_DIR"
export PATH="\$JAVA_HOME/bin:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH"
ENV
chmod +x "$ENV_FILE"

printf '\nSetup complete!\n'
printf '  Activate: source %s\n' "$ENV_FILE"
printf '  Run tests: ./run-tests.sh\n\n'
