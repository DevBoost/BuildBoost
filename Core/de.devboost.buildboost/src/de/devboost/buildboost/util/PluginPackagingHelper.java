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
package de.devboost.buildboost.util;

import java.util.Set;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.steps.ClasspathHelper;

public class PluginPackagingHelper {

	/**
	 * Add scripts to the content that package the given set of plug-ins as
	 * JAR file. The JAR files are written to the target directory. The plug-in
	 * identifiers will be used as file names for the JARs.
	 */
	public void addPackageAsJarFileScripts(XMLContent content,
			String targetDir, Set<Plugin> plugins) {
		
		for (Plugin plugin : plugins) {
			addPackageAsJarFileScript(content, targetDir, plugin);
		}
	}

	/**
	 * Add a script to the content that packages the given plug-in as JAR file.
	 * The JAR file is written to the target directory. The plug-in identifier
	 * will be used as file name for the JAR.
	 */
	public void addPackageAsJarFileScript(XMLContent content, String targetDir,
			Plugin plugin) {

		if (plugin.isProject()) {
		    String jarFile = getJarFileName(targetDir, plugin);
		    String binPath = new ClasspathHelper().getBinPath(plugin);
		    
			content.append("<jar destfile=\"" + jarFile + "\">");
			content.append("<fileset dir=\"" + binPath + "\" />");
		    content.append("<fileset dir=\"" + plugin.getAbsolutePath() + "\" >");
		    content.append("<include name=\"metamodel/**\" />");
		    content.append("<include name=\"META-INF/**\" />");
		    content.append("</fileset>");
		    content.append("</jar>");
		} else {
			// TODO?
		}
	}

	public String getJarFileName(String directory, Plugin plugin) {
		return directory + "/" + plugin.getIdentifier() + ".jar";
	}
}
