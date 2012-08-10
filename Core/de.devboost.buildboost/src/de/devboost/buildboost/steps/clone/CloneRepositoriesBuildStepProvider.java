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
package de.devboost.buildboost.steps.clone;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.RepositoriesFile;
import de.devboost.buildboost.artifacts.RepositoriesFile.Location;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

/**
 * The {@link CloneRepositoriesBuildStepProvider} add a {@link CloneRepositoriesBuildStep} 
 * for each plug-in project.
 */
public class CloneRepositoriesBuildStepProvider extends AbstractAntTargetGeneratorProvider {

	private File reposFolder;
	
	public CloneRepositoriesBuildStepProvider(File reposFolder) {
		super();
		this.reposFolder = reposFolder;
	}

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		if (artifact instanceof RepositoriesFile) {
			RepositoriesFile repositoriesFile = (RepositoriesFile) artifact;
			List<IAntTargetGenerator> steps = new ArrayList<IAntTargetGenerator>();
			List<Location> locations = repositoriesFile.getLocations();
			for (Location location : locations) {
				CloneRepositoriesBuildStep step = new CloneRepositoriesBuildStep(
						reposFolder, location);
				steps.add(step);
			}
			return steps;
		}
		return Collections.emptyList();
	}
}
