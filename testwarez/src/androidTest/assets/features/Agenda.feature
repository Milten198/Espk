Feature: TestWarez Agenda

    Scenario: Open search input
    Given Permission has been granted
    And App has started
    When Navigation drawer has been opened
    And I navigate to item "Prelegenci"
    And I choose speaker "Artur Król"
    And I scroll to Contacts section and tap on email address

#  Scenario: Open search input
#    Given Permission has been granted
#    And App has started
#    When I tap on search icon
#    Then Search input opens


#  Scenario: Change app orientation
#    Given Permission has been granted
#    And App has started
#    When I change orientation to landscape


#  @second
#  Scenario Outline: Find given event by search box
#    Given Permission has been granted
#    And App has started
#    When I tap on search icon
#    And I type "<searchedText>" into input field
#    Then I can see "<resultedText>" on a list
#    Examples:
#      | searchedText | resultedText               |
#      | VOLVO POLSKA | Testowanie w Volvo Group I |

#  @third
#  Scenario: Filter events by day
#    Given Permission has been granted
#    And App has started
#    When I tap on filter icon
#    And I tap on first 2 days
#    And I confirm filters
#    Then I can see only events for 3rd day