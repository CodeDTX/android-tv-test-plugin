# /setup-tv-testing

Sets up the CodeDTX Android TV testing platform in a consumer Android project.
Works in six phases. Never skips phases or blindly overwrites files.

---

## Phase 1 — Discover

Read the following files (if they exist) without modifying anything:

```
settings.gradle / settings.gradle.kts
build.gradle / build.gradle.kts        (root and all app modules)
gradle/libs.versions.toml
app/src/main/AndroidManifest.xml       (and any module-specific manifests)
app/src/androidTest/                   (existing test infrastructure)
.github/workflows/                     (existing CI)
```

Extract:
- Android application module name(s)
- AGP version, Gradle version, Kotlin version
- Compose BOM version (if present)
- `applicationId`
- Product flavors and build types → all build variants
- Launcher Activity class name (from `AndroidManifest.xml` — look for MAIN + LAUNCHER/LEANBACK_LAUNCHER intent filter)
- UI technology: Jetpack Compose (`buildFeatures.compose = true`), Leanback, or classic Views
- Instrumentation runner (`testInstrumentationRunner`)
- Whether `src/androidTest` exists and what's in it
- Existing GitHub Actions workflows
- Existing screenshot test infrastructure

---

## Phase 2 — Understand

Print a concise project report before touching any file:

```
Android TV Test Setup — Project Analysis
─────────────────────────────────────────
Application module   : :app
Application ID       : com.example.tv
UI technology        : Jetpack Compose
Launcher Activity    : MainActivity
Primary variant      : productionDebug
Instrumentation      : androidx.test.runner.AndroidJUnitRunner
Existing androidTest : Yes / No
Existing screenshots : Yes / No
Existing CI          : Yes / No
─────────────────────────────────────────
```

Ask the user to confirm or correct the report before proceeding to Phase 3.
Do NOT proceed without confirmation.

---

## Phase 3 — Plan

Determine the exact changes required. List them explicitly:

```
Files to CREATE:
  ✦ app/build.gradle.kts           — add plugin id("com.codedtx.tv-test")
  ✦ settings.gradle.kts            — add plugin repository (GitHub Packages)
  ✦ ~/.gradle/gradle.properties    — CODEDTX_GITHUB_TOKEN (developer only, not committed)

Files to GENERATE (via :tvTestSetup Gradle task):
  ✦ .github/workflows/ui-tests.yml
  ✦ .github/workflows/screenshots.yml
  ✦ app/src/androidTest/.../UiTest.kt
  ✦ app/src/androidTest/.../ScreenshotTest.kt
  ✦ app/src/androidTest/.../test/AppScreen.kt
  ✦ app/src/androidTest/.../test/AppTestTags.kt
  ✦ app/src/androidTest/.../test/AppTestConsts.kt

Files to PRESERVE (existing, developer-owned):
  (list any files already present that will not be touched)

Files to MODIFY:
  (list any existing files that need targeted edits)
```

Show the plan. Ask the user to confirm before Phase 4.

---

## Phase 4 — Apply

Apply changes in this order:

### 4a. Add plugin repository to `settings.gradle.kts`

Add to `pluginManagement.repositories` and `dependencyResolutionManagement.repositories`:

```kotlin
maven {
    url = uri("https://maven.pkg.github.com/codedtx/android-tv-testing")
    credentials {
        username = providers.gradleProperty("CODEDTX_GITHUB_USER").orNull
            ?: System.getenv("CODEDTX_GITHUB_USER")
        password = providers.gradleProperty("CODEDTX_GITHUB_TOKEN").orNull
            ?: System.getenv("CODEDTX_GITHUB_TOKEN")
    }
}
```

Only add — do not remove or reorder existing repositories.

### 4b. Apply plugin in app module `build.gradle.kts`

Add to the `plugins {}` block:

```kotlin
id("com.codedtx.tv-test") version "1.0.0"
```

If a `tvTest {}` DSL block is needed (non-default variant, emulator config), add it. Use only the
detected values — never add config that matches the default.

### 4c. Document local credentials (never commit)

Remind the user to add to `~/.gradle/gradle.properties` (global, never in the project):

```properties
CODEDTX_GITHUB_USER=their-github-username
CODEDTX_GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxx   # read:packages scope only
```

### 4d. Run `:tvTestSetup`

```bash
./gradlew :app:tvTestSetup
```

This generates all androidTest stubs and CI workflows automatically.

### 4e. Fill in LOAD_SIGNAL_TEXT

Edit `AppTestConsts.kt` — replace the TODO with the exact text visible in the app when the main
screen finishes loading data.

---

## Phase 5 — Validate

Run each check. If one fails, diagnose and fix before continuing.

```bash
# Plugin resolves and dependencies compile
./gradlew :app:dependencies --configuration androidTestReleaseRuntimeClasspath | grep codedtx

# androidTest compiles
./gradlew :app:compileDebugAndroidTestKotlin

# Task exists
./gradlew :app:tasks --group "TV Testing"
```

Report:
```
✓ plugin applied
✓ tv-test-utils resolves
✓ androidTest compiles
✓ tvTestSetup task registered
```

---

## Phase 6 — Final Report

```
Android TV testing configured successfully.

Changes applied:
  ✓ Plugin repository added to settings.gradle.kts
  ✓ Plugin applied in app/build.gradle.kts
  ✓ CI workflow: .github/workflows/ui-tests.yml
  ✓ CI workflow: .github/workflows/screenshots.yml
  ✓ Test stubs: UiTest.kt, ScreenshotTest.kt
  ✓ Infrastructure: AppScreen.kt, AppTestTags.kt, AppTestConsts.kt

Developer work remaining:
  1. Set LOAD_SIGNAL_TEXT in AppTestConsts.kt
  2. Add testTag() modifiers to your composables and register them in AppTestTags.kt
  3. Write @Test methods in UiTest.kt
  4. Write screenshot scenarios in ScreenshotTest.kt
  5. Push to trigger ui-tests.yml CI workflow
```

---

## Rules

- Never skip phases or merge Phase 2 + 3 confirmation into a single ask.
- Never overwrite a file unless it contains the `@generated CodeDTX` marker.
- Never commit `CODEDTX_GITHUB_TOKEN` or any credential.
- Never use `@main` workflow references — always use a pinned version tag (`@v1`).
- If the project already has androidTest infrastructure, integrate — do not duplicate.
- If detection is ambiguous (multiple app modules, complex flavor matrix), ask the user before
  proceeding to Phase 3.
