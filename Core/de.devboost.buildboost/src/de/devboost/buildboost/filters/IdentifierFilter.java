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
package de.devboost.buildboost.filters;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.model.IArtifact;

/**
 * A filter that accepts artifacts where the identifier is within a given set
 * of valid identifiers.
 */
public class IdentifierFilter extends AbstractFilter {
	
	private Set<String> validArtifactIdentifiers = new LinkedHashSet<String>(1);

	public IdentifierFilter(String validArtifactIdentifier) {
		this(Collections.singleton(validArtifactIdentifier));
	}

	public IdentifierFilter(Set<String> validArtifactIdentifiers) {
		this.validArtifactIdentifiers = validArtifactIdentifiers;
	}

	public boolean accept(IArtifact artifact) {
		String identifier = artifact.getIdentifier();
		if (validArtifactIdentifiers.contains(identifier)) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [valid identifiers = " + validArtifactIdentifiers + "]";
	}
}
