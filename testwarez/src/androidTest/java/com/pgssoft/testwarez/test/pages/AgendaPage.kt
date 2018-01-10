package com.pgssoft.testwarez.test.pages

import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.DrawerMatchers
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.uiautomator.UiDevice
import com.pgssoft.testwarez.R
import com.pgssoft.testwarez.test.utils.CustomMatcherKotlin
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString


/**
 * Created by lfrydrych on 15.12.2017.
 */

class AgendaPage {

    fun tapOnSearchIcon() {
        onView(withId(R.id.search_button)).perform(click())
    }

    fun checkSearchInputOpens() {
        onView(withId(R.id.search_src_text)).check(matches(isDisplayed()))
    }

    fun typeEventNameInSearchBox(searchedText: String) {
//        Thread.sleep(6000)
        onView(withId(R.id.search_src_text)).check(matches(isDisplayed()))
        onView(withId(R.id.search_src_text)).perform(typeText(searchedText))
    }

    fun checkEventTitleOnAList(resultedText: String) {
        Thread.sleep(1000)
        onView(withId(R.id.agenda_recycler_view))
                .check(matches(CustomMatcherKotlin.atPosition(1, hasDescendant(allOf(withId(R.id.title), withText(containsString(resultedText)))))))
    }

    fun tapOnFilterIcon() {
        onView(withId(R.id.main_menu_filter))
                .perform(click())
    }

    fun tapOnFilterDateOne() {
//        Thread.sleep(3000)
        onView(allOf(withId(R.id.tvItemFilterDate), withText("Środa, 15.11.2017")))
                .perform(click())
    }

    fun tapOnFilterDateTwo() {
        onView(allOf(withId(R.id.tvItemFilterDate), withText("Czwartek, 16.11.2017")))
                .perform(click())
    }

    fun confirmFilters() {
        onView(withId(R.id.bActivityFilterApply))
                .perform(click())
        // Thread.sleep(3000)
    }

    fun checkOnlyDay3rdEventsAreDisplayed() {
        onView(withId(R.id.agenda_recycler_view))
                .check(matches(CustomMatcherKotlin.atPosition(0, hasDescendant(allOf(withId(R.id.tvItemArchiveConferenceHeaderTitle), withText("3 DZIEŃ - piątek 17 listopada"))))))
    }

    fun changeOrientationToLandscape() {
        val device = UiDevice.getInstance(getInstrumentation())
        device.setOrientationLeft()
        device.setOrientationNatural()
        device.setOrientationRight()
    }

    fun openNavigationDrawer() {
        onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(ViewMatchers.withId(R.id.drawer_layout)).check(ViewAssertions.matches(DrawerMatchers.isOpen()))
    }

    fun navigateToItem(item: String) {
        Thread.sleep(3000)
        onView(ViewMatchers.withText(item)).check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())
    }

    fun chooseSpeaker(speaker: String) {
        Thread.sleep(3000)
        onView(withText(speaker)).perform(click())
    }

    fun scrollToContacts() {
        Thread.sleep(2000)
        onView(withId(R.id.description)).perform(swipeUp()).perform(swipeUp()).perform(swipeUp())
        onView(withText("KONTAKT")).check(matches(isDisplayed()))
    }

    fun tapOnEmailAddress() {
        Thread.sleep(2000)
        onView(withId(R.id.email)).perform(click())
    }

}
