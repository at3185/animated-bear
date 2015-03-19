package iCal2google;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

public class MyProperties {
	private Properties prop = new Properties();
	private final String PROPERTIES_FILE_NAME = "config.properties";
	public final String CLIENT_ID = "clientId";
	public final String CLIENT_SECRET = "clientSecret";
	public final String REFRESH_KEY = "refreshKey";

	public MyProperties() {
		File file = new File(PROPERTIES_FILE_NAME);
		if (!file.exists()) {
			createPropertiesFile(file);
		}

		InputStream input = null;
		try {
			input = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			prop.load(input);
		} catch (IOException e) {
			System.err.println("File could not be read.");
			resetConfig();
		}

		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createPropertiesFile(File file) {
		OutputStream output = null;
		try {
			output = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		// set the properties values
		System.out.println("Enter client ID value displayed by web app and press Enter");
		try {
			line = br.readLine();
			System.out.println("LINE IS:" + line);
			prop.setProperty(CLIENT_ID, line);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Enter client secret value displayed by web app and press Enter");
		try {
			line = br.readLine();
			System.out.println("LINE IS:" + line);
			prop.setProperty(CLIENT_SECRET, line);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// save properties file
		try {
			prop.store(output, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updatePropertiesFile() throws IOException {
		OutputStream output = new FileOutputStream(PROPERTIES_FILE_NAME);
		prop.store(output, null);
	}

	public void resetConfig() {
		File file = new File(PROPERTIES_FILE_NAME);
		file.delete();
		System.out.println("Deleted file! Please try again");
		System.exit(1);
	}

	public String getProperty(String key) {
		return prop.getProperty(key);
	}

	public void setProperty(String key, String value) {
		prop.setProperty(key, value);
	}
}
