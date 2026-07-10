# Future Development

This document lists Android platform features that match this KBBI dictionary app. The best candidates are features that improve quick lookup, offline access, learning reminders, and bookmark workflows.

## Strong Match

1. **WorkManager**
   - Sync or refresh cached popular words and proverbs in the background.
   - Preload a daily word or proverb.
   - Clean old search history and stale cache entries.

2. **Notifications**
   - Daily "Kata Hari Ini" reminder.
   - Daily proverb reminder.
   - Reminder to review saved bookmarks.
   - Should be opt-in from a settings screen.

3. **App Shortcuts** ✅
   - Long-press launcher shortcuts for search, bookmarks, proverbs, and random word. ✅
   - Useful because dictionary users often want fast entry points.

4. **Share Intent** ✅
   - Share a word definition from the detail screen. ✅
   - Receive shared text from other apps and search it in KBBI. ✅

5. **Home Screen Widgets**
   - Word of the day widget.\
   - Quick search widget.
   - Saved word or proverb widget.

6. **Text Selection / Process Text** ✅
   - Add "Search in KBBI" to Android text selection actions. ✅
   - This is one of the highest-value integrations for a dictionary app.

7. **Offline Mode Improvements**
   - Show whether a result comes from cache or remote.
   - Add an "available offline" indicator.
   - Allow users to cache bookmarked meanings intentionally.

8. **DataStore Settings**
   - Notification preferences.
   - Theme preference.
   - Daily word notification time.
   - Cache policy.
   - Recent history limit.

9. **Dynamic Theme / Material You**
   - Add Android dynamic color support.
   - Add light, dark, and system theme options.
   - Fits the existing Compose and Material 3 stack.

10. **Search Enhancements**
    - Voice search using Android speech recognition. ✅
    - Search suggestions from the local `entries.json` word index. ✅
    - Recent search chips.
    - Similar-word or typo suggestions.  ✅

## Good Match, But Secondary

1. **Biometric Protection**
   - Only useful if the app later adds private notes or protected saved words.
   - Not a priority for the current product.

2. **Camera / OCR**
   - Let users scan Indonesian text and tap words to search.
   - High value, but larger scope than the core dictionary workflow.

3. **Backup and Restore**
   - Backup bookmarks and search history.
   - Could use Android Auto Backup or manual JSON export/import.

4. **Deep Links**
   - Support links such as `kbbi://word/makan`.
   - Useful for shared definitions, widgets, notifications, and external integrations.

5. **Predictive Back and Edge-to-Edge Polish**
   - Improves platform feel.
   - Worth doing as UI quality work after core features are stable.

6. **Downloadable Offline Dictionary**
   - Very useful, but depends on dictionary data availability and licensing.
   - The current bundled asset contains entries only, not definitions.

## Weak Match / Probably Skip

- Location APIs
- Bluetooth
- NFC
- Health Connect
- Sensors
- Camera features unless they support OCR
- Foreground services unless the app has a real long-running user-visible task
- Media APIs unless pronunciation audio is added

## Recommended Roadmap

1. ✅ Text selection "Search in KBBI" (Done)
2. ✅ Share definition and receive shared text (Done)
3. ✅ App shortcuts for search, bookmarks, proverbs, and random word (Done)
4. ✅ Voice search using Android speech recognition (Done)
5. DataStore settings
6. Daily word notification
7. WorkManager cache refresh
8. Home screen widget
9. Dynamic theme
10. Deep links
11. OCR scan text
12. Backup/export for bookmarks and history

The strongest direction is to make KBBI better at quick lookup, offline cache, and daily learning. The most relevant Android feature set is Process Text, share intents, WorkManager, notifications, widgets, DataStore, and deep links.
