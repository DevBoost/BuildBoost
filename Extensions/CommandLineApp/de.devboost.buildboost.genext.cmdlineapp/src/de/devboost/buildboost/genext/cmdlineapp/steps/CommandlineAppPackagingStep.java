/*******************************************************************************
 * Copyright (c) 2006-2016
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
package de.devboost.buildboost.genext.cmdlineapp.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.util.PluginPackagingHelper;
import de.devboost.buildboost.util.StreamUtil;
import de.devboost.buildboost.util.XMLContent;

public class CommandlineAppPackagingStep extends AbstractAntTargetGenerator {

	private final Plugin plugin;
	private final IBuildListener buildListener;

	public CommandlineAppPackagingStep(Plugin plugin, IBuildListener buildListener) {
		this.plugin = plugin;
		this.buildListener = buildListener;
	}

	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
		XMLContent content = new XMLContent();

		String temporaryDir = "temp/cmdlineapps/" + plugin.getIdentifier();
		content.append("<mkdir dir=\"" + temporaryDir + "\" />");

		Set<Plugin> dependencies = plugin.getAllDependencies();

		PluginPackagingHelper packagingHelper = new PluginPackagingHelper();
		packagingHelper.addPackageAsJarFileScript(content, temporaryDir, plugin);
		packagingHelper.addPackageAsJarFileScripts(content, temporaryDir, dependencies);

		// add script content
		content.append("<jar destfile=\"dist/commandlineapps/" + plugin.getIdentifier() + ".jar\">");

		// TODO this is a dirty hack and will not work when BuildBoost is used in its packaged form
		URL classLocation = getClass().getProtectionDomain().getCodeSource().getLocation();
		String pathTail = getClass().getPackage().getName().replace(".", File.separator) + File.separator
				+ "jar-in-jar-loader.zip";
		try {
			content.append("<zipfileset src=\"" + new File(classLocation.toURI().getPath(), pathTail).getAbsolutePath()
					+ "\"/>");
		} catch (URISyntaxException e) {
			throw new BuildException(e.getMessage());
		}

		List<String> allLibraries = new ArrayList<String>();
		// add the project itself
		addProject(content, temporaryDir, plugin, allLibraries);
		// add the dependencies
		for (Plugin dependency : dependencies) {
			if (dependency.isProject()) {
				addProject(content, temporaryDir, dependency, allLibraries);
			} else {
				addTargetPlatformPlugin(content, dependency, allLibraries);
			}
		}
		
		Set<String> additionalFiles = getLines("additional_zip_contents.conf");
		if (additionalFiles != null) {
			for (String additionalFile : additionalFiles) {
				content.append("<fileset file=\"" + plugin.getLocation().getAbsolutePath() + File.separator + additionalFile + "\" />");
			}
		}

		allLibraries = removeExcludedLibraries(allLibraries);

		StringBuilder spaceSeparatedLibs = new StringBuilder();
		for (String lib : allLibraries) {
			spaceSeparatedLibs.append(" ");
			spaceSeparatedLibs.append(lib);
		}

		content.append("<manifest>");
		content.append(
				"<attribute name=\"Main-Class\" value=\"org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader\"/>");
		content.append("<attribute name=\"Rsrc-Main-Class\" value=\"" + plugin.getIdentifier() + ".Main\"/>");
		content.append("<attribute name=\"Class-Path\" value=\".\"/>");
		content.append("<attribute name=\"Rsrc-Class-Path\" value=\"./" + spaceSeparatedLibs.toString() + "\"/>");
		content.append("</manifest>");

		content.append("</jar>");

		AntTarget target = new AntTarget("package-cmdlineapp-" + plugin.getIdentifier(), content);
		return Collections.singleton(target);
	}

	private List<String> removeExcludedLibraries(List<String> allLibraries) {
		String filename = "excluded_libraries.conf";
		Set<String> librariesToRemoveFromClasspath = getLines(filename);
		if (librariesToRemoveFromClasspath == null) {
			return allLibraries;
		}

		Set<String> remainingLibraries = new LinkedHashSet<String>(allLibraries);
		for (String libraryToRemoveFromClasspath : librariesToRemoveFromClasspath) {
			boolean removed = remainingLibraries.remove(libraryToRemoveFromClasspath);
			if (removed) {
				buildListener.handleBuildEvent(BuildEventType.INFO,
						"Removed excluded library '" + libraryToRemoveFromClasspath + "' from JAR classpath");
			} else {
				buildListener.handleBuildEvent(BuildEventType.INFO,
						"Can't find excluded library '" + libraryToRemoveFromClasspath + "'");
			}
		}

		return new ArrayList<String>(remainingLibraries);
	}

	private Set<String> getLines(String filename) {
		String path = plugin.getAbsolutePath() + File.separator + filename;
		File excludeFile = new File(path);
		if (!excludeFile.exists()) {
			return null;
		}

		Set<String> librariesToRemoveFromClasspath = new LinkedHashSet<String>();
		try {
			String contentAsString = new StreamUtil().getContentAsString(new FileInputStream(excludeFile));
			contentAsString = contentAsString.replace("\r", "");
			String[] lines = contentAsString.split("\n");
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i].trim();
				if (line.isEmpty()) {
					continue;
				}
				librariesToRemoveFromClasspath.add(line);
			}
		} catch (FileNotFoundException e) {
			buildListener.handleBuildEvent(BuildEventType.ERROR, e.getMessage());
		} catch (IOException e) {
			buildListener.handleBuildEvent(BuildEventType.ERROR, e.getMessage());
		}
		return librariesToRemoveFromClasspath;
	}

	private void addTargetPlatformPlugin(XMLContent content, Plugin plugin, List<String> allLibraries) {
		File file = plugin.getFile();
		if (file.isFile()) {
			content.append("<fileset file=\"" + file.getAbsolutePath() + "\" />");
			allLibraries.add(file.getName());
		} else {
			// TODO extracted plug-ins must be packaged
		}
	}

	private void addProject(XMLContent content, String tempDir, Plugin project, List<String> allLibraries) {
		String jarName = project.getIdentifier() + ".jar";
		content.append("<fileset dir=\"" + tempDir + "\" includes=\"" + jarName + "\" />");
		allLibraries.add(jarName);
		for (String lib : project.getLibs()) {
			String absoluteLibPath = project.getAbsoluteLibPath(lib);
			File libFile = new File(absoluteLibPath);
			content.append("<zipfileset dir=\"" + libFile.getParentFile().getAbsolutePath() + "\" includes=\""
					+ libFile.getName() + "\"/>");
			allLibraries.add(libFile.getName());
		}
	}
}
