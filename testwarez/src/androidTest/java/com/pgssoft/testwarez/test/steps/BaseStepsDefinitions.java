package com.pgssoft.testwarez.test.steps;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.pgssoft.testwarez.feature.landingpage.LandingPageActivity;
import com.pgssoft.testwarez.test.utils.ActivityFinisher;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;

/**
 * Created by lfrydrych on 15.12.2017.
 */

public class BaseStepsDefinitions extends ActivityInstrumentationTestCase2<LandingPageActivity> {

    private Activity mActivity;
    private Context mInstrumentationContext;
    private Context mAppContext;

    public BaseStepsDefinitions() {
        super(LandingPageActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mInstrumentationContext = getInstrumentation().getContext();
        mAppContext = getInstrumentation().getTargetContext();
        mActivity = getActivity();
    }

    @After
    public void tearDown() throws Exception {
        ActivityFinisher.finishOpenActivities();
        getActivity().finish();
        super.tearDown();
    }

    @Given("App has started")
    public void givenAppHasStarted() throws InterruptedException {
        Thread.sleep(8000);
    }
}
