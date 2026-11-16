# Architecture (Phase 2)

Single Gradle module `:app`, namespace / applicationId `com.hayati.app`. Clean-Architecture
separation is done with packages, not with extra modules.

```
app/src/main/java/com/hayati/app/
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
    tasks/  statistics/  settings/
  domain/model/              OverviewItem / OverviewValue / OverviewEntry
```

Not created on purpose (nothing to put in them yet): `data/local`, `data/repository`,
`domain/repository`, `core/common`. They are added together with the first feature that needs
persistence.

## Key behaviors

- **Forced RTL**: `HayatiTheme` provides `LayoutDirection.Rtl` regardless of device language.
- **Unavailable vs real data**: `OverviewValue.Unavailable` is rendered as the "غير متاح بعد"
  badge. `OverviewValue.Available` is reserved for values that are actually computed/stored.
- **Navigation**: bottom-bar items use `popUpTo(start){saveState}` + `launchSingleTop` +
  `restoreState`, so tabs are not duplicated. Secondary screens (settings, worship sections) use
  `launchSingleTop` and hide the bottom bar.
- **Theme extensibility**: brand colors without a Material slot live in `HayatiExtraColors`
  (`LocalHayatiColors`); a light scheme can be added in `Theme.kt` without touching screens.
- **Typography**: letterSpacing is 0 everywhere (positive spacing breaks Arabic joining); the
  system font family is used so Android's Arabic-capable font does the shaping.

## SDK and dependency versions

| Item | Version |
|---|---|
| minSdk / targetSdk / compileSdk | 26 / 36 / 36 |
| Android Gradle Plugin | 8.9.1 |
| Kotlin (+ Compose compiler plugin) | 2.1.20 |
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

Versions are centralized in `gradle/libs.versions.toml`.

## Not used on purpose

Hilt, Room, DataStore, Firebase, analytics, ads, networking libraries, material-icons-extended.
See `docs/DECISIONS.md`.
