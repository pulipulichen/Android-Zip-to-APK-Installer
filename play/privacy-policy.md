# Privacy Policy — ZIP to APK Installer

Last updated: 2026-09-26

ZIP to APK Installer processes ZIP archives selected by the user to locate APK files and pass a selected file to Android's system installer.

## Data collection and sharing

The app does not create an account, use analytics or advertising SDKs, or send archive contents or personal information to a developer-controlled server. Selected archives and extracted APK files are handled on the device. Temporary extracted files are stored in the app's private cache and are subject to Android cache cleanup; users can also clear the app's storage in Android settings.

When the user chooses to install an APK, the selected file is shared with Android's system package installer using a temporary read-only content URI. Android's own system services handle the installation flow.

## Permissions

The app receives access only to an archive selected or shared by the user. It requests Android's `REQUEST_INSTALL_PACKAGES` capability so the user can launch the system installer. Android may require the user to explicitly allow this app as an installation source. The app cannot silently install packages.

## Children's privacy

The app is a general-purpose utility and does not knowingly collect personal information from children or other users.

## Changes and contact

Policy changes will be published at the public URL used in the app's Google Play listing.

Contact: REPLACE_WITH_MAINTAINER_EMAIL
