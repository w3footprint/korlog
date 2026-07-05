# Product Requirements Document
## TaxiWorkTracker
**Version:** 1.0  
**Date:** 2026-07-05  
**Author:** Ali Abdullah  
**Company:** W3Footprint

---

## 1. Problem Statement

Taxi and rideshare drivers in Sweden work across multiple platforms — Uber, Bolt, Cabonline, TaxiKurir, Sverigetaxi — often without a clear picture of their total working hours, earnings, or legal standing. Swedish law (Vägarbetstidslagen 2005:395) sets strict limits on driver working hours, and violations can result in losing a taxi license. At the same time, self-employed drivers are responsible for their own tax reporting (F-skatt, moms, körjournal) to Skatteverket.

There is currently no app on the Swedish market that combines working hour compliance with Swedish-specific tax tracking. Drivers either rely on paper logs, spreadsheets, or nothing at all.

TaxiWorkTracker solves this by giving drivers a simple, private tool that tracks their time and money — so they can focus on driving, not paperwork.

---

## 2. Goal

Build a professional Android application that helps Swedish taxi and rideshare drivers:
- Track working hours across sessions and platforms
- Stay within legal working hour limits
- Monitor earnings and prepare for tax reporting
- Feel in control of their own data — not monitored or judged

---

## 3. Target Users

### Primary — Solo Driver (Self-employed)
- Drives for one or multiple platforms (Uber, Bolt, Cabonline, etc.)
- Registered with F-skatt
- Needs to track hours for legal compliance and income for Skatteverket
- May not be tech-savvy — needs a simple, clear interface
- Language: Swedish preferred, English acceptable

### Secondary — Fleet Owner
- Owns or manages a small number of drivers
- Wants an overview of driver hours and earnings
- Needs reporting for administrative purposes

---

## 4. Core Principles

- **Co-pilot, not inspector.** The app works for the driver, not against them. Every message, label, and notification should feel supportive, never punishing.
- **Privacy first.** Driver data is never shared without explicit action. GDPR compliant by design.
- **Offline first.** The app works fully without internet. Cloud sync is a bonus, not a dependency.
- **Simple over clever.** If a feature adds confusion, it doesn't ship.

---

## 5. Legal & Compliance Context

| Rule | Limit | Source |
|---|---|---|
| Weekly hours (average over 4 months) | 48 hours | EU Directive 2002/15/EC |
| Weekly hours (single week hard cap) | 60 hours | Vägarbetstidslagen 2005:395 |
| Daily rest | Minimum 11 hours | Vägarbetstidslagen 2005:395 |
| Break requirement | After 6 hours of driving | Vägarbetstidslagen 2005:395 |
| VAT on taxi fares | 6% moms | Skatteverket |
| Mileage deduction rate | 25 SEK/mil (own car) | Skatteverket 2025 |

---

## 6. Feature Scope

### v1.0 — Free Tier

| Feature | Description |
|---|---|
| Session tracking | Start/stop timer per driving session |
| Earnings entry | Log earnings per session in SEK |
| Platform tagging | Tag session to Uber, Bolt, Cabonline, etc. |
| Legal hour dashboard | Weekly and monthly hours vs. legal limits |
| Break reminder | Notification after 6 hours of continuous driving |
| Compliance status | Clear "you're good" / "rest recommended" indicator |
| Session history | Last 30 days of sessions |
| Weekly/monthly summary | Hours and earnings totals |
| Swedish + English UI | Full localization |
| Local storage | All data stored on device with Room |
| Account / login | Firebase Auth (Google Sign-in + email) |
| Cloud backup | Firestore sync tied to account |

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
| Cloud sync | Firestore |
| Analytics | Firebase Analytics |
| Crash reporting | Firebase Crashlytics |
| Preferences | DataStore |
| In-app purchases | Google Play Billing |
| Build | Gradle with version catalog |
