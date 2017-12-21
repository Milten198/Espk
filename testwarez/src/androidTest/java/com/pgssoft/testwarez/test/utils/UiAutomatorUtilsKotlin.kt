package com.pgssoft.testwarez.test.utils

import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObjectNotFoundException
import android.support.test.uiautomator.UiSelector
import timber.log.Timber

/**
 * Created by lfrydrych on 21.12.2017.
 */

class UiAutomatorUtilsKotlin {
    companion object {

        @Throws(UiObjectNotFoundException::class)
        fun grantPermission(device: UiDevice) {
            val permissionEntry = device.findObject(UiSelector().text("ALLOW"))
            if (permissionEntry.exists()) {
                try {
                    permissionEntry.click()
                } catch (e: UiObjectNotFoundException) {
                    Timber.e(e, "There is no permissions dialog to interact with ")
                }

            }
        }
    }
}
