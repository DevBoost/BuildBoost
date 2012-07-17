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
package de.devboost.buildboost.discovery.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.UnresolvedDependency;

/**
 * This class can be used to extract information from 'MANIFEST.MF' files.
 */
public class ManifestReader {

	public final static String ALPHA = "[a-zA-Z0-9_\\.-]";
	public final static String ALPHA_AND_MINUS = "[a-zA-Z0-9_\\.]|-";
	public final static String QUALIFIED_NAME_REGEX = "(" + ALPHA + "+)";
	public final static String OPTION_NAME_REGEX = "((" + ALPHA_AND_MINUS + ")+)";
	public final static String QUALIFIED_NUMBER_REGEX = "([0-9\\.]+)";
	public final static String INCLUDING_EXCLUDING_REGEX = "(\\[|\\))";
	public final static String BUNDLE_VERSION_REGEX = "bundle-version=\\\"(" + QUALIFIED_NUMBER_REGEX + "|" + INCLUDING_EXCLUDING_REGEX + QUALIFIED_NUMBER_REGEX + "," + QUALIFIED_NUMBER_REGEX + INCLUDING_EXCLUDING_REGEX + ")\\\"";
	public final static String RESOLUTION_REFEX = "resolution:=\\\"?optional\\\"?";
	public final static String VISIBILITY_REGEX = "visibility:=\\\"?reexport\\\"?";
	public final static String OPTION_VALUE_REGEX = "((\\\"[^\\\"]+\\\")+|[^\\\"]([^;,])+)";
	public final static String OPTION_REGEX = ";[ ]*(" + OPTION_NAME_REGEX + "[:]?=" + OPTION_VALUE_REGEX + ")";
	public final static String DEPENDENCY_REGEX = 
			"(" + 
			QUALIFIED_NAME_REGEX + 
			"((" + OPTION_REGEX + ")*)" + 
			")" + 
			",?";

	private final static Pattern DEPENDENCY_PATTERN = Pattern.compile(DEPENDENCY_REGEX);

	private final static String REQUIRE_PREFIX = "Require-Bundle: ";
	public final static String NAME_PREFIX = "Bundle-SymbolicName: ";
	public final static String CLASSPATH_PREFIX = "Bundle-ClassPath: ";
	public final static String REQUIRE_VERSION_PREFIX = "bundle-version=";
	public final static String VERSION_PREFIX = "Bundle-Version: ";
	public final static String FRAGMENT_HOST_PREFIX = "Fragment-Host: ";
	public final static String RESOLUTION_OPTIONAL = "resolution:=\"optional\"";
			
	private final static Pattern REQUIRE_REGEX = Pattern.compile(REQUIRE_PREFIX + ".*(\r)?\n");
	public final static Pattern NAME_REGEX = Pattern.compile(NAME_PREFIX + ".*(\r)?\n");
	public final static Pattern CLASSPATH_REGEX = Pattern.compile(CLASSPATH_PREFIX + ".*(\r)?\n");
	private static final Pattern VERSION_REGEX = Pattern.compile(VERSION_PREFIX+ ".*(\r)?\n");
	private static final Pattern FRAGMENT_HOST_REGEX = Pattern.compile(FRAGMENT_HOST_PREFIX+ ".*(\r)?\n");
	
	private String content;
	private Set<String> classpath;
	private Set<UnresolvedDependency> dependencies;
	private String symbolicName;
	private UnresolvedDependency fragmentHost;
	private String version;
	
	public ManifestReader(InputStream manifestInputStream) throws IOException {
		content = getContentAsString(manifestInputStream);
		manifestInputStream.close();
		content = content.replace("\r\n ", "");
		content = content.replace("\n ", "");
		content = content.replace("\r ", "");
	}

	public Set<UnresolvedDependency> getDependencies() throws IOException {
		return getDependencies(content);
	}
	
	public String getSymbolicName() {
		if (symbolicName == null) {
			symbolicName = getValue(NAME_REGEX, NAME_PREFIX, "UNKNOWN_SYMBOLIC_NAME");
		}
		return symbolicName;
	}

	public Set<String> getBundleClassPath() {
		if (classpath == null) {
			classpath = new LinkedHashSet<String>();
			Matcher matcher = CLASSPATH_REGEX.matcher(content);
			if (matcher.find()) {
				String classpathLine = matcher.group();
				classpathLine = classpathLine.substring(CLASSPATH_PREFIX.length());
				String[] classpathEntries = classpathLine.split(",");
				for (String classpathEntry : classpathEntries) {
					String[] parts = classpathEntry.split("\\;");
					String path = parts[0].trim();
					if (".".equals(path)) {
						continue;
					}
					classpath.add(path);
				}
			}
		}
		return classpath;
	}

	private Set<UnresolvedDependency> getDependencies(String content) {
		if (dependencies == null) {
			dependencies = new LinkedHashSet<UnresolvedDependency>();
			Matcher matcher = REQUIRE_REGEX.matcher(content);
			if (matcher.find()) {
				String requiredBundlesLine = matcher.group();
				requiredBundlesLine = requiredBundlesLine.substring(REQUIRE_PREFIX.length());
				Matcher matcher2 = DEPENDENCY_PATTERN.matcher(requiredBundlesLine);
				while (matcher2.find()) {
					String bundleName = matcher2.group(2).trim();
					String allOptions = matcher2.group(3).trim();
					String[] options = allOptions.split(";( )*");
					
					// TODO use this two properties
					boolean inclusiveMin = true;
					boolean inclusiveMax = true;
					String minVersion = null;
					String maxVersion = null;
					boolean optional = false;
					boolean reexport = false;
					for (String option : options) {
						if ("".equals(option)) {
							continue;
						}
						String[] groups = getGroups(option, BUNDLE_VERSION_REGEX);
						if (groups != null) {
							inclusiveMin = "[".equals(groups[3]);
							String group4 = groups[4];
							minVersion = group4 == null ? null : group4.trim();
							String group5 = groups[5];
							maxVersion = group5 == null ? null : group5.trim();
							String group6 = groups[6];
							inclusiveMax = "]".equals(group6);
							continue;
						}
						groups = getGroups(option, RESOLUTION_REFEX);
						if (groups != null) {
							optional = true;
							continue;
						}
						groups = getGroups(option, VISIBILITY_REGEX);
						if (groups != null) {
							reexport = true;
							continue;
						}
					}
					if ("system.bundle".equals(bundleName)) {
						bundleName = "org.eclipse.osgi";
					}
					UnresolvedDependency dependency = new UnresolvedDependency(Plugin.class, bundleName, minVersion, inclusiveMin, maxVersion, inclusiveMax, optional, reexport);
					dependencies.add(dependency);
				}
			}
		}
		return dependencies;
	}

	private String[] getGroups(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			String[] groups = new String[matcher.groupCount() + 1];
			for (int i = 0; i < groups.length; i++) {
				groups[i] = matcher.group(i);
				//System.out.println("groups[" + i +"] = " + groups[i]);
			}
			return groups;
		}
		return null;
	}

	public static String getContentAsString(InputStream inputStream) throws IOException {
		StringBuffer content = new StringBuffer();
		InputStreamReader reader = new InputStreamReader(inputStream);
		int next = -1;
		while ((next = reader.read()) >= 0) {
			content.append((char) next);
		}
		return content.toString();
	}

	public String getVersion() {
		if (version == null) {
			String rawVersion = getValue(VERSION_REGEX, VERSION_PREFIX, "UNKNOWN_VERSION");
			
			Matcher matcher = Pattern.compile("[0-9]+(\\.[0-9]+)*").matcher(rawVersion);
			matcher.find();
			version = matcher.group();
		}
		return version;
	}

	public UnresolvedDependency getFragmentHost() {
		if (fragmentHost == null) {
			String fragmentHostBundleName = getValue(FRAGMENT_HOST_REGEX, FRAGMENT_HOST_PREFIX, null);
			fragmentHost = new UnresolvedDependency(Plugin.class, fragmentHostBundleName, null, true, null, true, false, false);
		}
		return fragmentHost;
	}
	
	private String getValue(Pattern regex, String prefix, String defaultValue) {
		Matcher matcher = regex.matcher(content);
		if (matcher.find()) {
			String symbolicNameLine = matcher.group();
			String[] parts = symbolicNameLine.split(";");
			symbolicNameLine = parts[0];
			symbolicNameLine = symbolicNameLine.replace("\n", "");
			symbolicNameLine = symbolicNameLine.replace("\r", "");
			symbolicNameLine = symbolicNameLine.substring(prefix.length());
			return symbolicNameLine;
		}
		return defaultValue;
	}
}
