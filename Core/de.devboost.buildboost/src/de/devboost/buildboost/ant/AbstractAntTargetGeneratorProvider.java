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
package de.devboost.buildboost.ant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.UnresolvedDependencyChecker;
import de.devboost.buildboost.model.AbstractBuildParticipant;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IArtifactDiscoverer;
import de.devboost.buildboost.model.IArtifactFilter;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildParticipant;

public abstract class AbstractAntTargetGeneratorProvider 
	extends AbstractBuildParticipant
	implements IAntTargetGeneratorProvider {
	
	@Override
	public void execute(IBuildContext context) throws BuildException {
		List<IArtifact> antTargets = new ArrayList<IArtifact>();
		Collection<IArtifact> artifacts = context.getDiscoveredArtifacts();
		for (IArtifact artifact : artifacts) {
			antTargets.addAll(getAntTargetGenerators(context, artifact));
		}
		context.addDiscoveredArtifacts(antTargets);
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
	
	public abstract Collection<? extends IArtifact> getAntTargetGenerators(
			IBuildContext context, IArtifact artifact);
}
