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
- `applicationId` (base, from `defaultConfig`)
- `applicationIdSuffix` on the `debug` build type (e.g. `.debug`) — note this separately; the CI workflow receives the base applicationId and discovers the debug package automatically at runtime
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
Debug App ID         : com.example.tv.debug   (if applicationIdSuffix = ".debug" on debug buildType)
UI technology        : Jetpack Compose
Launcher Activity    : MainActivity
Primary variant      : productionDebug
Instrumentation      : androidx.test.runner.AndroidJUnitRunner
Existing androidTest : Yes / No
Existing screenshots : Yes / No
Existing CI          : Yes / No
─────────────────────────────────────────
```

> **Note on `applicationIdSuffix`:** Pass the base `applicationId` (e.g. `com.example.tv`) as `app-id` to the
> CI workflow — NOT the debug package. The reusable workflow discovers the installed debug instrumentation
> package automatically at runtime using `adb shell pm list instrumentation`.

Ask the user to confirm or correct the report before proceeding to Phase 3.
Do NOT proceed without confirmation.

---

## Phase 3 — Plan

Determine the exact changes required. List them explicitly:

```
Files to CREATE:
  ✦ app/build.gradle.kts           — add plugin id("com.codedtx.tv-test")
  ✦ settings.gradle.kts            — add JitPack plugin repository (no credentials needed)

Files to GENERATE (via :tvTestSetup Gradle task):
  ✦ .github/workflows/codedtx-ui-tests.yml     — triggers on push; runs UI tests then screenshots
                                                   in sequence (screenshots job has needs: [ui-tests])
  ✦ .github/workflows/codedtx-screenshots.yml  — manual-only (workflow_dispatch) for on-demand runs
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
maven { url = uri("https://jitpack.io") }
```

Only add — do not remove or reorder existing repositories.

### 4b. Apply plugin in app module `build.gradle.kts`

Add to the `plugins {}` block:

```kotlin
id("com.codedtx.tv-test") version "1.0.0"
```

If a `tvTest {}` DSL block is needed (non-default variant, emulator config), add it. Use only the
detected values — never add config that matches the default.

### 4c. Run `:tvTestSetup`

```bash
./gradlew :app:tvTestSetup
```

This generates all androidTest stubs, CI workflows, and drops the `/setup-tv-testing` skill into
`.claude/commands/` automatically.

### 4d. Analyze the project's screens and navigation

Before writing any test code, read the following to understand what the app actually does:

```
app/src/main/java/              — all Composable screens, ViewModels, navigation graphs
app/src/main/res/               — layouts if Leanback/Views
AndroidManifest.xml             — confirmed Activity name
```

Extract:
- Screen names and their entry-point composables
- Navigation flow: what screen appears on launch, what D-pad actions lead where
- Loading/progress indicators: any `CircularProgressIndicator`, `LinearProgressIndicator`,
  shimmer, or visibility-toggled loading states
- `testTag()` modifiers already present on composables
- Any `LaunchedEffect`, `collectAsState`, or data-loading patterns that mean the UI
  is async (must be waited for before asserting or capturing)

Print a one-paragraph summary of what you found. Do NOT write any test code yet.

### 4e. Fill in LOAD_SIGNAL_TEXT and AppTestTags

Ask the user:
> "What text is visible in your app when the main screen has fully loaded its data?
>  (This is what `waitUntilLoaded()` polls for — it can be a title, a tab label, or
>  any string that only appears once the first data fetch is complete.)"

Wait for the answer. Then:
- Write `LOAD_SIGNAL_TEXT` in `AppTestConsts.kt`
- If `testTag()` modifiers were found in step 4e, populate `AppTestTags.kt` with them.
  If none exist yet, add a comment listing the composables that need tags added.

### 4f. Write `UiTest.kt` — real test methods based on project understanding

Replace the `// TODO: write your @Test methods below` comment with actual tests inferred
from the screen analysis in 4d. Follow this pattern for every test:

```kotlin
@Test
fun testInitialScreenLoads() {
    screen.waitUntilLoaded()
    screen.assertLoadSignalVisible()
}

@Test
fun testNavigateToNextScreen() {
    screen.waitUntilLoaded()
    nav.down()          // move focus to first item
    nav.ok()            // select it
    // assert expected state on the next screen
    screen.assertTextVisible("Expected title")
}
```

Rules for writing UiTest:
- Every test starts with `screen.waitUntilLoaded()` — no exceptions.
- Use `screen.waitForTagOrSkip("tag")` for elements that only appear in some flavors.
- Use `assert.waitForVisible("tag")` before asserting anything that loads async.
- Never hardcode sleep/delay — use wait helpers only.
- Write one test per user flow, not one test per assertion.
- If a loading spinner is present, call `assert.waitForVisible` on the content behind it
  rather than asserting on the spinner itself.

### 4h. Write initial screenshot — always first

Always write this as the first test in `ScreenshotTest.kt`:

```kotlin
@Test
fun screenshot_01_initialScreen() {
    screen.waitUntilLoaded()
    capture.capture("01_initial_screen.png")
}
```

This establishes the baseline. Do NOT write any further screenshot tests yet.

### 4h. Ask the user for additional screenshot scenarios

After writing the initial screenshot test, ask:

> "The initial screen screenshot is set up. What other screenshot scenarios do you need?
>  For example:
>  - After navigating to a specific section (e.g. 'after opening the Sports tab')
>  - A focused/selected state (e.g. 'a content card focused')
>  - An error or empty state
>  - A player or detail screen
>
>  Describe each scenario in plain English and I'll write the test."

Wait for the user's response. Then for each scenario described, write a `@Test` method
following these rules:

Rules for screenshot tests:
- File names must be zero-padded and descriptive: `02_sports_tab.png`, `03_card_focused.png`
- Every test starts with `screen.waitUntilLoaded()`.
- Navigation must use `nav.down()`, `nav.right()`, `nav.ok()`, `nav.back()` — never
  click by coordinate.
- If navigating to a new screen, call `assert.waitForVisible("tag")` or
  `assert.waitForText("text")` on content in that screen before capturing.
- If a progress bar or loading spinner is present after navigation, call
  `assert.waitForVisible("content_tag")` to wait for it to disappear before `capture.capture()`.
- Never capture while a loading indicator might still be on screen.
- Call `capture.capture("filename.png")` as the LAST line of each test.

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
  ✓ CI workflow: .github/workflows/codedtx-ui-tests.yml     (push-triggered; screenshots chain automatically via needs: [ui-tests])
  ✓ CI workflow: .github/workflows/codedtx-screenshots.yml  (manual workflow_dispatch only)
  ✓ Test stubs: UiTest.kt, ScreenshotTest.kt
  ✓ Infrastructure: AppScreen.kt, AppTestTags.kt, AppTestConsts.kt

Developer work remaining:
  1. Add testTag() modifiers to composables listed in AppTestTags.kt (if any were missing)
  2. Review and extend @Test methods in UiTest.kt as the app grows
  3. Run /setup-tv-testing again any time to add more screenshot scenarios
  4. Push to trigger codedtx-ui-tests.yml CI workflow
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
- Never write a screenshot test without first calling `screen.waitUntilLoaded()`.
- Never capture a screenshot while a loading indicator may still be visible — always wait for content.
- Never write screenshot scenarios without asking the user first (except the mandatory initial screen).
- Never use hardcoded sleeps or `Thread.sleep()` in any test — use wait helpers only.
- Screenshot filenames must be zero-padded and descriptive (`01_`, `02_`, etc.).
- Never pass a build-type-suffixed applicationId (e.g. `com.example.tv.debug`) as `app-id` — always use the base applicationId from `defaultConfig`. The CI workflow discovers the debug instrumentation package automatically.
