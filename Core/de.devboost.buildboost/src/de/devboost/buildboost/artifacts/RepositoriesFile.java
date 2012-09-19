/*******************************************************************************
 * Copyright (c) 2006-2012
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
package de.devboost.buildboost.artifacts;

import static de.devboost.buildboost.IConstants.BUILD_BOOST_REPOSITORY_URL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.artifacts.RepositoriesFile.Location;

@SuppressWarnings("serial")
public class RepositoriesFile extends AbstractArtifact {

	public static String SUB_DIR_SEPARATOR = "!";

	public static String[] SUPPORTED_TYPES = { "svn:", "git:", "get:" };

	public class Location {
		private String type;
		private String url;
		private Set<String> subDirectories;

		public Location(String type, String url) {
			super();
			this.type = type;
			this.url = url;
			this.subDirectories = new LinkedHashSet<String>();
			// TODO we wouldn't need this if we create an extra repository for
			// extensions
			if (BUILD_BOOST_REPOSITORY_URL.equals(url)) {
				subDirectories.add("Core/");
				subDirectories.add("Universal/");
			}
		}

		public String getType() {
			return type;
		}

		public String getUrl() {
			return url;
		}

		public Set<String> getSubDirectories() {
			return subDirectories;
		}

	}

	private File file;
	private Map<String, Location> locations;

	public RepositoriesFile(File file) throws BuildException {
		this.file = file;
		setIdentifier(file.getName());
		readContent(file);
	}

	private void readContent(File file) throws BuildException {
		locations = new LinkedHashMap<String, RepositoriesFile.Location>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String locationString;
			while ((locationString = reader.readLine()) != null) {
				locationString = locationString.trim();
				if (!isComment(locationString)) {
					boolean supportedTypeDetect = isSupportedProtocolTyp(
							locationString, locations);
					if (!supportedTypeDetect) {
						reader.close();
						System.out.println("INVALID Location:>"
								+ locationString + "<");
						throw new BuildException(
								"Cannot handle repository location: "
										+ locationString);
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isSupportedProtocolTyp(String locationString,
			Map<String, Location> locationsMap) {
		boolean supportedTypeDetect = false;
		for (String supportedType : SUPPORTED_TYPES) {
			if (locationString.startsWith(supportedType)) {
				String type = supportedType.substring(0,
						supportedType.length() - 1);
				String url = locationString.substring(supportedType.length())
						.trim();
				String subDirectory = null;
				int idx = url.lastIndexOf(SUB_DIR_SEPARATOR);
				if (idx != -1) {
					subDirectory = url.substring(idx + 1);
					url = url.substring(0, idx);
				}
				Location location = locationsMap.get(url);
				if (location == null) {
					location = new Location(type, url);
					locationsMap.put(url, location);
				}
				if (subDirectory != null) {
					location.getSubDirectories().add(subDirectory);
				}
				// if valid location then return true
				supportedTypeDetect = true;
				break;
			}
		}
		return supportedTypeDetect;
	}

	private boolean isComment(String locationString) {
		if (locationString.isEmpty()) {
			return true;
		}
		if (locationString.startsWith("//")) {
			return true;
		}
		return false;
	}

	public File getFile() {
		return file;
	}

	@Override
	public long getTimestamp() {
		return file.lastModified();
	}

	public List<Location> getLocations() {
		return new ArrayList<Location>(locations.values());
	}
}
