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
package de.devboost.buildboost.genext.emf.steps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.emf.artifacts.GeneratorModel;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

/**
 * The {@link GenerateGenModelCodeStepProvider} add a 
 * {@link GenerateGenModelCodeStep} for each EMF generator model.
 */
public class GenerateGenModelCodeStepProvider extends AbstractAntTargetGeneratorProvider {

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		if (artifact instanceof GeneratorModel) {
			List<Plugin> plugins = getDiscoveredPlugins(context);
			
			List<IAntTargetGenerator> steps = new ArrayList<IAntTargetGenerator>(1);
			GeneratorModel generatorModel = (GeneratorModel) artifact;
			steps.add(new GenerateGenModelCodeStep(plugins, generatorModel));
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
