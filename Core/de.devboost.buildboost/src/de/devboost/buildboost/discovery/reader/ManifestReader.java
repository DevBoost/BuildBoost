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

import de.devboost.buildboost.artifacts.Package;
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
	public final static String USES_REGEX = "uses:=\\\"?" + ALPHA + "\\\"?";
	public final static String OPTION_VALUE_REGEX = "((\\\"[^\\\"]+\\\")+|[^\\\"]([^;,])+)";
	public final static String OPTION_REGEX = ";[ ]*(" + OPTION_NAME_REGEX + "[:]?=" + OPTION_VALUE_REGEX + ")";
	public final static String DEPENDENCY_REGEX = 
			"(" + 
			QUALIFIED_NAME_REGEX + 
			"((" + OPTION_REGEX + ")*)" + 
			")" + 
			",?";

	private final static Pattern DEPENDENCY_PATTERN = Pattern.compile(DEPENDENCY_REGEX);

	public final static String REQUIRED_BUNDLE_PREFIX = "Require-Bundle: ";
	public final static String IMPORT_PACKAGE_PREFIX = "Import-Package: ";
	public final static String EXPORT_PACKAGE_PREFIX = "Export-Package: ";
	public final static String NAME_PREFIX = "Bundle-SymbolicName: ";
	public final static String CLASSPATH_PREFIX = "Bundle-ClassPath: ";
	public final static String REQUIRE_VERSION_PREFIX = "bundle-version=";
	public final static String VERSION_PREFIX = "Bundle-Version: ";
	public final static String FRAGMENT_HOST_PREFIX = "Fragment-Host: ";
	public final static String RESOLUTION_OPTIONAL = "resolution:=\"optional\"";
			
	public final static Pattern REQUIRED_BUNDLE_REGEX = Pattern.compile(REQUIRED_BUNDLE_PREFIX + ".*(\r)?\n");
	public final static Pattern IMPORT_PACKAGE_REGEX = Pattern.compile("(?<!c)" + IMPORT_PACKAGE_PREFIX + ".*(\r)?\n");
	public final static Pattern EXPORT_PACKAGE_REGEX = Pattern.compile(EXPORT_PACKAGE_PREFIX + ".*(\r)?\n");
	public final static Pattern NAME_REGEX = Pattern.compile(NAME_PREFIX + ".*(\r)?\n");
	public final static Pattern CLASSPATH_REGEX = Pattern.compile(CLASSPATH_PREFIX + ".*(\r)?\n");
	public static final Pattern VERSION_REGEX = Pattern.compile(VERSION_PREFIX+ ".*(\r)?\n");
	public static final Pattern FRAGMENT_HOST_REGEX = Pattern.compile(FRAGMENT_HOST_PREFIX+ ".*(\r)?\n");
	
	private String content;
	private Set<String> classpath;
	private Set<UnresolvedDependency> dependencies;
	private Set<String> exportedPackages;
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

	public Set<UnresolvedDependency> getDependencies() {
		if (dependencies == null) {
			dependencies = new LinkedHashSet<UnresolvedDependency>();
			findDependencies(content, Plugin.class, REQUIRED_BUNDLE_REGEX, REQUIRED_BUNDLE_PREFIX);
			findDependencies(content, Package.class, IMPORT_PACKAGE_REGEX, IMPORT_PACKAGE_PREFIX);
		}
		return dependencies;
	}
	
	public Set<String> getExportedPackages() {
		if (exportedPackages == null) {
			exportedPackages = findDependencies(content, null, EXPORT_PACKAGE_REGEX, EXPORT_PACKAGE_PREFIX);
			if (getSymbolicName().equals("org.eclipse.osgi")) {
				addOSGINativeDefaultPackageExports();
			}
		}
		return exportedPackages;
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
					//TODO this is declared in "org.eclipse.equinox.registry" but the JAR does not exist
					if ("runtime_registry_compatibility.jar".equals(path)) {
						continue;
					}
					classpath.add(path);
				}
			}
		}
		return classpath;
	}

	private Set<String> findDependencies(String content, Class<?> dependencyType, Pattern regex, String prefix) {
		Set<String> names = new LinkedHashSet<String>();
		Matcher matcher = regex.matcher(content);
		if (matcher.find()) {
			String requiredBundlesLine = matcher.group();
			requiredBundlesLine = requiredBundlesLine.substring(prefix.length());
			Matcher matcher2 = DEPENDENCY_PATTERN.matcher(requiredBundlesLine);
			while (matcher2.find()) {
				String bundleOrPackageName = matcher2.group(2).trim();
				String allOptions = matcher2.group(3).trim();
				String[] options = allOptions.split(";( )*");
				
				// TODO use this two properties
				boolean inclusiveMin = true;
				boolean inclusiveMax = true;
				String minVersion = null;
				String maxVersion = null;
				boolean optional = false;
				boolean reexport = false;
				//boolean uses = false;
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
					/*groups = getGroups(option, USES_REGEX);
					if (groups != null) {
						uses = true;
						continue;
					}*/
				}
				if ("system.bundle".equals(bundleOrPackageName)) {
					bundleOrPackageName = "org.eclipse.osgi";
				}
				if (dependencyType != null) {
					UnresolvedDependency dependency = new UnresolvedDependency(dependencyType, bundleOrPackageName, minVersion, inclusiveMin, maxVersion, inclusiveMax, optional, reexport);
					
					if (dependencyType == Package.class && getExportedPackages().contains(bundleOrPackageName)) {
						// cyclic! 
						// TODO improve package import/export support
					} else {
						dependencies.add(dependency);
					}
				}
				names.add(bundleOrPackageName);
			}
		}
		return names;
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
	
	//TODO read .profile file(s) instead!
	private void addOSGINativeDefaultPackageExports() {
		exportedPackages.add("javax.accessibility");
		exportedPackages.add("javax.activity");
		exportedPackages.add("javax.crypto");
		exportedPackages.add("javax.crypto.interfaces");
		exportedPackages.add("javax.crypto.spec");
		exportedPackages.add("javax.imageio");
		exportedPackages.add("javax.imageio.event");
		exportedPackages.add("javax.imageio.metadata");
		exportedPackages.add("javax.imageio.plugins.bmp");
		exportedPackages.add("javax.imageio.plugins.jpeg");
		exportedPackages.add("javax.imageio.spi");
		exportedPackages.add("javax.imageio.stream");
		exportedPackages.add("javax.management");
		exportedPackages.add("javax.management.loading");
		exportedPackages.add("javax.management.modelmbean");
		exportedPackages.add("javax.management.monitor");
		exportedPackages.add("javax.management.openmbean");
		exportedPackages.add("javax.management.relation");
		exportedPackages.add("javax.management.remote");
		exportedPackages.add("javax.management.remote.rmi");
		exportedPackages.add("javax.management.timer");
		exportedPackages.add("javax.naming");
		exportedPackages.add("javax.naming.directory");
		exportedPackages.add("javax.naming.event");
		exportedPackages.add("javax.naming.ldap");
		exportedPackages.add("javax.naming.spi");
		exportedPackages.add("javax.net");
		exportedPackages.add("javax.net.ssl");
		exportedPackages.add("javax.print");
		exportedPackages.add("javax.print.attribute");
		exportedPackages.add("javax.print.attribute.standard");
		exportedPackages.add("javax.print.event");
		exportedPackages.add("javax.rmi");
		exportedPackages.add("javax.rmi.CORBA");
		exportedPackages.add("javax.rmi.ssl");
		exportedPackages.add("javax.security.auth");
		exportedPackages.add("javax.security.auth.callback");
		exportedPackages.add("javax.security.auth.kerberos");
		exportedPackages.add("javax.security.auth.login");
		exportedPackages.add("javax.security.auth.spi");
		exportedPackages.add("javax.security.auth.x500");
		exportedPackages.add("javax.security.cert");
		exportedPackages.add("javax.security.sasl");
		exportedPackages.add("javax.sound.midi");
		exportedPackages.add("javax.sound.midi.spi");
		exportedPackages.add("javax.sound.sampled");
		exportedPackages.add("javax.sound.sampled.spi");
		exportedPackages.add("javax.sql");
		exportedPackages.add("javax.sql.rowset");
		exportedPackages.add("javax.sql.rowset.serial");
		exportedPackages.add("javax.sql.rowset.spi");
		exportedPackages.add("javax.swing");
		exportedPackages.add("javax.swing.border");
		exportedPackages.add("javax.swing.colorchooser");
		exportedPackages.add("javax.swing.event");
		exportedPackages.add("javax.swing.filechooser");
		exportedPackages.add("javax.swing.plaf");
		exportedPackages.add("javax.swing.plaf.basic");
		exportedPackages.add("javax.swing.plaf.metal");
		exportedPackages.add("javax.swing.plaf.multi");
		exportedPackages.add("javax.swing.plaf.synth");
		exportedPackages.add("javax.swing.table");
		exportedPackages.add("javax.swing.text");
		exportedPackages.add("javax.swing.text.html");
		exportedPackages.add("javax.swing.text.html.parser");
		exportedPackages.add("javax.swing.text.rtf");
		exportedPackages.add("javax.swing.tree");
		exportedPackages.add("javax.swing.undo");
		exportedPackages.add("javax.transaction");
		exportedPackages.add("javax.transaction.xa");
		exportedPackages.add("javax.xml");
		exportedPackages.add("javax.xml.datatype");
		exportedPackages.add("javax.xml.namespace");
		exportedPackages.add("javax.xml.parsers");
		exportedPackages.add("javax.xml.transform");
		exportedPackages.add("javax.xml.transform.dom");
		exportedPackages.add("javax.xml.transform.sax");
		exportedPackages.add("javax.xml.transform.stream");
		exportedPackages.add("javax.xml.validation");
		exportedPackages.add("javax.xml.xpath");
		exportedPackages.add("org.ietf.jgss");
		exportedPackages.add("org.omg.CORBA");
		exportedPackages.add("org.omg.CORBA_2_3");
		exportedPackages.add("org.omg.CORBA_2_3.portable");
		exportedPackages.add("org.omg.CORBA.DynAnyPackage");
		exportedPackages.add("org.omg.CORBA.ORBPackage");
		exportedPackages.add("org.omg.CORBA.portable");
		exportedPackages.add("org.omg.CORBA.TypeCodePackage");
		exportedPackages.add("org.omg.CosNaming");
		exportedPackages.add("org.omg.CosNaming.NamingContextExtPackage");
		exportedPackages.add("org.omg.CosNaming.NamingContextPackage");
		exportedPackages.add("org.omg.Dynamic");
		exportedPackages.add("org.omg.DynamicAny");
		exportedPackages.add("org.omg.DynamicAny.DynAnyFactoryPackage");
		exportedPackages.add("org.omg.DynamicAny.DynAnyPackage");
		exportedPackages.add("org.omg.IOP");
		exportedPackages.add("org.omg.IOP.CodecFactoryPackage");
		exportedPackages.add("org.omg.IOP.CodecPackage");
		exportedPackages.add("org.omg.Messaging");
		exportedPackages.add("org.omg.PortableInterceptor");
		exportedPackages.add("org.omg.PortableInterceptor.ORBInitInfoPackage");
		exportedPackages.add("org.omg.PortableServer");
		exportedPackages.add("org.omg.PortableServer.CurrentPackage");
		exportedPackages.add("org.omg.PortableServer.POAManagerPackage");
		exportedPackages.add("org.omg.PortableServer.POAPackage");
		exportedPackages.add("org.omg.PortableServer.portable");
		exportedPackages.add("org.omg.PortableServer.ServantLocatorPackage");
		exportedPackages.add("org.omg.SendingContext");
		exportedPackages.add("org.omg.stub.java.rmi");
		exportedPackages.add("org.w3c.dom");
		exportedPackages.add("org.w3c.dom.bootstrap");
		exportedPackages.add("org.w3c.dom.css");
		exportedPackages.add("org.w3c.dom.events");
		exportedPackages.add("org.w3c.dom.html");
		exportedPackages.add("org.w3c.dom.ls");
		exportedPackages.add("org.w3c.dom.ranges");
		exportedPackages.add("org.w3c.dom.stylesheets");
		exportedPackages.add("org.w3c.dom.traversal");
		exportedPackages.add("org.w3c.dom.views ");
		exportedPackages.add("org.xml.sax");
		exportedPackages.add("org.xml.sax.ext");
		exportedPackages.add("org.xml.sax.helpers");
	}
}
