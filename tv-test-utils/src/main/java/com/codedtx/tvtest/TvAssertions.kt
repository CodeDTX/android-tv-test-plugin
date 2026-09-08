package com.codedtx.tvtest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText

class TvAssertions(
    private val rule: ComposeTestRule,
    private val defaultTimeoutMs: Long = 30_000L,
) {

    fun assertFocused(testTag: String) =
        rule.onNodeWithTag(testTag).assertIsFocused()

    fun assertVisible(testTag: String) =
        rule.onNodeWithTag(testTag).assertIsDisplayed()

    fun assertTextVisible(text: String) =
        rule.onNodeWithText(text).assertIsDisplayed()

    fun waitForVisible(testTag: String, timeoutMs: Long = defaultTimeoutMs) {
        rule.waitUntil(timeoutMillis = timeoutMs) {
            rule.onAllNodes(hasTestTag(testTag)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun waitForText(text: String, timeoutMs: Long = defaultTimeoutMs) {
        rule.waitUntil(timeoutMillis = timeoutMs) {
            rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
