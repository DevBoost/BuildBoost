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
package de.devboost.buildboost.discovery;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.artifacts.CompiledPlugin;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.util.ArtifactUtil;

/**
 * The EclipseTargetPlatformAnalyzer can be used to scan an Eclipse instance
 * to detect all contained plug-ins. This is required to compile Eclipse
 * plug-in projects that depend on plug-in of an Eclipse target platform.
 */
public class EclipseTargetPlatformAnalyzer extends AbstractArtifactDiscoverer {

	private interface IArtifactCreator {
		
		public IArtifact create(File file) throws Exception;
	}
	
	private File targetPlatform;

	public EclipseTargetPlatformAnalyzer(File targetPlatform) {
		this.targetPlatform = targetPlatform;
	}

	//TODO the discover should traverse the folder hierarchy only once. 
	//     Could we optimize discovering in general such that there is only one traversal each time?
	public Collection<IArtifact> discoverArtifacts(IBuildContext context) {
		IBuildListener buildListener = context.getBuildListener();
		buildListener.handleBuildEvent(BuildEventType.INFO, "Analyzing target platform...");
		
		Set<IArtifact> artifacts = new LinkedHashSet<IArtifact>();
		
		// first, find plug-ins
		Set<File> pluginJarsAndDirs = findJarFilesAndPluginDirs(targetPlatform, new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				if (!file.getParentFile().getName().equals("plugins")) {
					return false;
				}
				// exclude JUnit 3, because this requires to check the bundle
				// version when resolving dependencies
				// TODO remove this once the versions are checked
				if (file.isDirectory() && isPluginDir(file) && !file.getName().contains("org.junit_3")) {
					return true;
				}
				if (file.isFile() && file.getName().endsWith(".jar")) {
					return true;
				}
				return false;
			}
		});
		artifacts.addAll(analyzeTargetPlatformJarFiles(pluginJarsAndDirs, "plug-in", buildListener, new IArtifactCreator() {
			
			@Override
			public IArtifact create(File file) throws Exception {
				return new CompiledPlugin(file);
			}
		}));
		
		// second, find features
		Set<File> featureJarsAndDirs = findJarFilesAndPluginDirs(targetPlatform, new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				if (!file.getParentFile().getName().equals("features")) {
					return false;
				}
				return file.isDirectory() && isFeatureDir(file);
			}

		});
		
		artifacts.addAll(analyzeTargetPlatformJarFiles(featureJarsAndDirs, "feature", buildListener, new IArtifactCreator() {
			
			@Override
			public IArtifact create(File fileDirectoryOrJar) throws Exception {
				if (fileDirectoryOrJar.isDirectory()) {
					return new EclipseFeature(new File(fileDirectoryOrJar, "feature.xml"));
				} else {
					return new EclipseFeature(fileDirectoryOrJar);
				}
			}
		}));
		
		return artifacts;
	}

	private Set<File> findJarFilesAndPluginDirs(File directory, FileFilter fileFilter) {
		if (!directory.isDirectory()) {
			return Collections.emptySet();
		}
		File[] allFiles = directory.listFiles();
		Set<File> result = new LinkedHashSet<File>();
		for (File file : allFiles) {
			//ignore projects and things inside projects
			if (file.isDirectory()) {
				File dotProject = new File(file, ".project");
				if (dotProject.exists()) {
					continue;
				}
			}
			
			if (fileFilter.accept(file)) {
				result.add(file);
			} else if (file.isDirectory()) {
				result.addAll(findJarFilesAndPluginDirs(file, fileFilter));
			}
		}
		return result;
	}

	private Set<IArtifact> analyzeTargetPlatformJarFiles(
			Set<File> targetPlatformFiles, 
			String type, 
			IBuildListener buildListener,
			IArtifactCreator creator) {
		Set<IArtifact> artifacts = new LinkedHashSet<IArtifact>();
		for (File targetPlatformFile : targetPlatformFiles) {
			IArtifact artifact;
			try {
				artifact = creator.create(targetPlatformFile);
			} catch (Exception e) {
				buildListener.handleBuildEvent(BuildEventType.WARNING, "Exception while analyzing target platform " + type + " " + targetPlatformFile.toString() + ": " + e.getMessage());
				continue;
			}
			if (artifact.getIdentifier() == null) {
				buildListener.handleBuildEvent(
						BuildEventType.INFO, 
						"Ignoring target platform " + type + " without name at " + targetPlatformFile.getAbsolutePath()
				);
				continue;
			}
			artifacts.add(artifact);
			buildListener.handleBuildEvent(
					BuildEventType.INFO, 
					"Found target platform " + type + " '" + artifact.getIdentifier() + "' at " + targetPlatformFile.getAbsolutePath()
			);
		}
		
		return new ArtifactUtil().getSetOfArtifacts(artifacts);
	}

	/**
	 * Checks whether the given directory contains a 'META-INF/MANIFEST.MF' file.
	 */
	private boolean isPluginDir(File directory) {
		File metaInfDir = new File(directory, "META-INF");
		if (!metaInfDir.exists()) {
			return false;
		}
		File manifestFile = new File(metaInfDir, "MANIFEST.MF");
		return manifestFile.exists();
	}
	
	private boolean isFeatureDir(File directory) {
		File featureDescriptor = new File(directory, "feature.xml");
		if (directory.getParentFile().getName().equals("features") && featureDescriptor.exists()) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + targetPlatform.getPath() + "]";
	}
}
