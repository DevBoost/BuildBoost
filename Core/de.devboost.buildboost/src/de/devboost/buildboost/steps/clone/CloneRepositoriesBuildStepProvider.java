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
package de.devboost.buildboost.steps.clone;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.UnresolvedDependencyChecker;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.ant.IAntTargetGeneratorProvider;
import de.devboost.buildboost.artifacts.RepositoriesFile;
import de.devboost.buildboost.artifacts.RepositoriesFile.Location;
import de.devboost.buildboost.model.AbstractBuildParticipant;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IArtifactDiscoverer;
import de.devboost.buildboost.model.IArtifactFilter;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildParticipant;

/**
 * The {@link CloneRepositoriesBuildStepProvider} add a {@link CloneRepositoriesBuildStep} for each
 * {@link RepositoriesFile}.
 */
public class CloneRepositoriesBuildStepProvider extends AbstractBuildParticipant implements IAntTargetGeneratorProvider {

	private final File reposFolder;

	public CloneRepositoriesBuildStepProvider(File reposFolder) {
		this.reposFolder = reposFolder;
	}

	public void execute(IBuildContext context) throws BuildException {
		List<IArtifact> antTargets = new ArrayList<IArtifact>();
		Collection<IArtifact> artifacts = context.getDiscoveredArtifacts();
		antTargets.add(getAntTargetGenerators(context, artifacts));
		context.addDiscoveredArtifacts(antTargets);
	}

	public IAntTargetGenerator getAntTargetGenerators(IBuildContext context, Collection<IArtifact> artifacts) {
		Collection<Location> locations = new ArrayList<RepositoriesFile.Location>();
		for (IArtifact artifact : artifacts) {
			if (artifact instanceof RepositoriesFile) {
				RepositoriesFile repositoriesFile = (RepositoriesFile) artifact;
				for (Location location : repositoriesFile.getLocations()) {
					boolean locationDuplicate = false;
					for (Location locationInList : locations) {
						if (locationInList.getUrl().equals(location.getUrl())) {
							locationDuplicate = true;
							locationInList.getSubDirectories().addAll(location.getSubDirectories());
							break;
						}
					}
					if (!locationDuplicate) {
						locations.add(location);
					}
				}
			}
		}

		removeOverlapsInSVNLocations(locations);

		CloneRepositoriesBuildStep step = new CloneRepositoriesBuildStep(reposFolder, locations);
		return step;
	}

	private void removeOverlapsInSVNLocations(Collection<Location> locations) {
		for (Location location1 : new ArrayList<Location>(locations)) {
			if (!"svn".equals(location1.getType())) {
				continue;
			}
			for (Iterator<Location> i = locations.iterator(); i.hasNext();) {
				Location location2 = i.next();
				if (!"svn".equals(location2.getType())) {
					continue;
				}
				String location1URL = location1.getUrl();
				if (!location1URL.endsWith("/")) {
					location1URL = location1URL + "/";
				}
				if (!location1.equals(location2) && location2.getUrl().startsWith(location1URL)) {
					i.remove();
				}
			}
		}
	}

	public boolean dependsOn(IBuildParticipant otherParticipant) {
		if (otherParticipant instanceof IArtifactDiscoverer) {
			return true;
		}
		if (otherParticipant instanceof IArtifactFilter) {
			return true;
		}
		if (otherParticipant instanceof UnresolvedDependencyChecker) {
			return true;
		}
		return false;
	}
}
