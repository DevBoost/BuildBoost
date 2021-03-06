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
package de.devboost.buildboost.artifacts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.ResolvedDependency;
import de.devboost.buildboost.model.UnresolvedDependency;

/**
 * AbstractArtifact is a base class for implementing artifact types. It provides common functionality shared across
 * artifact type, for example, the resolution of unresolved dependencies.
 * 
 * This class is intended to be subclasses by clients.
 */
@SuppressWarnings("serial")
public abstract class AbstractArtifact implements IArtifact, Serializable {

	private String identifier;
	private Collection<UnresolvedDependency> unresolvedDependencies = new LinkedHashSet<UnresolvedDependency>();
	private Collection<ResolvedDependency> resolvedDependencies = new LinkedHashSet<ResolvedDependency>();

	public String getIdentifier() {
		return identifier;
	}

	public Collection<IDependable> getDependencies() {
		Collection<IDependable> dependencies = new LinkedHashSet<IDependable>();
		for (ResolvedDependency dependency : resolvedDependencies) {
			dependencies.add(dependency.getTarget());
		}
		return Collections.unmodifiableCollection(dependencies);
	}

	protected void addDependency(IArtifact artifact) {
		UnresolvedDependency dependency = new UnresolvedDependency(artifact.getClass(), artifact.getIdentifier(), null,
				true, null, true, false, false);
		addResolvedDependency(dependency, artifact);
	}

	public Collection<UnresolvedDependency> getUnresolvedDependencies() {
		return unresolvedDependencies;
	}

	public Collection<ResolvedDependency> getResolvedDependencies() {
		return Collections.unmodifiableCollection(resolvedDependencies);
	}

	public Collection<ResolvedDependency> getAllResolvedDependencies() {

		Collection<ResolvedDependency> allDependencies = new LinkedHashSet<ResolvedDependency>();
		for (ResolvedDependency dependency : getResolvedDependencies()) {
			// Add direct dependency
			allDependencies.add(dependency);

			// Add transitive dependencies
			IDependable target = dependency.getTarget();
			if (target instanceof AbstractArtifact) {
				AbstractArtifact abstractArtifact = (AbstractArtifact) target;
				allDependencies.addAll(abstractArtifact.getAllResolvedDependencies());
			}
		}
		return allDependencies;
	}

	public ResolvedDependency getResolvedDependency(UnresolvedDependency unresolvedDependency) {
		for (ResolvedDependency resolvedDependency : resolvedDependencies) {
			if (resolvedDependency.getUnresolvedDependency().equals(unresolvedDependency)) {
				return resolvedDependency;
			}
		}
		return null;
	}

	public void addResolvedDependency(UnresolvedDependency dependency, IDependable dependable) {

		ResolvedDependency resolvedDependency = new ResolvedDependency(this, dependable, dependency);

		resolvedDependencies.add(resolvedDependency);
		unresolvedDependencies.remove(dependency);
	}

	protected void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void resolveDependencies(Collection<? extends IArtifact> allArtifacts) {
		for (IArtifact artifact : allArtifacts) {
			Collection<UnresolvedDependency> unresolvedDependencies = new ArrayList<UnresolvedDependency>(
					getUnresolvedDependencies());
			for (UnresolvedDependency nextDependency : unresolvedDependencies) {
				if (nextDependency.isFulfilledBy(artifact)) {
					addResolvedDependency(nextDependency, artifact);
				}
			}
		}
	}

	@Override
	public long getTimestamp() {
		return -1;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + getIdentifier() + "]";
	}
}
