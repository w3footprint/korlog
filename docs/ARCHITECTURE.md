# Technical Architecture
## TaxiWorkTracker
**Version:** 1.0  
**Date:** 2026-07-05  
**Author:** Ali Abdullah

---

## 1. Architecture Pattern

**MVVM + Clean Architecture**

The app is split into three independent layers. Each layer only communicates with the layer directly below it. UI never touches the database. Business logic never touches Android framework classes.

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │
│   Composables → ViewModels → UI State       │
├─────────────────────────────────────────────┤
│               Domain Layer                  │
│   Use Cases → Repository Interfaces → Models│
├─────────────────────────────────────────────┤
│                Data Layer                   │
│   Room (local) ←→ Firestore (remote)        │
│   Repository Implementations                │
└─────────────────────────────────────────────┘
```

### Key Rules
- `Composables` only observe `UiState` from `ViewModel` — no direct data access
- `ViewModels` only call `UseCases` — never DAOs or Firestore directly
- `UseCases` contain all business logic — they are pure Kotlin, fully testable
- `Repositories` abstract the data source — ViewModel doesn't know if data came from Room or Firestore
- `Room` is the single source of truth — Firestore is the sync mirror

---

## 2. Package Structure

```
se.w3footprint.taxiworktracker/
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── SessionDao.kt
│   │   ├── database/
│   │   │   └── TaxiDatabase.kt
│   │   └── entity/
│   │       └── SessionEntity.kt
│   ├── remote/
│   │   └── firestore/
│   │       └── FirestoreSessionSource.kt
│   └── repository/
│       └── SessionRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── DrivingSession.kt
│   │   ├── WorkStats.kt
│   │   └── ComplianceStatus.kt
│   ├── repository/
│   │   └── SessionRepository.kt
│   └── usecase/
│       ├── session/
│       │   ├── StartSessionUseCase.kt
│       │   ├── StopSessionUseCase.kt
│       │   ├── DeleteSessionUseCase.kt
│       │   └── GetSessionByIdUseCase.kt
│       └── stats/
│           ├── GetWeeklyStatsUseCase.kt
│           ├── GetMonthlyStatsUseCase.kt
│           └── GetComplianceStatusUseCase.kt
│
├── presentation/
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt
│   ├── dashboard/
│   │   ├── DashboardViewModel.kt
│   │   ├── DashboardScreen.kt
│   │   └── DashboardUiState.kt
│   ├── session/
│   │   ├── ActiveSessionViewModel.kt
│   │   ├── ActiveSessionScreen.kt
│   │   ├── SessionSummaryScreen.kt
│   │   └── ActiveSessionUiState.kt
│   ├── history/
│   │   ├── HistoryViewModel.kt
│   │   ├── HistoryScreen.kt
│   │   ├── SessionDetailScreen.kt
│   │   └── HistoryUiState.kt
│   ├── stats/
│   │   ├── StatsViewModel.kt
│   │   ├── StatsScreen.kt
│   │   └── StatsUiState.kt
│   ├── settings/
│   │   ├── SettingsViewModel.kt
│   │   ├── SettingsScreen.kt
│   │   ├── ProUpgradeScreen.kt
│   │   └── AboutScreen.kt
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   └── common/
│       ├── components/
│       │   ├── TimerDisplay.kt
│       │   ├── StatCard.kt
│       │   ├── ComplianceCard.kt
│       │   ├── SessionCard.kt
│       │   ├── PlatformChip.kt
│       │   └── ProBadge.kt
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
│
└── di/
    ├── DatabaseModule.kt
    ├── RepositoryModule.kt
    └── FirebaseModule.kt
```

---

## 3. Database Schema

### Table: `sessions`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique session ID |
| `start_time` | INTEGER | NOT NULL | Unix timestamp ms — session start |
| `end_time` | INTEGER | NOT NULL | Unix timestamp ms — session end |
| `duration_millis` | INTEGER | NOT NULL | endTime - startTime in ms |
| `earnings_sek` | REAL | NOT NULL, DEFAULT 0.0 | Earnings entered by driver |
| `distance_km` | REAL | NOT NULL, DEFAULT 0.0 | Distance driven (for körjournal) |
| `platform` | TEXT | NOT NULL, DEFAULT 'OTHER' | UBER/BOLT/CABONLINE/TAXIKURIR/SVERIGETAXI/OTHER |
| `notes` | TEXT | DEFAULT '' | Optional driver notes |
| `date` | INTEGER | NOT NULL | Unix timestamp ms — day of session |
| `synced_to_cloud` | INTEGER | NOT NULL, DEFAULT 0 | 0 = local only, 1 = synced |
| `remote_id` | TEXT | NULLABLE | Firestore document ID |

### Table: `active_session` (single-row state table)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY DEFAULT 1 | Always row ID 1 |
| `start_time` | INTEGER | NOT NULL | When the current session started |
| `platform` | TEXT | NOT NULL | Platform selected for this session |

This table survives app kills — if the app crashes mid-session, the session is recovered on next launch.

---

## 4. Data Flow

### Starting a Session
```
DashboardScreen
  → DashboardViewModel.startSession()
    → StartSessionUseCase(startTime, platform)
      → SessionRepository.saveActiveSession()
        → ActiveSessionDao.insert()   ← persisted immediately
```

### Stopping a Session
```
ActiveSessionScreen
  → ActiveSessionViewModel.stopSession(earnings, distance, notes)
    → StopSessionUseCase(...)
      → SessionRepository.finalizeSession()
        → SessionDao.insert(session)        ← save to history
        → ActiveSessionDao.clear()          ← clear active state
        → FirestoreSessionSource.sync()     ← background cloud sync
```

### Observing Stats
```
DashboardScreen observes DashboardViewModel.uiState (StateFlow)
  ← DashboardViewModel collects from GetWeeklyStatsUseCase()
    ← GetWeeklyStatsUseCase collects from SessionRepository.getSessionsByDateRange()
      ← SessionDao.getSessionsByDateRange() (Flow — auto-updates on DB change)
```

---

## 5. Offline-First Strategy

1. All writes go to Room first — never directly to Firestore
2. A background sync worker (WorkManager) checks for unsynced sessions (`synced_to_cloud = 0`) and pushes them to Firestore
3. On first login / app restore, Firestore data is pulled and merged into Room
4. Conflict resolution: `start_time` is the unique key — duplicates are ignored

---

## 6. Navigation Graph

```
NavHost
├── authGraph (startDestination = login)
│   ├── login
│   └── register
└── mainGraph (startDestination = dashboard)
    ├── dashboard
    │   └── activeSession (full screen)
    │       └── sessionSummary/{sessionId}
    ├── history
    │   └── sessionDetail/{sessionId}
    ├── stats
    └── settings
        ├── proUpgrade
        └── about
```

### Bottom Navigation
Shown on: `dashboard`, `history`, `stats`, `settings`  
Hidden on: `login`, `register`, `activeSession`

---

## 7. State Management

Each screen has its own `UiState` sealed class or data class:

```kotlin
// Example: DashboardUiState
data class DashboardUiState(
    val isLoading: Boolean = true,
    val isSessionActive: Boolean = false,
    val activeSessionDuration: Long = 0L,
    val weeklyStats: WorkStats = WorkStats.empty(),
    val monthlyStats: WorkStats = WorkStats.empty(),
    val complianceStatus: ComplianceStatus = ComplianceStatus.default(),
    val recentSessions: List<DrivingSession> = emptyList(),
    val error: String? = null
)
```

ViewModels expose `StateFlow<UiState>`. Composables collect with `collectAsStateWithLifecycle()`.

---

## 8. Dependency Injection (Hilt)

| Module | Provides |
|---|---|
| `DatabaseModule` | `TaxiDatabase`, `SessionDao`, `ActiveSessionDao` |
| `RepositoryModule` | `SessionRepository` (bound to `SessionRepositoryImpl`) |
| `FirebaseModule` | `FirebaseAuth`, `FirebaseFirestore` |

All ViewModels use `@HiltViewModel`. All use cases are plain classes injected via constructor.

---

## 9. Background Work (WorkManager)

| Worker | Trigger | Action |
|---|---|---|
| `CloudSyncWorker` | Every 15 min (when connected) | Push unsynced sessions to Firestore |
| `BreakReminderWorker` | Scheduled when session starts | Notify after 6h continuous driving |

---

## 10. Security

- Firebase Security Rules: users can only read/write their own documents (`request.auth.uid == userId`)
- No raw SQL — all queries through Room DAOs
- ProGuard/R8 enabled on release builds
- No sensitive data logged in production (BuildConfig.DEBUG guard)
- API keys in `local.properties`, never committed to git

---

## 11. Testing Strategy

| Layer | Tool | What is tested |
|---|---|---|
| Use Cases | JUnit 4 + MockK | Business logic, hour limit calculations, compliance rules |
| Repository | JUnit 4 + Room in-memory | DAO queries, data mapping |
| ViewModel | JUnit 4 + Turbine | UiState transitions, coroutine flows |
| UI | Compose UI Test | Critical user flows (start session, stop session) |

---

## 12. Build Variants

| Variant | App ID suffix | Description |
|---|---|---|
| debug | `.debug` | Local dev, logging enabled, Crashlytics disabled |
| release | — | Minified, signed, Crashlytics enabled |
