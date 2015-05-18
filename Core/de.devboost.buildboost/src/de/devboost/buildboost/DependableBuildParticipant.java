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
package de.devboost.buildboost;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildParticipant;
import de.devboost.buildboost.model.IDependable;

public class DependableBuildParticipant implements IDependable, IBuildParticipant {

	private IBuildParticipant delegate;
	private Collection<IDependable> dependencies = new LinkedHashSet<IDependable>();

	public DependableBuildParticipant(IBuildParticipant participant) {
		this.delegate = participant;
	}

	public void initializeDependencies(List<DependableBuildParticipant> otherParticipants) {
		for (DependableBuildParticipant otherParticipant : otherParticipants) {
			if (dependsOn(otherParticipant)) {
				dependencies.add(otherParticipant);
			}
			if (isReqiredFor(otherParticipant)) {
				otherParticipant.dependencies.add(this);
			}
		}
	}

	@Override
	public void execute(IBuildContext context) throws BuildException {
		delegate.execute(context);
	}

	@Override
	public boolean dependsOn(IBuildParticipant otherParticipant) {
		if (otherParticipant instanceof DependableBuildParticipant) {
			DependableBuildParticipant dependableBuildParticipant = (DependableBuildParticipant) otherParticipant;
			return delegate.dependsOn(dependableBuildParticipant.delegate);
		} else {
			return delegate.dependsOn(otherParticipant);
		}
	}

	@Override
	public boolean isReqiredFor(IBuildParticipant otherParticipant) {
		if (otherParticipant instanceof DependableBuildParticipant) {
			DependableBuildParticipant dependableBuildParticipant = (DependableBuildParticipant) otherParticipant;
			return delegate.isReqiredFor(dependableBuildParticipant.delegate);
		} else {
			return delegate.isReqiredFor(otherParticipant);
		}
	}

	@Override
	public Collection<IDependable> getDependencies() {
		return dependencies;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
