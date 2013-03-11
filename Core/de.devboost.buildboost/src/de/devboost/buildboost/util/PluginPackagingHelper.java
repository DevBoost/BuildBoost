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

	public void getPackageDependenciesScript(XMLContent content,
			String tempDir, Set<Plugin> plugins) {
		for (Plugin plugin : plugins) {
			getPackageDependenciesScript(content, tempDir, plugin);
		}
	}

	public void getPackageDependenciesScript(XMLContent content,
			String tempDir, Plugin plugin) {
		// each project the WebApp depends on is packaged as individual JAR
		// file
		if (plugin.isProject()) {
		    String jarFile = getJarFileName(tempDir, plugin);
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

	public String getJarFileName(String rootDir, Plugin dependency) {
		return rootDir + "/" + dependency.getIdentifier() + ".jar";
	}
}
