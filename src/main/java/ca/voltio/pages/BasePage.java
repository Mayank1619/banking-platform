package ca.voltio.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * An abstract foundation class for all Page Object classes in the framework.
 * It provides common utility methods to interact with web elements, handle browser-native validations,
 * and manage common web driver interactions.
 * <p>
 * All page-specific classes should extend this base class.
 */
public abstract class BasePage
{
	/*
	 * The previous iteration had an implicit wait set for the driver in the DriverUtils class and another WebDriverWait variable here, which caused several issues regarding tests stalling.
	 * Instead, we'll just set a pre-determined duration here and create a new WebDriverWait whenever we need it.
	 * As WebDriverWait is just a wrapper, it doesn't have a big impact on performance, so there's no need to cache it.
	 * Also, no need to have it set in the DriverUtils class or config.properties as only page objects need to know about it.
	 */
	
	
	/*
	 * The previous implementation had an implicit wait set for the driver in the DriverUtils class and another WebDriverWait variable in each 
	 * PageObject class, which caused several issues regarding tests stalling. Instead, we'll just set a pre-determined duration here and create a 
	 * new WebDriverWait whenever we need it. As WebDriverWait is just a wrapper, it has a negligible impact on performance, so there's no need to 
	 * cache it. There's also no need to have an implicit wait set in the DriverUtils class as this is more than enough. Also, there's no need to 
	 * set it in config.properties; only page object classes will be using these waits, so it makes to only define it here. Reserve config
	 * .properties for environment-specific configuration or 
	 */
	protected static final Duration UI_FIELD_VISIBLE_WAIT = Duration.ofSeconds(10);
	protected static final Duration SUBMISSION_WAIT = Duration.ofSeconds(20);

	protected final WebDriver webDriver;

	protected BasePage(WebDriver driver)
	{
		this.webDriver = driver;
	}

	/**
	 * Navigates the web driver to the specified URL.
	 *
	 * @param url the web address to navigate to. It should be a valid and well-formed URL.
	 */
	protected void openPage(String url)
	{
		this.webDriver.get(url);
	}

	/**
	 * Types the specified data into the given input field. This method waits until the input field
	 * becomes visible before clearing it and entering the data.
	 *
	 * @param inputField the {@code WebElement} representing the input field where data will be entered.
	 * @param data       the {@code String} data to type into the input field. If the data is null, the field will
	 *                   be cleared without entering any text.
	 */
	protected void typeDataIntoInputField(WebElement inputField, String data)
	{
		new WebDriverWait(this.webDriver, UI_FIELD_VISIBLE_WAIT)
				.until(ExpectedConditions.visibilityOf(inputField));

		inputField.clear();
		inputField.sendKeys(data == null ? "" : data);
	}

	/**
	 * Clicks on a specified web element after waiting for it to become clickable.
	 * This method is useful for ensuring that a web element is interactable
	 * before attempting to interact with it.
	 *
	 * @param element the WebElement to be clicked. It should be a valid and
	 *                visible element on the web page.
	 */
	protected void clickElement(WebElement element)
	{
		new WebDriverWait(this.webDriver, UI_FIELD_VISIBLE_WAIT)
				.until(ExpectedConditions.elementToBeClickable(element))
				.click();
	}

	/**
	 * Checks if any of the specified input fields have a browser-native HTML5 validation error.
	 * This validation typically applies to fields with constraints like email format, required fields,
	 * or other native validation rules provided by the browser.
	 *
	 * @param inputFields an array of {@code WebElement} representing the input fields to check for browser validation errors.
	 *                    Each field should be a valid and visible input element on the web page.
	 * @return {@code true} if a validation error is detected on any of the input fields, {@code false} otherwise.
	 */
	protected boolean hasBrowserValidationError(WebElement... inputFields)
	{
		for (WebElement inputField : inputFields)
		{
			try
			{
				String validationMessage = (String) ((JavascriptExecutor) this.webDriver)
						.executeScript("return arguments[0].validationMessage || '';", inputField);

				if (validationMessage != null && !validationMessage.trim().isEmpty())
				{
					return true;
				}
			}
			catch (Exception ignored)
			{
				// Ignoring an exception if caught, so we can keep checking the remaining fields.
			}
		}

		return false;
	}

	/**
	 * Checks whether the specified web element is currently displayed on the page.
	 * <p>
	 * This method safely handles cases where the element is null, missing from the DOM,
	 * stale, or otherwise not interactable at the moment of checking.
	 *
	 * @param element the {@code WebElement} to check for visibility.
	 * @return {@code true} if the element is non-null and displayed; {@code false} otherwise.
	 */
	protected boolean isElementDisplayed(WebElement element)
	{
		try
		{
			return element != null && element.isDisplayed();
		}
		catch (NoSuchElementException | StaleElementReferenceException ignored)
		{
			return false;
		}
	}

	/**
	 * Retrieves the visible error banner text from a list of error banners on the page.
	 * This method iterates through the provided {@code List} of error banner {@code WebElement}s,
	 * checks if each banner is displayed, and returns the trimmed text of the first visible banner.
	 * If no error banners are visible, an empty string is returned.
	 *
	 * @param errorBannerMessages a {@code List} of {@code WebElement} representing error banner messages
	 *                            on the web page. Each element in the list should correspond to a potential
	 *                            error banner that may appear on the page.
	 * @return the text of the first visible error banner as a {@code String}, or an empty string if none
	 * are visible.
	 */
	protected String getVisibleErrorBannerText(List<WebElement> errorBannerMessages)
	{
		if (errorBannerMessages == null)
		{
			return "";
		}

		for (WebElement banner : errorBannerMessages)
		{
			if (this.isElementDisplayed(banner))
			{
				return banner.getText().trim();
			}
		}

		return "";
	}
}
