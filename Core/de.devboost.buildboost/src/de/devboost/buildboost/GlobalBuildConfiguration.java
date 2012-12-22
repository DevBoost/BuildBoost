package de.devboost.buildboost;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * This class is a singleton!
 * 
 */
public class GlobalBuildConfiguration {

	final private static Properties userProperties = new Properties();

	// none extern instance creation support
	private GlobalBuildConfiguration() {
		System.out.println("GlobalUserConfig: set defaults");
		userProperties.setProperty("debug", "0");
	}

	// lazy init by static holder class
	private static class Holder {
		private static final GlobalBuildConfiguration INSTANCE = new GlobalBuildConfiguration();

		private Holder() {
			System.out.println("GlobalUserConfig: read config file");
			readGlobalConfiguration();
		}
	}

	public static GlobalBuildConfiguration getInstance() {
		return Holder.INSTANCE;
	}

	private static void readGlobalConfiguration() {
		final String userHomePath = System.getProperty("user.home");
		final String globalConfigFileName = userHomePath + File.separator
				+ ".buildboost";
		final File globalConfigFile = new File(globalConfigFileName);

		if (globalConfigFile.exists() && globalConfigFile.length() > 1) {
			System.out
					.println("Loading user defined global configuration from .buildboost file.");
			FileReader reader = null;
			try {
				reader = new FileReader(globalConfigFile);
				userProperties.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out
					.println("There was no user configuration found in the users .buildboost file");
		}
	}

	public String getConfigItem(final String key) {

		readGlobalConfiguration();
		return userProperties.getProperty(key);

	}

	public String getConfigItem(final String key, final String defaultValue) {
		readGlobalConfiguration();
		return userProperties.getProperty(key, defaultValue);

	}

}
