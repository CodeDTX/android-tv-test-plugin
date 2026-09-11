# android-tv-test-plugin

Reusable Android TV UI testing platform by [CodeDTX](https://github.com/CodeDTX).

Provides:
- **`tv-test-utils`** — Compose test helpers for D-pad navigation, screen objects, assertions, and screenshot capture
- **`com.codedtx.tv-test` Gradle plugin** — scaffolds CI workflows and androidTest stubs into any consumer project
- **Reusable GitHub Actions workflows** — TV emulator setup, UI test runs, and screenshot capture

---

## Prerequisites

No authentication needed — the library is hosted on [JitPack](https://jitpack.io), a public registry that builds directly from this GitHub repository. No PAT or credentials required.

---

## Installing in a consumer project

### 1. Add JitPack to `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
        google()
        mavenCentral()
    }
}
```

No CI secret is needed — JitPack is a public registry and Gradle resolves it without credentials.

### 2. Apply the plugin in your app module's `build.gradle.kts`

```kotlin
plugins {
    // ... your existing plugins
    id("com.codedtx.tv-test") version "1.0.0"
}
```

### 3. Generate test stubs and CI workflows

```bash
./gradlew :app:tvTestSetup
```

This creates:
- `.github/workflows/codedtx-ui-tests.yml`
- `.github/workflows/codedtx-screenshots.yml`
- `app/src/androidTest/.../UiTest.kt`
- `app/src/androidTest/.../ScreenshotTest.kt`
- `app/src/androidTest/.../test/AppScreen.kt`
- `app/src/androidTest/.../test/AppTestTags.kt`
- `app/src/androidTest/.../test/AppTestConsts.kt`

---

## After setup

Open `AppTestConsts.kt` and set `LOAD_SIGNAL_TEXT` to the text visible in your app when the main screen finishes loading. Then write your `@Test` methods in `UiTest.kt`.

Push to your repo — the generated CI workflows trigger automatically. Screenshots run after UI tests pass.

---

## Publishing a new version

Tag a release to verify the build and update the `v1` floating tag:

```bash
git tag 1.0.0
git push origin 1.0.0
```

The `publish.yml` workflow runs automatically, verifies the JitPack build command succeeds, then moves the `v1` tag to the new release. JitPack serves the artifact on first consumer request.
