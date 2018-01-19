package com.pgssoft.testwarez.test.cucumber.runner

/**
 * Created by mnicpon on 07/11/2017.
 */

import android.os.Bundle

import cucumber.api.android.CucumberInstrumentationCore

class CucumberTestRunner : android.support.test.runner.AndroidJUnitRunner() {

    private val instrumentationCore = CucumberInstrumentationCore(this)

    override fun onCreate(bundle: Bundle) {
        super.onCreate(bundle)
        instrumentationCore.create(bundle)
    }

    override fun onStart() {
        waitForIdleSync()
        instrumentationCore.start()
    }
}
