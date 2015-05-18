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
package de.devboost.buildboost.model;

import java.util.Collection;

/**
 * An {@link IArtifact} is something that is subject to the build process. Each artifact requires a unique identifier
 * and may expose dependencies to other artifacts.
 */
public interface IArtifact extends IDependable {

	/**
	 * Returns the unique identifier for this artifact.
	 */
	public String getIdentifier();

	// TODO currently unused / for incremental build / needed?
	public long getTimestamp();

	public void resolveDependencies(Collection<? extends IArtifact> allArtifacts);

	public Collection<UnresolvedDependency> getUnresolvedDependencies();
}
