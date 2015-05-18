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
package de.devboost.buildboost.ant;

import java.util.ArrayList;
import java.util.Collection;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.model.AbstractBuildParticipant;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildParticipant;

public class AntTargetGeneratorRunner extends AbstractBuildParticipant {

	@Override
	public boolean dependsOn(IBuildParticipant otherParticipant) {
		return otherParticipant instanceof IAntTargetGeneratorProvider;
	}

	@Override
	public void execute(IBuildContext context) throws BuildException {
		Collection<IArtifact> antTargets = new ArrayList<IArtifact>();
		Collection<IArtifact> discoveredArtifacts = context.getDiscoveredArtifacts();
		for (IArtifact artifact : discoveredArtifacts) {
			if (artifact instanceof IAntTargetGenerator) {
				IAntTargetGenerator antTargetGenerator = (IAntTargetGenerator) artifact;
				Collection<AntTarget> generatedAntTargets = antTargetGenerator.generateAntTargets();
				for (AntTarget antTarget : generatedAntTargets) {
					antTargets.add(antTarget);
				}
			}
		}
		context.addDiscoveredArtifacts(antTargets);
	}
}
