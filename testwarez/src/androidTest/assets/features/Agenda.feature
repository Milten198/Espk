Feature: TestWarez Agenda Search

  Scenario: Open search input
    Given App has started
    When I tap on search icon
    Then Search input opens

  Scenario: Find given event by search box
    Given App has started
    When I tap on search icon
    And I type into input field
    Then I can see given event on a list

  Scenario: Filter events by day
    Given App has started
    When I tap on filter icon
    And I tap on first 2 days
    And I confirm filters
    Then I can see only events for 3rd day




