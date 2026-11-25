# eryx

An offline-first, ad-free Android calculator built with Jetpack Compose and an Apple-inspired **Liquid Glass** aesthetic—featuring translucent frosted surfaces, GPU backdrop blur, solid colors without gradients, custom Canvas vector icons, and spring physics.

## Features

- **Standard & Scientific Modes**: Exact `BigDecimal` (`DECIMAL128`) arithmetic, implicit multiplication, smart percentage handling, `DEG`/`RAD`/`GRAD` angle modes + explicit `°` suffix, `2nd` inverse & hyperbolic trig, logs (`ln`, `log₁₀`, `log₂`), factorials/Gamma, `Ans`, `EXP`, `|x|`, and `MC`/`MR`/`M+`/`M-`/`MS` memory registers.
- **Interactive 2D Graphing**: Multi-function plotter with pinch-to-zoom, pan, tap-to-trace `(x, y)` coordinates, value table, and parameter sliders (`a`, `b`).
- **Unit & Currency Converter**: 12 categories (`Length`, `Weight`, `Temperature`, `Currency`, `Area`, `Volume`, `Speed`, `Time`, `Data`, `Energy`, `Pressure`, `Angle`) with 28 offline currencies and optional live ECB exchange rates (`api.frankfurter.app`).
- **Financial Calculators**: `GST / Tax` (inclusive & exclusive + CGST/SGST), `Loan EMI`, `Discount`, `Percentage`, `Tip & Split`, and `Compound Interest`.
- **Searchable History & Gestures**: SQLite-backed history with search, date/pinned/favorites filters, swipe-left `[Copy] [Edit] [Delete]` actions, cursor-editable expressions, swipe-left/right Undo/Redo, copy/paste, and sharing.
- **Ergonomics & Accessibility**: One-handed thumb reachability (`Standard`, `Left Thumb`, `Right Thumb`), Light/Dark/System themes, 5 accent colors, Circular/Rounded key layouts, customizable haptics & synthesized sound click, Text-to-Speech voice results, landscape mode, and a Jetpack Glance home screen widget.

## Build & Install

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```

Prebuilt release APK: [`eryx-v1.0.5.apk`](./eryx-v1.0.5.apk) (Android 8.0+ / API 26+, `2.0 MB`).

## Publishing App Updates

`eryx` includes an in-app updater (**Settings → About & Updates**, plus an optional 12-hour quiet check) that checks both **GitHub Releases** (`releases/latest` with an `.apk` asset) and `update.json` on `main`, downloads the new APK directly, and launches Android's package installer.

To release a newer version (e.g., `1.0.1`):
1. Increment `versionCode` and `versionName` in `app/build.gradle.kts` (signed with the **same signing key** as the installed app).
2. Build the release APK: `./gradlew assembleRelease`.
3. Either:
   - **Option A (GitHub Release)**: Create a GitHub Release tagged `v1.0.1` and attach the `.apk` file.
   - **Option B (`update.json`)**: Commit the new APK and update `versionCode`, `versionName`, `apkUrl`, and `releaseNotes` in `update.json` on `main`.
