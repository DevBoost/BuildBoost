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
package de.devboost.buildboost.discovery;

import java.util.Collection;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.model.AbstractBuildParticipant;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IArtifactDiscoverer;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;

public abstract class AbstractArtifactDiscoverer extends AbstractBuildParticipant 
	implements IArtifactDiscoverer {

	@Override
	public void execute(IBuildContext context) throws BuildException {
		IBuildListener buildListener = context.getBuildListener();
		buildListener.handleBuildEvent(
				BuildEventType.INFO, 
				"Running artifact discoverer: " + this);
		Collection<IArtifact> discoveredArtifacts = discoverArtifacts(context);
		context.addDiscoveredArtifacts(discoveredArtifacts);
	}
}
