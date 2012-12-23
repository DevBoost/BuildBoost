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

import java.util.Collection;

import de.devboost.buildboost.model.AbstractBuildParticipant;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.model.IBuildParticipant;
import de.devboost.buildboost.model.UnresolvedDependency;

public class UnresolvedDependencyChecker extends AbstractBuildParticipant {

	@Override
	public boolean dependsOn(IBuildParticipant otherParticipant) {
		if (otherParticipant instanceof DependencyResolver) {
			return true;
		}
		return false;
	}

	@Override
	public void execute(IBuildContext context) throws BuildException {
		Collection<IArtifact> discoveredArtifacts = context.getDiscoveredArtifacts();
		IBuildListener buildListener = context.getBuildListener();
		// check for unresolved dependencies
		boolean foundUnresolvedDependency = false;
		for (IArtifact artifact : discoveredArtifacts) {
			Collection<UnresolvedDependency> unresolvedDependencies = artifact.getUnresolvedDependencies();
			for (UnresolvedDependency unresolvedDependency : unresolvedDependencies) {
				if (unresolvedDependency.isOptional()) {
					continue;
				}
				String message = "Found unresolved dependency in artifact " + artifact + ": " + unresolvedDependency;
				if (!context.ignoreUnresolvedDependencies()) {
					buildListener.handleBuildEvent(BuildEventType.ERROR, message);
					foundUnresolvedDependency = true;
				} else {
					buildListener.handleBuildEvent(BuildEventType.WARNING, message);
				}
			}
		}
		if (foundUnresolvedDependency) {
			throw new BuildException("Found unresolved dependencies");
		}
	}
}
