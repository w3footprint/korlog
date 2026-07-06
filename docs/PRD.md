# Product Requirements Document
## KörLog
**Version:** 1.0  
**Date:** 2026-07-05  
**Author:** Ali Abdullah  
**Company:** W3Footprint

---

## 1. Problem Statement

Taxi and rideshare drivers in Sweden work across multiple platforms — Uber, Bolt, Cabonline, TaxiKurir, Sverigetaxi — often without a clear picture of their total working hours, earnings, or time on the road. At the same time, self-employed drivers are responsible for their own tax reporting (F-skatt, moms, körjournal) to Skatteverket.

There is currently no app on the Swedish market that combines working hour tracking with Swedish-specific tax support. Drivers either rely on paper logs, spreadsheets, or nothing at all.

KörLog solves this by giving drivers a simple, private tool that tracks their time and money — so they can focus on driving, not paperwork. The driver is always in control: they can edit sessions after the fact, adjust their personal hour targets, and decide what data to export.

---

## 2. Goal

Build a professional Android application that helps Swedish taxi and rideshare drivers:
- Track working hours across sessions and platforms
- Stay on top of their weekly and monthly targets — on their own terms
- Monitor earnings and prepare for tax reporting
- Feel in control of their own data — not monitored or judged

---

## 3. Target Users

### Primary — Solo Driver (Self-employed)
- Drives for one or multiple platforms (Uber, Bolt, Cabonline, etc.)
- Registered with F-skatt
- Needs to track hours and income for planning and Skatteverket
- May not be tech-savvy — needs a simple, clear interface
- Language: Swedish preferred, English acceptable

### Secondary — Fleet Owner
- Owns or manages a small number of drivers
- Wants an overview of driver hours and earnings
- Needs reporting for administrative purposes

---

## 4. Core Principles

- **Co-pilot, not inspector.** The app works for the driver, not against them. Every message, label, and notification should feel supportive, never punishing.
- **Driver in control.** Sessions can be edited after the fact. Hour targets are adjustable. Nothing is locked or automatic without the driver's action.
- **Privacy first.** Driver data is never shared without explicit action. GDPR compliant by design.
- **Offline first.** The app works fully without internet. Cloud sync is a bonus, not a dependency.
- **Simple over clever.** If a feature adds confusion, it doesn't ship.

---

## 5. Hours Reference

KörLog's default hour targets are based on Swedish working time regulations for taxi drivers under Vägarbetstidslagen (2005:395), implementing EU Directive 2002/15/EC. They are shown as informational guides — not hard stops.

| Rule | Limit | Source |
|---|---|---|
| Weekly hours (average over 4 months) | 48 hours | EU Directive 2002/15/EC |
| Weekly hours (single week) | 60 hours | Vägarbetstidslagen 2005:395 |
| Daily rest | Minimum 11 hours | Vägarbetstidslagen 2005:395 |
| Break suggestion | After 6 hours of driving | Vägarbetstidslagen 2005:395 |
| VAT on taxi fares | 6% moms | Skatteverket |
| Mileage deduction rate | 25 SEK/mil (own car) | Skatteverket 2025 |

Drivers can adjust the weekly and monthly targets in Settings to match their own situation.

---

## 6. Feature Scope

### v1.0 — Free Tier

| Feature | Description |
|---|---|
| Session tracking | Start/stop timer per driving session |
| Break tracking | Pause/resume within a session; break time excluded from driving time |
| Earnings entry | Log earnings per session in SEK |
| Platform tagging | Tag session to Uber, Bolt, Cabonline, etc. |
| Edit sessions | Edit earnings, notes, distance after saving |
| Hours overview | Weekly and monthly hours vs. adjustable personal targets |
| Break reminder | Notification after 6 hours of continuous driving |
| Hours status | Clear "all clear" / "rest recommended" indicator |
| Session history | Scrollable session log with platform filter |
| Pull-to-refresh | Manual sync trigger on history screen |
| Weekly/monthly summary | Hours and earnings totals |
| Swedish + English UI | Full localization |
| Local storage | All data stored on device with Room (offline-first) |
| Account / login | Firebase Auth (email + Google Sign-in) |
| Cloud sync | Real-time Firestore sync across devices, tied to account |

### v1.0 — Pro Tier (one-time purchase ~99 SEK)

| Feature | Description |
|---|---|
| Unlimited history | All sessions, no 30-day cap |
| Körjournal | Auto mileage log at 25 SEK/mil, exportable PDF |
| Moms tracker | 6% VAT calculation on earnings |
| Export reports | PDF and CSV for Skatteverket |
| Multi-platform income split | Breakdown by platform |
| Monthly F-skatt summary | Income summary ready for declaration |
| Fleet owner view | Overview of multiple drivers |

### Future (v2+)

- Expense tracking (fuel, maintenance, car wash)
- Annual tax summary
- Home screen widget (quick-start timer)
- Skatteverket integration (if API becomes available)
- Smart tachograph data import

---

## 7. Out of Scope (v1.0)

- Trip-by-trip tracking (only session-level)
- Route or GPS tracking
- Integration with Uber/Bolt APIs
- Payroll or invoice generation
- Web app or iOS version

---

## 8. Monetization

- **Free tier:** Core tracking features, 30-day history
- **Pro tier:** One-time purchase of ~99 SEK via Google Play Billing
- **No ads** — a professional work tool should not have advertising

---

## 9. Non-Functional Requirements

| Requirement | Target |
|---|---|
| Crash-free rate | > 99% |
| App startup time | < 2 seconds (cold start) |
| Offline functionality | 100% — no internet required for core features |
| GDPR compliance | Full — data deletion on request, no third-party sharing |
| Min Android version | API 26 (Android 8.0) |
| Target SDK | API 35 (Android 15) |
| Languages | Swedish (primary), English |

---

## 10. Success Metrics (Post-Launch)

- 500 installs within first 3 months
- 4.0+ rating on Google Play
- < 1% crash rate
- 10% free-to-Pro conversion rate

---

## 11. Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Local database | Room |
| Dependency injection | Hilt |
| Navigation | Navigation Compose |
| State management | ViewModel + StateFlow |
| Auth | Firebase Auth |
| Cloud sync | Firestore (real-time listener) |
| Analytics | Firebase Analytics |
| Crash reporting | Firebase Crashlytics |
| Preferences | DataStore |
| In-app purchases | Google Play Billing |
| Build | Gradle with version catalog |
