# DummyShop

DummyShop is a native Android shopping app built with Kotlin and Jetpack Compose. It uses the [DummyJSON Products API](https://dummyjson.com/docs/products) to provide a fast, modern product browsing experience with search, category filters, sorting, pagination, wishlist support, and a cart.

## Overview

The app is designed to feel responsive even when the network is weak or unavailable. Product data is cached locally with Room, and the UI uses Paging 3 to show previous results immediately while background refreshes keep the content up to date.

## Features

- Browse products from the DummyJSON API
- Search products by keyword
- Filter by category and sort by different criteria
- Infinite scrolling with paging
- View detailed product information
- Save products to wishlist and cart
- Use cached content when offline with a clear offline banner

## Install the APK

A ready-to-install debug APK is included in this repository at [apk/app-debug.apk](apk/app-debug.apk). Install it on an emulator or Android device with:

```bash
adb install apk/app-debug.apk
```

This is a debug build signed with Android’s default debug keystore, which is suitable for testing and local use.

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- Retrofit + OkHttp + kotlinx.serialization
- Room
- Paging 3
- Coil
- Manual constructor injection via DummyShopApp

## Architecture

Room is the single source of truth for everything shown on screen. The UI never renders a network response directly.

```text
data/remote   → Retrofit ProductApi + DTOs (dummyjson.com)
data/local    → Room entities, DAOs, and AppDatabase (local cache)
data/repository → ProductRepository + ProductsRemoteMediator (merge network data into Room)
domain        → plain Kotlin Product model used by the UI
ui/*          → Compose screens and ViewModels, organized by feature
```

### Product list and pagination

ProductsRemoteMediator uses Paging 3 to load one page at a time from the relevant DummyJSON endpoint. It stores results in Room so that:

- cached results appear immediately,
- pull-to-refresh works smoothly,
- network failures do not wipe the already cached list.

### Wishlist and cart

Wishlist and cart entries are stored locally and joined against the same cached product table, so they load instantly and refresh in the background when possible.

### Product detail screen

The detail screen reads cached product data first and then refreshes it in the background. If the refresh fails but cached data exists, the app shows an offline-friendly banner instead of failing completely.

## Build and run

Requirements:

- Android SDK
- JDK 17+
- Android Studio or the Android command-line tools

Run:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The project pins the Gradle JVM to the Android Studio bundled JBR in gradle.properties to avoid version mismatches.

## Testing

The test suite includes:

- ProductsRemoteMediatorTest for paging and caching behavior
- ProductRepositoryTest for wishlist, cart, and refresh logic

## Offline behavior

To manually verify the offline experience:

1. Launch the app while online and browse a few products.
2. Add one item to the wishlist and one to the cart.
3. Disable network access on the emulator or device.
4. Refresh the list or reopen the wishlist, cart, or detail screen.

Cached content should remain visible with an offline banner, while a fully cold start without any cached data should show a retry state instead.
