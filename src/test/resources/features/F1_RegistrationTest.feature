@F1 @Registration @Ready
Feature: As a new user, I want to create an account using valid registration details so that I can access the application with my own account.

  Background:
    Given User opens the registration page link
    And registration account type step is displayed

  @TC-001 @CreatesData @Ready
  Scenario: User should be able to register a personal account with valid details
    When User selects Account Type as "Personal" and continues registration
    And User enters registration email "uniqueEmail" password "Testing123!" name "Test User" address "100 Auto Main Street" and date of birth "1990-01-01"
    And User submits the registration form
    Then User should successfully complete registration

  @TC-002 @CreatesData @Ready
  Scenario: User should not be able to register with an already registered email
    When User selects Account Type as "Personal" and continues registration
    And User enters registration email "uniqueEmail" password "Testing123!" name "Test User" address "100 Auto Main Street" and date of birth "1990-01-01"
    And User submits the registration form
    Then User should successfully complete registration
    And User logs out
    Given User opens the registration page link
    And registration account type step is displayed
    When User selects Account Type as "Personal" and continues registration
    And User enters registration email "currentRegistrationEmail" password "Testing123!" name "Test User" address "100 Auto Main Street" and date of birth "1990-01-01"
    And User submits the registration form
    Then User should see a duplicate email registration error

  @TC-003 @Ready
  Scenario Outline: User should not be able to register a personal account when registration details are invalid
    When User selects Account Type as "Personal" and continues registration
    And User enters registration email "<email>" password "<password>" name "<name>" address "<address>" and date of birth "<dateOfBirth>"
    And User submits the registration form
    Then User should see a registration validation error
    Examples:
      | email               | password    | name      | address              | dateOfBirth |
      | invalid-email-input | Testing123! | Test User | 100 Auto Main Street | 1990-01-01  |
      | uniqueEmail         | 12345       | Test User | 100 Auto Main Street | 1990-01-01  |
      | blank               | Testing123! | Test User | 100 Auto Main Street | 1990-01-01  |
      | uniqueEmail         | blank       | Test User | 100 Auto Main Street | 1990-01-01  |
      | uniqueEmail         | Testing123! | blank     | 100 Auto Main Street | 1990-01-01  |
      | uniqueEmail         | Testing123! | Test User | blank                | 1990-01-01  |
      | uniqueEmail         | Testing123! | Test User | 100 Auto Main Street | blank       |
      | uniqueEmail         | Testing123! | Test User | 100 Auto Main Street | underageDob |
    
  # As of writing this comment, we are unable to create a business account due to the backend schema not matching the front-end schema 
  # (front-end asks for business number, but backend is still looking for dateOfBirth). Leave this as @NotReady until that issue has been resolved.
  @TC-004 @CreatesData @NotReady
  Scenario: User should be able to register a business account with valid details
    When User selects Account Type as "Business" and continues registration
    And User enters registration email "uniqueEmail" password "Testing123!" name "Test Company" address "100 Auto Main Street" and business number "123456789"
    And User submits the registration form
    Then User should successfully complete registration

  @TC-005 @Ready
  Scenario Outline: User should not be able to register a business account when business number is invalid
    When User selects Account Type as "Business" and continues registration
    And User enters registration email "<email>" password "<password>" name "<companyName>" address "<address>" and business number "<businessNumber>"
    And User submits the registration form
    Then User should see a registration validation error
    Examples:
      | email       | password    | companyName  | address              | businessNumber |
      | uniqueEmail | Testing123! | Test Company | 100 Auto Main Street | blank          |
      | uniqueEmail | Testing123! | Test Company | 100 Auto Main Street | 12345          |
      | uniqueEmail | Testing123! | Test Company | 100 Auto Main Street | abcdefghi      |