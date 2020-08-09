Feature: E2E test for API books store
  Background: User generates token for Authorisation
    Given I am an authorized user

    Scenario: Authorized user is able to add/remove books
      Given A list of books are available
      When I add a book to my reading list
      Then The book is added
      When I remove a book from my reading list
      Then The book is removed