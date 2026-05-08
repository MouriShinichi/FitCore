# FitCore - AI-Driven Android Fitness Tracker

> **100% AI-generated** | 22 Java source files | 5 modules | Fully local SQLite storage

FitCore is a lightweight, privacy-first fitness tracking Android app built entirely from scratch with AI coding agents (Claude Code + Cursor + Codex). No cloud, no ads, no tracking — your data stays on your device.

---

## Features

### Home Dashboard
- Greeting by time of day (Good morning / afternoon / evening)
- Current fitness plan card with quick access
- Workout count, total minutes, and continuous streak stats
- Daily goal progress visualization

### Workout Recording
- 100+ exercises across 5 categories: Cardio, Strength, Flexibility, Ball Sports, Other
- Built-in workout timer with start/pause/resume
- MET-based calorie estimation
- Feeling rating (1-5 stars)
- Save confirmation with detailed summary dialog

### Analytics
- Total workouts and hours overview
- 7-day check-in heatmap
- MPAndroidChart bar chart for weekly workout minutes
- Week navigation and date picker

### Smart Reminders
- Custom daily time picker
- One-time or day-of-week scheduling (Mon-Sun)
- Instant test notification
- 22 motivational quotes (Chinese)

### Profile & Guides
- Avatar customization
- Edit profile (gender, age, height, weight)
- BMI calculator with classification guide
- Calorie expenditure reference

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |
| UI | Material Design Components, ConstraintLayout |
| Navigation | BottomNavigationView (5 tabs) |
| Charts | MPAndroidChart v3.1.0 |
| Database | SQLite via SQLiteOpenHelper |
| Auth | SHA-256 password hashing |
| Session | SharedPreferences |
| Notifications | NotificationManager + BroadcastReceiver |

---

## Architecture

```
app/src/main/java/com/example/fitcore/
├── LoginActivity.java           # Auth (login / register toggle)
├── MainActivity.java            # 5-tab container
├── activity/
│   ├── AccountInfoActivity.java
│   ├── BMIGuideActivity.java
│   ├── CalorieGuideActivity.java
│   ├── EditProfileActivity.java
│   ├── PlanListActivity.java
│   ├── ReminderSettingsActivity.java
│   ├── WorkoutDetailActivity.java
│   ├── WorkoutHistoryActivity.java
│   └── WorkoutTimerActivity.java
├── fragment/
│   ├── HomeFragment.java        # Dashboard
│   ├── RecordFragment.java      # Workout recording
│   ├── AnalyticsFragment.java   # Charts & stats
│   ├── ReminderFragment.java    # Notification scheduling
│   └── ProfileFragment.java     # User settings
├── adapter/
│   ├── PlanAdapter.java
│   └── WorkoutAdapter.java
├── model/
│   ├── User.java
│   ├── FitnessPlan.java
│   ├── WorkoutRecord.java
│   └── ReminderSettings.java
├── database/
│   └── DatabaseHelper.java      # 5 tables, v2 migration
└── utils/
    ├── SessionManager.java
    └── NotificationHelper.java
```

---

## Database Schema

| Table | Purpose |
|-------|---------|
| `users` | User accounts (username, password hash, profile) |
| `fitness_plans` | 4 seed plans (Push/Pull/Legs, HIIT, Flexibility, Endurance Running) |
| `user_plans` | User plan selections with progress tracking |
| `workout_records` | Exercise type, duration, feeling, calories, timestamp |
| `reminder_settings` | Time, mode (daily/custom), days of week |

---

## Build

```bash
git clone https://github.com/MouriShinichi/FitCore.git
cd FitCore
./gradlew assembleDebug
```

Open with Android Studio Hedgehog (2023.1.1+) or later.

---

## AI Development Process

This project was built as a proof-of-concept for AI-driven mobile development:

- **Claude Code**: Architecture design, database schema (5 tables), complex logic (MET estimation, chart rendering, reminder scheduling)
- **Codex**: UI layout generation and Material Design component adaptation
- **Cursor**: Code review, bug fixing, and detail refinement

All 141 files (9,346 lines) were generated in 3 days through multi-agent collaboration.

---

## License

MIT License — do whatever you want with it.

---

*Built with Claude Code, Cursor, and Codex. Submitted as part of the Xiaomi MiMo Orbit Creator Incentive Program.*
