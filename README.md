# AI — Arfin Illustrator

**AI (Arfin Illustrator)** is a mobile-first, touch-friendly Android vector & graphic design application built with **Kotlin** and **Jetpack Compose** (Material Design 3), featuring a dark charcoal (`#1E1E1E`) and vibrant orange (`#FF7A00`) studio interface with mixed **Bangla + English** UX.

- **Version:** `1.0.0`
- **Developer:** মোঃ ফেরদাউস মোল্লা (Md. Ferdaus Molla)
- **Mobile:** `+8801934411199`
- **Email:** `ferdausharfin@gmail.com` • `ferdausharfin@yahoo.com`

---

## Key Features

- **Brand Identity & 2.5s Animated Splash Screen**
  - Custom adaptive launcher icon and glowing gradient `"Ai"` badge with `"Arfin Illustrator"` typography and animated loading progress bar.
- **Custom Size Chart (300 DPI) & Smart Mode**
  - **Inch Chart (1"×1" to 1"×20")**: `300×300 px` up to `300×6000 px`.
  - **Feet Chart (1'×1' to 1'×20')**: `3600×3600 px` up to `3600×72000 px`.
  - **Custom Size Box**: Enter Width + Height, select unit (`Inch`, `Feet`, `Cm`, `Pixel`) and DPI (`72`, `150`, `300`).
  - **Smart Mode**: Keeps large feet/inch banners smooth and lag-free on screen while exporting at crisp 300 DPI.
- **20 Ready Bangla Templates (`রেডি টেমপ্লেট`)**
  - Filterable by `All`, `মাদ্রাসা`, `ইসলামিক`, `ব্যবসা`, `উৎসব`, `খেলা` and searchable by Bangla name:
    1. মাদ্রাসা ভর্তি বিজ্ঞপ্তি • 2. মাহফিল পোস্টার • 3. ঈদ মোবারক কার্ড • 4. জুম্মা মোবারক পোস্ট • 5. বিবাহের কার্ড • 6. জন্মদিনের কার্ড • 7. দোকান উদ্বোধন ব্যানার • 8. চাকরির বিজ্ঞপ্তি • 9. কোচিং ভর্তি পোস্টার • 10. ফেসবুক পেজ প্রমোশন • 11. ইউটিউব থাম্বনেইল • 12. ব্যবসার লোগো টেমপ্লেট • 13. ঈদ অফার / দোকানের অফার • 14. মৃত্যু সংবাদ / শোক বার্তা • 15. রক্তদান পোস্টার • 16. ক্রিকেট / ফুটবল টুর্নামেন্ট • 17. মাদ্রাসার ফলাফল / সংবর্ধনা • 18. হজ্জ মোবারক / ওমরাহ • 19. নববর্ষ শুভেচ্ছা • 20. বিজয় দিবস / স্বাধীনতা দিবস.
- **Illustrator-Style Vector Editor**
  - Select, Pen Tool (smooth Bezier curves), Brush (size & opacity), Shapes (Rectangle, Circle, Triangle, Line, Star, Polygon with fill, stroke, stroke width, corner radius), Text, Image (Gallery/Camera, Crop, Rotate, Opacity, Offline Background Remover), 107 Vector Stickers & Islamic Calligraphy elements, Color Picker (HSV, Gradient, Recent Colors, Eyedropper), Alignment, Grid, Ruler, Snap, Layers right-slide panel, and 100-step Undo/Redo.
  - **Bengali + English Fonts**: *Hind Siliguri, Noto Serif Bengali, Noto Sans Bengali, Baloo Da 2, Tiro Bangla, Amiri, SolaimanLipi, SutonnyMJ, Siyam Rupali, BenSen, Charukola, Li Ador Noir, Li Alinur, Mina, Atma, Galada, Anek Bangla, Jost* and more — all previewed with `"আরফিন ইলাস্ট্রেটর"`.
- **High-Resolution Export & Offline Auto-Save**
  - **PNG, JPG, SVG, VECTOR, PDF, WEBP** at **1x, 2x, 4K (Default), 8K Ultra HD**, Transparent Background toggle, Gallery save, and direct share to WhatsApp, Facebook, Messenger.
  - Works 100% offline with 30-second Room SQLite auto-save, Dark/Light mode, and 3-step onboarding tutorial.

---

## How to Build & Install on Android

1. Open this project in **Android Studio** (or build via Gradle CLI).
2. Connect an Android device (Android 7.0+ / API 24+) or launch an Android Emulator.
3. Run:
   ```bash
   gradle :app:assembleDebug
   ```
4. Install `app/build/outputs/apk/debug/app-debug.apk` on your Android device.

---

## Rebuild APK with GitHub Actions

1. Push this repository to GitHub (`main` or `master` branch).
2. Open the **Actions** tab on GitHub and select **Build Android APK**.
3. Each push automatically builds the APK, or click **Run workflow** to rebuild manually.
4. Download **ArfinIllustrator-debug-apk** from the workflow run's **Artifacts** section.

