# Morphe Bridge

A small native Android app that forwards Pixel Now Playing song requests to an explicitly configured YouTube Music-compatible app.

The bridge uses the allowlisted package ID `com.pandora.android` so Now Playing can discover it. It cannot coexist with the real Pandora Android app. It has no launcher entry, player, background service, network permission, or persistent notification.

Now Playing's generic connected-service request provides artist and title metadata but not a YouTube video ID. The bridge therefore opens YouTube Music search results. If an exact supported YouTube Music watch URL is supplied, it forwards that URL.

Targets are configured in `TargetResolver.kt` and tried in priority order. Unknown music handlers are never selected.

Debug builds log the incoming intent and its extras to logcat for troubleshooting. These values can include song titles and artist names. Release builds disable this logging.

## Build

Install JDK 17 or later and Android SDK 37, then run:

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleRelease
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Release builds require signing before installation.

## Test the bridge

```powershell
adb shell am start `
  -a android.media.action.MEDIA_PLAY_FROM_SEARCH `
  -c android.intent.category.DEFAULT `
  -p com.pandora.android `
  --es android.intent.extra.focus "vnd.android.cursor.item/audio" `
  --es android.intent.extra.artist "Example Artist" `
  --es android.intent.extra.title "Example Track" `
  --es query "Example Artist Example Track"
```
