# TaxiWorkTracker

An Android app for Swedish taxi and rideshare drivers to track working hours, monitor earnings, and stay within legal driving time limits.

## What it does

Taxi drivers in Sweden work across multiple platforms — Uber, Bolt, Cabonline and others — often without a clear picture of their total hours or income. Swedish law sets strict limits on driving time, and violations can cost a driver their license.

TaxiWorkTracker keeps all of that in one place. Start a session when you begin driving, stop it when you're done, log your earnings and distance. The app handles the rest — weekly and monthly summaries, legal hour tracking, mileage logs for tax deductions, and VAT calculations ready for Skatteverket.

The app is built for the driver, not against them. No surveillance, no alarms for normal behavior. Just clear, honest information that helps drivers protect their license and understand their income.

## Features

- Session timer with platform tagging (Uber, Bolt, Cabonline, etc.)
- Weekly and monthly hours vs. Swedish legal limits (Vägarbetstidslagen 2005:395)
- Break reminder after 6 hours of continuous driving
- Earnings tracking per session and platform
- Session history with filters
- **Pro:** Körjournal (mileage log) at Skatteverket rates
- **Pro:** Moms (6% VAT) tracker
- **Pro:** PDF and CSV export for tax reporting
- **Pro:** Fleet owner view
- Cloud backup with Firebase, offline-first
- Swedish and English language support

## Tech stack

- Kotlin + Jetpack Compose
- MVVM + Clean Architecture
- Room (local database)
- Hilt (dependency injection)
- Navigation Compose
- Firebase Auth + Firestore
- WorkManager
- Google Play Billing

## Requirements

- Android 8.0 (API 26) or higher
- Google Play Services

## Project structure

```
app/src/main/java/se/w3footprint/taxiworktracker/
├── data/          # Room database, Firestore, repository implementations
├── domain/        # Models, repository interfaces, use cases
├── presentation/  # Screens, ViewModels, UI state
└── di/            # Hilt dependency injection modules
```

## Documentation

- [Product Requirements](docs/PRD.md)
- [Design Specification](docs/DESIGN_SPEC.md)
- [Architecture](docs/ARCHITECTURE.md)

## Legal context

This app is designed around Swedish working time regulations for taxi drivers under **Vägarbetstidslagen (2005:395)**, implementing EU Directive 2002/15/EC:

- 48h/week average limit (measured over 4 months)
- 60h single-week hard cap
- 11h minimum daily rest
- Mandatory break after 6 hours

## License

Copyright © 2026 W3Footprint. All rights reserved.
