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
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.codedtx.tv-test") {
                useModule("com.github.CodeDTX.android-tv-test-plugin:tv-test-plugin:${requested.version}")
            }
        }
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

## After setup — use Claude Code to finish the configuration

`tvTestSetup` generates stub files with `TODO` placeholders. Instead of filling them manually, open the project in [Claude Code](https://claude.com/claude-code) and run:

```
/setup-tv-testing
```

This is a Claude Code skill that was dropped into `.claude/commands/setup-tv-testing.md` by the Gradle task. When you run it, Claude:

1. **Reads your project** — scans `AndroidManifest.xml`, your Composable screens, ViewModels, navigation graphs, and existing CI workflows to understand your app
2. **Prints a project report** — confirms your application ID, launcher Activity, UI technology, flavors, and existing test infrastructure. You verify before anything is written.
3. **Fills all placeholders automatically** — sets `LOAD_SIGNAL_TEXT` in `AppTestConsts.kt` to the actual text visible when your screen finishes loading, populates `AppTestTags.kt` with `testTag()` values found in your composables
4. **Writes real test methods** — generates `@Test` methods in `UiTest.kt` based on your actual screens and D-pad navigation flows, not generic stubs
5. **Writes the initial screenshot test** — sets up `ScreenshotTest.kt` with your first capture scenario, then asks you what additional screenshots you need
6. **Validates the setup** — runs `./gradlew dependencies` and `compileDebugAndroidTestKotlin` to confirm everything resolves and compiles

### Requirements

- [Claude Code](https://claude.com/claude-code) installed (`npm install -g @anthropic-ai/claude-code`)
- Run from the root of your consumer project

### Example session

```
> /setup-tv-testing

Android TV Test Setup — Project Analysis
─────────────────────────────────────────
Application module   : :app
Application ID       : com.example.mytv
UI technology        : Jetpack Compose
Launcher Activity    : MainActivity
Primary variant      : productionDebug
─────────────────────────────────────────
Confirm? (yes to proceed)
```

Claude then writes tests specific to your app — not copy-paste stubs.

---

Push to your repo — the generated CI workflows trigger automatically. Screenshots run after UI tests pass.

---

## Publishing a new version

Tag a release to verify the build and update the `v1` floating tag:

```bash
git tag 1.0.0
git push origin 1.0.0
```

The `publish.yml` workflow runs automatically, verifies the JitPack build command succeeds, then moves the `v1` tag to the new release. JitPack serves the artifact on first consumer request.
