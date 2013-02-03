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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.ArtifactUtil;
import de.devboost.buildboost.util.EclipsePluginHelper;

/**
 * A {@link PluginFinder} can be used to discover Eclipse plug-in projects.
 * Such projects typically contain a file called '.project'.
 * 
 * TODO inherit from {@link AbstractFileFinder}?
 */
public class PluginFinder extends AbstractArtifactDiscoverer {

	private File directory;

	public PluginFinder(File directory) {
		this.directory = directory;
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) {
		IBuildListener buildListener = context.getBuildListener();

		Collection<File> allProjectDirs = findProjectDirs(directory, buildListener);
		allProjectDirs = sortByPathName(allProjectDirs);
		checkForDuplicates(allProjectDirs);
		Set<IArtifact> allPlugins = convertToPlugins(allProjectDirs, context.getBuildListener());
		// make unmodifiable
		return Collections.unmodifiableSet(new ArtifactUtil().getSetOfArtifacts(allPlugins));
	}

	private Set<File> findProjectDirs(File directory, IBuildListener buildListener) {
		Set<File> projectDirs = new LinkedHashSet<File>();
		if (!directory.exists()) {
			buildListener.handleBuildEvent(
				BuildEventType.ERROR, 
				"Directory " + directory.getAbsolutePath() + " does not exist."
			);
			return projectDirs;
		}
		if (!directory.isDirectory()) {
			buildListener.handleBuildEvent(
				BuildEventType.ERROR, 
				"File " + directory.getAbsolutePath() + " is not a directory."
			);
			return projectDirs;
		}
		boolean isProject = new EclipsePluginHelper().isProject(directory);
		if (isProject) {
			projectDirs.add(directory);
			// do not examine children of projects
			return projectDirs;
		}
		// dive into child directories
		File[] children = directory.listFiles();
		if (children == null) {
			return projectDirs;
		}
		for (File child : children) {
			if (!child.isDirectory()) {
				continue;
			}
			projectDirs.addAll(findProjectDirs(child, buildListener));
		}
		return projectDirs;
	}

	private Collection<File> sortByPathName(Collection<File> allProjectDirs) {
		List<File> unsorted = new ArrayList<File>();
		unsorted.addAll(allProjectDirs);
		Collections.sort(unsorted, new Comparator<File>() {

			public int compare(File file1, File file2) {
				return file1.getAbsolutePath().compareTo(file2.getAbsolutePath());
			}
		});
		return unsorted;
	}

	private void checkForDuplicates(Collection<File> allProjectDirs) {
		// check whether there are multiple projects with the same name
		for (File projectDir : allProjectDirs) {
			for (File otherDir : allProjectDirs) {
				if (projectDir == otherDir) {
					continue;
				}
				if (otherDir.getName().equals(projectDir.getName())) {
					System.out.println("WARNING: Found plug-ins with duplicate name at: " + projectDir.getAbsolutePath() + " and " + otherDir.getAbsolutePath());
				}
			}
		}
	}

	private Set<IArtifact> convertToPlugins(Collection<File> projectDirs, IBuildListener listener) {
		Set<IArtifact> pluginsAndExportedPackages = new LinkedHashSet<IArtifact>();
		for (File projectDir : projectDirs) {
			Plugin newPlugin;
			try {
				newPlugin = new Plugin(projectDir);
				/*
				// TODO this belongs somewhere else
				// Do not add dependency to Tomcat, WebApps must declare a 
				// dependency to javax.servlet on their own
				if (projectDir.getName().endsWith(".webapp")) {
					UnresolvedDependency tomcatDependency = new UnresolvedDependency(Plugin.class, "org.apache.tomcat_6_0_32", null, true, null, true, false, false);
					newPlugin.getUnresolvedDependencies().add(tomcatDependency);
				}
				*/
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
			if (newPlugin.isExperimental()) {
				listener.handleBuildEvent(BuildEventType.INFO, "Ignoring EXPERIMENTAL project: " + newPlugin.getIdentifier());
				continue;
			}
			if (newPlugin.getSourceFolders().length == 0) {
				listener.handleBuildEvent(BuildEventType.INFO, "Project without source folders: " + newPlugin.getIdentifier());
			}
			listener.handleBuildEvent(BuildEventType.INFO, "Discovered project: " + newPlugin.getIdentifier());
			pluginsAndExportedPackages.add(newPlugin);
			pluginsAndExportedPackages.addAll(newPlugin.getExportedPackages());
		}
		return pluginsAndExportedPackages;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + directory.getAbsolutePath() + "]";
	}
}
