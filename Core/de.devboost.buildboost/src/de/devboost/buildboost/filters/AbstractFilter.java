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
package de.devboost.buildboost.filters;

import java.util.ArrayList;
import java.util.Collection;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.DependencyResolver;
import de.devboost.buildboost.model.AbstractBuildParticipant;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IArtifactDiscoverer;
import de.devboost.buildboost.model.IArtifactFilter;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.model.IBuildParticipant;

public abstract class AbstractFilter extends AbstractBuildParticipant implements IArtifactFilter {

	@Override
	public void execute(IBuildContext context) throws BuildException {
		IBuildListener buildListener = context.getBuildListener();
		Collection<IArtifact> artifacts = context.getDiscoveredArtifacts();
		Collection<IArtifact> artifactsToRemove = new ArrayList<IArtifact>(); 
		for (IArtifact artifact : artifacts) {
			if (!accept(artifact)) {
				buildListener.handleBuildEvent(BuildEventType.INFO, "Artifact " + artifact + " is removed because of filter " + this);
				artifactsToRemove.add(artifact);
			}
		}
		context.removeDiscoveredArtifacts(artifactsToRemove);
	}
	
	public OrFilter or(IArtifactFilter other) {
		return new OrFilter(this, other);
	}
	
	@Override
	public boolean dependsOn(IBuildParticipant otherParticipant) {
		if (otherParticipant instanceof IArtifactDiscoverer) {
			return true;
		}
		if (otherParticipant instanceof DependencyResolver) {
			return true;
		}
		return false;
	}
}
