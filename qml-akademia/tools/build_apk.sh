#!/usr/bin/env bash
# Buduje podpisany plik APK bez Android Studio / Gradle.
#
# Wymagane narzędzia (Ubuntu/Debian):
#   sudo apt install aapt apksigner zipalign android-sdk-platform-23 openjdk-17-jdk python3 zip
#   D8 (kompilator do DEX): https://storage.googleapis.com/r8-releases/raw/8.7.18/r8lib.jar
#
# Zmienne środowiskowe (opcjonalne):
#   ANDROID_JAR  - ścieżka do android.jar (domyślnie z pakietu android-sdk-platform-23)
#   R8_JAR       - ścieżka do r8lib.jar
#   KEYSTORE     - plik keystore do podpisu (tworzony automatycznie, jeśli nie istnieje)
#   KS_PASS      - hasło keystore
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO="$(cd "$ROOT/.." && pwd)"
ANDROID_JAR="${ANDROID_JAR:-/usr/lib/android-sdk/platforms/android-23/android.jar}"
R8_JAR="${R8_JAR:-/opt/r8/r8lib-8.7.18.jar}"
KEYSTORE="${KEYSTORE:-$ROOT/signing/release.keystore}"
KS_PASS="${KS_PASS:-qmlakademia}"
KEY_ALIAS="qmlakademia"

VERSION_NAME="$(sed -n 's/.*android:versionName="\([^"]*\)".*/\1/p' "$ROOT/android/AndroidManifest.xml")"
OUT_APK="${OUT_APK:-$REPO/release/QML-Akademia-$VERSION_NAME.apk}"
BUILD="$ROOT/build"

echo "==> Czyszczenie katalogu build"
rm -rf "$BUILD"
mkdir -p "$BUILD"/{res,gen,classes,dex,assets/www}

echo "==> Generowanie treści (content.js)"
python3 "$ROOT/tools/build_content.py"

echo "==> Kopiowanie aplikacji webowej do assets/www"
cp -r "$ROOT/web/." "$BUILD/assets/www/"

echo "==> aapt2 compile"
aapt2 compile --dir "$ROOT/android/res" -o "$BUILD/res/compiled.zip"

echo "==> aapt2 link"
aapt2 link -o "$BUILD/base.apk" \
  -I "$ANDROID_JAR" \
  --manifest "$ROOT/android/AndroidManifest.xml" \
  -A "$BUILD/assets" \
  --java "$BUILD/gen" \
  --auto-add-overlay \
  "$BUILD/res/compiled.zip"

echo "==> javac"
find "$ROOT/android/java" "$BUILD/gen" -name '*.java' > "$BUILD/sources.txt"
javac -nowarn --release 8 -encoding UTF-8 \
  -classpath "$ANDROID_JAR" \
  -d "$BUILD/classes" @"$BUILD/sources.txt" 2>&1 | grep -v "warning: \[options\]" || true
test -f "$BUILD/classes/pl/qmlakademia/app/MainActivity.class"

echo "==> d8 (DEX)"
find "$BUILD/classes" -name '*.class' > "$BUILD/classes.txt"
java -cp "$R8_JAR" com.android.tools.r8.D8 --release --min-api 24 \
  --lib "$ANDROID_JAR" --output "$BUILD/dex" @"$BUILD/classes.txt"

echo "==> Dodawanie classes.dex"
(cd "$BUILD/dex" && zip -q -X "$BUILD/base.apk" classes.dex)

echo "==> zipalign"
zipalign -p -f 4 "$BUILD/base.apk" "$BUILD/aligned.apk"

if [ ! -f "$KEYSTORE" ]; then
  echo "==> Tworzenie klucza podpisu ($KEYSTORE)"
  mkdir -p "$(dirname "$KEYSTORE")"
  keytool -genkeypair -keystore "$KEYSTORE" -storepass "$KS_PASS" -keypass "$KS_PASS" \
    -alias "$KEY_ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=QML Akademia, O=QML Akademia, C=PL" >/dev/null 2>&1
fi

echo "==> apksigner"
mkdir -p "$(dirname "$OUT_APK")"
apksigner sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --ks-key-alias "$KEY_ALIAS" \
  --out "$OUT_APK" "$BUILD/aligned.apk"
rm -f "$OUT_APK.idsig"
apksigner verify --verbose "$OUT_APK" | head -5

echo "==> Gotowe: $OUT_APK ($(du -h "$OUT_APK" | cut -f1))"
