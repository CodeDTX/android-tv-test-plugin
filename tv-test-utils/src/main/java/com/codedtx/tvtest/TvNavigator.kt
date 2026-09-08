package com.codedtx.tvtest

import android.view.KeyEvent
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

class TvNavigator(private val rule: ComposeTestRule) {

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun up()    = dpad(KeyEvent.KEYCODE_DPAD_UP)
    fun down()  = dpad(KeyEvent.KEYCODE_DPAD_DOWN)
    fun left()  = dpad(KeyEvent.KEYCODE_DPAD_LEFT)
    fun right() = dpad(KeyEvent.KEYCODE_DPAD_RIGHT)
    fun ok()    = dpad(KeyEvent.KEYCODE_DPAD_CENTER)
    fun back() {
        device.pressBack()
        rule.waitForIdle()
    }

    private fun dpad(keyCode: Int) {
        device.pressKeyCode(keyCode)
        rule.waitForIdle()
    }
}
