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

import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.model.IBuildParticipant;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.util.ArtifactUtil;
import de.devboost.buildboost.util.Sorter;

/**
 * The {@link AutoBuilder} is a generic builder that can be configured with sets of
 * build participants. These participants can search for artifacts that must be 
 * built and provide respective build scripts. The {@link AutoBuilder} class is the
 * main entry point to the BuildBoost system. A typical use of this class is to
 * configure an {@link IBuildContext} with appropriate discoverers, filters and 
 * build step providers, pass this context to the constructor 
 * {@link #AutoBuilder(IBuildContext)} and call {@link #generateAntTargets()} 
 * to obtain scripts that implement the build.
 * 
 * Alternatively, one can use build stages, which encapsulate typical 
 * configurations, for example a discoverer for Eclipse plug-in projects and
 * a build step provider that creates a compile script for such projects. The
 * use of build stages is recommended, but only applicable if respective stages
 * are available. 
 */
public class AutoBuilder {

	private IBuildContext context;

	public AutoBuilder(IBuildContext context) {
		this.context = context;
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		IBuildListener listener = context.getBuildListener();
		List<IBuildParticipant> participants = context.getBuildParticipants();
		participants = sort(participants);
		for (IBuildParticipant participant : participants) {
			listener.handleBuildEvent(BuildEventType.INFO, "Sorted participant order: " + participant);
		}
		for (IBuildParticipant participant : participants) {
			listener.handleBuildEvent(BuildEventType.INFO, "Executing build participant: " + participant);
			participant.execute(context);
		}
		
		Collection<AntTarget> targets = new ArrayList<AntTarget>();
		Collection<IArtifact> discoveredArtifacts = context.getDiscoveredArtifacts();
		for (IArtifact artifact : discoveredArtifacts) {
			if (artifact instanceof AntTarget) {
				AntTarget antTarget = (AntTarget) artifact;
				targets.add(antTarget);
			}
		}
		return targets;
	}

	private List<IBuildParticipant> sort(List<IBuildParticipant> participants) {
		List<DependableBuildParticipant> dependables = new ArrayList<DependableBuildParticipant>();
		for (IBuildParticipant participant : participants) {
			dependables.add(new DependableBuildParticipant(participant));
		}
		for (DependableBuildParticipant dependable : dependables) {
			dependable.initializeDependencies(dependables);
		}
		List<IDependable> sorted = new Sorter().topologicalSort(new ArtifactUtil().getConcreteList(dependables, IDependable.class));
		return new ArtifactUtil().getConcreteList(sorted, IBuildParticipant.class);
	}
}
