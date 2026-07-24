# Deploy Package

This folder contains built APK artifacts produced from the project.

## Included files
- `app-release.apk` — signed release APK (ready to install)
- `README.md` — these instructions

> Security: the release keystore is NOT included. Do NOT commit any keystore or private keys to the repo.

## How to attach the APK to a GitHub repo

1. Make sure the `deploy/app-release.apk` file exists (this repo already contains it).
2. Ensure the keystore is not in the repo (it was removed). The `.gitignore` is configured to ignore keystores.
3. From the project root, add and commit the APK and README:

```bash
git add deploy/app-release.apk deploy/README.md .gitignore
git commit -m "Add signed release APK and deploy README"
git push origin HEAD
```

4. (Optional) Tag the release:

```bash
git tag -a v1.0 -m "v1.0 release"
git push origin --tags
```

## How someone can install the APK on their phone

1. Transfer `app-release.apk` to the phone or use `adb` with the phone connected.
2. Install with `adb`:

```bash
adb install -r deploy/app-release.apk
```

3. Launch the app (replace package/activity if needed):

```bash
adb shell am start -n com.nikunjagarwala.dummyshop/com.nikunjagarwala.dummyshop.MainActivity
```

## Notes

- This is a signed release APK; it can be installed on devices directly.
- Do NOT include `deploy/release.keystore` or `key.properties` in the repo — keep them private.
- If you need me to generate a Play Store AAB or walk through Play Store signing, I can help.
