package ca.voltio.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Represents the authenticated home page of the application, serving as a Page Object
 * to model the functionality and behaviors of the post-login landing page.
 * This class provides methods to interact with key user interface elements
 * on the home page, such as navigation tabs and profile features.
 * It extends the {@code BasePage} to leverage common page utility methods.
 * <p>
 * The authenticated home page includes elements like the "Overview" and "My Accounts" sections,
 * as well as the ability to log out of the application.
 */
public final class AuthHomePage extends BasePage
{
	// Looks like the class name for the tabs in the navbar (Overview, My Account) changes depending on which one is selected.
	@FindBy(xpath = "//*[contains(@class, 'subnav-btn') and normalize-space()='Overview']")
	private WebElement overviewButton;

	// "My Accounts" tab is using <a> tag, while "Overview" tab is a button.
	@FindBy(xpath = "//*[contains(@class, 'subnav-btn') and normalize-space()='My Accounts']")
	private WebElement myAccountsHrefLink;

	@FindBy(className = "navbar-avatar-btn")
	private WebElement avatarProfileButton;

	@FindBy(xpath = "//button[contains(@class, 'navbar-dropdown-item') and normalize-space()='Log Out']")
	private WebElement logoutButton;

	// TODO: Probably gnna need more Home Page specific checks here once the automation scripts get bigger.
	// If you want, it might be better to create Page Objects when doing additional functionality (transfer funds, create account, etc)
	// instead of putting it all here.
	public AuthHomePage(WebDriver driver)
	{
		super(driver);

		PageFactory.initElements(driver, this);
	}

	/**
	 * Logs the user out of the authenticated session by interacting with the
	 * user interface elements related to the profile and logout functionality.
	 * <p>
	 * This method performs the following steps:
	 * <ol>
	 *     <li>Clicks on the avatar/profile button to open the user menu.</li>
	 *     <li>Clicks on the logout button within the opened user menu.</li>
	 * </ol>
	 * <p>
	 * It ensures that the user is logged out by triggering the relevant UI events.
	 * This may redirect the user to an unauthenticated state or a login screen
	 * depending on the application's behavior.
	 */
	public void logout()
	{
		clickElement(this.avatarProfileButton);
		clickElement(this.logoutButton);
	}

	/**
	 * Determines whether the current page is the authenticated home page of the application.
	 * This is validated by checking the presence of key UI elements specific to this page,
	 * such as the avatar/profile button and at least one of the navigation options
	 * ("Overview" button or "My Accounts" link).
	 *
	 * @return {@code true} if the avatar/profile button is displayed and at least one of the
	 * navigation options (Overview button or My Accounts link) is visible; {@code false} otherwise.
	 */
	public boolean isOnAuthenticatedHomePage()
	{
		return isElementDisplayed(this.avatarProfileButton) &&
				(isElementDisplayed(this.overviewButton) || isElementDisplayed(this.myAccountsHrefLink));
	}
}