package com.pgssoft.testwarez.test.runner; /**
 * Created by mnicpon on 07/11/2017.
 */

import android.os.Bundle;

import cucumber.api.android.CucumberInstrumentationCore;

public class CucumberTestRunner extends android.support.test.runner.AndroidJUnitRunner {

    private final CucumberInstrumentationCore instrumentationCore = new CucumberInstrumentationCore(this);

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        instrumentationCore.create(bundle);
    }

    @Override
    public void onStart() {
        waitForIdleSync();
        instrumentationCore.start();
    }
}
