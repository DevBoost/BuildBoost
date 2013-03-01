/*******************************************************************************
 * Copyright (c) 2006-2013
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
package de.devboost.buildboost.buildext.cmdlineapp;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.steps.ClasspathHelper;
import de.devboost.buildboost.util.XMLContent;

public class CommandlineAppPackagingStep extends AbstractAntTargetGenerator {

	private Plugin plugin;

	public CommandlineAppPackagingStep(Plugin plugin) {
		this.plugin = plugin;
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		XMLContent content = new XMLContent();
		// add script content
		content.append("<jar destfile=\"dist/commandlineapps/" + plugin.getIdentifier() + ".jar\">");
		
		// TODO this is a dirty hack and will not work when BuildBoost is used in its packaged form
		URL classLocation = getClass().getProtectionDomain().getCodeSource().getLocation();
		String pathTail = getClass().getPackage().getName().replace(".", File.separator) + File.separator + "jar-in-jar-loader.zip";
		content.append("<zipfileset src=\"" + new File(classLocation.getPath(), pathTail).getAbsolutePath() + "\"/>");
		
		List<String> allLibraries = new ArrayList<String>();
		// add the project itself
		addProject(content, plugin, allLibraries);
		// add the dependencies
	    Set<Plugin> dependencies = plugin.getAllDependencies();
	    for (Plugin dependency : dependencies) {
	    	if (dependency.isProject()) {
				addProject(content, dependency, allLibraries);
	    	}
	    }
		
		StringBuilder spaceSeparatedLibs = new StringBuilder();
		for (String lib : allLibraries) {
			spaceSeparatedLibs.append(" ");
			spaceSeparatedLibs.append(lib);
		}

		content.append("<manifest>");
		content.append("<attribute name=\"Main-Class\" value=\"org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader\"/>");
		content.append("<attribute name=\"Rsrc-Main-Class\" value=\"" + plugin.getIdentifier() + ".Main\"/>");
		content.append("<attribute name=\"Class-Path\" value=\".\"/>");
		content.append("<attribute name=\"Rsrc-Class-Path\" value=\"./" + spaceSeparatedLibs.toString() + "\"/>");
		content.append("</manifest>");
		
		content.append("</jar>");
		
		AntTarget target = new AntTarget("package-cmdlineapp-" + plugin.getIdentifier(), content);
		return Collections.singleton(target);
	}

	private void addProject(XMLContent content, Plugin project,
			List<String> allLibraries) {
		content.append("<fileset dir=\"" + new ClasspathHelper().getBinPath(project) + "\"/>");
		for (String lib : project.getLibs()) {
			String absoluteLibPath = project.getAbsoluteLibPath(lib);
			File libFile = new File(absoluteLibPath);
		    content.append("<zipfileset dir=\"" + libFile.getParentFile().getAbsolutePath() + "\" includes=\"" + libFile.getName() + "\"/>");
		    allLibraries.add(libFile.getName());
		}
	}
}
