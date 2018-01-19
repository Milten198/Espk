package com.pgssoft.testwarez.test.test

import cucumber.api.CucumberOptions

@CucumberOptions(features = arrayOf("features"),
        glue = arrayOf("com.pgssoft.testwarez.test.cucumber.steps"),
        plugin = arrayOf("pretty",
                "html:/mnt/sdcard/cucumber-reports/cucumber-html-report",
                "json:/mnt/sdcard/cucumber-reports/cucumber.json",
                "junit:/mnt/sdcard/cucumber-reports/cucumber.xml"),
        tags = arrayOf("@agenda, @test1, @test2"))
class CucumberTestCase