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

/**
 * A filter that accepts all artifacts with an identifier that matches a given regular expression.
 */
public class IdentifierRegexFilter extends AbstractFilter {

	private String regularExpression;

	public IdentifierRegexFilter(String regularExpression) {
		this.regularExpression = regularExpression;
	}

	public boolean accept(IArtifact artifact) {
		String identifier = artifact.getIdentifier();
		if (identifier != null && identifier.matches(regularExpression)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [regularExpression = " + regularExpression + "]";
	}
}
