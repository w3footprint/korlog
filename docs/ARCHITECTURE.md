# Technical Architecture
## KörLog
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
se.w3footprint.korlog/
│
├── data/
│   ├── auth/
│   │   └── AuthRepository.kt
│   ├── local/
│   │   ├── dao/
│   │   │   └── SessionDao.kt
│   │   ├── database/
│   │   │   └── TaxiDatabase.kt
│   │   └── entity/
│   │       └── SessionEntity.kt
│   ├── remote/
│   │   └── firestore/
│   │       └── FirestoreRepository.kt
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
│   │   ├── RegisterScreen.kt
│   │   └── ForgotPasswordScreen.kt
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

**Room database name:** `taxi_database`  
**Current version:** 4

### Table: `sessions`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique session ID |
| `syncId` | TEXT | NOT NULL, DEFAULT '' | Reserved for future cloud deduplication |
| `userId` | TEXT | NOT NULL, DEFAULT '' | Firebase UID — scopes all queries per user |
| `startTime` | INTEGER | NOT NULL | Unix timestamp ms — session start |
| `endTime` | INTEGER | NOT NULL | Unix timestamp ms — session end |
| `durationMillis` | INTEGER | NOT NULL | Driving time in ms (excludes break time) |
| `breakDurationMillis` | INTEGER | NOT NULL, DEFAULT 0 | Total break time in ms |
| `earningsSek` | REAL | NOT NULL, DEFAULT 0.0 | Earnings entered by driver |
| `distanceKm` | REAL | NOT NULL, DEFAULT 0.0 | Distance driven (for körjournal) |
| `platform` | TEXT | NOT NULL, DEFAULT 'OTHER' | UBER/BOLT/CABONLINE/TAXIKURIR/SVERIGETAXI/OTHER |
| `notes` | TEXT | NOT NULL, DEFAULT '' | Optional driver notes |
| `date` | INTEGER | NOT NULL | Unix timestamp ms — day of session |

### Migration History

| Migration | Change |
|---|---|
| v1 → v2 | Added `breakDurationMillis` column |
| v2 → v3 | Added `userId` column |
| v3 → v4 | Added `syncId` column |

All migrations are additive (ALTER TABLE ADD COLUMN) — no data loss.

---

## 4. Data Flow

### Starting a Session
```
DashboardScreen
  → DashboardViewModel.startSession()
    → StartSessionUseCase(startTime, platform)
      → SessionRepository.saveActiveSession()
        → ActiveSessionStore (in-memory + DataStore)
```

### Stopping a Session
```
ActiveSessionScreen
  → ActiveSessionViewModel.confirmStop(earnings, distance, notes)
    → StopSessionUseCase(...)
      → SessionRepository.insertSession(session)   ← tagged with userId
        → SessionDao.insertSession()               ← Room (source of truth)
        → FirestoreRepository.upsertSession()      ← mirrored to Firestore
```

### Observing Stats
```
DashboardScreen observes DashboardViewModel.uiState (StateFlow)
  ← DashboardViewModel collects from GetWeeklyStatsUseCase()
    ← GetWeeklyStatsUseCase collects from SessionRepository.getAllSessions(userId)
      ← SessionDao.getAllSessions(userId) (Flow — auto-updates on DB change)
```

---

## 5. Sync Strategy

KörLog uses an offline-first approach. Room is always the source of truth.

### Login sync (bidirectional, one-time)
On sign-in, `SessionRepositoryImpl.syncFromCloud()` runs:
1. Push all local sessions (tagged with current `userId`) to Firestore
2. Pull all sessions from Firestore and insert into Room

### Real-time sync (ongoing)
After login sync, a Firestore snapshot listener runs for the duration of the session:

```kotlin
fun observeSessions(): Flow<List<SessionEntity>> = callbackFlow {
    val uid = auth.currentUser?.uid ?: run { close(); return@callbackFlow }
    val listener = sessionsCollection(uid).addSnapshotListener { snapshot, error ->
        if (error != null || snapshot == null) return@addSnapshotListener
        trySend(snapshot.documents.mapNotNull { it.toEntity(uid) })
    }
    awaitClose { listener.remove() }
}
```

The listener inserts new/updated sessions into Room and deletes sessions locally that no longer exist in Firestore. This keeps all devices in sync without polling.

### Pull-to-refresh
The history screen supports manual sync via `PullToRefreshBox`. This calls `syncFromCloud()` on demand.

### User data isolation
All Room queries are scoped by `userId`. All Firestore documents live under `users/{uid}/sessions/{id}`. Security rules enforce that `request.auth.uid == userId`.

---

## 6. Navigation Graph

```
NavHost
├── authGraph (startDestination = login)
│   ├── login
│   ├── register
│   └── forgotPassword
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
Hidden on: `login`, `register`, `forgotPassword`, `activeSession`

---

## 7. State Management

Each screen has its own `UiState` data class:

```kotlin
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
| `DatabaseModule` | `TaxiDatabase`, `SessionDao` |
| `RepositoryModule` | `SessionRepository` (bound to `SessionRepositoryImpl`) |
| `FirebaseModule` | `FirebaseAuth`, `FirebaseFirestore` |

All ViewModels use `@HiltViewModel`. All use cases are plain classes injected via constructor.

---

## 9. Background Work

| Worker | Trigger | Action |
|---|---|---|
| `BreakReminderWorker` | Scheduled when session starts | Notify after 6h continuous driving |

Cloud sync uses Firestore's real-time listener (not WorkManager). No background polling job is needed.

---

## 10. Security

- Firebase Security Rules: users can only read/write their own documents (`request.auth.uid == userId`)
- All Room queries include `userId` parameter — no cross-user data access
- No raw SQL — all queries through Room DAOs
- ProGuard/R8 enabled on release builds
- No sensitive data logged in production (BuildConfig.DEBUG guard)
- API keys in `local.properties`, never committed to git

---

## 11. Testing Strategy

| Layer | Tool | What is tested |
|---|---|---|
| Repository | JUnit 4 + MockK | Data isolation per user, sync logic, delete guards |
| Use Cases | JUnit 4 + MockK | Business logic, hour limit calculations |
| ViewModel | JUnit 4 + Turbine | UiState transitions, coroutine flows |
| UI | Compose UI Test | Critical user flows (start session, stop session) |

---

## 12. Build Variants

| Variant | App ID suffix | Description |
|---|---|---|
| debug | `.debug` | Local dev, logging enabled, Crashlytics disabled |
| release | — | Minified, signed, Crashlytics enabled |
