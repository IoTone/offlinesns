#!/usr/bin/env bash
# Apply a brand concept's icon + splash to the Android app.
# Usage: brand/apply.sh [seele|nerv|aghud|hyperlocal|drpop|recon]
# Default: seele (the shipped default). iOS assets are not regenerated
# here (do that later via flutter_launcher_icons once a concept is
# chosen). Requires ImageMagick (`magick`).
set -euo pipefail

C="${1:-seele}"
HERE="$(cd "$(dirname "$0")" && pwd)"
SVG="$HERE/$C/icon.svg"
RES="$HERE/../meshmore_sns_app/responsive_starter_app/android/app/src/main/res"
[ -f "$SVG" ] || { echo "unknown concept: $C"; exit 1; }

case "$C" in
  seele) BG="#000000";; nerv) BG="#0A0E1A";; aghud) BG="#05060B";;
  hyperlocal) BG="#0B0F17";; drpop) BG="#101014";; recon) BG="#000000";;
esac

echo "applying '$C' (bg $BG) → $RES"

# --- legacy launcher PNGs (full-bleed icon) ---
# (portable: macOS ships bash 3.2 — no associative arrays)
for pair in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
  d=${pair%%:*}; p=${pair##*:}
  magick -background none "$SVG" -resize ${p}x${p} -strip -depth 8 \
    "$RES/mipmap-$d/launcher_icon.png"
done

# --- adaptive foreground (108dp canvas, content in the safe zone) ---
for pair in mdpi:108 hdpi:162 xhdpi:216 xxhdpi:324 xxxhdpi:432; do
  d=${pair%%:*}; cv=${pair##*:}
  inner=$(python3 -c "print(round($cv*0.64))")
  magick -background none "$SVG" -resize ${inner}x${inner} \
    -gravity center -extent ${cv}x${cv} -strip -depth 8 \
    "$RES/mipmap-$d/ic_launcher_fg.png"
done

# --- adaptive icon xml (valid: background colour + foreground) ---
mkdir -p "$RES/mipmap-anydpi-v26"
cat > "$RES/mipmap-anydpi-v26/launcher_icon.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_bg"/>
    <foreground android:drawable="@mipmap/ic_launcher_fg"/>
</adaptive-icon>
XML

# --- splash logo (density-independent) ---
magick -background none "$SVG" -resize 768x768 -strip -depth 8 \
  "$RES/drawable/splash_logo.png"
cp "$RES/drawable/splash_logo.png" "$RES/drawable-v21/splash_logo.png"

# --- colours ---
cat > "$RES/values/colors.xml" <<XML
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_bg">$BG</color>
    <color name="splash_bg">$BG</color>
</resources>
XML

# --- pre-12 splash: solid colour + centred logo ---
for sub in drawable drawable-v21; do
cat > "$RES/$sub/launch_background.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@color/splash_bg"/>
    <item android:gravity="center">
        <bitmap android:src="@drawable/splash_logo"/>
    </item>
</layer-list>
XML
done

# --- Android 12+ system splash ---
write_v31 () {
  local f="$1" parent="$2"
cat > "$f" <<XML
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="LaunchTheme" parent="$parent">
        <item name="android:windowSplashScreenBackground">@color/splash_bg</item>
        <item name="android:windowSplashScreenAnimatedIcon">@drawable/splash_logo</item>
        <item name="android:windowLayoutInDisplayCutoutMode">shortEdges</item>
    </style>
    <style name="NormalTheme" parent="$parent">
        <item name="android:windowBackground">?android:colorBackground</item>
    </style>
</resources>
XML
}
mkdir -p "$RES/values-v31" "$RES/values-night-v31"
write_v31 "$RES/values-v31/styles.xml"       "@android:style/Theme.Light.NoTitleBar"
write_v31 "$RES/values-night-v31/styles.xml" "@android:style/Theme.Black.NoTitleBar"

echo "done: '$C' applied. Rebuild the app to see it."
