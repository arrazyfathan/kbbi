# Future development roadmap

This roadmap reflects the current multi-module application as of August 2026. It separates shipped capabilities from potential work; it is not a release commitment.

## Shipped platform capabilities

- [x] Text-selection lookup through `ACTION_PROCESS_TEXT`
- [x] Receive shared plain text through `ACTION_SEND`
- [x] Share and copy dictionary definitions
- [x] Launcher shortcuts for search, bookmarks, proverbs, and random word
- [x] Voice search through Android speech recognition
- [x] Local word suggestions and searchable bundled word index
- [x] Word and proverb deep links
- [x] Room-backed offline cache for opened words and proverb content
- [x] Preferences DataStore for reminders and haptic feedback
- [x] Daily word, daily proverb, and bookmark-review notifications
- [x] WorkManager scheduling for configurable reminders
- [x] English and Indonesian per-app language selection
- [x] Semantic, user-configurable haptic feedback
- [x] In-app GitHub release update checks
- [x] Privacy policy, terms and conditions, and open-source licenses
- [x] Optional word translation in the detail screen

## Recommended next investments

### 1. Make offline state visible

- Indicate whether a result came from the network or local cache.
- Show an explicit “available offline” state for previously opened entries.
- Let users pre-cache bookmarked meanings and explain storage usage.
- Add a safe cache-management screen with size and last-updated information.

### 2. Improve search quality

- Add typo-tolerant and similar-word suggestions.
- Rank local suggestions using history and bookmark signals.
- Highlight matching text consistently across word and proverb results.
- Improve multi-word query handling without weakening exact KBBI lookups.

### 3. Add home-screen entry points

- Quick-search widget.
- Word-of-the-day widget backed by the existing reminder content flow.
- Bookmark or proverb widget with deep-link navigation.

### 4. Expand personalization

- System, light, and dark theme choices.
- Material You dynamic color where supported.
- Reminder-day selection and quiet-hour controls.
- Configurable search-history retention.

### 5. Protect and move user data

- Android Auto Backup validation for bookmarks and preferences.
- Manual bookmark/history export and import using a documented JSON format.
- Conflict-safe restore behavior with migration tests.

## Larger opportunities

### Downloadable offline dictionary

Full offline definitions would be highly valuable, but require an appropriate data source, clear licensing, versioned downloads, integrity checks, migrations, storage controls, and incremental updates. The current bundled asset contains word entries only.

### OCR-assisted lookup

Camera or image OCR could extract Indonesian text and route selected words into the existing search flow. This should be implemented only with clear permission, privacy, and on-device-processing behavior.

### Pronunciation

Text-to-speech or licensed pronunciation audio could complement definitions. This would require language-quality evaluation, accessibility controls, and media lifecycle handling.

## Lower priority

These platform areas do not currently match the product's core dictionary workflows:

- Location
- Bluetooth
- NFC
- Health Connect
- General-purpose sensors
- Foreground services without a user-visible long-running task
- Biometrics unless private notes or protected collections are introduced

## Product direction

The strongest direction remains quick lookup, trustworthy offline behavior, and lightweight daily learning. New work should reuse the existing domain boundaries, remain optional where permissions are involved, and preserve the app's account-free local-first experience.
