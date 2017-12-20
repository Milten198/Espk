package com.pgssoft.testwarez.test.pages

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.v7.widget.RecyclerView
import android.view.View
import com.pgssoft.testwarez.R
import com.pgssoft.testwarez.test.utils.CustomMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

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

    fun typeEventNameInSearchBox() {
        Thread.sleep(6000)
        onView(withId(R.id.search_src_text)).check(matches(isDisplayed()))
        onView(withId(R.id.search_src_text)).perform(typeText("VOLVO POLSKA"))
    }

    fun checkEventTitleOnAList() {
        Thread.sleep(1000)
        onView(withId(R.id.agenda_recycler_view))
                .check(matches(CustomMatcher.atPosition(1, hasDescendant(allOf(withId(R.id.title), withText("VOLVO POLSKA | Testowanie w Volvo Group I"))))))
    }

    fun tapOnFilterIcon() {
        onView(withId(R.id.main_menu_filter))
                .perform(click())
    }

    fun tapOnFilterDateOne() {
        Thread.sleep(3000)
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
                .check(matches(CustomMatcher.atPosition(0, hasDescendant(allOf(withId(R.id.tvItemArchiveConferenceHeaderTitle), withText("3 DZIEŃ - piątek 17 listopada"))))))
    }
}
