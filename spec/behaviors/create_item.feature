Feature: Create Item
  As a client
  I want to create an Item via POST /items
  So that the server persists and returns an id

  Scenario: successful create
    Given a valid Item payload
    When I POST the payload to /items
    Then the response status is 201
    And the response body contains an id

