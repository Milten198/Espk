package com.pgssoft.testwarez.test.steps;

import com.pgssoft.testwarez.test.pages.AgendaPage;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Created by lfrydrych on 15.12.2017.
 */

public class AgendaSteps {

    private AgendaPage agendaPage = new AgendaPage();

    @When("I tap on search icon")
    public void whenITapOnSearchIcon() {
        agendaPage.tapOnSearchIcon();
    }

    @Then("Search input opens")
    public void thenSearchInputOpens() {
        agendaPage.checkSearchInputOpens();
    }
}
