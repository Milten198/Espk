package com.pgssoft.testwarez.test.utils;

import cucumber.api.CucumberOptions;

@CucumberOptions(
        features = "features",
        glue = {"com.pgssoft.testwarez.test.steps"},
        plugin = {"pretty"}
)

public class CucumberTestCase {
}