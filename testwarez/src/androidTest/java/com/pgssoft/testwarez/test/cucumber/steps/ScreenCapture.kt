package com.pgssoft.testwarez.test.cucumber.steps

import android.graphics.Bitmap
import android.support.test.runner.screenshot.ScreenCaptureProcessor
import android.support.test.runner.screenshot.Screenshot

import java.io.IOException

/**
 * Created by lfrydrych on 19.01.2018.
 */

class ScreenCapture : ScreenCaptureProcessor {
    @Throws(IOException::class)
    override fun process(capture: android.support.test.runner.screenshot.ScreenCapture): String? {
        return null
    }

    fun captureScreenshot(name: String) {
        val capture = Screenshot.capture()
        capture.format = Bitmap.CompressFormat.PNG
        capture.name = name
        try {
            capture.process()
        } catch (ex: IOException) {
            throw IllegalStateException(ex)
        }

    }
}
