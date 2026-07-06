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
| Secondary | #10B981 | Positive states, earnings, "all clear" |
| Warning | #F59E0B | Soft nudges (approaching target) |
| Error | #EF4444 | Stop button, destructive actions only |
| Background | #0F172A | App background (dark) |
| Surface | #1E293B | Cards, bottom sheets |
| Surface Variant | #334155 | Input fields, dividers |
| On Background | #F1F5F9 | Primary text |
| On Surface | #CBD5E1 | Secondary text |
| On Surface Variant | #94A3B8 | Tertiary text, labels |

### Typography (Material 3)

| Style | Font | Size | Weight | Usage |
|---|---|---|---|---|
| Display Large | Inter | 57sp | 400 | Timer display (auto-shrinks on small screens) |
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
| Buttons | 12–14dp |
| Chips | 20dp (pill) |
| Bottom sheet | 28dp top corners |
| Input fields | 12dp |

---

## 3. Screen Inventory

| Screen | Route | Auth Required |
|---|---|---|
| Splash | splash | No |
| Login | auth/login | No |
| Register | auth/register | No |
| Forgot Password | auth/forgot | No |
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
│   ├── Register
│   └── Forgot Password
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
│  [≡]   KörLog    [👤]           │  ← TopBar
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
│  │ Dina timmar             │   │  ← Hours card
│  │ ████████░░░░  12.5/60h  │   │
│  │ ✓ Klart — kör på        │   │
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
│           KÖRTID                │
│                                 │
│         02 : 34 : 17            │  ← Giant timer (auto-shrinks)
│                                 │
│    [ ☕ Ta en paus ]            │  ← Break button
│                                 │
│    [ ■ Avsluta pass ]           │  ← Stop button (red)
│                                 │
└─────────────────────────────────┘

On stop → bottom sheet slides up:
┌─────────────────────────────────┐
│  Avsluta passet                 │
│  02:34:17                       │
├─────────────────────────────────┤
│  Intäkter (SEK)  [        ]    │
│  Körsträcka (km) [        ]    │
│  Plattform: [Uber] [Bolt] ...  │
│  Anteckningar    [        ]    │
│                                 │
│  [ SPARA PASSET ]               │
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

Pull-to-refresh supported — dragging down syncs from cloud.

### 5.4 Statistics Screen

```
┌─────────────────────────────────┐
│  Statistik                      │
├─────────────────────────────────┤
│  [Vecka] [Månad] [År]           │  ← Period tabs (smooth crossfade)
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
│  Timmar per dag                 │
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
│  │ Veckogräns (tim)    60  │   │  ← Adjustable
│  │ Månadsgräns (tim)  192  │   │  ← Adjustable
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
| `StatCard` | Small card showing a metric (hours/earnings) |
| `ComplianceCard` | Progress bar + status message for hours overview |
| `SessionCard` | History list item (date, time, duration, earnings, platform) |
| `PlatformChip` | Selectable platform tag (Uber, Bolt, etc.) |
| `EarningsInput` | Currency-formatted text field (SEK) |
| `PeriodTabRow` | Week / Month / Year tab selector |
| `PlatformBarChart` | Horizontal bar chart for earnings by platform |
| `ProBadge` | Lock icon + "PRO" label for gated features |
| `StartStopButton` | Large primary CTA that toggles between states |

---

## 7. Tone of Voice

| Situation | Surveillance (avoid) | Co-pilot (use) |
|---|---|---|
| All clear | "No violations" | "Klart — kör på" / "All clear — keep going" |
| Approaching 48h | "Warning: 45h worked" | "48h+ den här veckan — bra jobbat, överväg en paus" |
| Weekly target reached | "You exceeded the limit" | "Veckans mål nått — vila rekommenderas" / "Weekly goal reached" |
| 60h reached | "Stop driving — legal limit" | "60h nådd — du har förtjänat en ordentlig vila" |
| Break suggestion | "Break required by law" | "Du har kört 6 timmar — en 30-minuterspaus håller dig skarp" |
| Session saved | "Session recorded" | "Passet sparat! Bra jobbat." |
| Auth tagline | "Track hours to stay legal" | "Din tid, dina intäkter, dina villkor" |
| About tagline | — | "Byggd för svenska förare som värdesätter sin tid" |

---

## 8. Localization Keys (Sample)

| Key | Swedish | English |
|---|---|---|
| `auth_tagline` | Din tid, dina intäkter, dina villkor | Your time, your earnings, your way |
| `compliance_title` | Dina timmar | Your hours |
| `compliance_good` | Klart — kör på | All clear — keep going |
| `compliance_rest` | Dags att ladda om | Time to recharge |
| `compliance_limit` | Veckans mål nått | Weekly goal reached |
| `compliance_hard_limit_warning` | 60h nådd — du har förtjänat en ordentlig vila | 60h reached — you've earned a proper rest |
| `about_tagline` | Byggd för svenska förare som värdesätter sin tid | Built for Swedish drivers who value their time |
| `session_start` | Starta pass | Start driving |
| `session_stop` | Avsluta pass | Stop driving |
| `history_title` | Historik | History |
| `stats_title` | Statistik | Statistics |
| `settings_title` | Inställningar | Settings |
| `pro_upgrade` | Uppgradera till PRO | Upgrade to PRO |
