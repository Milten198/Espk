package com.pgssoft.testwarez.test.steps

import com.pgssoft.testwarez.test.pages.AgendaPage

import cucumber.api.java.en.Then
import cucumber.api.java.en.When

/**
 * Created by lfrydrych on 15.12.2017.
 */

class AgendaSteps {

    private val agendaPage = AgendaPage()

    @When("I tap on search icon")
    fun whenITapOnSearchIcon() {
        agendaPage.tapOnSearchIcon()
    }

    @Then("Search input opens")
    fun thenSearchInputOpens() {
        agendaPage.checkSearchInputOpens()
    }
}
