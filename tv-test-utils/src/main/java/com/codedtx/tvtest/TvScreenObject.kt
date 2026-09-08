package com.codedtx.tvtest

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assume

open class TvScreenObject(
    protected val rule: ComposeTestRule,
    val loadSignalText: String,           // text that appears when the screen is ready
    val liveButtonText: String = "LIVE",  // text on the live/go-live button, if present
    val loadTimeoutMs: Long    = 30_000L, // max wait — covers slow CI networks
) {
    private val nav = TvNavigator(rule)

    fun waitUntilLoaded() {
        rule.waitUntil(timeoutMillis = loadTimeoutMs) {
            rule.onAllNodes(hasText(loadSignalText))
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.waitForIdle()
    }

    fun waitForTagOrSkip(testTag: String, skipMessage: String = "Element '$testTag' not present") {
        val appeared = runCatching {
            rule.waitUntil(timeoutMillis = loadTimeoutMs) {
                rule.onAllNodes(hasTestTag(testTag)).fetchSemanticsNodes().isNotEmpty()
            }
        }.isSuccess
        Assume.assumeTrue(skipMessage, appeared)
    }

    fun assertLoadSignalVisible() =
        rule.onNodeWithText(loadSignalText).assertIsDisplayed()

    fun assertLiveButtonVisible() =
        rule.onNodeWithText(liveButtonText).assertIsDisplayed()

    fun assertTextVisible(text: String) =
        rule.onNodeWithText(text).assertIsDisplayed()

    fun assertTagVisible(testTag: String) =
        rule.onNodeWithTag(testTag).assertIsDisplayed()

    fun assertTagFocused(testTag: String) =
        rule.onNodeWithTag(testTag).assertIsFocused()

    fun assertTextFocused(text: String) =
        rule.onNodeWithText(text).assertIsFocused()

    fun assertContentVisible(title: String) =
        rule.onNodeWithText(title, substring = true).assertIsDisplayed()

    fun clickTag(testTag: String) {
        rule.onNodeWithTag(testTag).performClick()
        rule.waitForIdle()
    }

    fun navigateRightUntilFocused(testTag: String, maxPresses: Int = 10) {
        val isFocused = SemanticsMatcher("isFocused") {
            it.config.getOrElseNullable(SemanticsProperties.Focused) { null } == true
        }
        repeat(maxPresses) {
            val focused = rule.onAllNodes(hasTestTag(testTag).and(isFocused))
                .fetchSemanticsNodes().isNotEmpty()
            if (focused) return
            nav.right()
        }
    }
}
