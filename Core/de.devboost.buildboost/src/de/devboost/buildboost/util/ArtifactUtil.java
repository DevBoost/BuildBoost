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
package de.devboost.buildboost.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IDependable;

/**
 * A utility class to convert between sets of artifacts with different type 
 * parameters, where the actuals contents of the sets are compatible, but the
 * Java type inferences cannot recognize this.
 */
public class ArtifactUtil {

	public <T extends IDependable> Set<IDependable> getSetOfDependables(Collection<T> concreteArtifacts) {
		Set<IDependable> result = new LinkedHashSet<IDependable>();
		for (T concreteArtifact : concreteArtifacts) {
			result.add(concreteArtifact);
		}
		return result;
	}

	public <T extends IArtifact> Set<IArtifact> getSetOfArtifacts(Collection<T> concreteArtifacts) {
		Set<IArtifact> result = new LinkedHashSet<IArtifact>();
		for (T concreteArtifact : concreteArtifacts) {
			result.add(concreteArtifact);
		}
		return result;
	}

	public <T> Set<T> getConcreteSet(Collection<?> abstractArtifacts, Class<T> clazz) {
		Set<T> result = new LinkedHashSet<T>();
		for (Object abstractArtifact : abstractArtifacts) {
			if (clazz.isInstance(abstractArtifact)) {
				result.add(clazz.cast(abstractArtifact));
			} else {
				throw new RuntimeException("Can't cast " + abstractArtifact + " to " + clazz.getName());
			}
		}
		return result;
	}

	public <T> List<T> getConcreteList(Collection<?> abstractArtifacts, Class<T> clazz) {
		List<T> result = new ArrayList<T>();
		for (Object abstractArtifact : abstractArtifacts) {
			if (clazz.isInstance(abstractArtifact)) {
				result.add(clazz.cast(abstractArtifact));
			} else {
				throw new RuntimeException("Can't cast " + abstractArtifact + " to " + clazz.getName());
			}
		}
		return result;
	}

	public <T> Collection<T> filter(Collection<?> collection, Class<T> clazz) {
		List<T> result = new ArrayList<T>();
		for (Object next : collection) {
			if (clazz.isInstance(next)) {
				result.add(clazz.cast(next));
			}
		}
		return result;
	}
}
