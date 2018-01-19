package com.pgssoft.testwarez.test.utils

import com.pgssoft.testwarez.R

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId

/**
 * Created by lfrydrych on 09.01.2018.
 */

class CustomScrollActions {

    fun tapOn() {
        onView(withId(R.id.search_button)).perform(click())
    }

}
