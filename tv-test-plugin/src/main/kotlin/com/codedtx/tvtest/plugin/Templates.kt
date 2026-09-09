package com.codedtx.tvtest.plugin

object Templates {

    // ── CI Workflows ──────────────────────────────────────────────────────────

    fun uiTestsWorkflow(module: String, flavorGradle: String, appId: String) = """
name: UI Tests

on:
  push:
    branches: [ main, dev, 'feat/**' ]
  workflow_dispatch:

jobs:
  ui-tests:
    uses: codedtx/android-tv-testing/.github/workflows/tv-ui-tests.yml@v1
    with:
      module: $module
      flavor: $flavorGradle
      app-id: $appId
    secrets:
      CODEDTX_GITHUB_TOKEN: ${'$'}{{ secrets.CODEDTX_GITHUB_TOKEN }}
""".trimIndent()

    fun screenshotsWorkflow(module: String, flavorGradle: String, appId: String) = """
name: Screenshots

on:
  workflow_dispatch:

jobs:
  screenshots:
    uses: codedtx/android-tv-testing/.github/workflows/tv-screenshots.yml@v1
    with:
      module: $module
      flavor: $flavorGradle
      app-id: $appId
    secrets:
      CODEDTX_GITHUB_TOKEN: ${'$'}{{ secrets.CODEDTX_GITHUB_TOKEN }}
""".trimIndent()

    // ── androidTest Stubs ─────────────────────────────────────────────────────

    fun uiTestStub(pkg: String) = """
package $pkg

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.codedtx.tvtest.TvAssertions
import com.codedtx.tvtest.TvNavigator
import ${pkg}.test.AppScreen
import ${pkg}.test.AppTestConsts
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TODO_YourActivity>()
    //                                             ^^^^^^^^^^^^^^^^^^
    //                                             Replace with your Activity class

    private lateinit var screen: AppScreen
    private lateinit var nav: TvNavigator
    private lateinit var assert: TvAssertions

    @Before
    fun setUp() {
        screen = AppScreen(composeTestRule)
        nav    = TvNavigator(composeTestRule)
        assert = TvAssertions(composeTestRule)
    }

    // TODO: write your @Test methods below.
    //
    // Available on screen: waitUntilLoaded(), assertLoadSignalVisible(),
    //   assertLiveButtonVisible(), assertTextVisible(), assertTagFocused(),
    //   assertContentVisible(), waitForTagOrSkip(), navigateRightUntilFocused()
    //
    // Available on nav: up(), down(), left(), right(), ok(), back()
    //
    // Available on assert: waitForText(), waitForVisible(), assertFocused()
    //
    // Pattern for each test:
    //   screen.waitUntilLoaded()
    //   // navigate or interact
    //   // assert the expected state
}
""".trimIndent()

    fun screenshotTestStub(pkg: String) = """
package $pkg

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.codedtx.tvtest.TvNavigator
import com.codedtx.tvtest.TvScreenCapture
import ${pkg}.test.AppScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TODO_YourActivity>()
    //                                             ^^^^^^^^^^^^^^^^^^
    //                                             Replace with your Activity class

    private lateinit var screen: AppScreen
    private lateinit var nav: TvNavigator
    private lateinit var capture: TvScreenCapture

    @Before
    fun setUp() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        screen  = AppScreen(composeTestRule)
        nav     = TvNavigator(composeTestRule)
        capture = TvScreenCapture(composeTestRule, device)
    }

    // TODO: write your screenshot scenarios below.
    // Pattern for each screenshot:
    //   screen.waitUntilLoaded()
    //   // navigate to the desired UI state
    //   capture.capture("descriptive_name.png")
}
""".trimIndent()

    fun screenObjectStub(pkg: String) = """
package ${pkg}.test

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.codedtx.tvtest.TvScreenObject

class AppScreen(rule: ComposeTestRule) : TvScreenObject(
    rule           = rule,
    loadSignalText = AppTestConsts.LOAD_SIGNAL_TEXT,
    liveButtonText = AppTestConsts.LIVE_BUTTON_TEXT,
    loadTimeoutMs  = AppTestConsts.LOAD_TIMEOUT_MS,
) {
    // Add screen-specific helpers here as your test suite grows.
    //
    // Inherited from TvScreenObject:
    //   waitUntilLoaded(), waitForTagOrSkip(), assertLoadSignalVisible(),
    //   assertLiveButtonVisible(), assertTextVisible(), assertTagVisible(),
    //   assertTagFocused(), assertTextFocused(), assertContentVisible(),
    //   clickTag(), navigateRightUntilFocused()
}
""".trimIndent()

    fun testTagsStub(pkg: String) = """
package ${pkg}.test

object AppTestTags {
    // Add semantic test tags that match the testTag() modifiers in your composables.
    //
    // Example:
    //   const val FILTER_BAR   = "filter_bar"
    //   fun filterChip(id: String) = "filter_chip_${'$'}id"
}
""".trimIndent()

    fun testConstantsStub(pkg: String) = """
package ${pkg}.test

object AppTestConsts {

    // Text that appears in your UI when the screen has fully loaded data.
    // waitUntilLoaded() polls for this — set it to match your composable exactly.
    const val LOAD_SIGNAL_TEXT = "TODO: text visible when screen is ready"

    // Text on your live/go-live button. Remove LIVE_BUTTON_TEXT usage if not applicable.
    const val LIVE_BUTTON_TEXT = "LIVE"

    // Ceiling for event-driven waits. 30s covers real API calls on slow CI networks.
    // waitUntilLoaded() proceeds immediately when the signal appears — this is not a sleep.
    const val LOAD_TIMEOUT_MS  = 30_000L

    // Add project-specific constants here as needed.
    // Examples: tab names, filter chip labels, section headers, expected counts.
}
""".trimIndent()
}
