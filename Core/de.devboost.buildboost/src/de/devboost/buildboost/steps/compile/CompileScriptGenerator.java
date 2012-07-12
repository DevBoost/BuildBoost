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
package de.devboost.buildboost.steps.compile;

import static de.devboost.buildboost.IConstants.NL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.util.ArtifactUtil;
import de.devboost.buildboost.util.Sorter;
import de.devboost.buildboost.util.XMLContent;

/**
 * A utility class that can create Ant scripts to compile Eclipse plug-in 
 * projects.
 */
public class CompileScriptGenerator {

	/**
	 * Returns an ANT script that compiles all the given projects in the correct
	 * order.
	 * 
	 * @param pluginsToCompile
	 * @param targetPlatformPlugins 
	 * @return
	 */
	public String getCompileAntScript(Collection<Plugin> pluginsToCompile, Set<Plugin> targetPlatformPlugins) {
		if (pluginsToCompile.contains(null)) {
			throw new RuntimeException("null not allowed");
		}
		Set<IDependable> targetPlatformArtifacts = new ArtifactUtil().getSetOfDependables(targetPlatformPlugins);
		// create ANT script that compiles the given collection of projects
		XMLContent content = new XMLContent();
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		content.append("<project basedir=\".\" default=\"compile-plugins\" name=\"Compile plug-ins\">");
		content.append("<property environment=\"env\"/>");
		content.append("<target name=\"compile-plugins\">");
		List<IDependable> sortedPlugins = new ArrayList<IDependable>();
		Sorter sorter = new Sorter();
		sortedPlugins.addAll(pluginsToCompile);
		for (IDependable plugin : sortedPlugins) {
			List<IDependable> cycle = sorter.findCycle(plugin, targetPlatformArtifacts);
			if (cycle == null) {
				continue;
			}
			System.out.println("Cycle: " + cycle);
		}
		sortedPlugins = sorter.topologicalSort(sortedPlugins, targetPlatformArtifacts);
		for (IDependable artifact : sortedPlugins) {
			if (artifact instanceof Plugin) {
				Plugin plugin = (Plugin) artifact;
				Collection<AntTarget> targets = new CompileProjectStep(plugin, null, null).generateAntTargets();
				for (AntTarget antTarget : targets) {
					content.append(antTarget.getContent());
				}
			}
		}
		content.append("</target>" + NL);
		content.append("</project>" + NL);
		return content.toString();
	}
}
