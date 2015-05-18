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
package de.devboost.buildboost.filters;

import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IArtifactFilter;

/**
 * A {@link NegatingFilter} accepts all artifacts that are discarded by a given other filter.
 */
public class NegatingFilter extends AbstractFilter {

	private IArtifactFilter filterToNegate;

	public NegatingFilter(IArtifactFilter filterToNegate) {
		super();
		this.filterToNegate = filterToNegate;
	}

	public boolean accept(IArtifact artifact) {
		return !filterToNegate.accept(artifact);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + filterToNegate + "]";
	}
}
