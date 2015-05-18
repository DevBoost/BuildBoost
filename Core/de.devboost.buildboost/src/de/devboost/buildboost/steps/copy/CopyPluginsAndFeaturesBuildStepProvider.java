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
package de.devboost.buildboost.steps.copy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.CompiledPlugin;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

/**
 * The {@link CopyPluginsAndFeaturesBuildStepProvider} add a {@link CopyPluginsAndFeaturesBuildStep} for each bundled
 * plug-in. This provider does not apply to plug-in projects.
 */
public class CopyPluginsAndFeaturesBuildStepProvider extends AbstractAntTargetGeneratorProvider {

	private final File targetDir;

	public CopyPluginsAndFeaturesBuildStepProvider(File targetDir) {
		super();
		this.targetDir = targetDir;
	}

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		if (artifact instanceof CompiledPlugin || artifact instanceof EclipseFeature) {
			List<IAntTargetGenerator> steps = new ArrayList<IAntTargetGenerator>(1);
			steps.add(new CopyPluginsAndFeaturesBuildStep(artifact, targetDir));
			return steps;
		}
		return Collections.emptyList();
	}
}
