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
package de.devboost.buildboost.genext.updatesite.steps;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

public class BuildUpdateSiteStepProvider extends AbstractAntTargetGeneratorProvider {

	private File targetDir;
	
	public BuildUpdateSiteStepProvider(File targetDir) {
		super();
		this.targetDir = targetDir;
	}

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		if (artifact instanceof EclipseUpdateSiteDeploymentSpec) {
			EclipseUpdateSiteDeploymentSpec updateSite = (EclipseUpdateSiteDeploymentSpec) artifact;
			IAntTargetGenerator step = new BuildUpdateSiteStep(updateSite, targetDir);
			return Collections.singletonList(step);
		}
		return Collections.emptyList();
	}
}
