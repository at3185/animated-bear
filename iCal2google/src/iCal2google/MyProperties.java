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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class MyProperties {
	private Properties prop = new Properties();
	// encryptor used to store and read properties file
	private StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
	private final String PROPERTIES_FILE_NAME = "config.properties";
	public final String CLIENT_ID = "clientId";
	public final String CLIENT_SECRET = "clientSecret";
	public final String REFRESH_KEY = "refreshKey";
	public final String CALID = "CalID";

	public MyProperties() {
		// set your own password here
		// In this example a random string is used in conjunction with some
		// system variables
		encryptor.setPassword("RKUsZR7CNogRC27MsRBeeRaHR7te92gkEF4xBxVM85Ee1 " + System.getProperty("user.name")
				+ System.getProperty("os.arch") + System.getProperty("java.home"));

		File file = new File(PROPERTIES_FILE_NAME);
		if (!file.exists()) {
			createPropertiesFile();
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
			deletePropFile();
		}

		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createPropertiesFile() {
		System.out
				.println("Open your developer project from google console(https://console.developers.google.com/project) and go to Credentials section");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		// set the properties values
		System.out.println("Enter Client ID value displayed by application and press Enter..");
		try {
			line = br.readLine();
			// System.out.println("LINE IS:" + line);
			setProperty(CLIENT_ID, line);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Enter Client Secret value displayed by application and press Enter..");
		try {
			line = br.readLine();
			// System.out.println("LINE IS:" + line);
			setProperty(CLIENT_SECRET, line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Writes the properties file to disk
	public void writePropFileToDisk() {
		OutputStream output = null;
		try {
			output = new FileOutputStream(PROPERTIES_FILE_NAME);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			prop.store(output, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Deletes the properties file. Used to reset settings.
	public void deletePropFile() {
		File file = new File(PROPERTIES_FILE_NAME);
		file.delete();
		System.out.println("Deleted file! Please try again!");
		System.exit(1);
	}

	// This method decrypts the value returned by Properties object. Returns
	// null if key does not exist.
	public String getProperty(String key) {
		String encValue = prop.getProperty(key);
		if (encValue == null)
			return encValue;
		encValue = encValue.trim();
		return encryptor.decrypt(encValue);
	}

	// This method encrypts the value before storing it in the Properties
	// object.
	public void setProperty(String key, String value) {
		value = encryptor.encrypt(value);
		prop.setProperty(key, value);
		// save properties file
		writePropFileToDisk();
	}
}
