package com.pgssoft.testwarez.test.steps

import com.pgssoft.testwarez.test.pages.AgendaPage
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

/**
 * Created by lfrydrych on 15.12.2017.
 */

class AgendaSteps {

    private val agendaPage = AgendaPage()

    @When("^I tap on search icon$")
    fun whenITapOnSearchIcon() {
        agendaPage.tapOnSearchIcon()
    }

    @When("I tap on filter icon")
    fun whenITapOnFilterIcon() {
        agendaPage.tapOnFilterIcon()
    }

    @When("I change orientation with UiAutomator")
    fun whenIChangeOrientationToLandscape() {
        agendaPage.changeOrientationWithUiAutomator()
    }

    @When("I change orientation with Espresso")
    fun whenIChangeOrientationWithEspresso() {
        agendaPage.changeOrientationWithEspresso()
    }

    @And("^I type \"(.+)\" into input field$")
    fun andITypeEventNameIntoInputFields(searchedText: String) {
        agendaPage.typeEventNameInSearchBox(searchedText)
    }

    @And("^I tap on first 2 days$")
    fun andITapOnFirst2Days() {
        agendaPage.tapOnFilterDateOne()
        agendaPage.tapOnFilterDateTwo()
    }

    @And("^I confirm filters$")
    fun andIConfirm() {
        agendaPage.confirmFilters()
    }

    @Then("^Search input opens$")
    fun thenSearchInputOpens() {
        agendaPage.checkSearchInputOpens()
    }

    @Then("^I can see \"(.+)\" on a list$")
    fun thenICanSeeGivenEventOnAList(resultedText: String) {
        agendaPage.checkEventTitleOnAList(resultedText)
    }

    @Then("^I can see only events for 3rd day$")
    fun thenICanSeeOnlyEventsFor3rdDay() {
        agendaPage.checkOnlyDay3rdEventsAreDisplayed()
    }

    @When("Navigation drawer has been opened")
    fun andNavigationDrawerHasBeenOpened() {
        agendaPage.openNavigationDrawer()
    }

    @And("I navigate to item \"(.+)\"")
    fun andINavigateToItemAtIndex(item: String) {
        agendaPage.navigateToItem(item)
    }

    @And("I choose speaker \"(.+)\"")
    fun andIChooseSpeaker(speaker: String) {
        agendaPage.chooseSpeaker(speaker)
    }

    @And("I scroll to Contacts section and tap on email address")
    fun andIScrollToContactsSection() {
        agendaPage.scrollToContacts()
        agendaPage.tapOnEmailAddress()
        agendaPage.typeEmailDetails()
    }
}
