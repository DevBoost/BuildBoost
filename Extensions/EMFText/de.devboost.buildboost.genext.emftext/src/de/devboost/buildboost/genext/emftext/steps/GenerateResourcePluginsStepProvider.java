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
package de.devboost.buildboost.genext.emftext.steps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.emftext.artifacts.ConcreteSyntaxDefinition;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

/**
 * The {@link GenerateResourcePluginsStepProvider} add a 
 * {@link GenerateResourcePluginsStep} for each EMFText syntax definition.
 */
public class GenerateResourcePluginsStepProvider extends AbstractAntTargetGeneratorProvider {

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		if (artifact instanceof ConcreteSyntaxDefinition) {
			List<Plugin> plugins = getDiscoveredPlugins(context);
			
			ConcreteSyntaxDefinition syntaxDefinition = (ConcreteSyntaxDefinition) artifact;
			List<IAntTargetGenerator> steps = new ArrayList<IAntTargetGenerator>(1);
			steps.add(new GenerateResourcePluginsStep(plugins, syntaxDefinition));
			return steps;
		} else {
			return Collections.emptyList();
		}
	}
	
	private List<Plugin> getDiscoveredPlugins(IBuildContext context) {
		Collection<IArtifact> discoveredArtifacts = context.getDiscoveredArtifacts();
		List<Plugin> plugins = new ArrayList<Plugin>();
		for (IArtifact discoveredArtifact : discoveredArtifacts) {
			if (discoveredArtifact instanceof Plugin) {
				Plugin plugin = (Plugin) discoveredArtifact;
				plugins.add(plugin);
			}
		}
		return plugins;
	}
}
