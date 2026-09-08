package com.codedtx.tvtest

import android.os.Environment
import android.util.Log
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Assert.assertTrue
import java.io.File

class TvScreenCapture(
    private val rule: ComposeTestRule,
    private val device: UiDevice,
) {
    private val tag = "TvScreenCapture"

    val outputDir: File by lazy {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "screenshots")
        val ok = dir.mkdirs() || dir.exists()
        Log.i(tag, "Screenshot dir: ${dir.absolutePath} (exists=$ok)")
        assertTrue("Screenshot directory could not be created: ${dir.absolutePath}", dir.exists())
        dir
    }

    fun capture(filename: String) {
        rule.waitForIdle()
        val file = File(outputDir, if (filename.endsWith(".png")) filename else "$filename.png")
        val ok = device.takeScreenshot(file)
        Log.i(tag, "takeScreenshot → $ok | ${file.absolutePath}")
        Log.i(tag, "exists=${file.exists()} size=${if (file.exists()) file.length() else -1L} bytes")
        assertTrue("takeScreenshot() returned false for $filename", ok)
        assertTrue("Screenshot file missing: ${file.absolutePath}", file.exists())
        assertTrue("Screenshot file is empty: ${file.absolutePath}", file.length() > 0)
    }
}
