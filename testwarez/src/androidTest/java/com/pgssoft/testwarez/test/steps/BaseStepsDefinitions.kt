package com.pgssoft.testwarez.test.steps

import android.app.Activity
import android.content.Context
import android.test.ActivityInstrumentationTestCase2
import com.pgssoft.testwarez.feature.landingpage.LandingPageActivity
import com.pgssoft.testwarez.test.utils.ActivityFinisher
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.Given
import junit.framework.Assert

/**
 * Created by lfrydrych on 15.12.2017.
 */

class BaseStepsDefinitions : ActivityInstrumentationTestCase2<LandingPageActivity>(LandingPageActivity::class.java) {

    private var mActivity: Activity? = null
    private var mInstrumentationContext: Context? = null
    private var mAppContext: Context? = null

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        mInstrumentationContext = instrumentation.context
        mAppContext = instrumentation.targetContext
        mActivity = activity
    }

//    @Before
//    @Throws(Exception::class)
//    fun grantPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            instrumentation.getUiAutomation().executeShellCommand(
//                    "pm grant " + InstrumentationRegistry.getTargetContext().packageName
//                            + " android.permission.WRITE_EXTERNAL_STORAGE"
//            )
//        }
//    }

    @After
    @Throws(Exception::class)
    public override fun tearDown() {
        ActivityFinisher.finishOpenActivities()
        activity.finish()
        super.tearDown()
    }

    @Given("App has started")
    @Throws(InterruptedException::class)
    fun givenAppHasStarted() {
        Assert.assertNotNull(mActivity)
    }


}
