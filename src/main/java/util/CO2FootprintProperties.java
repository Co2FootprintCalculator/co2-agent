package util;

import org.springframework.core.env.MissingRequiredPropertiesException;

import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Objects;
import java.util.Properties;

public class CO2FootprintProperties {

	// API-Keys
	private final String OpenRouteServiceAPIKey;

	// How many rows of the car database shall be downloaded? Set it to "-1" for all rows
	private final Integer CarDatabaseRows;

	// MySQL Cache Database
	private final String CacheDatabaseHost;
	private final String CacheDatabasePort;
	private final String CacheDatabaseName;
	private String CacheDatabaseTableName;
	private final String CacheDatabaseUser;
	private final String CacheDatabasePassword;
	private final String CacheDatabaseUseSSL;

	public CO2FootprintProperties() throws MissingRequiredPropertiesException, IOException {
		Properties properties = new Properties();
		String configFileName = "config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFileName);
		properties.load(Objects.requireNonNull(inputStream));

		OpenRouteServiceAPIKey = properties.getProperty("OpenRouteServiceAPIKey");
		CarDatabaseRows = Integer.valueOf(properties.getProperty("CarDatabaseRows", "-1"));
		CacheDatabaseHost = properties.getProperty("CacheDatabaseHost");
		CacheDatabasePort = properties.getProperty("CacheDatabasePort");
		CacheDatabaseName = properties.getProperty("CacheDatabaseName");
		CacheDatabaseTableName = properties.getProperty("CacheDatabaseTableName");
		CacheDatabaseUser = properties.getProperty("CacheDatabaseUser");
		CacheDatabasePassword = properties.getProperty("CacheDatabasePassword");
		CacheDatabaseUseSSL = properties.getProperty("CacheDatabaseUseSSL");

		if (!allVariablesSet()) {
			throw new InvalidPropertiesFormatException("One or more properties are missing. Check config.properties");
		}
	}

	private boolean allVariablesSet() {
		return OpenRouteServiceAPIKey != null && CarDatabaseRows != null && CacheDatabaseHost != null && CacheDatabasePort != null
				&& CacheDatabaseName != null && CacheDatabaseUser != null && CacheDatabasePassword != null && CacheDatabaseUseSSL != null;
	}

	public String getOpenRouteServiceAPIKey() {
		return OpenRouteServiceAPIKey;
	}

	public Integer getCarDatabaseRows() {
		return CarDatabaseRows;
	}

	public String getCacheDatabaseHost() {
		return CacheDatabaseHost;
	}

	public String getCacheDatabasePort() {
		return CacheDatabasePort;
	}

	public String getCacheDatabaseName() {
		return CacheDatabaseName;
	}

	public String getCacheDatabaseUser() {
		return CacheDatabaseUser;
	}

	public String getCacheDatabasePassword() {
		return CacheDatabasePassword;
	}

	public String getCacheDatabaseUseSSL() {
		return CacheDatabaseUseSSL;
	}

	public String getCacheDatabaseTableName() {
		return CacheDatabaseTableName;
	}
}
