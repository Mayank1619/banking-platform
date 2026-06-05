package ca.voltio.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for handling configuration properties stored in a
 * {@code config.properties} file.
 * <p>
 * This class provides methods to retrieve property values defined in the
 * configuration file. It also supports overriding property values through system
 * properties.
 * <p>
 * The configuration file {@code config.properties} is expected to be present
 * in the 'resources' folder. The absence of this file or issues during loading
 * will result in a runtime exception.
 * <p>
 * This class is designed to be used in a static context and cannot be instantiated.
 */
public final class ConfigPropertiesUtils
{
	private static final Properties PROPERTIES = loadProperties();

	private ConfigPropertiesUtils() { }

	/**
	 * Loads the configuration properties from the {@code config.properties} file.
	 *
	 * @return a {@code Properties} object containing the key-value pairs from the
	 *         {@code config.properties} file.
	 * @throws IllegalStateException if the {@code config.properties} file is not found
	 *                               or if an I/O error occurs during the file loading process.
	 */
	private static Properties loadProperties()
	{
		Properties properties = new Properties();

		try (InputStream inputStream = ConfigPropertiesUtils.class.getClassLoader().getResourceAsStream("config.properties"))
		{
			if (inputStream == null)
			{
				throw new IllegalStateException("config.properties was not found in the test resources folder.");
			}

			properties.load(inputStream);
			return properties;
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Unable to load config.properties.", e);
		}
	}

	public static String getBaseUrl()
	{
		return getProperty("baseUrl", "");
	}

	public static String getBrowser()
	{
		return getProperty("browser", "chrome");
	}
	
	private static String getProperty(String key, String defaultValue)
	{
		if (key == null || key.isBlank())
		{
			throw new IllegalArgumentException("Key cannot be null or blank");
		}

		return System.getProperty(key, PROPERTIES.getProperty(key, defaultValue));
	}
}