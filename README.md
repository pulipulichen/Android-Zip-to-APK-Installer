# Android ZIP to APK Installer

Open a ZIP archive from an Android file manager or share menu. The app finds APK files inside, lets you choose one when the archive contains multiple APKs, and hands the selected APK to Android's system installer. The user confirms installation in Android; this app never installs packages silently.

## Build

The GitHub Actions workflow builds a debug APK for pull requests and pushes. A signed release APK and Android App Bundle (AAB) are built on pushes and tags after setting the following repository **Actions secrets**:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded, private release keystore (`base64 -w 0 release.jks`) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias in the keystore |
| `ANDROID_KEY_PASSWORD` | Key password |

Keep a secure backup of the keystore and passwords. Every published update must use the same keystore and alias; losing them prevents updates signed with this key. Never commit the keystore or passwords to the repository.

Workflow artifacts are named `zip-to-apk-installer-debug` and `zip-to-apk-installer-release`; the release artifact contains both APK and AAB outputs. The debug build has a `.debug` application ID and cannot update the Play Store release.

## Usage

1. Open a `.zip` file in a file manager and choose **ZIP to APK Installer**.
2. Select an APK if the archive contains more than one.
3. If Android asks, allow this app to install unknown apps.
4. Review and confirm installation in Android's system installer.

ZIP contents are processed locally. The app extracts only `.apk` files into its private cache, limits processing to 100 APKs, 512 MB per APK, and 1 GB total expanded data, then removes temporary files with the app cache. Android controls whether an APK can be installed and displays its own confirmation and verification screens.

## Google Play materials

Draft store copy, privacy policy, release notes, and a pre-submission checklist are in [`play/`](play/). Replace the privacy policy contact placeholder and host the policy at a public URL before submitting the app.
