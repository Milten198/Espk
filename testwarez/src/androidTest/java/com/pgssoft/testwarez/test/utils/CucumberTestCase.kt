package com.pgssoft.testwarez.test.utils

import cucumber.api.CucumberOptions

@CucumberOptions(features = arrayOf("features"),
        glue = arrayOf("com.pgssoft.testwarez.test.steps"),
        plugin = arrayOf("pretty"))
class CucumberTestCase