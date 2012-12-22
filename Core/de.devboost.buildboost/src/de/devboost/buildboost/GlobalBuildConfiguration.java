package de.devboost.buildboost;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Used as Singleton: only one instance in a jvm is needed (user changes during
 * the build are not permitted)
 * 
 */
public class GlobalBuildConfiguration {

	final private Properties userProperties = new Properties();

	private Boolean wasRead = Boolean.FALSE;

	// the one and only instance should be created by BuildContext
	GlobalBuildConfiguration() {
		userProperties.setProperty("debug", "0");
		readGlobalConfiguration();
	}

	private void readGlobalConfiguration() {
		final String userHomePath = System.getProperty("user.home");
		final String globalConfigFileName = userHomePath + File.separator
				+ ".buildboost";
		final File globalConfigFile = new File(globalConfigFileName);

		// not thread safe !
		// but it is ok, because changes of configuration are permitted
		if (Boolean.FALSE.equals(this.wasRead)) {

			if (globalConfigFile.exists() && globalConfigFile.length() > 1) {
				System.out
						.println("Loading user defined global configuration from .buildboost file.");
				FileReader reader = null;
				try {
					reader = new FileReader(globalConfigFile);
					userProperties.load(reader);
					this.wasRead = Boolean.TRUE;
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
	}
}
