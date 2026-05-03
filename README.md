# Zammy

> A full-featured Android agent client for self-hosted [Zammad](https://zammad.org/) (v6/v7) helpdesk instances.

[![CI](https://github.com/relexx/Zammy/actions/workflows/ci.yml/badge.svg)](https://github.com/relexx/Zammy/actions/workflows/ci.yml)
[![Release](https://github.com/relexx/Zammy/actions/workflows/release.yml/badge.svg)](https://github.com/relexx/Zammy/actions/workflows/release.yml)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg)](https://kotlinlang.org/)

---

## Screenshots

| Login | Tickets | Ticket Detail | Settings |
|-------|---------|---------------|----------|
| Sign in to your Zammad server | Open / Pending / Closed tabs | Article timeline, reply, edit status | Sync interval, trust-all-certs |

---

## Features

- **Ticket management** — Open, Pending, and Closed tabs with live search and pull-to-refresh
- **Ticket detail** — Full article/comment timeline with inline reply dialog
- **Reply with attachments** — Pick one or more files directly from the device
- **Status & priority editing** — Change ticket state and priority without leaving the detail screen
- **Create tickets** — Subject, description, group selector, priority, and file attachments
- **Background sync** — WorkManager-based periodic sync at 15, 30, or 60-minute intervals
- **Push notifications** — Two channels (New Tickets / Ticket Updates) with deep-link into ticket detail
- **Trust-all-certificates** — Toggle for self-hosted servers with self-signed CAs
- **Offline-first** — Room database caches tickets; stale entries are replaced on each sync
- **Localisation** — Full English and German (`values-de`) string resources

---

## Requirements

- Android 8.0 (API 26) or higher
- A self-hosted Zammad v6 or v7 instance accessible over HTTP/HTTPS
- A Zammad agent account (username + password)

---

## Installation

### Download APK

Grab the latest `zammy-vX.X.X.apk` from the [Releases](https://github.com/relexx/Zammy/releases) page, then:

1. Enable **Install from unknown sources** in your Android settings
2. Open the downloaded APK and install

### Build from source

```bash
# Clone the repository
git clone https://github.com/relexx/Zammy.git
cd Zammy

# Build a debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Architecture

Zammy follows **MVVM + Clean Architecture** with a strict three-layer separation:

```
app/
└── src/main/kotlin/com/zammy/app/
    ├── data/
    │   ├── api/          # Retrofit service + API models
    │   ├── local/        # Room database, DAO, entities
    │   └── repository/   # Repository implementations
    ├── di/               # Hilt modules (AppModule)
    ├── domain/
    │   ├── model/        # Domain models (Ticket, Article, …)
    │   ├── repository/   # Repository interfaces
    │   └── usecase/      # Business logic use cases
    ├── navigation/       # NavHost + route definitions
    ├── presentation/
    │   ├── createticket/ # Create Ticket screen + ViewModel
    │   ├── login/        # Login screen + ViewModel
    │   ├── settings/     # Settings screen + ViewModel
    │   ├── ticketdetail/ # Ticket Detail screen + ViewModel
    │   └── tickets/      # Tickets list screen + ViewModel
    ├── util/             # AuthInterceptor, BaseUrlInterceptor, DynamicTrustManager
    ├── worker/           # TicketSyncWorker (WorkManager)
    ├── MainActivity.kt
    └── ZammyApp.kt
```

### Key technology choices

| Concern | Library |
|---------|---------|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Persistence | Room |
| Background work | WorkManager |
| Async | Kotlin Coroutines + Flow |
| Credential storage | EncryptedSharedPreferences (AES256-GCM) |
| Build system | Gradle Kotlin DSL + version catalog |

---

## Configuration

On first launch, enter your Zammad server URL and agent credentials. All settings are stored in `EncryptedSharedPreferences` — credentials are never written to plain storage.

### Self-signed certificates

Enable **Trust all certificates** in Settings if your Zammad instance uses a self-signed CA. This bypasses certificate validation only; hostname verification is performed by the system verifier when this option is disabled.

---

## CI / CD

| Workflow | Trigger | Action |
|----------|---------|--------|
| `ci.yml` | Push / PR → `main` | Build debug APK + run unit tests |
| `release.yml` | Push to `v*` tag or `release/*` branch, or manual dispatch | Build signed release APK + create GitHub Release |

To trigger a manual release:

1. Go to **Actions → Release → Run workflow**
2. Enter the version (e.g. `1.0.0`)
3. The signed APK will be attached to the new GitHub Release automatically

---

## Contributing

1. Fork the repository and create your branch from `main`
2. Make your changes and add tests where appropriate
3. Ensure `./gradlew testDebugUnitTest` passes
4. Open a pull request

---

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
