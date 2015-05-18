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
package de.devboost.buildboost.steps.compile;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.CompiledPlugin;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

public class ExtractPluginZipStepProvider extends AbstractAntTargetGeneratorProvider {

	private File targetPlatformCache;

	public ExtractPluginZipStepProvider(File targetPlatformCache) {
		super();
		this.targetPlatformCache = targetPlatformCache;
	}

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		if (artifact instanceof CompiledPlugin) {
			CompiledPlugin plugin = (CompiledPlugin) artifact;
			if (plugin.isZipped() && !plugin.getLibs().isEmpty()) {
				IAntTargetGenerator step = new ExtractPluginZipStep(plugin, targetPlatformCache);
				return Collections.singletonList(step);
			}
		}
		return Collections.emptyList();
	}
}
