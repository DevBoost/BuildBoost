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
package de.devboost.buildboost.genext.maven.steps;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.genext.maven.artifacts.MavenRepositorySpec;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

public class BuildMavenRepositoryStepProvider extends AbstractAntTargetGeneratorProvider {

	private File artifactsFolder;
	
	public BuildMavenRepositoryStepProvider(File artifactsFolder) {
		super();
		this.artifactsFolder = artifactsFolder;
	}

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		if (artifact instanceof MavenRepositorySpec) {
			MavenRepositorySpec repositorySpec = (MavenRepositorySpec) artifact;
			IAntTargetGenerator step = new BuildMavenRepositoryStep(context,
					repositorySpec, artifactsFolder);
			return Collections.singletonList(step);
		}
		return Collections.emptyList();
	}
}
