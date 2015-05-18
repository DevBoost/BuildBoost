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
package de.devboost.buildboost.discovery;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.artifacts.CompiledPlugin;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.artifacts.InvalidMetadataException;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.util.ArtifactUtil;
import de.devboost.buildboost.util.EclipsePluginHelper;

/**
 * The EclipseTargetPlatformAnalyzer can be used to scan an Eclipse instance to detect all contained plug-ins. This is
 * required to compile Eclipse plug-in projects that depend on plug-ins of an Eclipse target platform.
 */
// TODO is there a overlap with FeatureFinder?
public class EclipseTargetPlatformAnalyzer extends AbstractArtifactDiscoverer {

	public static final String ARTIFACT_CACHE_FILE_NAME = "artifact_cache.ser";

	private interface IArtifactCreator {

		public IArtifact create(File file) throws IOException, InvalidMetadataException;
	}

	/**
	 * The {@link CompiledPluginCreator} creates a {@link CompiledPlugin} from a file.
	 */
	private static class CompiledPluginCreator implements IArtifactCreator {

		@Override
		public IArtifact create(File file) throws IOException, InvalidMetadataException {
			return new CompiledPlugin(file);
		}
	}

	/**
	 * The {@link EclipseFeatureCreator} creates an {@link EclipseFeature} from a file.
	 */
	private static class EclipseFeatureCreator implements IArtifactCreator {

		@Override
		public IArtifact create(File fileDirectoryOrJar) throws IOException {
			if (fileDirectoryOrJar.isDirectory()) {
				return new EclipseFeature(new File(fileDirectoryOrJar, "feature.xml"), true);
			} else {
				return new EclipseFeature(fileDirectoryOrJar, true);
			}
		}
	}

	private final File targetPlatformLocation;

	public EclipseTargetPlatformAnalyzer(File targetPlatform) {
		super();
		this.targetPlatformLocation = targetPlatform;
	}

	// TODO the discover should traverse the folder hierarchy only once.
	// Could we optimize discovering in general such that there is only one traversal each time?
	public Collection<IArtifact> discoverArtifacts(IBuildContext context) throws BuildException {

		IBuildListener buildListener = context.getBuildListener();
		buildListener.handleBuildEvent(BuildEventType.INFO, "Analyzing target platform...");

		// TODO Clarify why Jendrik had to deactivate the cache
		/*
		 * Set<IArtifact> cachedArtifacts = loadDiscoveredArtifacts(); if (cachedArtifacts != null) {
		 * buildListener.handleBuildEvent(BuildEventType.INFO, "Loaded cached target platform info: " +
		 * cachedArtifacts); return cachedArtifacts; }
		 */

		Set<IArtifact> artifacts = new LinkedHashSet<IArtifact>();

		// first, find plug-ins and create respective artifact objects
		Set<File> pluginJarsAndDirs = findFiles(targetPlatformLocation, new EclipsePluginFileFilter());
		Set<IArtifact> foundPlugins = analyzeTargetPlatformJarFiles(pluginJarsAndDirs, "plug-in", buildListener,
				new CompiledPluginCreator());

		// second, find features and create respective artifact objects
		Set<File> featureJarsAndDirs = findFiles(targetPlatformLocation, new EclipseFeatureFileFilter());
		Set<IArtifact> foundFeatures = analyzeTargetPlatformJarFiles(featureJarsAndDirs, "feature", buildListener,
				new EclipseFeatureCreator());

		// third, add all found artifacts to result set
		artifacts.addAll(foundPlugins);
		artifacts.addAll(foundFeatures);
		// TODO saveDiscoveredArtifacts(artifacts);
		return artifacts;
	}

	@SuppressWarnings("unused")
	private void saveDiscoveredArtifacts(Set<IArtifact> artifacts) throws BuildException {
		try {
			File artifactsFile = new File(targetPlatformLocation, ARTIFACT_CACHE_FILE_NAME);
			FileOutputStream fos = new FileOutputStream(artifactsFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(artifacts);
			fos.close();
		} catch (IOException e) {
			throw new BuildException("Can't save discovered artifacts: " + e.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private Set<IArtifact> loadDiscoveredArtifacts() throws BuildException {
		File cacheFile = new File(targetPlatformLocation, ARTIFACT_CACHE_FILE_NAME);
		if (!cacheFile.exists()) {
			return null;
		}

		try {
			FileInputStream fis = new FileInputStream(cacheFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Object object = ois.readObject();
			fis.close();

			if (object instanceof Set) {
				return (Set<IArtifact>) object;
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new BuildException("Can't load list of discovered artifacts: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new BuildException("Can't load list of discovered artifacts: " + e.getMessage());
		}
	}

	private Set<File> findFiles(File directory, FileFilter fileFilter) {
		// Make sure we've got a directory as argument
		if (!directory.isDirectory()) {
			return Collections.emptySet();
		}

		File[] filesInDirectory = directory.listFiles();
		if (filesInDirectory == null) {
			return Collections.emptySet();
		}

		Set<File> result = new LinkedHashSet<File>();
		for (File file : filesInDirectory) {
			boolean isDirectory = file.isDirectory();

			// Ignore projects and things inside projects
			if (isDirectory) {
				boolean isProject = EclipsePluginHelper.INSTANCE.isProject(file);
				if (isProject) {
					continue;
				}
			}

			boolean accepted = fileFilter.accept(file);
			if (accepted) {
				result.add(file);
			}

			boolean isTargetPlatformRoot = "eclipse".equals(file.getName())
					&& "target-platform".equals(file.getParentFile().getName());

			// Search for nested JARs and plug-ins if we're in the root
			// directory of the target platform. The latter condition is a
			// workaround for the Refactory build where the target platform
			// root contains a META-INF folder.
			if ((isDirectory && !accepted) || (isDirectory && isTargetPlatformRoot)) {
				result.addAll(findFiles(file, fileFilter));
			}
		}
		return result;
	}

	private Set<IArtifact> analyzeTargetPlatformJarFiles(Set<File> targetPlatformFiles, String type,
			IBuildListener buildListener, IArtifactCreator creator) {

		Set<IArtifact> artifacts = new LinkedHashSet<IArtifact>();
		for (File targetPlatformFile : targetPlatformFiles) {
			IArtifact artifact;
			try {
				artifact = creator.create(targetPlatformFile);
			} catch (IOException e) {
				buildListener.handleBuildEvent(BuildEventType.WARNING, "Exception while analyzing target platform "
						+ type + " " + targetPlatformFile.toString() + ": " + e.getMessage());
				continue;
			} catch (InvalidMetadataException e) {
				buildListener.handleBuildEvent(BuildEventType.INFO, "Skipping invalid target platform JAR "
						+ targetPlatformFile.getAbsolutePath());
				continue;
			}
			if (artifact.getIdentifier() == null) {
				buildListener.handleBuildEvent(BuildEventType.INFO, "Ignoring target platform " + type
						+ " without name at " + targetPlatformFile.getAbsolutePath());
				continue;
			}
			artifacts.add(artifact);
			if (artifact instanceof CompiledPlugin) {
				Plugin plugin = (Plugin) artifact;
				artifacts.addAll(plugin.getExportedPackages());
			}
			buildListener.handleBuildEvent(
					BuildEventType.INFO,
					"Found target platform " + type + " '" + artifact.getIdentifier() + "' at "
							+ targetPlatformFile.getAbsolutePath());
		}

		return new ArtifactUtil().getSetOfArtifacts(artifacts);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + targetPlatformLocation.getPath() + "]";
	}
}
