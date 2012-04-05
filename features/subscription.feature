Feature: Subscriptions
  In order to allow subscriptions to be managed
  Should provide interactions on those subscriptions

Scenario: Admin page, unauthenticated
  Given I am not logged in
  When I go to the "subscription index" page
  Then I should be at the "login" page
  # And the response is a redirect

Scenario: Admin page, user
  Given a normal user is logged in
  When I go to the "subscription index" page
  Then I should be at the "login" page
  # And the response is a redirect

Scenario: Admin page, user
  Given I am logged in as an admin
  When I go to the "subscription index" page
  Then I should see a list of subscriptions
  # And the response is sucsessful

Scenario: Ostatus subscription page
  Given I am not logged in
  When I go to the "ostatus sub" page
  Then I should see a form

Scenario: User subscriptions, default
  Given I am not logged in
  And a user exists
  When I go to the "subscriptions" page for that user
  Then I should see a list of subscriptions
  # And I should get a "HTML" document

Scenario: User subscriptions, activity streams
  Given I am not logged in
  And a user exists
  When I request the "subscriptions" page for that user with a "as" format
  Then I should get a "as" document