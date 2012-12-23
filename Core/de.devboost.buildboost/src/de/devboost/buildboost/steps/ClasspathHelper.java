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
package de.devboost.buildboost.steps;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.IConstants;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IDependable;
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
		XMLContent normalizedClassPath = removeDuplicateEntries(result);
		return normalizedClassPath;
	}

	// TODO move this to the XMLContent class
	private XMLContent removeDuplicateEntries(XMLContent result) {
		// remove duplicate entries
		String content = result.toString();
		String[] lines = content.split(IConstants.NL);
		Set<String> uniqueLines = new LinkedHashSet<String>();
		for (String line : lines) {
			uniqueLines.add(line);
		}
		XMLContent normalizedClassPath = new XMLContent();
		for (String uniqueLine : uniqueLines) {
			normalizedClassPath.append(uniqueLine);
		}
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
