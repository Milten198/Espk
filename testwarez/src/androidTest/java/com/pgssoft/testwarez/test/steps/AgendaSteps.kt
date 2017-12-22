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

    @And("^I type into input field$")
    fun andITypeEventNameIntoInputFields() {
        agendaPage.typeEventNameInSearchBox()
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

    @Then("^I can see given event on a list$")
    fun thenICanSeeGivenEventOnAList() {
        agendaPage.checkEventTitleOnAList()
    }

    @Then("^I can see only events for 3rd day$")
    fun thenICanSeeOnlyEventsFor3rdDay() {
        agendaPage.checkOnlyDay3rdEventsAreDisplayed()
    }
}
