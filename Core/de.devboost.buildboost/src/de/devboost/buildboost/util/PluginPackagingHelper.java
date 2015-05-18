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
package de.devboost.buildboost.util;

import java.io.File;
import java.util.Set;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.steps.ClasspathHelper;

public class PluginPackagingHelper {

	/**
	 * Add scripts to the content that package the given set of plug-ins as JAR file. The JAR files are written to the
	 * target directory. The plug-in identifiers will be used as file names for the JARs.
	 */
	public void addPackageAsJarFileScripts(XMLContent content, String targetDir, Set<Plugin> plugins) {

		for (Plugin plugin : plugins) {
			addPackageAsJarFileScript(content, targetDir, plugin);
		}
	}

	/**
	 * Add a script to the content that packages the given plug-in as JAR file. The JAR file is written to the target
	 * directory. The plug-in identifier will be used as file name for the JAR.
	 */
	public void addPackageAsJarFileScript(XMLContent content, String targetDir, Plugin plugin) {

		if (plugin.isProject()) {
			String jarFile = getJarFileName(targetDir, plugin);
			String binPath = new ClasspathHelper().getBinPath(plugin);

			String pluginPath = plugin.getAbsolutePath();

			content.append("<jar destfile=\"" + jarFile + "\" manifest=\"" + pluginPath + "/META-INF/MANIFEST.MF\">");
			content.append("<fileset dir=\"" + binPath + "\" />");
			content.append("<fileset dir=\"" + pluginPath + "\" >");
			content.append("<include name=\"metamodel/**\" />");
			content.append("<include name=\"META-INF/**\" />");
			content.append("</fileset>");
			content.append("</jar>");
		} else {
			// TODO?
		}
	}

	/**
	 * Adds a script that updates the version, vendor and name of the plug-in's manifest.
	 */
	public void addUpdateManifestScript(XMLContent content, Plugin plugin, String pluginVersion, String pluginVendor,
			String pluginName) {

		String pluginID = plugin.getIdentifier();
		File pluginDirectory = plugin.getFile();
		String pluginPath = pluginDirectory.getAbsolutePath();

		boolean isPackaged = plugin.isJarFile();
		if (isPackaged) {
			content.append("<echo message=\"Plug-in '" + pluginID + "' is already packaged.\"/>");
		} else {
			// package plug-in
			content.append("<echo message=\"Updating manifest of plug-in '" + pluginID + "'.\"/>");
			content.append("<manifest file=\"" + pluginPath + "/META-INF/MANIFEST.MF\" mode=\"update\">");
			// FIXME Only set bundle version if it does not end with .v[0-9]*
			content.append("<attribute name=\"Bundle-Version\" value=\"" + pluginVersion + ".v${buildid}\"/>");
			// only replace vendor if one is specified in the update site
			// specification
			if (pluginVendor != null) {
				content.append("<attribute name=\"Bundle-Vendor\" value=\"" + pluginVendor + "\"/>");
			}
			content.append("<attribute name=\"Bundle-SymbolicName\" value=\"" + pluginID + "; singleton:=true\"/>");
			// only replace plug-in name if one is specified in the update
			// site specification
			if (pluginName != null) {
				content.append("<attribute name=\"Bundle-Name\" value=\"" + pluginName + "\"/>");
			}
			content.append("</manifest>");
		}
		content.appendLineBreak();
	}

	public String getJarFileName(String directory, Plugin plugin) {
		return directory + "/" + plugin.getIdentifier() + ".jar";
	}
}
