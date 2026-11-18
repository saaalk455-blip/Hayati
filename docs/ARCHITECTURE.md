# Architecture (Phase 4)

Single Gradle module `:app`, namespace / applicationId `com.hayati.app`. Clean-Architecture
separation is done with packages, not with extra modules.

```
app/src/main/java/com/hayati/app/
  HayatiApplication.kt       creates AppContainer
  AppContainer.kt            manual DI: database, repository, TimeProvider
  AppViewModelProvider.kt    ViewModel factory (HomeViewModel, TasksViewModel)
  MainActivity.kt            edge-to-edge, sets HayatiTheme { HayatiApp() }
  HayatiApp.kt               Scaffold + bottom bar (shown only on the 4 main screens)
  navigation/                HayatiRoutes, TopLevelDestination, HayatiNavHost
  core/designsystem/
    theme/                   Color, Type, Shape/Spacing, Theme (forced RTL)
    components/              HayatiCard, Headers, StateComponents, CrescentDecoration
    icons/                   HayatiIcons (original path-based icons)
  feature/
    home/                    HomeScreen, HomeViewModel (StateFlow)
    worship/                 WorshipScreen, WorshipSection, placeholder screen
    tasks/                   TasksScreen, TasksViewModel (add/edit/complete/delete)
    statistics/  settings/
  domain/model/              Overview*, Task, TaskTitle
  domain/repository/         TaskRepository
  data/local/                TaskEntity, TaskDao, HayatiDatabase (Room, v1)
  data/repository/           TaskRepositoryImpl
  core/common/               TimeProvider, SystemTimeProvider
```

Data flow: `TasksScreen` -> `TasksViewModel` (StateFlow) -> `TaskRepository` -> `TaskDao` -> Room.
Tasks (Phase 3) are scoped to a calendar day (`epochDay`); `refreshDay()` runs on ON_START.

## Key behaviors

- **Forced RTL**: `HayatiTheme` provides `LayoutDirection.Rtl` regardless of device language.
- **Unavailable vs real data**: `OverviewValue.Unavailable` is rendered as the "غير متاح بعد"
  badge. `OverviewValue.TasksProgress` is the only real value so far (read from Room).
- **Navigation**: bottom-bar items use `popUpTo(start){saveState}` + `launchSingleTop` +
  `restoreState`, so tabs are not duplicated. Secondary screens (settings, worship sections) use
  `launchSingleTop` and hide the bottom bar.
- **Theme extensibility**: brand colors without a Material slot live in `HayatiExtraColors`
  (`LocalHayatiColors`); a light scheme can be added in `Theme.kt` without touching screens.
- **Typography**: letterSpacing is 0 everywhere (positive spacing breaks Arabic joining); the
  system font family is used so Android's Arabic-capable font does the shaping.

## Prayer system (Phase 4)

```
domain/prayer/model        Prayer, PrayerEvent, PrayerLocation, PrayerSettings, PrayerTimes,
                           PrayerLog, PrayerSnapshot (Loading|NotConfigured|Error|Ready), PlannedAlert
domain/prayer/repository   PrayerTimeCalculator, PrayerSettingsRepository, PrayerLogRepository,
                           PrayerAlarmScheduler, NotificationPermissionChecker
domain/prayer/usecase      PrayerSnapshotProvider (single source of truth), NextPrayerResolver,
                           AlertPlanner, HistoryBuilder, ManualLocationValidator
data/prayer                AdhanPrayerTimeCalculator (adhan2), PrayerLogEntity/Dao/RepositoryImpl,
                           DataStorePrayerSettingsRepository, AndroidLocationProvider
core/notifications         PrayerNotificationCoordinator, AlarmManagerPrayerScheduler,
                           PrayerNotificationPresenter, NotificationContent, PrayerAlarmReceiver,
                           PrayerSystemEventReceiver, PrayerMaintenanceWorker, PrayerAutomation
feature/prayer             PrayerScreen/ViewModel, PrayerHistoryScreen/ViewModel,
                           PrayerSettingsScreen/ViewModel
```

- Data flow: settings (DataStore) + logs (Room) + a shared 1-second ticker ->
  `PrayerSnapshotProvider.observe()` -> `PrayerViewModel` and `HomeViewModel`. Home and the Prayer
  screen therefore always agree. The ticker is `shareIn(WhileSubscribed)` and screens collect with
  lifecycle awareness, so nothing ticks in the background.
- Prayer day = local date in the location's own IANA timezone, re-derived on every tick; tracking
  stores that date as `epochDay`.
- Notifications: `PrayerAutomation` watches settings and calls the coordinator, which cancels
  yesterday..+2 days and schedules today and tomorrow with `AlarmManager`. Receivers reschedule on
  boot, timezone change, time change, app update and exact-alarm permission change. WorkManager
  runs only a 12-hour maintenance reschedule. Details: `docs/PRAYER_SYSTEM.md`.
- Database is version 2 (`prayer_logs` added by `MIGRATION_1_2`); see `docs/DATABASE_SCHEMA.md`.
- Settings screen links to Prayer settings; routes `prayer/settings` and `prayer/history`.

## SDK and dependency versions

| Item | Version |
|---|---|
| minSdk / targetSdk / compileSdk | 26 / 36 / 36 |
| Android Gradle Plugin | 8.9.1 |
| Kotlin (+ Compose compiler plugin) | 2.1.21 |
| KSP | 2.1.21-2.0.2 |
| Room | 2.7.1 |
| Gradle (wrapper) | 8.14.3 |
| JDK (CI) | Temurin 17 |
| Compose BOM | 2025.04.01 |
| Material 3 / Compose UI | from the BOM |
| activity-compose | 1.10.1 |
| navigation-compose | 2.8.9 |
| lifecycle (runtime-compose, viewmodel-compose) | 2.8.7 |
| core-ktx | 1.16.0 |
| kotlinx-coroutines-android | 1.10.1 |
| JUnit (tests) | 4.13.2 |
| Robolectric (DB tests) | 4.14.1 |
| androidx.test core / coroutines-test | 1.6.1 / 1.10.1 |

Versions are centralized in `gradle/libs.versions.toml`.

## Not used on purpose

Hilt (D-010), DataStore, Firebase, analytics, ads, networking libraries, material-icons-extended.
See `docs/DECISIONS.md`.
