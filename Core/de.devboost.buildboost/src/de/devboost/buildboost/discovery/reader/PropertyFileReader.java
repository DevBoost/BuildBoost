/*******************************************************************************
 * Copyright (c) 2006-2015
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Dresden, Amtsgericht Dresden, HRB 34001
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Dresden, Germany
 *      - initial API and implementation
 ******************************************************************************/
package de.devboost.buildboost.discovery.reader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyFileReader {

	private Properties properties;

	public PropertyFileReader(File file) {
		readPropertyFile(file);
	}

	private void readPropertyFile(File file) {
		properties = new Properties();
		try {
			Reader reader = new FileReader(file);
			properties.load(reader);
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException("Exception while reading property file: " + e.getMessage());
		}
	}

	public String getValue(String... path) {
		StringBuilder key = new StringBuilder();
		for (int i = 0; i < path.length - 1; i++) {
			key.append(path[i]);
			key.append("/");
		}
		if (path.length > 0) {
			key.append(path[path.length - 1]);
		}
		return getValueInternal(key.toString());
	}

	private String getValueInternal(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			return null;
		}
		if (value.startsWith("$")) {
			return getValueInternal(value.substring(1));
		}
		return value;
	}

	public Map<String, String> getValues(String... path) {
		StringBuilder key = new StringBuilder();
		Map<String, String> values = new LinkedHashMap<String, String>();
		for (int i = 0; i < path.length; i++) {
			key.append(path[i]);
			key.append("/");
		}
		
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			if (entry.getKey().toString().startsWith(key.toString())) {
				String subKey = entry.getKey().toString().substring(key.toString().length());
				if (!subKey.contains("/")) {
					values.put(subKey, entry.getValue().toString());
				}
			}
		}
		
		return values;
	}
}
