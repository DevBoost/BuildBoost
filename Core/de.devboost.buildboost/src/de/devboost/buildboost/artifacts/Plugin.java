/*******************************************************************************
 * Copyright (c) 2006-2014
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.devboost.buildboost.discovery.reader.DotClasspathReader;
import de.devboost.buildboost.discovery.reader.ManifestReader;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.IFileArtifact;
import de.devboost.buildboost.model.UnresolvedDependency;

/**
 * A Plug-in represents an OSGi bundle (i.e., an Eclipse plug-in) that is either
 * found in a target platform (e.g., an Eclipse distribution) or that is part
 * of the workspace that is subject to the build process.
 */
public class Plugin extends AbstractArtifact implements IFileArtifact, Serializable {

	private static final long serialVersionUID = 7995849293974638695L;

	/**
	 * The set of plug-in fragments that complement this plug-in.
	 */
	private Set<Plugin> fragments = new LinkedHashSet<Plugin>();	
	private Plugin fragmentHost = null;

	/**
	 * The plug-in that is complemented by this plug-in (if this plug-in is a
	 * fragment).
	 */
	private UnresolvedDependency unresolvedFragmentHost;
	
	/**
	 * The libraries that are required by this plug-in.
	 */
	private Set<String> libs = new LinkedHashSet<String>();
	
	/**
	 * The location of this plug-in. This can be either a directory or a JAR 
	 * file.
	 */
	protected File location;
	
	/**
	 * The absolute path to the location of the plug-in. Stored for performance
	 * reasons only.
	 */
	private String absolutePath;

	private Set<String> allLibs;

	private Set<String> sourceFolders = new LinkedHashSet<String>();
	
	private Set<Plugin> allDependencies;
	
	private Set<Package> exportedPackages;

	private String name;

	private String version;

	/**
	 * Create a descriptor for the plug-in at the given location. Reads the
	 * manifest and class path information if available.
	 * 
	 * @param location
	 * @throws IOException 
	 * @throws Exception
	 */
	public Plugin(File location) throws IOException {
		super();
		this.location = location;
		analyzeManifest();
		analyzeClassPath();
	}
	
	private void analyzeManifest() throws IOException {
		InputStream manifestInputStream = getManifestInputStream();
		if (manifestInputStream == null) {
			setIdentifier(location.getName());
			exportedPackages = Collections.emptySet();
		} else {
			ManifestReader reader = new ManifestReader(manifestInputStream);
			manifestInputStream.close();
			
			Set<UnresolvedDependency> unresolvedDependencies = reader.getDependencies();
			addAlternativeIdentifiers(unresolvedDependencies);
			getUnresolvedDependencies().addAll(unresolvedDependencies);
			unresolvedFragmentHost = reader.getFragmentHost();
			libs.addAll(reader.getBundleClassPath());
			addWebLibraries();
			
			setIdentifier(reader.getSymbolicName());
			setName(reader.getName());
			setVersion(reader.getVersion());
			
			Set<String> exportedPackageName = reader.getExportedPackages();
			exportedPackages = new LinkedHashSet<Package>();
			for (String packageName : exportedPackageName) {
				exportedPackages.add(new Package(packageName, this));
			}
		}
	}
	
	private void addAlternativeIdentifiers(
			Set<UnresolvedDependency> unresolvedDependencies) {
		Properties properties = new Properties();
		File location = getLocation();
		if (!location.isDirectory()) {
			return;
		}
		File alternativesFile = new File(location, "alternative_dependencies.properties");
		if (!alternativesFile.exists()) {
			return;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(alternativesFile);
			properties.load(fis);
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
		
		Set<UnresolvedDependency> removableDependencies = new LinkedHashSet<UnresolvedDependency>();
		for (UnresolvedDependency unresolvedDependency : unresolvedDependencies) {
			String identifier = unresolvedDependency.getIdentifier();
			String alternativesValue = properties.getProperty(identifier);
			if (alternativesValue == null) {
				continue;
			}
			if (alternativesValue.trim().isEmpty()) {
				removableDependencies.add(unresolvedDependency);
				continue;
			}
			
			String[] alternatives = alternativesValue.split(",");
			for (String alternative : alternatives) {
				unresolvedDependency.addAlternativeIdentifier(alternative);
			}
		}
		
		unresolvedDependencies.removeAll(removableDependencies);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	private void addWebLibraries() {
		// TODO this belongs somewhere else
		if (location.isDirectory()) {
			File webLibsDir = new File(new File(new File(location, "WebContent"), "WEB-INF"), "lib");
			if (webLibsDir.exists()) {
				File[] webLibs = webLibsDir.listFiles(new FileFilter() {
					
					public boolean accept(File file) {
						return file.isFile() && file.getName().endsWith(".jar");
					}
				});
				if (webLibs == null) {
					return;
				}
				for (File webLib : webLibs) {
					try {
						String relativePath = webLib.getCanonicalFile().getAbsolutePath().substring(location.getCanonicalFile().getAbsolutePath().length() + 1);
						libs.add(relativePath);
					} catch (IOException e) {
						// TODO handle exception
						System.out.println("IOException: " + e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Resolves all unresolved dependencies (denoted by symbolic names) by 
	 * replacing them with actual references to plug-in objects in the given set 
	 * of available plug-ins.
	 * 
	 * @param allArtifacts all artifacts that were discovered
	 */
	@Override
	public void resolveDependencies(Collection<? extends IArtifact> allArtifacts) {
		for (IArtifact artifact : allArtifacts) {
			Collection<UnresolvedDependency> unresolvedDependencies = new ArrayList<UnresolvedDependency>(getUnresolvedDependencies());
			
			for (UnresolvedDependency dependency : unresolvedDependencies) {
				if (dependency.isFulfilledBy(artifact)) {
					addResolvedDependency(dependency, artifact);
				}
			}
			// --- inserted in superclass code ----
			if (artifact instanceof Plugin) {
				Plugin plugin = (Plugin) artifact;
				if (unresolvedFragmentHost != null && unresolvedFragmentHost.isFulfilledBy(plugin)) {
					plugin.addFragment(this);
					fragmentHost = plugin;
					addDependency(plugin);
				}
			}
		}
	}
	
	private void addFragment(Plugin pluginFragment) {
		fragments.add(pluginFragment);
	}

	private Collection<Plugin> getPluginDependencies() {
		Collection<Plugin> result = new LinkedHashSet<Plugin>();
		for (IDependable artifact : getDependencies()) {
			if (artifact instanceof Plugin) {
				Plugin plugin = (Plugin) artifact;
				result.add(plugin);
				if (getFragmentHost() != null) {
					result.add(getFragmentHost());
				}
			}
			if (artifact instanceof Package) {
				Package p = (Package) artifact;
				result.add(p.getExportingPlugin());
			}
		}
		return Collections.unmodifiableCollection(result);
	}
	
	/**
	 * Returns all plug-ins that this plug-in depends on, except the ones that
	 * are part of the target platform.
	 */
	public Set<Plugin> getDependenciesExcludingTargetPlatform() {
		Set<Plugin> result = new LinkedHashSet<Plugin>();
		for (IDependable dependency : getDependencies()) {
			if (dependency instanceof Plugin) {
				Plugin plugin = (Plugin) dependency;
				if (!plugin.isProject()) {
					continue;
				}
				result.add(plugin);
			}
		}
		return Collections.unmodifiableSet(result);
	}
	
	public Set<Package> getExportedPackages() {
		return exportedPackages;
	}
	
	public Set<String> getLibs() {
		return libs;
	}

	/**
	 * Returns all dependencies transitively.
	 */
	public Set<Plugin> getAllDependencies() {
		if (allDependencies == null) {
			Collection<Plugin> pluginDependencies = getPluginDependencies();
			
			allDependencies = new LinkedHashSet<Plugin>();
			allDependencies.addAll(pluginDependencies);
			allDependencies.addAll(fragments);
			
			for (Plugin dependency : pluginDependencies) {
				allDependencies.addAll(dependency.getAllDependencies());
			}
			for (Plugin fragment : fragments) {
				allDependencies.addAll(fragment.getAllDependencies());
			}
		}
		return allDependencies;
	}
	
	/**
	 * Returns the absolute paths of all libraries (transitively).
	 */
	public Set<String> getAllLibPaths() {
		if (allLibs == null) {
			allLibs = new LinkedHashSet<String>();
			for (String lib : getLibs()) {
				allLibs.add(getAbsoluteLibPath(lib));
			}
			for (Plugin dependency : getPluginDependencies()) {
				allLibs.addAll(dependency.getAllLibPaths());
			}
		}
		return allLibs;
	}

	/**
	 * Returns the absolute path of the given library.
	 */
	public String getAbsoluteLibPath(String lib) {
		String prefix = getAbsolutePath() + "/";
		if (lib.startsWith("/")) {
			// absolute paths to libraries must be handled differently
			return prefix + ".." + lib;
		} else {
			return prefix + lib;
		}
	}
	
	public File getLocation() {
		return location;
	}
	
	public String getAbsolutePath() {
		if (absolutePath == null) {
			absolutePath = location.getAbsolutePath();
		}
		return absolutePath;
	}
	
	/**
	 * Returns the absolute locations of the source folders in this plug-in, if
	 * there are any.
	 */
	public File[] getSourceFolders() {
		if (location.isFile()) {
			return new File[0];
		}
		
		if (sourceFolders == null) {
			return new File[0];
		}

		File[] sourceFolderFiles = new File[sourceFolders.size()];
		int i = 0;
		for (String	sourceFolder : sourceFolders) {
			sourceFolderFiles[i] = new File(location, sourceFolder);
			i++;
		}
		return sourceFolderFiles;
	}

	/**
	 * Returns the relative paths of the source folders in this plug-in, if
	 * there are any.
	 */
	public Set<String> getRelativeSourceFolders() {
		if (location.isFile()) {
			return Collections.emptySet();
		}
		
		if (sourceFolders == null) {
			return Collections.emptySet();
		}

		return sourceFolders;
	}

	/**
	 * Checks whether this plug-in depends on the given other plug-in.
	 */
	public boolean dependsOn(Plugin otherPlugin) {
		// check direct dependency
		if (getDependencies().contains(otherPlugin)) {
			return true;
		}
		for (Plugin dependency : getPluginDependencies()) {
			if (dependency.dependsOn(otherPlugin)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Plugin other = (Plugin) obj;
		if (getIdentifier() == null) {
			if (other.getIdentifier() != null)
				return false;
		} else if (!getIdentifier().equals(other.getIdentifier()))
			return false;
		return true;
	}

	public boolean hasManifest() {
		return getManifestFile().exists();
	}
	
	/**
	 * Returns an input stream to access the manifest of this plug-in.
	 * 
	 * This method is protected to allow test to override it.
	 */
	protected InputStream getManifestInputStream() throws IOException {
		File pluginLocation = getLocation();
		if (pluginLocation.isFile()) {
			ZipFile zipFile = new ZipFile(pluginLocation);
			ZipEntry manifestEntry = zipFile.getEntry("META-INF/MANIFEST.MF");
			if (manifestEntry == null) {
				return null;
			}
			InputStream inputStream = zipFile.getInputStream(manifestEntry);
			return inputStream;
		} else {
			File manifest = getManifestFile();
			if (!manifest.exists()) {
				System.out.println("INFO: Project without MANIFEST: " + getLocation().getName());
				return null;
			}
			return new FileInputStream(manifest);
		}
	}

	private File getManifestFile() {
		File manifest = new File(new File(getLocation(), "META-INF"), "MANIFEST.MF");
		return manifest;
	}
	
	private void analyzeClassPath() throws IOException {
		InputStream dotClassPathInputStream = getDotClasspathInputStream();
		if (dotClassPathInputStream != null) {
			DotClasspathReader classpathReader = new DotClasspathReader(dotClassPathInputStream);
			this.libs.addAll(classpathReader.getLibraries());
			this.sourceFolders.addAll(classpathReader.getSourceFolders());
			dotClassPathInputStream.close();
		}
	}

	/**
	 * This method is protected to allow test to override it.
	 */
	protected InputStream getDotClasspathInputStream() throws FileNotFoundException {
		File pluginLocation = getLocation();
		if (pluginLocation.isDirectory()) {
			File dotClassPathFile = new File(pluginLocation, ".classpath");
			if (!dotClassPathFile.exists()) {
				return null;
			}
			return new FileInputStream(dotClassPathFile);
		} else {
			// JARs do usually not contain .classpath files since they contain
			// compiled plug-ins.
			return null;
		}
	}
	
	public boolean isExperimental() {
		return new File(getAbsolutePath(), "EXPERIMENTAL").exists();
	}

	/**
	 * Returns true if this plug-in is a project that is built. If the plug-in
	 * was obtained from another location (e.g., as a JAR from an Eclipse
	 * distribution), this method will return false.
	 */
	public boolean isProject() {
		return true;
	}
	
	public boolean isJarFile() {
		return location.isFile() && location.getName().endsWith(".jar");
	}

	public Plugin getFragmentHost() {
		return fragmentHost;
	}

	public File getFile() {
		return getLocation();
	}

}
