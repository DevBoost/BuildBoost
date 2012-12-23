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

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.IAntTargetGeneratorProvider;

/**
 * The IBuildParticipant is an empty interface that is extended by concrete
 * interfaces that provide access to specific services required while executing
 * a build. The most imports extensions of this interface are: 
 * {@link IArtifactDiscoverer},
 * {@link IArtifactFilter}, and
 * {@link IAntTargetGeneratorProvider}.
 * 
 * The sole purpose of this interface is to allow to pass all services that 
 * participate in a build in a single list to the {@link AutoBuilder} class.
 */
public interface IBuildParticipant {
	
	// TODO update documentation
	public boolean dependsOn(IBuildParticipant otherParticipant);
	
	public boolean isReqiredFor(IBuildParticipant otherParticipant);
	
	public void execute(IBuildContext context) throws BuildException;
}
