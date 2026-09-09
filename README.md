# android-tv-testing

Reusable Android TV UI testing platform by [CodeDTX](https://github.com/CodeDTX).

Provides:
- **`tv-test-utils`** — Compose test helpers for D-pad navigation, screen objects, assertions, and screenshot capture
- **`com.codedtx.tv-test` Gradle plugin** — scaffolds CI workflows and androidTest stubs into any consumer project
- **Reusable GitHub Actions workflows** — TV emulator setup, UI test runs, and screenshot capture

---

## Prerequisites

This library is hosted on GitHub Packages and requires authentication to download.

### 1. Generate a GitHub PAT

Go to [github.com/settings/tokens](https://github.com/settings/tokens) → New token → select `read:packages` scope → copy the token.

### 2. Add credentials to your global Gradle properties

Create or edit `~/.gradle/gradle.properties` (never commit this file):

```properties
CODEDTX_GITHUB_USER=your-github-username
CODEDTX_GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxx
```

This is a one-time setup per machine.

---

## Installing in a consumer project

### 1. Add the plugin repository to `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/codedtx/android-tv-testing")
            credentials {
                username = providers.gradleProperty("CODEDTX_GITHUB_USER").orNull ?: System.getenv("CODEDTX_GITHUB_USER")
                password = providers.gradleProperty("CODEDTX_GITHUB_TOKEN").orNull ?: System.getenv("CODEDTX_GITHUB_TOKEN")
            }
        }
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/codedtx/android-tv-testing")
            credentials {
                username = providers.gradleProperty("CODEDTX_GITHUB_USER").orNull ?: System.getenv("CODEDTX_GITHUB_USER")
                password = providers.gradleProperty("CODEDTX_GITHUB_TOKEN").orNull ?: System.getenv("CODEDTX_GITHUB_TOKEN")
            }
        }
        google()
        mavenCentral()
    }
}
```

### 2. Apply the plugin in your app module's `build.gradle.kts`

```kotlin
plugins {
    // ... your existing plugins
    id("com.codedtx.tv-test") version "1.0.2"
}
```

### 3. Generate test stubs and CI workflows

```bash
./gradlew :app:tvTestSetup
```

This creates:
- `.github/workflows/ui-tests.yml`
- `.github/workflows/screenshots.yml`
- `app/src/androidTest/.../UiTest.kt`
- `app/src/androidTest/.../ScreenshotTest.kt`
- `app/src/androidTest/.../test/AppScreen.kt`
- `app/src/androidTest/.../test/AppTestTags.kt`
- `app/src/androidTest/.../test/AppTestConsts.kt`

### 4. Add the CI secret to your repo

In your TV app repo: **Settings → Secrets and variables → Actions → New secret**
- Name: `CODEDTX_GITHUB_TOKEN`
- Value: a GitHub PAT with `read:packages` scope

---

## After setup

Open `AppTestConsts.kt` and set `LOAD_SIGNAL_TEXT` to the text visible in your app when the main screen finishes loading. Then write your `@Test` methods in `UiTest.kt`.

---

## Publishing a new version

Tag a release to publish both artifacts to GitHub Packages:

```bash
git tag 1.0.3
git push origin 1.0.3
```

The `publish.yml` workflow runs automatically on any version tag.
