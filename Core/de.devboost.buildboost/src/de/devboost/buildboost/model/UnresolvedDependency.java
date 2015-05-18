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

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An UnresolvedDependency is a pair consisting of a symbolic artifact identifier and a version. UnresolvedDependency
 * objects are used to represent dependencies between artifacts when the actual artifacts are not known yet. During the
 * analysis process performed by BuildBoost, UnresolvedDependency objects are replaced by links to actual artifact
 * objects.
 */
public class UnresolvedDependency implements Serializable {

	private static final long serialVersionUID = 7284232444651755786L;

	private Class<?> type;
	private String identifier;
	private Set<String> alternativeIdentifiers = new LinkedHashSet<String>(1);
	private String minVersion;
	private String maxVersion;
	private boolean optional;
	private boolean reexport;
	private boolean inclusiveMin;
	private boolean inclusiveMax;

	/**
	 * Creates a new unresolved dependency object of the given type having the given identifier.
	 * 
	 * @param type
	 *            the type of artifact the dependency refers to
	 * @param identifier
	 *            the symbolic identifier of the referenced artifact
	 * @param minVersion
	 *            the minimal version that is required
	 * @param inclusiveMin
	 *            a flag indicating whether the minimal version is inclusive or not
	 * @param maxVersion
	 *            the maximum version that is required
	 * @param inclusiveMax
	 *            a flag indicating whether the maximum version is inclusive or not
	 * @param optional
	 *            a flag indicating whether the dependency is optional
	 * @param reexport
	 *            a flag indicating whether this dependency is re-exported
	 */
	public UnresolvedDependency(Class<?> type, String identifier, String minVersion, boolean inclusiveMin,
			String maxVersion, boolean inclusiveMax, boolean optional, boolean reexport) {
		super();
		this.type = type;
		this.identifier = identifier;
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
		this.optional = optional;
		this.reexport = reexport;
		this.inclusiveMin = inclusiveMin;
		this.inclusiveMax = inclusiveMax;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getMinVersion() {
		return minVersion;
	}

	public String getMaxVersion() {
		return maxVersion;
	}

	public boolean isOptional() {
		return optional;
	}

	public boolean isReexported() {
		return reexport;
	}

	public boolean isInclusiveMin() {
		return inclusiveMin;
	}

	public boolean isInclusiveMax() {
		return inclusiveMax;
	}

	public void addAlternativeIdentifier(String alternativeIdentifier) {
		alternativeIdentifiers.add(alternativeIdentifier);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((minVersion == null) ? 0 : minVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnresolvedDependency other = (UnresolvedDependency) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		}
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		}
		if (!type.equals(other.type)) {
			return false;
		}
		if (!identifier.equals(other.identifier)) {
			return false;
		}
		if (minVersion == null) {
			if (other.minVersion != null)
				return false;
		} else if (!minVersion.equals(other.minVersion))
			return false;
		return true;
	}

	/**
	 * Returns true if this dependency is fulfilled by the given artifact.
	 */
	public boolean isFulfilledBy(IArtifact artifact) {
		// check type
		if (!type.isInstance(artifact)) {
			return false;
		}

		// check symbolic identifier
		String artifactIdentifier = artifact.getIdentifier();
		if (artifactIdentifier.equals(getIdentifier())) {
			return true;
		}

		// check alternative symbolic identifiers
		if (alternativeIdentifiers.contains(artifactIdentifier)) {
			return true;
		}

		// TODO check versions for compatibility
		return false;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	@Override
	public String toString() {
		return type.getSimpleName() + " id=" + identifier + (minVersion == null ? "" : ", version=" + minVersion)
				+ (optional ? ", optional" : "") + (reexport ? ", reexport" : "");
	}
}
