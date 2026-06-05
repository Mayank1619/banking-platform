package ca.voltio.stepdefs;

import ca.voltio.utils.DriverUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * The {@code Hooks} class is a utility class designed for managing setup and teardown
 * operations in test execution. It includes methods annotated with testing lifecycle hooks
 * to ensure the proper initialization and cleanup of WebDriver instances.
 *
 * <ul>
 *   <li>The {@code setUp} method is executed before each test scenario to initialize the WebDriver.</li>
 *   <li>The {@code tearDown} method is executed after each test scenario to handle cleanup operations,
 *       including taking a screenshot if a test fails and properly quitting the WebDriver instance.</li>
 * </ul>
 */
public final class Hooks
{
	/**
	 * Initializes the WebDriver instance before each test scenario.
	 * <p>
	 * This method is annotated with the {@code @Before} hook from the testing framework,
	 * ensuring it is executed prior to the execution of each test. It retrieves or creates
	 * a new WebDriver instance using the {@code DriverUtils} class. The WebDriver instance
	 * is managed at a thread-local level, ensuring thread safety and isolation when tests
	 * are executed concurrently.
	 * <p>
	 * Delegates the creation and initialization of the WebDriver to the 
	 * {@code DriverUtils.getInstance().getWebDriver()} method.
	 */
	@Before
	public void setUp()
	{
		DriverUtils.getInstance().getWebDriver();
	}

	/**
	 * Cleans up resources and performs post-test scenario operations.
	 * <p>
	 * This method is executed after each test scenario and is annotated with the
	 * {@code @After} hook from the testing framework. It performs necessary cleanup
	 * operations, including capturing a screenshot if the scenario has failed and 
	 * shutting down the WebDriver instance.
	 * <p>
	 * Screenshots of failed scenarios are attached to the test execution report 
	 * for debugging purposes.
	 *
	 * @param scenario the test scenario that has just completed execution. Provides 
	 *                 information about the scenario, including its status and metadata.
	 */
	@After
	public void tearDown(Scenario scenario)
	{
		if (scenario.isFailed())
		{
			WebDriver webDriver = DriverUtils.getInstance().getWebDriver();
			byte[] screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);

			scenario.attach(screenshot, "image/png", scenario.getName());
		}

		DriverUtils.getInstance().quitWebDriver();
	}
}
