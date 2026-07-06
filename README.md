# KörLog

**Your time, your earnings, your way.**

A professional Android app for Swedish taxi and rideshare drivers to track working hours, log earnings, and stay on top of their week — on their own terms.

## What it does

Taxi drivers in Sweden juggle multiple platforms — Uber, Bolt, Cabonline and others — often without a clear picture of their total hours or income. Keeping that picture clear shouldn't require paperwork, spreadsheets, or guesswork.

KörLog puts the driver in control. Start a session when you begin driving, stop it when you're done, log your earnings and distance. The app handles the rest — weekly and monthly summaries, hour tracking with customisable personal goals, mileage logs for tax deductions, and earnings breakdowns by platform.

No red alerts for doing your job. Just clear, honest information that works for the driver.

## Features

- Session timer with break tracking and platform tagging (Uber, Bolt, Cabonline, etc.)
- Weekly and monthly hours overview with adjustable personal targets
- Break reminder after 6 hours of continuous driving
- Earnings tracking per session and platform
- Edit sessions after the fact — fix a typo, adjust earnings, update notes
- Session history with search and filters
- Real-time cloud sync across devices (Firebase)
- Full offline support — everything works without internet
- Swedish and English language support
- **Pro:** Körjournal (mileage log) at Skatteverket rates
- **Pro:** Moms (6% VAT) tracker
- **Pro:** PDF and CSV export for tax reporting
- **Pro:** Fleet owner view

## Tech stack

- Kotlin + Jetpack Compose
- MVVM + Clean Architecture
- Room (local database, offline-first)
- Hilt (dependency injection)
- Navigation Compose
- Firebase Auth + Firestore (real-time sync)
- WorkManager
- Google Play Billing

## Requirements

- Android 8.0 (API 26) or higher
- Google Play Services

## Project structure

```
app/src/main/java/se/w3footprint/korlog/
├── data/          # Room database, Firestore, repository implementations
├── domain/        # Models, repository interfaces, use cases
├── presentation/  # Screens, ViewModels, UI state
└── di/            # Hilt dependency injection modules
```

## Documentation

- [Product Requirements](docs/PRD.md)
- [Design Specification](docs/DESIGN_SPEC.md)
- [Architecture](docs/ARCHITECTURE.md)

## Hours reference

KörLog shows your weekly and monthly hours against default targets of 48h (average) and 60h (single week) — the standard reference points for Swedish taxi drivers. You can adjust these in Settings to match your own situation.

## License

Copyright © 2026 W3Footprint. All rights reserved.
