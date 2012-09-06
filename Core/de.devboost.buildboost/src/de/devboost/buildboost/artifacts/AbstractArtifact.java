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
package de.devboost.buildboost.artifacts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;

/**
 * AbstractArtifact is a base class for implementing artifact types. It provides
 * common functionality shared across artifact type, for example, the resolution
 * of unresolved dependencies.
 * 
 * This class is intended to be subclasses by clients.
 */
@SuppressWarnings("serial")
public abstract class AbstractArtifact implements IArtifact, Serializable {

	private String identifier;
	private Collection<IDependable> dependencies = new LinkedHashSet<IDependable>();
	private Collection<UnresolvedDependency> unresolvedDependencies = new LinkedHashSet<UnresolvedDependency>();
	private Collection<UnresolvedDependency> resolvedDependencies = new LinkedHashSet<UnresolvedDependency>();

	public String getIdentifier() {
		return identifier;
	}
	
	public Collection<IDependable> getDependencies() {
		return Collections.unmodifiableCollection(dependencies);
	}

	public void addDependency(IArtifact artifact) {
		dependencies.add(artifact);
	}

	public Collection<UnresolvedDependency> getUnresolvedDependencies() {
		return unresolvedDependencies;
	}
	
	public Collection<UnresolvedDependency> getResolvedDependencies() {
		return resolvedDependencies;
	}

	protected void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void resolveDependencies(Collection<? extends IArtifact> allArtifacts) {
		List<UnresolvedDependency> resolvedDependencies = new ArrayList<UnresolvedDependency>();
		for (IArtifact artifact : allArtifacts) {
			for (UnresolvedDependency nextDependency : getUnresolvedDependencies()) {
				if (nextDependency.isFulfilledBy(artifact)) {
					resolvedDependencies.add(nextDependency);
					dependencies.add(artifact);
				}
			}
			getUnresolvedDependencies().removeAll(resolvedDependencies);
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
