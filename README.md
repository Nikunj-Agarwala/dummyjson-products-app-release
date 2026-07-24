# DummyShop

A native Android product browser built on the [DummyJSON Products API](https://dummyjson.com/docs/products): search/filter/sort with scroll-based pagination, a product detail screen, and a local wishlist/cart — all cache-first with background refresh, so the app stays usable offline.

## Install the APK

A ready-to-install debug build is checked into this repo at [`apk/app-debug.apk`](apk/app-debug.apk). Sideload it onto a device or emulator:

```
adb install apk/app-debug.apk
```

It's a debug-signed build (Android's default debug keystore) rather than a release build, since a release build needs a real signing keystore that isn't appropriate to fabricate for a submission like this.

## Architecture

Room is the single source of truth for everything shown on screen; the UI never renders a network response directly.

```
data/remote   → Retrofit ProductApi + DTOs (dummyjson.com)
data/local    → Room entities/DAOs/AppDatabase (the cache)
data/repository → ProductRepository + ProductsRemoteMediator (merges network into Room)
domain        → plain Kotlin Product model used by the UI
ui/*          → Compose screens + ViewModels, one package per screen
```

**Product list & pagination.** `ProductsRemoteMediator` (Paging 3) fetches one page at a time from `/products`, `/products/search`, or `/products/category/{slug}` (whichever filter mode is active) and merges it into two Room tables: `products` (canonical per-id cache, shared with wishlist/cart/detail) and `list_entries` (an ordered bridge table recording which product sits at which position for the *current* search/filter/sort combination — the `queryKey`). The Compose list screen reads from a `PagingSource` backed by `list_entries JOIN products`, so:
- Cached results for the current query render immediately, before any network call resolves.
- A failed network load surfaces as `LoadState.Error` from Paging 3 without touching what's already cached — the UI shows an **"offline, showing cached results"** banner over the existing list, or a full retry screen if there's nothing cached yet for that query.
- Pull-to-refresh and infinite scroll are both just Paging 3 `REFRESH`/`APPEND` loads.

**Wishlist & cart.** Two small local tables (`wishlist`, `cart`) joined against the same `products` cache. Opening either screen renders instantly from Room, then kicks off a bounded-concurrency background refetch of each saved product's `/products/{id}` to keep details fresh — same cache-first-with-error-banner treatment as the list.

**Product detail.** Reads the cached product (already present if you navigated from a list/wishlist/cart row) and triggers a background refresh; shows a banner if the refresh fails but cached data exists, or a full retry state if it's never been cached and the fetch fails.

**Known API constraint:** DummyJSON doesn't support combining a search query and a category filter in one request, so the list screen treats them as mutually exclusive — picking a category clears the search box and vice versa. Sorting applies to whichever mode is active.

## Tech stack

Kotlin, Jetpack Compose (Material 3) + Navigation-Compose, Retrofit + OkHttp + kotlinx.serialization, Room, Paging 3, Coil, manual constructor-injection (no DI framework) via `DummyShopApp`.

## Running it

```
./gradlew assembleDebug          # build apk/app-debug.apk-equivalent output
./gradlew testDebugUnitTest      # unit tests (Robolectric + in-memory Room)
```

Requires the Android SDK (`compileSdk`/`targetSdk` 35, `minSdk` 24) and JDK 17+. `gradle.properties` pins the Gradle JVM to Android Studio's bundled JBR so it doesn't accidentally pick up a newer system JDK.

### Unit tests

`ProductsRemoteMediatorTest` (in-memory Room + a fake `ProductApi`) verifies: a `REFRESH` caches the first page and detects end-of-pagination, `APPEND` continues from the right offset and accumulates results, and — the important one — a network failure returns `MediatorResult.Error` **without deleting** whatever was already cached. `ProductRepositoryTest` covers wishlist add/remove, cart quantity updates (including auto-remove at zero), and the background product refresh path.

### Manually verifying the offline behavior

1. Launch the app online, browse the list, open a couple of products, add one to your wishlist and one to your cart.
2. Turn off the emulator's network (`adb shell svc wifi disable && adb shell svc data disable`, or toggle airplane mode).
3. Pull-to-refresh the list, reopen the Wishlist/Cart tabs, and reopen a product detail page — cached content stays visible with an "offline, showing cached results" banner.
4. Force-clear app data and relaunch fully offline — the list, wishlist, and cart all show a plain retry screen instead, since there's nothing cached yet.
