/*******************************************************************************
 * Copyright (c) 2006-2013
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
package de.devboost.buildboost.model;

public class ResolvedDependency {

	private IArtifact source;
	private IDependable target;
	private UnresolvedDependency unresolvedDependency;
	
	public ResolvedDependency(IArtifact source, IDependable target,
			UnresolvedDependency unresolvedDependency) {
		super();
		this.source = source;
		this.target = target;
		this.unresolvedDependency = unresolvedDependency;
	}

	public IArtifact getSource() {
		return source;
	}

	public IDependable getTarget() {
		return target;
	}

	public UnresolvedDependency getUnresolvedDependency() {
		return unresolvedDependency;
	}
}