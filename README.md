# NEXUS X6 Gaming Hub

A responsive Indonesian-language gaming companion dashboard designed for POCO X6 / 120 Hz gaming sessions.

## Features
- Balanced, Smooth Feel, and Focus presets (recommendations only)
- Crosshair preview with six shapes, six colors, and adjustable size
- Visual tuning preview for saturation, contrast, and clarity
- Voice preset/pitch controls as a UI prototype
- Impact and haptic comfort controls as a UI prototype
- GitHub Pages deployment through GitHub Actions

## Important limitations
This is a static browser app. It does **not** modify Android system settings, increase the display beyond its 120 Hz hardware limit, increase game FPS, overlay a crosshair on top of other games, process microphone audio in real time, or change game audio/haptics globally. Those functions require native Android implementation and suitable permissions/APIs. Use in-game and official device settings for real changes.

## Run locally
Open `index.html` in a modern browser.

## Publish
The workflow at `.github/workflows/deploy-pages.yml` deploys the repository to GitHub Pages when pushed to `main`. In repository Settings → Pages, select **GitHub Actions** as the source.
