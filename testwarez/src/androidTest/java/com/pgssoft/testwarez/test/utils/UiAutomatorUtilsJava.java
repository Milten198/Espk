package com.pgssoft.testwarez.test.utils;

import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import timber.log.Timber;

/**
 * Created by lfrydrych on 21.12.2017.
 */

public class UiAutomatorUtilsJava {

    public UiAutomatorUtilsJava() {
    }

    public static void grantPermission(UiDevice device) throws UiObjectNotFoundException {
        UiObject permissionEntry = device.findObject(new UiSelector().text("ALLOW"));
        if (permissionEntry.exists()) {
            try {
                permissionEntry.click();
            } catch (UiObjectNotFoundException e) {
                Timber.e(e, "There is no permissions dialog to interact with ");
            }
        }
    }
}
