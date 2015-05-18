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

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.IAntTargetGeneratorProvider;

/**
 * An {@link IBuildParticipant} can contribute to a build. It is executed by the {@link AutoBuilder} class by calling
 * {@link #execute(IBuildContext)}. To determine the order of execution in case there are multiple participants, the
 * methods {@link #dependsOn(IBuildParticipant)} and {@link #isReqiredFor(IBuildParticipant)} are used.
 * 
 * The most imports extensions of this interface are: {@link IArtifactDiscoverer}, {@link IArtifactFilter}, and
 * {@link IAntTargetGeneratorProvider}.
 */
public interface IBuildParticipant {

	/**
	 * Returns true if this participant depends on the other participant, which means that this participant must be
	 * executed after the other participant.
	 */
	public boolean dependsOn(IBuildParticipant otherParticipant);

	/**
	 * Returns true if this participant is required by the other participant, which means that this participant must be
	 * executed before the other participant.
	 */
	public boolean isReqiredFor(IBuildParticipant otherParticipant);

	/**
	 * Execute this participant.
	 * 
	 * @param context
	 *            the context in which the execution takes place
	 * @throws BuildException
	 *             if something goes terribly wrong
	 */
	public void execute(IBuildContext context) throws BuildException;
}
