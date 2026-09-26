# Android ZIP to APK Installer

Open a ZIP archive from an Android file manager or share menu. The app finds APK files inside, lets you choose one when the archive contains multiple APKs, and hands the selected APK to Android's system installer. The user confirms installation in Android; this app never installs packages silently.

## Build

The GitHub Actions workflow builds a debug APK for pull requests and pushes. Pushes and tags also build a release APK with a repository-owned **fixed CI signing key**, so the `zip-to-apk-installer-release` APK has a stable signature across workflow runs without requiring Actions secrets.

The fixed CI key is intentionally public and is only for installable GitHub Actions artifacts. Its certificate SHA-256 fingerprint is `C5:62:42:BF:5F:94:F2:2E:A1:70:95:0F:5C:7E:13:39:08:B0:71:39:60:5F:61:70:04:90:45:6D:74:DA:70:65`. Do **not** use that public key as the Google Play production/upload key.

To additionally build a Play-signed Android App Bundle (AAB), configure these repository **Actions secrets**:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded, private release keystore (`base64 -w 0 release.jks`) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias in the keystore |
| `ANDROID_KEY_PASSWORD` | Key password |

If those secrets are absent, the workflow still succeeds and uploads the fixed-signature release APK; it only skips the Play-signed AAB. Keep a secure backup of the Play keystore and passwords. Every Play update must use the same upload/signing setup, and the private Play key must never be committed to the repository.

Workflow artifacts are named `zip-to-apk-installer-debug` and `zip-to-apk-installer-release`. The debug build has a `.debug` application ID and cannot update the release app.

## Usage

1. Open a `.zip` file in a file manager and choose **ZIP to APK Installer**.
2. Select an APK if the archive contains more than one.
3. If Android asks, allow this app to install unknown apps.
4. Review and confirm installation in Android's system installer.

ZIP contents are processed locally. The app extracts only `.apk` files into its private cache, limits processing to 100 APKs, 512 MB per APK, and 1 GB total expanded data, then removes temporary files with the app cache. Android controls whether an APK can be installed and displays its own confirmation and verification screens.

## Google Play materials

Draft store copy, privacy policy, release notes, and a pre-submission checklist are in [`play/`](play/). Replace the privacy policy contact placeholder and host the policy at a public URL before submitting the app.
