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
import de.devboost.buildboost.util.StringUtil;

/**
 * A {@link OrFilter} accepts all artifacts that are accepted by at least one filter from a given set of filters.
 */
public class OrFilter extends AbstractFilter {

	private IArtifactFilter[] filters;

	/**
	 * Creates a disjunctive filter over the given list of filters.
	 */
	public OrFilter(IArtifactFilter... filters) {
		super();
		this.filters = filters;
	}

	/**
	 * Returns true if the artifact is accepted by one of the filters.
	 */
	public boolean accept(IArtifact artifact) {
		for (IArtifactFilter filter : filters) {
			if (filter.accept(artifact)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + new StringUtil().explode(filters, ", ") + "]";
	}
}
