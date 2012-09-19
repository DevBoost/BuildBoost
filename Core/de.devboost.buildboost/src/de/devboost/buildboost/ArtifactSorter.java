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
package de.devboost.buildboost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.devboost.buildboost.model.AbstractBuildParticipant;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IArtifactDiscoverer;
import de.devboost.buildboost.model.IArtifactFilter;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.model.IBuildParticipant;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.util.ArtifactUtil;
import de.devboost.buildboost.util.Sorter;

public class ArtifactSorter extends AbstractBuildParticipant {

	@Override
	public boolean dependsOn(IBuildParticipant otherParticipant) {
		if (otherParticipant instanceof DependencyResolver) {
			return true;
		}
		if (otherParticipant instanceof UnresolvedDependencyChecker) {
			return true;
		}
		if (otherParticipant instanceof IArtifactDiscoverer) {
			return true;
		}
		if (otherParticipant instanceof IArtifactFilter) {
			return true;
		}
		return false;
	}

	@Override
	public void execute(IBuildContext context) throws BuildException {
		Collection<IArtifact> discoveredArtifacts = context.getDiscoveredArtifacts();
		IBuildListener buildListener = context.getBuildListener();
		Sorter pluginSorter = new Sorter();

		// exclude dependencies that cannot be included while sorting
		Set<IDependable> discoveredDependables = new ArtifactUtil().getSetOfDependables(discoveredArtifacts);
		Set<IDependable> transientHull = pluginSorter.getTransientHull(discoveredDependables);
		transientHull.removeAll(discoveredArtifacts);
		for (IDependable artifact : transientHull) {
			buildListener.handleBuildEvent(
				BuildEventType.INFO, 
				"Artifact " + artifact + " is excluded from topological sorting (artifact is a transitive dependency, but was not selected for build)."
			);
		}

		// sort artifacts topologically
		List<IDependable> sortedArtifacts = pluginSorter.topologicalSort(
				new ArrayList<IDependable>(discoveredArtifacts), 
				transientHull);
		
		// replace artifacts
		context.removeDiscoveredArtifacts(discoveredArtifacts);
		context.addDiscoveredArtifacts(new ArtifactUtil().getConcreteList(sortedArtifacts, IArtifact.class));
	}
}
