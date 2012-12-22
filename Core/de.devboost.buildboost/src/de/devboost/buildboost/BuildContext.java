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

	// Singleton: only one instance in a jvm is needed (user changes during the build are not permitted)
	private static GlobalBuildConfiguration globalBuildConfiguration=new GlobalBuildConfiguration();
	
	public static GlobalBuildConfiguration getGlobalBuildConfiguration() {
		return globalBuildConfiguration;
	}

	private Collection<IArtifact> discoveredArtifacts = new LinkedHashSet<IArtifact>();
	private IBuildListener buildListener;
	private boolean ignoreUnresolvedDependencies;
	private List<IBuildParticipant> buildParticipants = new ArrayList<IBuildParticipant>();

	
	
	public BuildContext() {
		super();
		// add default participants
		addBuildParticipant(new DependencyResolver());
		addBuildParticipant(new UnresolvedDependencyChecker());
		addBuildParticipant(new ArtifactSorter());
		addBuildParticipant(new AntTargetGeneratorRunner());
	}
	
	public Collection<IArtifact> getDiscoveredArtifacts() {
		return Collections.unmodifiableCollection(discoveredArtifacts);
	}

	public IBuildListener getBuildListener() {
		if (buildListener == null) {
			buildListener = new SystemOutListener();
		}
		return buildListener;
	}

	public void setBuildListener(IBuildListener buildListener) {
		this.buildListener = buildListener;
	}

	public boolean ignoreUnresolvedDependencies() {
		return ignoreUnresolvedDependencies;
	}

	public void setIgnoreUnresolvedDependencies(boolean ignoreUnresolvedDependencies) {
		this.ignoreUnresolvedDependencies = ignoreUnresolvedDependencies;
	}

	public void addDiscoveredArtifacts(Collection<IArtifact> artifactsToAdd) {
		discoveredArtifacts.addAll(artifactsToAdd);
	}
	
	public void removeDiscoveredArtifacts(Collection<IArtifact> artifactsToRemove) {
		discoveredArtifacts.removeAll(artifactsToRemove);
	}

	public void addBuildParticipant(IBuildParticipant participant) {
		buildParticipants.add(participant);
	}

	public List<IBuildParticipant> getBuildParticipants() {
		return buildParticipants;
	}
}
