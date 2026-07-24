# Deploy Package

This folder contains the signed release APK for the DummyShop app.

## Included files
- `app-release.apk` — signed release APK (ready to install)

> Security: the release keystore is NOT included. Do NOT commit any keystore or private keys to the repo.

## Downloading from GitHub

1. Open the repository in your browser: https://github.com/Nikunj-Agarwala/dummyjson-products-app-release
2. Download `deploy/app-release.apk` from the `deploy` folder or from a Release asset if attached.

## Install directly on the phone (no PC)

1. Download `app-release.apk` using your phone browser or transfer via cloud storage.
2. Open the downloaded APK file in a file manager.
3. If prompted, allow installation from unknown sources for the installing app.
4. Tap Install and wait for completion.

## Install using a computer (ADB)

1. Enable Developer Options and USB debugging on the phone.
2. Connect the phone to your computer via USB.
3. From the repository root (or after downloading `app-release.apk`), run:

```bash
adb install -r deploy/app-release.apk
adb shell am start -n com.nikunjagarwala.dummyshop/com.nikunjagarwala.dummyshop.MainActivity
```

4. If installation fails due to an existing debug app, uninstall the debug version first:

```bash
adb uninstall com.nikunjagarwala.dummyshop.debug
```

## Notes

- This APK is signed for release and can be installed on devices directly.
- Keep `release.keystore` and `key.properties` private and do not commit them.
- If you want the APK attached to a GitHub Release (recommended), create a release on GitHub and upload `app-release.apk` as an asset.
