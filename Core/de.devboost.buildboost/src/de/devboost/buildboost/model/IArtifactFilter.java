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
package de.devboost.buildboost.model;

/**
 * An IArtifactFilter can be used to filter the set of discovered artifacts.
 * This allows, for example, to exclude experimental projects from the build
 * process. It also provides a very fine-grained control over which artifacts
 * will be subject to the build an which will not be processed.
 */
public interface IArtifactFilter extends IBuildParticipant {

	/**
	 * Returns true if the given artifact shall be processed during the current
	 * build.
	 */
	public boolean accept(IArtifact artifact);
}
