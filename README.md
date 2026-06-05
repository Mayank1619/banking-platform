# UI Automation README [v1.0]

## Version History 

| Date       | Version | Notes |
|------------|---:|---|
| 2026-06-05 | 1.0 | Initial onboarding README for future QA contributors. |

## Project Overview

This project contains UI automation tests for the **Voltio banking web application**.

The purpose of this repository is to provide a maintainable Selenium/Cucumber automation suite that future QA contributors can expand as additional application features become available or require regression coverage.

The current suite focuses on browser-based end-to-end validation of the live hosted Voltio application.

## Intended Audience

This README is intended for future QA engineers who will be maintaining, expanding, and running the UI automation suite locally.

The guide assumes that contributors have some familiarity with:

- Java
- Selenium WebDriver
- Cucumber/Gherkin
- JUnit
- Page Object Model design

However, the project conventions are documented here so that new contributors can onboard consistently.

## Tech Stack

This project uses:

- **Java**
- **Maven**
- **Selenium WebDriver**
- **Cucumber**
- **JUnit Platform**
- **Page Object Model**
- **Cucumber HTML/JSON/JUnit reports**
> Note: This project uses **JUnit 5 / JUnit Platform**, not JUnit 4. Do not copy those patterns into this project (such as @RunWith); follow the existing `TestRunner` structure instead.

## Spec-Driven Development

This project follows a **Spec-Driven Development** approach.

All automation scenarios and test cases should be based on the relevant feature specification before they are implemented in Cucumber. QA contributors should not create new automation coverage only by exploring the UI or making assumptions about expected behavior.

Before adding or updating a feature test:

1. Review the feature's specification.
2. Identify the user flows, acceptance criteria, validation rules, and expected outcomes.
3. Convert those requirements into clear Cucumber scenarios.
4. Confirm which scenarios are ready for automation.
5. Mark blocked, incomplete, or uncertain scenarios as `@NotReady`.
6. Update the README if the feature introduces new framework conventions, setup requirements, or known limitations.

The development team uses **GitHub Spec Kit** as part of their workflow. QA contributors do **not** need to use GitHub Spec Kit directly to create 
or maintain automation tests. QAs only need to make sure their test cases are traceable back to the relevant feature specification.

Because spec file locations may vary, this README does not provide a single consistent link to every spec. When adding a new feature, document the relevant spec source in the feature file or PR notes when possible.

## Helpful Learning Resources

The following resources may be useful for contributors who are new to the tools or workflow used in this project.

> YouTube Video: [Spec-Driven Development: AI Assisted Coding Explained](https://www.youtube.com/watch?v=mViFYTwWvcM)

> YouTube Playlist: [GitHub Spec Kit Tutorial](https://www.youtube.com/playlist?list=PL4cUxeGkcC9h9RbDpG8ZModUzwy45tLjb)

> YouTube Video: [GitHub Spec Kit Demo](https://www.youtube.com/watch?v=a9eR1xsfvHg)

While the GitHub Spec Kit isn't being used for QA, it's good to get a basic understanding of what it is and how it functions.

Suggested topics include:

- Spec-Driven Development
- GitHub Spec Kit overview
- Selenium WebDriver with Java
- Cucumber/Gherkin basics
- Page Object Model design
- JUnit test execution

## Prerequisites

### Java

This project currently targets **Java 26**.

Use **Java 26 or higher** where possible. If the project is updated to use a newer JDK, make sure the full automation suite still runs successfully before committing the change.

Check your Java version with:

```bash
java --version
```

### Maven

This project uses Maven for dependency management and build configuration.

Use the most up-to-date stable Maven version available where possible. If Maven is updated, make sure the full automation suite still runs successfully before committing the change.

Check your Maven version with:

```bash
mvn --version
```

> Note: If `mvn` is not recognized on your machine, Maven may not be installed globally or may not be added to your system `PATH`. In that case, run tests directly through the `TestRunner` class from your IDE.

### IDE

This project is IDE-agnostic. You may use IntelliJ IDEA, Eclipse, VS Code, or another Java-capable IDE.

This project was created using IntelliJ IDEA, so if you're using IntelliJ IDEA:

- The `.idea` folder may contain local IDE configuration.
- If imports or test execution do not work immediately, reload Maven dependencies from the `pom.xml`.
- Make sure the project SDK is set to Java 26 or higher.
- Run the `TestRunner` class to execute the Cucumber suite.

## Browser Support

The automation framework currently supports the following browser values:

- `chrome`
- `edge`
- `firefox`
- `safari`
- `ie`

The active browser is controlled through `config.properties`.

Selenium Manager handles browser driver resolution automatically, so contributors should not need to manually download or configure browser drivers.

> Note: Browser availability still depends on the operating system. For example, Safari is only available on macOS, and Internet Explorer support depends on the local Windows/browser setup.

## Environment

All testing is currently performed against the hosted Voltio application.

There is no separate QA-local environment for this automation suite at the moment.

The hosted application URL should not be documented in this README or committed to the repository. Ask the team for the current URL and store it only in your local `config.properties` file.

## Configuration

Local environment configuration should be stored in:

```text
src/test/resources/config.properties
```

This file is intentionally ignored by Git because it may contain sensitive environment values, such as the hosted application URL.

Use the example config file as a template:

```text
src/test/resources/config-example.properties
```

To set up your local config:

1. Copy `config-example.properties`.
2. Rename the copy to `config.properties`.
3. Replace the placeholder values with the correct local values.
4. Do not commit `config.properties`.

Example shape:

```properties
baseUrl={INSERT_LINK_TO_APP_HERE}
browser={chrome | firefox | edge | safari | ie}
```

### Configuration Guidelines

Use `config.properties` for values that are expected to change between environments or local runs, such as:

- Base URL
- Browser choice

Do **not** commit sensitive environment values. As of now, this mainly refers to the hosted application URL.


Current project structure:

```text
Voltio-Selenium-UI-Automation
├── .idea
├── .mvn
├── src
│   ├── main
│   │   ├── java
│   │   │   └── ca.voltio
│   │   │       ├── pages
│   │   │       └── utils
│   │   │           ├── ConfigPropertiesUtils
│   │   │           └── DriverUtils
│   │   └── resources
│   └── test
│       ├── java
│       │   └── ca.voltio
│       │       ├── stepdefs
│       │       └── TestRunner
│       └── resources
│           ├── features
│           ├── testdata
│           └── config-example.properties
├── pom.xml
└── README.md
```

### Important Directories

#### `src/main/java/ca/voltio/pages`

Contains Page Object Model classes.

Each page object should represent a single page or meaningful application area. Common browser and element interaction helpers should remain in shared base classes where appropriate.

#### `src/main/java/ca/voltio/utils`

Contains reusable utilities used by the framework.

Current examples include:

- Browser driver management
- Configuration property reading

#### `src/test/resources/features`

Contains Cucumber `.feature` files written in Gherkin.

Feature files should describe user-facing behavior and should be understandable by QA contributors and other project stakeholders.

#### `src/test/java/ca/voltio/stepdefs`

Contains Cucumber step definition classes.

Step definitions should connect Gherkin steps to page object actions and assertions.

#### `src/test/java/ca/voltio/TestRunner.java`

Main local test runner for the Cucumber suite.

Run this class from your IDE to execute the configured test set.

#### `src/test/resources/config-example.properties`

Template file for local configuration.

Copy this file to `config.properties`, then fill in the required local values.

Do not commit `config.properties`.

#### `reports`

Contains generated Cucumber report output. Generated report files should not be committed.

## Running Tests

### Recommended Local Test Execution

Run the `TestRunner` class from your IDE:

```text
src/test/java/ca/voltio/TestRunner.java
```

The runner is configured to execute Cucumber tests with the following tag filter:

```text
@Ready and not @NotReady
```

This means:

- Scenarios tagged `@Ready` are included.
- Scenarios tagged `@NotReady` are excluded.
- A scenario marked both `@Ready` and `@NotReady` will be excluded.

### Maven Test Execution

If Maven is installed and available from your terminal, tests may also be run with:

```bash
mvn test
```

If `mvn` is not recognized, run the `TestRunner` class directly from your IDE instead.

## Cucumber Tagging Conventions

Feature files should follow the current annotation/tagging conventions.

Common tags include:

```text
@F1
@Registration
@Ready
@NotReady
@TC-001
@CreatesData
```

### Required Tag Usage

Use `@Ready` for scenarios that are stable and should run as part of the default local regression suite.

Use `@NotReady` for scenarios that should not run by default, such as:

- Scenarios blocked by application defects
- Scenarios for incomplete features
- Proof-of-concept scenarios
- Experimental test flows
- Temporarily unstable tests

### Example

```gherkin
@F1 @Registration @Ready
Feature: As a new user, I want to create an account using valid registration details so that I can access the application with my own account.

  @TC-001 @CreatesData @Ready
  Scenario: User should be able to register a personal account with valid details
    Given User opens the registration page link
    And registration account type step is displayed
    When User selects Account Type as "Personal" and continues registration
    And User enters registration email "uniqueEmail" password "Testing123!" name "Test User" address "100 Auto Main Street" and date of birth "1990-01-01"
    And User submits the registration form
    Then User should successfully complete registration
```

## Naming Conventions

Future test additions should follow the existing project naming style.

### Feature Files

Use a feature identifier followed by a clear feature name:

```text
F1_RegistrationTest.feature
```

Recommended pattern:

```text
F<number>_<FeatureName>Test.feature
```

Examples:

```text
F2_LoginTest.feature
F3_AccountOverviewTest.feature
F4_TransfersTest.feature
```

### Step Definition Classes

Step definition classes should match the related feature file name where possible.

Current pattern:

```text
F1_RegistrationTest_Steps.java
```

Recommended pattern:

```text
F<number>_<FeatureName>Test_Steps.java
```

### Page Object Classes

Page object classes should be named after the page or application area they represent.

Examples:

```text
LoginPage.java
RegistrationPage.java
AuthHomePage.java
```

### Scenario Tags

Test case tags should use the following format:

```text
@TC-001
@TC-002
@TC-003
```

Use feature-level tags for grouping:

```text
@F1
@Registration
```

Use execution-status tags:

```text
@Ready
@NotReady
```

Use data-impact tags where useful:

```text
@CreatesData
```

## Page Object Model Guidelines

This project follows the Page Object Model pattern.

When adding or updating automation coverage:

1. Put page-specific locators and page actions inside page object classes.
2. Keep Gherkin steps readable and business-focused.
3. Keep step definitions thin where possible.
4. Avoid putting Selenium locator logic directly inside step definitions.
5. Reuse shared helper methods from base classes where appropriate.
6. Create a new page object when a new page or major application area is being automated.
7. Avoid turning one page object into a catch-all class for unrelated features.

## Java Code Style Guidelines

### JavaDocs

JavaDocs are required for page objects, utility methods, and step/helper methods where behavior is not immediately obvious.

At minimum, JavaDocs should explain:

- What the method/class represents
- Important parameters
- Return values
- Any important preconditions or postconditions

### Assertions

Use clear assertion messages so failures are easy to understand.

Good assertion messages should explain what was expected and what did not happen.

### Waits

Prefer explicit waits for UI synchronization.

Avoid introducing implicit waits unless there is a clear framework-level reason and the full suite has been checked for wait-related side effects.

## Test Data

No shared test credentials are currently stored in this project.

Registration tests generate unique email addresses as needed because the live application database resets approximately every 15 minutes.

Because the database resets automatically, manual test data cleanup is not currently required.

### Test Data Guidelines

- Do not commit real user credentials.
- Do not commit sensitive data.
- Generate unique user data where needed.
- Keep test data readable in feature files where possible.
- Use aliases such as `uniqueEmail` or `blank` only when step definitions intentionally support them.

## Reports

The `TestRunner` is configured to generate Cucumber reports in the `reports` directory.

Generated outputs include:

```text
reports/cucumber-report.json
reports/cucumber-report.junit
reports/cucumber-report.html
```

The runner also enables Cucumber publishing.

### Viewing HTML Reports

After running the suite, open:

```text
reports/cucumber-report.html
```

in a browser.

### Report Commit Policy

Generated reports should **not** be committed.

Before committing, check that generated report files are not included in your pending changes.

## Known Limitations

This section should be updated whenever a limitation is fixed or a new limitation is discovered.

### Live Environment Dependency

The suite runs against the live hosted application.

Because there is no dedicated QA-local environment, test stability depends on:

- The hosted application being available
- The live app behaving consistently
- The database reset behavior
- Network availability

### Database Reset

The database appears to reset approximately every 15 minutes.

This is useful for test cleanup, but it also means that test data should not be assumed to persist for long periods.

## Flakiness Notes

No known flaky tests are currently documented.

If a test becomes flaky:

1. Confirm whether the issue is caused by the test code, application behavior, environment availability, or data reset timing.
2. Add a note to this README if the issue affects future contributors.
3. Tag the scenario as `@NotReady` if it should not run in the default suite.
4. Re-enable it with `@Ready` only once the issue is resolved.

## CI/CD

There is currently no confirmed CI/CD pipeline for this automation suite.

Tests are run manually and locally by executing the `TestRunner` class from an IDE.

If CI/CD is added later, update this README with:

- Pipeline location
- Trigger rules
- Required environment variables
- Browser/headless configuration
- Report artifact location
- Failure triage process

## Contribution Guidelines

### Branching

This repository should have one primary UI automation branch that acts as the stable, working, presentation-ready version of the automation suite.

The `qa-selenium-ui-suite` branch should be treated as the **main UI automation branch**.

The `qa-selenium-ui-suite` branch should always represent the version of the project that is:

- Stable
- Working locally
- Presentation-ready
- Focused on the official UI automation suite
- Free of incomplete experiments or proof-of-concept work

#### Standard QA Changes

For normal automation work, such as adding, removing, or editing official test coverage:

1. Branch from the `qa-selenium-ui-suite` branch.
2. Make the required changes.
3. Run the `TestRunner` class locally.
4. Confirm the suite is stable.
5. Open a pull request back into the `qa-selenium-ui-suite` branch.
6. Merge only when the changes are reviewed and working.
7. Once the merge has been made, delete that branch.

Examples of standard QA changes include:

- Adding new feature test coverage
- Updating page objects
- Updating step definitions
- Fixing broken locators
- Refactoring existing framework code
- Updating README documentation
- Removing obsolete official test coverage

#### Proof-of-Concept / Upskilling Branches

For experimentation, upskilling, or proof-of-concept work, branch from the `qa-selenium-ui-suite` branch but do **not** merge the work back into the 
`qa-selenium-ui-suite` branch.

Examples of proof-of-concept or experimentation work include:

- Self-healing locator experiments
- Excel-driven test data experiments
- Alternative framework patterns
- Tooling experiments
- Spike branches
- Demo-only automation approaches

These branches should stay up to date with the main `qa-selenium-ui-suite` branch where possible, but they should remain separate from the stable 
automation suite.

Proof-of-concept branches should not be merged into the `qa-selenium-ui-suite` branch unless the team explicitly decides that the experimental 
approach is now part of the official framework direction.

#### Branching Summary

```text
main UI automation branch
├── official feature/fix branch -> merge back after review and successful local run
├── official refactor branch -> merge back after review and successful local run
├── proof-of-concept branch -> do not merge back by default
└── upskilling/experiment branch -> do not merge back by default
```

#### Main UI Automation Branch Rule

If a change is not intended to be part of the stable, production-ready, presentation-ready UI automation suite, it should not be merged into the 
`qa-selenium-ui-suite` branch.

### Commit Messages

There is no strict commit message convention at the moment. Once a convention is established, document it here.

### Before Opening a Pull Request

Before opening a PR or asking for review:

1. Run the `TestRunner` class locally.
2. Confirm only `@Ready` tests are enabled for default execution.
3. Confirm blocked or experimental scenarios are tagged `@NotReady`.
4. Confirm new or updated scenarios are based on the relevant feature specification.
5. Make sure generated report files are not committed.
6. Make sure no credentials, hosted URLs, or sensitive environment values are committed.
7. Update this README if setup, execution, configuration, reports, or known limitations changed.
8. Confirm new code follows the existing project structure and naming conventions.
9. Confirm JavaDocs are added where required.
10. Confirm tabs are used for indentation.

## Adding a New Feature Test

When adding coverage for a new feature:

1. Review the feature's specification first.

2. Identify the behavior that should be automated from the spec, including:

    - Main happy path flows
    - Negative validation scenarios
    - Required field behavior
    - Boundary cases
    - Known blocked or incomplete behavior

3. Create a new `.feature` file under:

    ```text
    src/test/resources/features
    ```

4. Name it using the existing convention:

    ```text
    F<number>_<FeatureName>Test.feature
    ```

5. Add feature-level tags such as:

    ```text
    @F2 @Login @Ready
    ```

6. Add scenarios with test case tags:

    ```text
    @TC-001 @Ready
    ```

7. Create or update the related step definition class under:

    ```text
    src/test/java/ca/voltio/stepdefs
    ```

8. Create or update page objects under:

    ```text
    src/main/java/ca/voltio/pages
    ```

9. Reuse existing utilities where appropriate.

10. Run the `TestRunner` class locally.

11. Review the generated report.

12. Update this README if any new setup, limitation, or convention is introduced.

## Troubleshooting

### `mvn` Is Not Recognized

If running:

```bash
mvn test
```

fails because `mvn` is not recognized, Maven is likely not installed globally or is not configured in your system `PATH`.

Use the IDE test runner instead by running:

```text
src/test/java/ca/voltio/TestRunner.java
```

### Browser Does Not Launch

Check:

1. The `browser` value in `config.properties`.
2. That the selected browser is installed locally.
3. That the selected browser is supported on your operating system.
4. That Selenium Manager is able to resolve the required driver.

### Tests Are Running Scenarios That Should Be Excluded

Check the scenario tags.

The default runner should use:

```text
@Ready and not @NotReady
```

Any scenario that is not ready for normal execution should include:

```text
@NotReady
```

### Registration Tests Fail Unexpectedly

Consider:

1. Whether the live application is available.
2. Whether the database reset occurred during the test.
3. Whether the generated user data is unique.
4. Whether the application behavior has changed.
5. Whether a known limitation now affects the scenario.

## Maintainers / Contacts

No formal maintainer or contact information is currently documented.

Update this section when a QA owner, team channel, or escalation process is available.