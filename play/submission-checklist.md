# Google Play submission checklist

- [ ] Replace maintainer email placeholders in both store listings and the privacy policy.
- [ ] Publish `privacy-policy.md` at a stable, publicly accessible HTTPS URL and enter that URL in Play Console.
- [ ] Create Play Console high-resolution app icon (512×512), feature graphic (1024×500), and phone screenshots from the production build.
- [ ] Complete the Data safety form consistently with the policy: no developer-side collection, sharing, or SDK-based tracking is implemented.
- [ ] Complete the content rating questionnaire and target audience declarations in Play Console.
- [ ] Declare `REQUEST_INSTALL_PACKAGES` in Play Console and explain that the core app purpose directly enables user-initiated installation of packages from ZIP archives. Google Play restricts this permission to apps whose core functionality directly involves user-initiated package installation.
- [ ] The app targets API 36, matching the Google Play requirement for new apps effective August 31, 2026; recheck Play Console before submission in case requirements change.
- [ ] Set the four GitHub Actions signing secrets and retain a secure backup of the keystore.
- [ ] Install the release APK on supported Android versions; verify ZIP sharing, multiple APK selection, unknown-source permission, cancellation, and installer confirmation.
