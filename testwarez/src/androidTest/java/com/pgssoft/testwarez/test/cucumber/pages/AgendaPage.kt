package com.pgssoft.testwarez.test.pages

import android.support.test.InstrumentationRegistry
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
import android.support.test.uiautomator.UiSelector
import android.view.View
import com.pgssoft.testwarez.R
import com.pgssoft.testwarez.test.cucumber.steps.ScreenCapture
import com.pgssoft.testwarez.test.utils.CustomMatcherKotlin
import com.pgssoft.testwarez.test.utils.OrientationChangeAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString


/**
 * Created by lfrydrych on 15.12.2017.
 */

class AgendaPage {

    private var device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private var searchButton = withId(R.id.search_button)
    private var searchBar = withId(R.id.search_bar)
    private var searchSrcText = withId(R.id.search_src_text)
    private var agendaList = withId(R.id.agenda_recycler_view)
    private var title = withId(R.id.title)
    private var mainMenuFilter = withId(R.id.main_menu_filter)
    private var itemFilterDateOne = allOf(withId(R.id.tvItemFilterDate), withText("Środa, 15.11.2017"))
    private var itemFilterDateTwo = allOf(withId(R.id.tvItemFilterDate), withText("Czwartek, 16.11.2017"))
    val screenCapture = ScreenCapture()

    fun tapOnSearchIcon() {
        onView(searchButton).perform(click())
    }

    fun checkSearchInputOpens() {
        onView(searchBar).check(matches(isDisplayed()))
    }

    fun typeEventNameInSearchBox(searchedText: String) {
        onView(searchSrcText).perform(typeText(searchedText))
    }

    fun checkEventTitleOnAList(resultedText: String) {
        Thread.sleep(1000)
        onView(agendaList)
                .check(matches(CustomMatcherKotlin.atPosition(1, hasDescendant(allOf(title, withTextCoitains(resultedText))))))
    }

    private fun withTextCoitains(text: String): Matcher<View>? {
        return withText(containsString(text))
    }

    fun tapOnFilterIcon() {
        onView(mainMenuFilter)
                .perform(click())
    }

    fun tapOnFilterDateOne() {
//        Thread.sleep(3000)
        onView(itemFilterDateOne)
                .perform(click())
    }

    fun tapOnFilterDateTwo() {
        onView(itemFilterDateTwo)
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

    fun changeOrientationWithUiAutomator() {
        device.setOrientationLeft()
        device.setOrientationNatural()
        device.setOrientationRight()
    }

    fun changeOrientationWithEspresso() {
        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())
        Thread.sleep(3000)
        onView(isRoot()).perform(OrientationChangeAction.orientationPortrait())
    }

    fun openNavigationDrawer() {
        onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(ViewMatchers.withId(R.id.drawer_layout)).check(ViewAssertions.matches(DrawerMatchers.isOpen()))
        screenCapture.captureScreenshot("Pic of test 2")
    }

    fun navigateToItem(item: String) {
        onView(ViewMatchers.withText(item)).check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())
    }

    fun chooseSpeaker(speaker: String) {
        Thread.sleep(3000)
        onView(withText(speaker)).perform(click())
        screenCapture.captureScreenshot("Pic of test 2")
    }

    fun scrollToContacts() {
        Thread.sleep(3000)
        onView(withId(R.id.description)).perform(swipeUp()).perform(swipeUp()).perform(swipeUp())
        onView(withText("KONTAKT")).check(matches(isDisplayed()))
    }

    fun tapOnEmailAddress() {
        Thread.sleep(3000)
        onView(withId(R.id.email)).perform(click())
    }

    fun typeEmailDetails() {
        val emailTitle = device.findObject(UiSelector().text("Subject"))
        val emailAddress = device.findObject(UiSelector().resourceId("com.google.android.gm:id/to"))
        val sendButton = device.findObject(UiSelector().resourceId("com.google.android.gm:id/send"))

        emailAddress.clearTextField()
        emailAddress.setText("frydrychlu@gmail.com")

        emailTitle.click()
        emailTitle.setText("Wiadomosc")

        sendButton.click()
    }
}