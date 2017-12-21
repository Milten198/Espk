package com.pgssoft.testwarez.test.steps

import android.app.Activity
import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.uiautomator.UiDevice
import android.test.ActivityInstrumentationTestCase2
import com.pgssoft.testwarez.feature.landingpage.LandingPageActivity
import com.pgssoft.testwarez.test.utils.ActivityFinisher
import com.pgssoft.testwarez.test.utils.UiAutomatorUtilsKotlin
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import junit.framework.Assert

/**
 * Created by lfrydrych on 15.12.2017.
 */

class BaseStepsDefinitions : ActivityInstrumentationTestCase2<LandingPageActivity>(LandingPageActivity::class.java) {

    private var mActivity: Activity? = null
    private var mInstrumentationContext: Context? = null
    private var mAppContext: Context? = null
    private var device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        mInstrumentationContext = instrumentation.context
        mAppContext = instrumentation.targetContext
        mActivity = activity
    }

    @After
    @Throws(Exception::class)
    public override fun tearDown() {
        ActivityFinisher.finishOpenActivities()
        activity.finish()
        super.tearDown()
    }

    @And("App has started")
    @Throws(InterruptedException::class)
    fun givenAppHasStarted() {
        Thread.sleep(6000)
        Assert.assertNotNull(mActivity)
    }

    @Given("Permission has been granted")
    @Throws(InterruptedException::class)
    fun givenPermissionHasBeenGranted() {
        UiAutomatorUtilsKotlin.grantPermission(device)
    }

}
