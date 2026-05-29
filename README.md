# NourishCycle 🌿

A beautiful Android fertility diet tracker built with Jetpack Compose and Material 3. Displays a personalised 7-day meal plan, tracks daily completions, and surfaces weekly insights through animated charts and KPIs.

---

## Screenshots

> Install the APK from [`app/build/outputs/apk/debug/app-debug.apk`](app/build/outputs/apk/debug/app-debug.apk) to try it directly.

---

## Features

| Feature | Detail |
|---------|--------|
| **Time-based greeting** | Good Morning / Afternoon / Evening, refreshed live |
| **7-day diet plan** | Swipeable `HorizontalPager` — today's plan opened by default |
| **Glassmorphism tiles** | Custom gradient + border simulation, works on all API 28+ devices |
| **Animated checkboxes** | Spring-animated checkmark drawing per meal slot |
| **Stagger animations** | Meal cards stagger in on first visit; instant on revisit |
| **Insights dashboard** | Bar chart (Vico), custom donut chart, streak, best day, trend KPI |
| **Trend KPI** | Uptrend / Downtrend / Flat / Insufficient — like a stock ticker |
| **Exact meal notifications** | 7 creative notifications daily at exact meal times via `AlarmManager` |
| **Boot persistence** | Alarms rescheduled automatically after device reboot |
| **Cycle start date** | User-configurable via Material 3 `DatePicker` bottom sheet |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 (BOM 2025.05.01) |
| Charts | Vico 2.0.1 (bar) + custom Canvas donut |
| Database | Room 2.7.1 with KSP |
| Preferences | DataStore 1.1.1 |
| Navigation | Compose Navigation 2.8.9 |
| Animations | `Animatable`, `AnimatedVisibility`, `AnimatedContent`, `infiniteTransition` |
| Notifications | `AlarmManager.setExactAndAllowWhileIdle`, `BroadcastReceiver` |
| Min SDK | 28 (Android 9) |
| Target SDK | 35 (Android 15) |

---

## Architecture

```
app/
├── data/
│   ├── DietPlanData.kt          # Hardcoded 7-day meal plan
│   ├── CompletionEntity.kt      # Room entity (calendarDate × slotIndex)
│   ├── DietDao.kt               # Upsert + batch query DAO
│   ├── DietDatabase.kt          # Room singleton
│   └── PreferencesRepository.kt # DataStore + cycle window logic
├── viewmodel/
│   ├── HomeViewModel.kt         # Home screen state + toggle logic
│   └── InsightsViewModel.kt     # KPI calculations (streak, trend, best day)
├── ui/
│   ├── splash/SplashScreen.kt   # Animated leaf + particle burst
│   ├── home/
│   │   ├── HomeScreen.kt        # Greeting, pager, day chips, FAB
│   │   ├── DayPlanPage.kt       # Single day's meal list
│   │   └── MealSlotCard.kt      # Glass tile with animated checkbox
│   ├── insights/InsightsScreen.kt
│   └── components/
│       ├── GlassTile.kt         # Glassmorphism component
│       ├── DonutChart.kt        # Fixed-angle Canvas donut (7 segments)
│       ├── TrendChip.kt         # Animated trend indicator
│       ├── AnimatedCheckbox.kt
│       └── SkeletonLoader.kt
├── notification/
│   ├── NotificationMessages.kt  # 49 creative daily messages
│   ├── NotificationHelper.kt    # Channel + notification builder
│   ├── MealNotificationScheduler.kt
│   ├── MealNotificationReceiver.kt
│   └── BootReceiver.kt
└── navigation/AppNavigation.kt
```

---

## Getting Started

### Prerequisites

No Android Studio required. Everything downloads as plain binaries:

```bash
# One-time setup — downloads JDK 21 + Android SDK into ~/dev-tools/
chmod +x setup-dev-env.sh
./setup-dev-env.sh
```

### Build & test

```bash
# Activate dev environment
source ~/dev-tools/env.sh

# Run all 50 unit tests (JVM only, no device needed)
./run-tests.sh

# Run a specific test class
./run-tests.sh Preferences   # PreferencesRepositoryTest
./run-tests.sh Insights      # InsightsCalculatorTest
./run-tests.sh Greeting      # GreetingTest

# Build debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Run instrumented tests (needs USB-connected device or emulator)
./run-instrumented-tests.sh
```

---

## Meal Schedule

| Slot | Time | Meal |
|------|------|------|
| 0 | 7:00 AM | Morning Drink |
| 1 | 8:30 AM | Breakfast |
| 2 | 11:00 AM | Mid-Morning Snack |
| 3 | 1:00 PM | Lunch |
| 4 | 4:30 PM | Evening Snack |
| 5 | 7:30 PM | Dinner |
| 6 | 9:30 PM | Night Drink |

---

## Tests

| Test suite | Tests | Type |
|------------|-------|------|
| `PreferencesRepositoryTest` | 12 | JVM unit |
| `InsightsCalculatorTest` | 14 | JVM unit |
| `GreetingTest` | 11 | JVM unit |
| `MealSchedulerTest` | 7 | JVM unit |
| `NotificationMessagesTest` | 6 | JVM unit |
| `DietDaoTest` | 9 | Instrumented (Room in-memory) |
| `NavigationTest` | 4 | Instrumented (Compose UI) |
| **Total** | **63** | |

---

## License

Private — WeFit personal project.
