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
package de.devboost.buildboost.steps;

import java.util.Collection;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.XMLContent;

public class ClasspathHelper {

	private String prefix;
	private String suffix;

	public ClasspathHelper() {
		this("<pathelement location=\"");
	}

	public ClasspathHelper(String prefix) {
		this(prefix, "\" />");
	}

	public ClasspathHelper(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public XMLContent getClasspath(IDependable artifact, boolean includeSelf) {
		XMLContent result;
		if (artifact instanceof Plugin) {
			Plugin plugin = (Plugin) artifact;
			result = getClasspath(plugin, includeSelf);
		} else {
			result = new XMLContent();
			Collection<IDependable> dependencies = artifact.getDependencies();
			for (IDependable dependency : dependencies) {
				result.append(getClasspath(dependency, includeSelf));
			}
		}

		XMLContent normalizedClassPath = result.removeDuplicateEntries();
		return normalizedClassPath;
	}

	public XMLContent getClasspath(Plugin plugin, boolean includePlugin) {
		XMLContent classpath = new XMLContent();
		if (includePlugin) {
			classpath.append(getPluginClassPath(plugin));
		}
		// add libraries
		for (String lib : plugin.getAllLibPaths()) {
			classpath.append(prefix + lib + suffix);
		}

		for (Plugin dependency : plugin.getAllDependencies()) {
			classpath.append(getPluginClassPath(dependency));
		}
		for (UnresolvedDependency dependency : plugin.getUnresolvedDependencies()) {
			classpath.append("<!-- Skipping unresolved dependency: " + dependency.getIdentifier() + "-->");
		}
		return classpath;
	}

	private XMLContent getPluginClassPath(Plugin plugin) {
		XMLContent classpath = new XMLContent();
		classpath.append(prefix + plugin.getAbsolutePath() + suffix);
		return classpath;
	}

	public String getBinPath(Plugin project) {
		return project.getFile().getAbsolutePath();
	}
}
