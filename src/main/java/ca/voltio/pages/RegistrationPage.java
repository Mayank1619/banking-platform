package ca.voltio.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * The {@code RegistrationPage} class represents the page object model for the user registration page
 * in a web application. It extends the {@link BasePage} class and provides methods to interact with
 * and perform actions on the registration page.
 * <p>
 * This class contains WebElements representing UI elements on the registration page and the
 * actions that can be performed, such as selecting account types, entering registration details,
 * simulating form submission, and validating registration states.
 * <p>
 * Key functionalities:
 * <ul>
 *     <li>Navigate to the registration page.</li>
 *     <li>Enter registration details for personal or business accounts.</li>
 *     <li>Submit the registration form.</li>
 *     <li>Verify success or error conditions during registration.</li>
 * </ul>
 */
public final class RegistrationPage extends BasePage
{
	private final AuthHomePage authHomePage;

	// Can probably change these locators from xPath to just straight ID checks since the registration page is relatively simplistic
	// with a handful of elements and stable tag IDs.
	@FindBy(xpath = "//select[@id='register-type']")
	private WebElement accountTypeDropdown;

	@FindBy(xpath = "//button[@type='button' and normalize-space()='Continue']")
	private WebElement continueButton;

	@FindBy(xpath = "//input[@id='register-username']")
	private WebElement emailInputField;

	@FindBy(xpath = "//input[@id='register-password']")
	private WebElement passwordInputField;

	@FindBy(xpath = "//input[@id='register-name']")
	private WebElement nameInputField;

	@FindBy(xpath = "//input[@id='register-address']")
	private WebElement addressInputField;

	@FindBy(xpath = "//input[@id='register-dob']")
	private WebElement dateOfBirthInputField;

	@FindBy(xpath = "//input[@id='register-gbn']")
	private WebElement businessNumberInputField;

	@FindBy(xpath = "//button[@type='submit' and normalize-space()='Create Account']")
	private WebElement createAccountButton;

	@FindBy(xpath = "//div[@class = 'banner error']")
	private List<WebElement> errorBannerMessages;

	public RegistrationPage(WebDriver driver)
	{
		super(driver);
		this.authHomePage = new AuthHomePage(driver);

		PageFactory.initElements(driver, this);
	}

	/**
	 * Opens the registration page by navigating to the specified registration URL.
	 *
	 * @param registrationUrl the URL of the registration page to be opened.
	 *                        It should be a valid and well-formed web address.
	 */
	public void openRegistrationPage(String registrationUrl)
	{
		openPage(registrationUrl);
	}

	/**
	 * Waits for the "Account Type" dropdown to become visible on the registration page.
	 * This method uses an explicit wait to ensure the dropdown is rendered in the DOM
	 * and is visible before interacting with it. The wait duration is determined by
	 * the pre-configured UI field visibility timeout.
	 * <p>
	 * This method is useful for preventing test failures caused by attempts to interact with
	 * page elements that may not be fully loaded or visible yet. It ensures synchronization
	 * between test actions and the state of the web page.
	 * <p>
	 * <b>Pre-conditions:</b>
	 * <ul>
	 *     <li>The web driver must have been navigated to the registration page where the "Account Type" dropdown is present.</li>
	 *     <li>The dropdown element must be valid and correctly initialized.</li>
	 * </ul>
	 *
	 * <p>
	 * <b>Post-conditions:</b>
	 * <ul>
	 *     <li>The method completes when the "Account Type" dropdown is confirmed to be visible.</li>
	 *     <li>If the dropdown does not become visible within the specified wait time, a timeout exception will be thrown.</li>
	 * </ul>
	 */
	public void waitForRegistrationAccountTypeStep()
	{
		new WebDriverWait(webDriver, UI_FIELD_VISIBLE_WAIT)
				.until(ExpectedConditions.visibilityOf(this.accountTypeDropdown));
	}

	/**
	 * Selects the account type from the dropdown menu and proceeds to the next step
	 * of the registration process by clicking the "Continue" button. This method waits
	 * for specific input fields related to the selected account type to become visible
	 * before completing execution.
	 *
	 * @param accountType the type of account to be selected. Possible values are
	 *                    "Business" and "Personal". This determines which additional
	 *                    input fields are required during the registration process
	 *                    (e.g., "Business Number" for Business accounts or
	 *                    "Date of Birth" for Personal accounts).
	 */
	public void selectAccountTypeAndContinue(String accountType)
	{
		WebDriverWait wait = new WebDriverWait(webDriver, UI_FIELD_VISIBLE_WAIT);
		wait.until(ExpectedConditions.visibilityOf(this.accountTypeDropdown));

		Select accountTypeSelectBox = new Select(this.accountTypeDropdown);
		accountTypeSelectBox.selectByVisibleText(accountType);

		wait.until(ExpectedConditions.elementToBeClickable(this.continueButton))
				.click();

		wait.until(ExpectedConditions.visibilityOf(this.emailInputField));
		wait.until(ExpectedConditions.visibilityOf(this.passwordInputField));
		wait.until(ExpectedConditions.visibilityOf(this.nameInputField));
		wait.until(ExpectedConditions.visibilityOf(this.addressInputField));

		if (accountType.equalsIgnoreCase("Business"))
		{
			wait.until(ExpectedConditions.visibilityOf(this.businessNumberInputField));
		}
		else if (accountType.equalsIgnoreCase("Personal"))
		{
			wait.until(ExpectedConditions.visibilityOf(this.dateOfBirthInputField));
		}
	}

	/**
	 * Enters the personal registration details into the corresponding input fields on the registration page.
	 * This method inputs data for email, password, name, address, and date of birth.
	 *
	 * @param email       the email address to be entered into the email input field. Must be a valid email format.
	 * @param password    the password to be entered into the password input field. The password must meet the
	 *                    required complexity criteria of the registration system.
	 * @param name        the full name to be entered into the name input field. It should include first and last name.
	 * @param address     the residential address to be entered into the address input field. Should be a properly
	 *                    formatted address.
	 * @param dateOfBirth the date of birth to be entered into the date of birth input field. This should be in the
	 *                    format accepted by the application (e.g., "yyyy-MM-dd").
	 */
	public void enterPersonalRegistrationDetails(String email, String password, String name, String address, String dateOfBirth)
	{
		typeDataIntoInputField(this.emailInputField, email);
		typeDataIntoInputField(this.passwordInputField, password);
		typeDataIntoInputField(this.nameInputField, name);
		typeDataIntoInputField(this.addressInputField, address);

		// Input field is slightly different for date of birth, and from initial testing, ran into issues with sendKeys(), 
		// hence the separate helper with JavaScriptExecutor.
		this.typeDateIntoDateOfBirthField(dateOfBirth);
	}

	/**
	 * Enters the business registration details into the corresponding input fields
	 * on the registration page. This method inputs data for email, password, name,
	 * address, and business number.
	 *
	 * @param email          the email address to be entered into the email input field.
	 *                       Must be a valid email format.
	 * @param password       the password to be entered into the password input field.
	 *                       The password must meet the required complexity criteria
	 *                       of the registration system.
	 * @param name           the business name to be entered into the name input field.
	 *                       This represents the registered name of the business.
	 * @param address        the business address to be entered into the address input field.
	 *                       Should be a properly formatted address.
	 * @param businessNumber the business registration number to be entered into the
	 *                       business number input field. This should match the
	 *                       format required by the registration system.
	 */
	public void enterBusinessRegistrationDetails(String email, String password, String name, String address, String businessNumber)
	{
		typeDataIntoInputField(this.emailInputField, email);
		typeDataIntoInputField(this.passwordInputField, password);
		typeDataIntoInputField(this.nameInputField, name);
		typeDataIntoInputField(this.addressInputField, address);
		typeDataIntoInputField(this.businessNumberInputField, businessNumber);
	}

	/**
	 * Submits the registration form by clicking the "Create Account" button.
	 * This method ensures that the registration process proceeds to the next
	 * step after all required fields have been populated and validated.
	 * <p>
	 * <b>Pre-conditions:</b>
	 * <ul>
	 *     <li>The "Create Account" button must be present and visible on the page.</li>
	 *     <li>All required fields in the registration form should be completed before invoking this method to avoid validation errors.</li>
	 * </ul>
	 * <p>
	 * <b>Post-conditions:</b>
	 * <ul>
	 *     <li>The registration form submission process will be initiated.</li>
	 *     <li>Any server-side validation or further navigation steps will be triggered as part of the form submission handling.</li>
	 * </ul>
	 * <p>
	 * This method relies on the {@link BasePage#clickElement(WebElement)} utility to ensure that the
	 * "Create Account" button is interactable before performing the click action.
	 */
	public void submitRegistrationForm()
	{
		clickElement(this.createAccountButton);
	}

	/**
	 * Checks whether the registration process was successful.
	 * This method evaluates the current state of the page and determines success
	 * based on the following criteria:
	 * <ul>
	 *     <li>The URL of the page must no longer contain "/register".</li>
	 *     <li>The authenticated home page must display specific indicators that confirm the user is logged in successfully.</li>
	 *     <li>The error banner message must show no visible text (indicating no errors occurred during registration).</li>
	 * </ul>
	 * <p>
	 * If the above conditions are met within the predefined timeout, the registration
	 * is considered successful.
	 *
	 * @return {@code true} if the registration was successful based on the evaluated
	 * criteria, {@code false} otherwise (e.g., if a timeout occurs or the
	 * criteria are not satisfied).
	 */
	public boolean isRegistrationSuccessful()
	{
		try
		{
			return new WebDriverWait(webDriver, SUBMISSION_WAIT)
					.until(_ ->
					{
						String currentUrl = this.webDriver.getCurrentUrl();
						if (currentUrl == null)
						{
							System.out.println("RegistrationPage's isRegistrationSuccessful method has a null 'currentUrl'.");
							return false;
						}

						currentUrl = currentUrl.toLowerCase();

						return !currentUrl.contains("/register")
								&& this.authHomePage.isOnAuthenticatedHomePage()
								&& getVisibleErrorBannerText(this.errorBannerMessages).trim().isEmpty();
					});
		}
		catch (TimeoutException e)
		{
			return false;
		}
	}

	/**
	 * Checks whether an error message indicating a duplicate email registration
	 * is displayed on the registration page. The method waits for a specific error
	 * banner text containing "already registered" to become visible and returns
	 * the result accordingly.
	 * <p>
	 * The method uses an explicit wait with a predefined timeout to ensure
	 * that the error banner text is evaluated only after it appears or the timeout
	 * expires. If the error banner is not displayed within the timeout, the
	 * method returns false.
	 *
	 * @return {@code true} if the error banner is visible and contains the text
	 * "already registered" (case-insensitive), or {@code false} if the error
	 * banner is not displayed or the text is not found within the timeout.
	 */
	public boolean isDuplicationEmailErrorDisplayed()
	{
		// We have this because there's a scenario specifically checking for attempting to register with an already existing email.
		// As such, I figured having it in its own method would be better. Again, should you find a better solution, by all means you can adjust this.
		try
		{
			return new WebDriverWait(webDriver, SUBMISSION_WAIT)
					.until(_ -> getVisibleErrorBannerText(this.errorBannerMessages).toLowerCase().contains("already registered"));
		}
		catch (TimeoutException e)
		{
			return false;
		}
	}

	/**
	 * Checks if a registration validation error is currently displayed.
	 * <p>
	 * The method waits for a predefined duration to determine if any validation error message
	 * or browser validation error is present on the registration form. It evaluates the visibility
	 * of error banners as well as browser validation errors on specific input fields.
	 *
	 * @return {@code true} if a validation error message is displayed or if any browser validation error
	 * is detected on the input fields; {@code false} otherwise.
	 */
	public boolean isRegistrationValidationErrorDisplayed()
	{
		try
		{
			return new WebDriverWait(webDriver, SUBMISSION_WAIT)
					.until(_ -> !this.getVisibleErrorBannerText(this.errorBannerMessages).trim().isBlank()
							|| hasBrowserValidationError(this.emailInputField, this.passwordInputField, this.nameInputField,
							this.addressInputField, this.dateOfBirthInputField, this.businessNumberInputField));
		}
		catch (TimeoutException e)
		{
			return false;
		}
	}

	/**
	 * Types the specified date value into the date of birth input field and triggers the necessary input and change events.
	 *
	 * @param date the date value to be entered into the date of birth field. If null, the field will be cleared.
	 */
	private void typeDateIntoDateOfBirthField(String date)
	{
		new WebDriverWait(webDriver, UI_FIELD_VISIBLE_WAIT)
				.until(ExpectedConditions.visibilityOf(this.dateOfBirthInputField));

		this.dateOfBirthInputField.clear();

		JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;

		// If you're wondering what this does or how I came up with it, click here: https://tinyurl.com/du4shbr8
		jsExecutor.executeScript(
				"const input = arguments[0];" +
						"const value = arguments[1];" +
						"const nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
						"nativeInputValueSetter.call(input, value);" +
						"input.dispatchEvent(new Event('input', { bubbles: true }));" +
						"input.dispatchEvent(new Event('change', { bubbles: true }));",
				this.dateOfBirthInputField,
				date == null ? "" : date);
	}
}