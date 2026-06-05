package ca.voltio.pages;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Represents the login page of a web application. This class provides methods
 * to interact with the login page's elements, such as entering user credentials,
 * submitting the login form, and verifying the outcome of the login attempt.
 * The class extends {@link BasePage}, which provides common functionality for
 * web page interactions.
 * <p>
 * This is a final class and cannot be extended. It encapsulates the behavior
 * specific to the login page and manages interactions with its web components.
 */
public final class LoginPage extends BasePage
{
	private final AuthHomePage authHomePage;

	@FindBy(xpath = "//input[@id='login-username']")
	private WebElement usernameInputField;

	@FindBy(xpath = "//input[@id='login-password']")
	private WebElement passwordInputField;

	@FindBy(xpath = "//button[@type='submit' and normalize-space()='Sign In']")
	private WebElement signInButton;

	@FindBy(xpath = "//div[@class = 'banner error']")
	private List<WebElement> errorBannerMessages;

	public LoginPage(WebDriver driver)
	{
		super(driver);
		this.authHomePage = new AuthHomePage(driver);

		PageFactory.initElements(driver, this);
	}

	/**
	 * Navigates to the login page using the provided URL.
	 *
	 * @param loginUrl the URL of the login page to navigate to. It should be a valid and well-formed URL.
	 */
	public void openLoginPage(String loginUrl)
	{
		openPage(loginUrl);
	}

	/**
	 * Waits until the login form elements (username input field, password input field, and sign-in button)
	 * are visible on the page. This ensures that the login form is fully loaded and ready for interaction.
	 * <p>
	 * Uses an explicit wait to monitor the visibility of each required element on the page.
	 * If the elements do not become visible within the defined wait time, a timeout exception will be thrown.
	 * <p>
	 * This method should be invoked before attempting to interact with the login form elements.
	 */
	public void waitForLoginForm()
	{
		WebDriverWait wait = new WebDriverWait(webDriver, UI_FIELD_VISIBLE_WAIT);

		wait.until(ExpectedConditions.visibilityOf(this.usernameInputField));
		wait.until(ExpectedConditions.visibilityOf(this.passwordInputField));
		wait.until(ExpectedConditions.visibilityOf(this.signInButton));
	}

	/**
	 * Enters the login credentials into the username and password input fields on the login page.
	 * This method waits for the input fields to be visible, clears any existing text, and inputs the provided credentials.
	 *
	 * @param username the username to be entered into the username input field. It should be a valid string.
	 * @param password the password to be entered into the password input field. It should be a valid string.
	 */
	public void enterLoginCredentials(String username, String password)
	{
		typeDataIntoInputField(this.usernameInputField, username);
		typeDataIntoInputField(this.passwordInputField, password);
	}

	/**
	 * Submits the login form by clicking on the sign-in button.
	 * This method assumes that the login form elements are already
	 * visible and interactable on the page.
	 * <p>
	 * It uses the {@code clickElement} method to ensure that the
	 * sign-in button is clicked only when it is ready to be interacted with.
	 * <p>
	 * Preconditions:
	 * <ul>
	 *     <li>The sign-in button must be available as a valid and visible web element.</li>
	 *     <li>The login form should be fully loaded on the page.</li>
	 * </ul>
	 * <p>
	 * Postconditions:
	 * <ul>
	 *     <li>Triggers the form submission action, which is expected to start the login process
	 *     or provide feedback if submission fails.</li>
	 * </ul>
	 */
	public void submitLoginForm()
	{
		clickElement(this.signInButton);
	}

	/**
	 * Determines whether the login attempt was successful by evaluating several conditions:
	 * <ul>
	 *     <li>The current URL should not indicate the login page.</li>
	 *     <li>The authenticated home page must show specific indicators confirming successful authentication.</li>
	 *     <li>No visible error banners should be present on the page.</li>
	 * </ul>
	 * <p>
	 * This method uses explicit waits to handle asynchronous page loading and checks the result
	 * within a specified timeout period.
	 * <p>
	 * If any of the conditions are not met during the wait or a timeout occurs, the method will
	 * return false.
	 *
	 * @return {@code true} if the login was successfully completed (all conditions are met);
	 * {@code false} otherwise.
	 */
	public boolean isLoginSuccessful()
	{
		try
		{
			return new WebDriverWait(webDriver, SUBMISSION_WAIT)
					.until(_ ->
					{
						String currentUrl = webDriver.getCurrentUrl();
						if (currentUrl == null)
						{
							System.out.println("LoginPage's isLoginSuccessful method has a null 'currentUrl'.");
							return false;
						}

						currentUrl = currentUrl.toLowerCase();

						return !currentUrl.contains("/login")
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
	 * Determines whether a login error message is currently displayed on the page.
	 * The detection includes one of the following conditions:
	 * <ul>
	 *     <li>The visible error banner contains non-blank text.</li>
	 *     <li>Browser validation errors exist in the username or password input fields.</li>
	 * </ul>
	 * <p>
	 * This method uses explicit wait logic to monitor the error message's visibility within
	 * a defined timeout period. If the timeout occurs without meeting either condition,
	 * the method will return {@code false}.
	 *
	 * @return {@code true} if a login error is visible or browser validation errors are present;
	 * {@code false} otherwise.
	 */
	public boolean isLoginErrorDisplayed()
	{
		try
		{
			return new WebDriverWait(this.webDriver, SUBMISSION_WAIT)
					.until(_ -> !getVisibleErrorBannerText(this.errorBannerMessages).trim().isBlank()
							|| this.hasBrowserValidationError(this.usernameInputField, this.passwordInputField));
		}
		catch (TimeoutException e)
		{
			return false;
		}
	}
}