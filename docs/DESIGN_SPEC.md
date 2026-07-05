# Design Specification
## KörLog
**Version:** 1.0  
**Date:** 2026-07-05  
**Author:** Ali Abdullah

---

## 1. Design Philosophy

The app should feel like a trusted co-pilot — calm, clear, and always on the driver's side. No red alerts for normal behavior. No surveillance tone. Clean, dark-friendly UI that works at night in a car.

**Keywords:** Trustworthy · Minimal · Professional · Swedish

---

## 2. Design System

### Colors

| Token | Hex | Usage |
|---|---|---|
| Primary | #2563EB | Buttons, active states, links |
| Primary Container | #EFF6FF | Chip backgrounds, subtle highlights |
| Secondary | #10B981 | Positive states, earnings, "you're good" |
| Warning | #F59E0B | Soft warnings (approaching limit) |
| Error | #EF4444 | Hard limit exceeded only |
| Background | #0F172A | App background (dark) |
| Surface | #1E293B | Cards, bottom sheets |
| Surface Variant | #334155 | Input fields, dividers |
| On Background | #F1F5F9 | Primary text |
| On Surface | #CBD5E1 | Secondary text |
| On Surface Variant | #94A3B8 | Tertiary text, labels |

### Typography (Material 3)

| Style | Font | Size | Weight | Usage |
|---|---|---|---|---|
| Display Large | Inter | 57sp | 400 | Timer display |
| Headline Large | Inter | 32sp | 700 | Screen titles |
| Headline Medium | Inter | 28sp | 600 | Section headers |
| Title Large | Inter | 22sp | 600 | Card titles |
| Title Medium | Inter | 16sp | 500 | List item titles |
| Body Large | Inter | 16sp | 400 | Body text |
| Body Medium | Inter | 14sp | 400 | Secondary body |
| Label Large | Inter | 14sp | 500 | Buttons |
| Label Medium | Inter | 12sp | 500 | Chips, tags |

### Spacing

| Token | Value |
|---|---|
| xs | 4dp |
| sm | 8dp |
| md | 16dp |
| lg | 24dp |
| xl | 32dp |
| xxl | 48dp |

### Corner Radius

| Component | Radius |
|---|---|
| Cards | 16dp |
| Buttons | 12dp |
| Chips | 8dp |
| Bottom sheet | 28dp top corners |
| Input fields | 12dp |

---

## 3. Screen Inventory

| Screen | Route | Auth Required |
|---|---|---|
| Splash | splash | No |
| Onboarding | onboarding | No |
| Login | auth/login | No |
| Register | auth/register | No |
| Dashboard | dashboard | Yes |
| Active Session | session/active | Yes |
| Session Summary | session/summary/{id} | Yes |
| History | history | Yes |
| Session Detail | history/{id} | Yes |
| Statistics | stats | Yes |
| Settings | settings | Yes |
| Pro Upgrade | pro | Yes |
| About | settings/about | Yes |

---

## 4. Navigation Structure

```
Root
├── Auth Graph
│   ├── Login
│   └── Register
└── Main Graph (Bottom Nav)
    ├── Dashboard
    │   └── Active Session (full screen overlay)
    ├── History
    │   └── Session Detail
    ├── Statistics
    └── Settings
        ├── Pro Upgrade
        └── About
```

### Bottom Navigation Items

| Tab | Icon | Label (SV) | Label (EN) |
|---|---|---|---|
| Dashboard | Home | Hem | Home |
| History | History | Historik | History |
| Statistics | BarChart | Statistik | Statistics |
| Settings | Settings | Inställningar | Settings |

---

## 5. Screen Wireframes

### 5.1 Dashboard

```
┌─────────────────────────────────┐
│  [≡]   KörLog    [👤]      │  ← TopBar
├─────────────────────────────────┤
│                                 │
│  ┌─────────────────────────┐   │
│  │  Idag · Monday 5 Jul    │   │  ← Date chip
│  │                         │   │
│  │      00 : 00 : 00       │   │  ← Large timer
│  │                         │   │
│  │   [ START DRIVING ]     │   │  ← CTA button
│  └─────────────────────────┘   │
│                                 │
│  ┌──────────┐  ┌──────────┐   │
│  │ Denna    │  │ Denna    │   │  ← Stat cards
│  │ vecka    │  │ månad    │   │
│  │ 12.5h    │  │ 48.0h    │   │
│  │ 2 340 kr │  │ 9 200 kr │   │
│  └──────────┘  └──────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │ Licenskydd              │   │  ← Compliance card
│  │ ████████░░░░  12.5/60h  │   │
│  │ ✓ Du är redo att köra   │   │
│  └─────────────────────────┘   │
│                                 │
│  Senaste pass                   │  ← Recent sessions
│  ┌─────────────────────────┐   │
│  │ Idag  08:00–12:30  340kr│   │
│  └─────────────────────────┘   │
│                                 │
└─────────────────────────────────┘
│  Hem  │ Historik │ Stat │ Inst  │  ← Bottom Nav
```

### 5.2 Active Session Screen

```
┌─────────────────────────────────┐
│           KÖRT TID              │
│                                 │
│         02 : 34 : 17            │  ← Giant timer
│                                 │
│    ┌──────────────────────┐    │
│    │  Plattform           │    │
│    │  [Uber ▼]            │    │  ← Platform picker
│    └──────────────────────┘    │
│                                 │
│    ┌──────────────────────┐    │
│    │  Intäkter (SEK)      │    │
│    │  [          ]        │    │  ← Earnings input
│    └──────────────────────┘    │
│                                 │
│    ┌──────────────────────┐    │
│    │  Körsträcka (mil)    │    │
│    │  [          ]        │    │  ← Distance input
│    └──────────────────────┘    │
│                                 │
│    ┌──────────────────────┐    │
│    │  Anteckningar        │    │
│    │  [                 ] │    │
│    └──────────────────────┘    │
│                                 │
│                                 │
│    [ AVSLUTA PASS ]             │  ← Stop button (red)
│                                 │
└─────────────────────────────────┘
```

### 5.3 History Screen

```
┌─────────────────────────────────┐
│  Historik              [Filter] │
├─────────────────────────────────┤
│  [Alla] [Uber] [Bolt] [Cab...]  │  ← Platform filter chips
│                                 │
│  Denna vecka                    │
│  ┌─────────────────────────┐   │
│  │ Mån 30 Jun              │   │
│  │ 08:00 → 14:30 · 6.5h   │   │
│  │ Uber         1 250 kr   │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │ Mån 30 Jun              │   │
│  │ 15:00 → 19:00 · 4.0h   │   │
│  │ Bolt           820 kr   │   │
│  └─────────────────────────┘   │
│                                 │
│  Förra veckan                   │
│  ┌─────────────────────────┐   │
│  │ ...                     │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
│  Hem  │ Historik │ Stat │ Inst  │
```

### 5.4 Statistics Screen

```
┌─────────────────────────────────┐
│  Statistik                      │
├─────────────────────────────────┤
│  [Vecka] [Månad] [År]           │  ← Period tabs
│                                 │
│  ┌─────────────────────────┐   │
│  │ Totalt denna vecka      │   │
│  │ 32.5 timmar             │   │
│  │ 6 840 kr                │   │
│  │ ████████████░░░ 32/60h  │   │  ← Progress bar
│  └─────────────────────────┘   │
│                                 │
│  Intäkter per plattform         │
│  ┌─────────────────────────┐   │
│  │ Uber    ████████  4 200 │   │
│  │ Bolt    █████     1 840 │   │
│  │ Övrigt  ██          800 │   │
│  └─────────────────────────┘   │
│                                 │
│  Timme-för-timme                │
│  ┌─────────────────────────┐   │
│  │  [Bar chart by day]     │   │
│  └─────────────────────────┘   │
│                                 │
│  [⬇ Exportera rapport] (PRO)   │
└─────────────────────────────────┘
│  Hem  │ Historik │ Stat │ Inst  │
```

### 5.5 Settings Screen

```
┌─────────────────────────────────┐
│  Inställningar                  │
├─────────────────────────────────┤
│  Konto                          │
│  ┌─────────────────────────┐   │
│  │ 👤 ali@email.com        │   │
│  │ Logga ut                │   │
│  └─────────────────────────┘   │
│                                 │
│  Prenumeration                  │
│  ┌─────────────────────────┐   │
│  │ ⭐ Uppgradera till PRO  │   │
│  │ 99 kr · engångsbetalning│   │
│  └─────────────────────────┘   │
│                                 │
│  Arbetstidsgränser              │
│  ┌─────────────────────────┐   │
│  │ Veckogräns (tim)    60  │   │
│  │ Månadsgräns (tim)  192  │   │
│  └─────────────────────────┘   │
│                                 │
│  App                            │
│  ┌─────────────────────────┐   │
│  │ Språk          Svenska  │   │
│  │ Notifikationer    På    │   │
│  │ Mörkt läge        På    │   │
│  └─────────────────────────┘   │
│                                 │
│  Om appen · Integritetspolicy   │
└─────────────────────────────────┘
│  Hem  │ Historik │ Stat │ Inst  │
```

---

## 6. Component Library

| Component | Description |
|---|---|
| `TimerDisplay` | Large monospaced timer (HH:MM:SS) |
| `StatCard` | Small card showing a metric (hours/earnings) |
| `ComplianceCard` | Progress bar + status message for legal hours |
| `SessionCard` | History list item (date, time, duration, earnings, platform) |
| `PlatformChip` | Selectable platform tag (Uber, Bolt, etc.) |
| `EarningsInput` | Currency-formatted text field (SEK) |
| `PeriodTabRow` | Week / Month / Year tab selector |
| `PlatformBarChart` | Horizontal bar chart for earnings by platform |
| `ProBadge` | Lock icon + "PRO" label for gated features |
| `StartStopButton` | Large primary CTA that toggles between states |

---

## 7. Tone of Voice

| Situation | Wrong (surveillance) | Right (co-pilot) |
|---|---|---|
| Approaching limit | "Warning: 45h worked" | "Starkt jobbat — 45 av 60h denna vecka" |
| Limit reached | "You exceeded legal hours" | "Du har nått veckogränsen — vila rekommenderas" |
| After 6h driving | "Break required by law" | "Du har kört 6 timmar — dags för en paus" |
| New session saved | "Session recorded" | "Pass sparat ✓" |
| Good compliance | — | "Du är redo att köra" |

---

## 8. Localization Keys (Sample)

| Key | Swedish | English |
|---|---|---|
| `dashboard.title` | Hem | Home |
| `session.start` | Starta pass | Start driving |
| `session.stop` | Avsluta pass | Stop driving |
| `stats.weekly_hours` | Timmar denna vecka | Hours this week |
| `stats.monthly_hours` | Timmar denna månad | Hours this month |
| `compliance.good` | Du är redo att köra | You're good to drive |
| `compliance.rest` | Vila rekommenderas | Rest recommended |
| `compliance.limit` | Veckogränsen nådd | Weekly limit reached |
| `history.title` | Historik | History |
| `settings.title` | Inställningar | Settings |
| `pro.upgrade` | Uppgradera till PRO | Upgrade to PRO |
