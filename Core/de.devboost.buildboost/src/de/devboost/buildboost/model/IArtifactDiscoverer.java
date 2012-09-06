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

import java.util.Collection;

import de.devboost.buildboost.BuildException;

/**
 * An IArtifactDiscoverer can be used to discover artifacts that are required 
 * while performing a build. Prominent examples of such discoverers are project
 * finders and target platform analyzers.
 * 
 * IArtifactDiscoverers are use in the very beginning of a build to collect all
 * artifacts that will be incorporated in the build. After the artifacts have
 * been found, the can be filtered using {@link IArtifactFilter}s. The resulting
 * set of artifacts is then sorted according to their dependencies and 
 * appropriate build steps are performed.
 */
public interface IArtifactDiscoverer extends IBuildParticipant {

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) throws BuildException;
}
