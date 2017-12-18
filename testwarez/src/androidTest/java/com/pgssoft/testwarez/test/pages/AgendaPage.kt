package com.pgssoft.testwarez.test.pages

import com.pgssoft.testwarez.R

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*

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

    fun typeEventNameInSearchBox(eventName: String) {
        onView(withId(R.id.search_src_text)).perform(typeText(eventName))
    }

    fun checkEventTitleOnAList() {
        onView(withId(R.id.title)).check(matches(withText("Automatyzacja testów przy pomocy Selenium w RobotFramework")))
    }
}
