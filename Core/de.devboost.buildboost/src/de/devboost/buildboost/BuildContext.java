/*******************************************************************************
 * Copyright (c) 2006-2016
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
package de.devboost.buildboost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import de.devboost.buildboost.ant.AntTargetGeneratorRunner;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.model.IBuildParticipant;
import de.devboost.buildboost.util.SystemOutListener;

/**
 * A default implementation of the IBuildContext interface.
 */
public class BuildContext implements IBuildContext {

	private final Collection<IArtifact> discoveredArtifacts = new LinkedHashSet<IArtifact>();
	private final List<IBuildParticipant> buildParticipants = new ArrayList<IBuildParticipant>();
	
	private IBuildListener buildListener;
	private boolean ignoreUnresolvedDependencies;

	public BuildContext() {
		super();
		// add default participants
		addBuildParticipant(new DependencyResolver());
		addBuildParticipant(new UnresolvedDependencyChecker());
		addBuildParticipant(new ArtifactSorter());
		addBuildParticipant(new AntTargetGeneratorRunner());
	}

	@Override
	public Collection<IArtifact> getDiscoveredArtifacts() {
		return Collections.unmodifiableCollection(discoveredArtifacts);
	}

	@Override
	public IBuildListener getBuildListener() {
		if (buildListener == null) {
			buildListener = new SystemOutListener();
		}
		return buildListener;
	}

	public void setBuildListener(IBuildListener buildListener) {
		this.buildListener = buildListener;
	}

	@Override
	public boolean ignoreUnresolvedDependencies() {
		return ignoreUnresolvedDependencies;
	}

	public void setIgnoreUnresolvedDependencies(boolean ignoreUnresolvedDependencies) {
		this.ignoreUnresolvedDependencies = ignoreUnresolvedDependencies;
	}

	@Override
	public void addDiscoveredArtifacts(Collection<IArtifact> artifactsToAdd) {
		discoveredArtifacts.addAll(artifactsToAdd);
	}

	@Override
	public void removeDiscoveredArtifacts(Collection<IArtifact> artifactsToRemove) {
		discoveredArtifacts.removeAll(artifactsToRemove);
	}

	@Override
	public void addBuildParticipant(IBuildParticipant participant) {
		buildParticipants.add(participant);
	}

	@Override
	public List<IBuildParticipant> getBuildParticipants() {
		return buildParticipants;
	}
}
