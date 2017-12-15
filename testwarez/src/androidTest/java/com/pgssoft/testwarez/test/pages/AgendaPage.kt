package com.pgssoft.testwarez.test.pages

import com.pgssoft.testwarez.R

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId

/**
 * Created by lfrydrych on 15.12.2017.
 */

class AgendaPage {

    fun tapOnSearchIcon() {
        onView(withId(R.id.search_bar)).perform(click())
    }

    fun checkSearchInputOpens() {
        onView(withId(R.id.search_src_text)).check(matches(isDisplayed()))
    }
}
