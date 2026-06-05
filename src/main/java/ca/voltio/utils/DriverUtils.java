package ca.voltio.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 * Utility class to manage WebDriver instances in a thread-safe manner.
 * This class provides methods to initialize, retrieve, and quit WebDriver instances
 * for various browser types based on the configuration.
 * <p>
 * This class follows the Singleton design pattern, ensuring a single shared instance
 * across the application lifecycle.
 * <p>
 * Thread safety is achieved using a {@link ThreadLocal} to store WebDriver instances
 * for individual threads.
 */
public final class DriverUtils
{
	private static final DriverUtils INSTANCE = new DriverUtils();
	private final ThreadLocal<WebDriver> webDriver = new ThreadLocal<>();

	private DriverUtils() { }

	private WebDriver createWebDriver()
	{
		String driverName = ConfigPropertiesUtils.getBrowser().trim().toLowerCase();
		WebDriver driver = switch (driverName)
		{
			case "chrome" -> new ChromeDriver();
			case "firefox" -> new FirefoxDriver();
			case "edge" -> new EdgeDriver();
			case "safari" -> new SafariDriver();
			case "ie" -> new InternetExplorerDriver();
			default -> throw new IllegalArgumentException("Unsupported browser: " + driverName);
		};

		driver.manage().window().maximize();
		return driver;
	}

	public void quitWebDriver()
	{
		WebDriver driver = this.webDriver.get();

		if (driver != null)
		{
			driver.quit();
			this.webDriver.remove();
		}
	}

	public static DriverUtils getInstance()
	{
		return INSTANCE;
	}

	public WebDriver getWebDriver()
	{
		WebDriver driver = this.webDriver.get();

		if (driver == null)
		{
			driver = this.createWebDriver();
			this.webDriver.set(driver);
		}

		return driver;
	}
}
