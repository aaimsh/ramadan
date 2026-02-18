#!/bin/bash
set -e

# === Configuration ===
PROJECT_DIR="/home/user/ramadan/apk-src"
BUILD_DIR="/home/user/ramadan/build"
ANDROID_JAR="/usr/lib/android-sdk/platforms/android-23/android.jar"
AAPT2="aapt2"
AAPT="aapt"
DX="/usr/lib/android-sdk/build-tools/debian/dx"
ZIPALIGN="zipalign"
APKSIGNER="apksigner"
KOTLIN_LIB="/usr/share/kotlin/kotlinc/lib"

# Output
APK_NAME="iftar-tracker"
OUTPUT_DIR="/home/user/ramadan"

echo "=== Iftar Tracker APK Build ==="
echo ""

# Clean
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"/{gen,obj,classes,apk}

# === Step 1: Compile resources with aapt2 ===
echo "[1/7] Compiling resources..."
mkdir -p "$BUILD_DIR/compiled-res"

# Compile each resource directory
for res_dir in "$PROJECT_DIR"/res/*/; do
    if [ -d "$res_dir" ]; then
        for res_file in "$res_dir"*; do
            if [ -f "$res_file" ]; then
                $AAPT2 compile "$res_file" -o "$BUILD_DIR/compiled-res/" 2>&1
            fi
        done
    fi
done

echo "   Compiled resources: $(ls "$BUILD_DIR/compiled-res/" | wc -l) files"

# === Step 2: Link resources and generate R.java ===
echo "[2/7] Linking resources & generating R.java..."
$AAPT2 link \
    -I "$ANDROID_JAR" \
    --manifest "$PROJECT_DIR/AndroidManifest.xml" \
    --java "$BUILD_DIR/gen" \
    -o "$BUILD_DIR/apk/${APK_NAME}-unaligned.apk" \
    --auto-add-overlay \
    "$BUILD_DIR/compiled-res/"*.flat

echo "   R.java generated at: $BUILD_DIR/gen/com/ramadan/iftartracker/R.java"

# === Step 3: Compile R.java ===
echo "[3/7] Compiling R.java..."
javac -source 8 -target 8 \
    -bootclasspath "$ANDROID_JAR" \
    -classpath "$ANDROID_JAR" \
    -d "$BUILD_DIR/classes" \
    "$BUILD_DIR/gen/com/ramadan/iftartracker/R.java" 2>&1

# === Step 4: Compile Kotlin sources ===
echo "[4/7] Compiling Kotlin sources..."
kotlinc \
    -classpath "$ANDROID_JAR:$BUILD_DIR/classes" \
    -jvm-target 1.8 \
    -d "$BUILD_DIR/classes" \
    "$PROJECT_DIR/java/com/ramadan/iftartracker/MainActivity.kt" 2>&1

echo "   Classes compiled: $(find "$BUILD_DIR/classes" -name "*.class" | wc -l) files"

# === Step 5: Convert to DEX ===
echo "[5/7] Converting to DEX format..."

# Include Kotlin stdlib in the dex
KOTLIN_STDLIB="$KOTLIN_LIB/kotlin-stdlib.jar"

$DX --dex \
    --output="$BUILD_DIR/classes.dex" \
    "$BUILD_DIR/classes" \
    "$KOTLIN_STDLIB" 2>&1

echo "   DEX created: $(ls -lh "$BUILD_DIR/classes.dex" | awk '{print $5}')"

# === Step 6: Add DEX to APK and align ===
echo "[6/7] Packaging APK..."

# Add classes.dex into the APK (which already has resources from aapt2 link)
cd "$BUILD_DIR"
cp "apk/${APK_NAME}-unaligned.apk" "${APK_NAME}-with-dex.apk"

# Use zip to add classes.dex
zip -j "${APK_NAME}-with-dex.apk" classes.dex

# Zipalign
$ZIPALIGN -f 4 "${APK_NAME}-with-dex.apk" "${APK_NAME}-aligned.apk"

echo "   Aligned APK: $(ls -lh "${APK_NAME}-aligned.apk" | awk '{print $5}')"

# === Step 7: Sign the APK ===
echo "[7/7] Signing APK..."

# Generate a debug keystore
keytool -genkeypair \
    -keystore "$BUILD_DIR/debug.keystore" \
    -storepass android \
    -keypass android \
    -alias androiddebugkey \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" 2>&1

$APKSIGNER sign \
    --ks "$BUILD_DIR/debug.keystore" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias androiddebugkey \
    --out "$OUTPUT_DIR/${APK_NAME}.apk" \
    "${APK_NAME}-aligned.apk"

echo ""
echo "=== BUILD SUCCESSFUL ==="
echo "APK: $OUTPUT_DIR/${APK_NAME}.apk"
echo "Size: $(ls -lh "$OUTPUT_DIR/${APK_NAME}.apk" | awk '{print $5}')"

# Verify
$APKSIGNER verify "$OUTPUT_DIR/${APK_NAME}.apk" && echo "Signature: VALID" || echo "Signature: INVALID"

echo ""
echo "Install with: adb install ${APK_NAME}.apk"
