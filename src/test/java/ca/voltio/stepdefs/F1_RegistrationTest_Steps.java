package ca.voltio.stepdefs;

import ca.voltio.pages.AuthHomePage;
import ca.voltio.pages.RegistrationPage;
import ca.voltio.utils.ConfigPropertiesUtils;
import ca.voltio.utils.DriverUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDate;

public final class F1_RegistrationTest_Steps
{
	private RegistrationPage registrationPage;
	private String currentRegistrationEmail;

	@Given("User opens the registration page link")
	public void userOpensTheRegistrationPageLink()
	{
		this.registrationPage = new RegistrationPage(DriverUtils.getInstance().getWebDriver());
		this.registrationPage.openRegistrationPage(this.buildRegistrationUrl(ConfigPropertiesUtils.getBaseUrl()));
	}

	@And("registration account type step is displayed")
	public void registrationAccountTypeStepIsDisplayed()
	{
		this.registrationPage.waitForRegistrationAccountTypeStep();
	}

	@When("User selects Account Type as {string} and continues registration")
	public void userSelectsAccountTypeAsAndContinuesRegistration(String accountType)
	{
		this.registrationPage.selectAccountTypeAndContinue(accountType);
	}

	@And("User enters registration email {string} password {string} name {string} address {string} and date of birth {string}")
	public void userEntersRegistrationEmailPasswordNameAddressAndDateOfBirth(String email, String password, String name, String address, String dateOfBirth)
	{
		this.registrationPage.enterPersonalRegistrationDetails(this.resolveEmail(email),
				this.resolveInputValue(password), this.resolveInputValue(name),
				this.resolveInputValue(address), this.resolveDateOfBirth(dateOfBirth));
	}

	@And("User enters registration email {string} password {string} name {string} address {string} and business number {string}")
	public void userEntersRegistrationEmailPasswordNameAddressAndBusinessNumber(String email, String password, String name, String address, String businessNumber)
	{
		this.registrationPage.enterBusinessRegistrationDetails(this.resolveEmail(email),
				this.resolveInputValue(password), this.resolveInputValue(name),
				this.resolveInputValue(address), this.resolveInputValue(businessNumber));
	}

	@And("User submits the registration form")
	public void userSubmitsTheRegistrationForm()
	{
		this.registrationPage.submitRegistrationForm();
	}

	@Then("User should successfully complete registration")
	public void userShouldSuccessfullyCompleteRegistration()
	{
		Assertions.assertTrue(this.registrationPage.isRegistrationSuccessful(),
				"Expected registration to complete successfully, but the authenticated home page was not reached.");
	}

	@And("User logs out")
	public void userLogsOut()
	{
		AuthHomePage authHomePage = new AuthHomePage(DriverUtils.getInstance().getWebDriver());
		authHomePage.logout();
	}

	@Then("User should see a duplicate email registration error")
	public void userShouldSeeADuplicateEmailRegistrationError()
	{
		Assertions.assertTrue(this.registrationPage.isDuplicationEmailErrorDisplayed(),
				"Expected duplicate email registration error, but no duplicate email error was displayed.");
	}

	@Then("User should see a registration validation error")
	public void userShouldSeeARegistrationValidationError()
	{
		Assertions.assertTrue(this.registrationPage.isRegistrationValidationErrorDisplayed(),
				"Expected registration validation error, but no validation error was displayed.");
	}

	private String buildRegistrationUrl(String baseUrl)
	{
		if (baseUrl == null || baseUrl.trim().isEmpty())
		{
			return "";
		}

		String normalizedBaseUrl = baseUrl.trim();
		if (normalizedBaseUrl.endsWith("/"))
		{
			normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
		}

		return normalizedBaseUrl + "/register";
	}

	/*
	 * In the Registration Feature file, you'll notice that in the Scenarios, there are a lot of aliases, such as "uniqueEmail", "existingRegisteredEmail", "blank", and the DOB
	 * aliases. The three methods below this comment are meant to address it and substitute those aliases with specific values, depending on the alias.
	 */
	private String resolveEmail(String email)
	{
		if (email == null)
		{
			return "";
		}

		// Note: as of writing this, the database resets quite often (every ~15 minutes), so we can't cache account data for testing purposes.
		// We need our scenarios to generate unique credentials.
		String token = email.trim();
		if (token.equalsIgnoreCase("uniqueEmail"))
		{
			this.currentRegistrationEmail = "user." + System.currentTimeMillis() + "@test.com";
			return this.currentRegistrationEmail;
		}

		if (token.equalsIgnoreCase("currentRegistrationEmail"))
		{
			if (this.currentRegistrationEmail == null || this.currentRegistrationEmail.isBlank())
			{
				throw new IllegalStateException("currentRegistrationEmail was requested before uniqueEmail was generated.");
			}

			return this.currentRegistrationEmail;
		}

		return this.resolveInputValue(email);
	}

	private String resolveDateOfBirth(String dateOfBirth)
	{
		if (dateOfBirth == null)
		{
			return "";
		}

		// There's no guarantee when these tests will be executed. Hard-coding a date for negative testing will work for now, but what about in 5 years?
		// Overkill, for sure, as realistically this won't be an issue, but might as well future-proof this.
		String token = dateOfBirth.trim();
		LocalDate eighteenYearsFromNow = LocalDate.now().minusYears(18);

		if (token.equalsIgnoreCase("underageDob"))
		{
			return eighteenYearsFromNow.plusWeeks(1).toString();
		}

		if (token.equalsIgnoreCase("exactly18Dob"))
		{
			return eighteenYearsFromNow.toString();
		}

		if (token.equalsIgnoreCase("adultDob"))
		{
			return eighteenYearsFromNow.minusWeeks(1).toString();
		}

		return this.resolveInputValue(dateOfBirth);
	}

	/*
	 * Yeah, this looks weird, but pretty much in the scenario outline, if an example has a column set to blank, return an empty string; otherwise,
	 * just return the original value.
	 * I figured a helper function would be good here. Couldn't think of a better implementation given the time constraints, so if you have a more 
	 * efficient way, by all means you can adjust this.
	 */
	private String resolveInputValue(String value)
	{
		if (value == null)
		{
			return "";
		}

		String token = value.trim();
		if (token.equalsIgnoreCase("blank"))
		{
			return "";
		}

		return value;
	}
}