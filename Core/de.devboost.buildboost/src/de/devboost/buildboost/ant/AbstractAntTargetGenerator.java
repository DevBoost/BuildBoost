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

import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;

public abstract class AbstractAntTargetGenerator implements IAntTargetGenerator {

	@Override
	public String getIdentifier() {
		return toString();
	}

	@Override
	public Collection<IDependable> getDependencies() {
		return Collections.emptySet();
	}

	@Override
	public void resolveDependencies(Collection<? extends IArtifact> allArtifacts) {
		// do nothing
	}

	@Override
	public Collection<UnresolvedDependency> getUnresolvedDependencies() {
		return Collections.emptySet();
	}

	@Override
	public long getTimestamp() {
		return -1;
	}
}
