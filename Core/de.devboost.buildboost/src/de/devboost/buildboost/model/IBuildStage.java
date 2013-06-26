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

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;

public interface IBuildStage {

	public AntScript getScript() throws BuildException;
	
	/**
	 * Build stages can be disabled which means that the code for the respective
	 * build is generated but not executed.
	 */
	public boolean isEnabled();

	/**
	 * Returns the priority of this build stage. This is currently used to
	 * determine the order in which build stages are executed.
	 */
	// TODO use explicit dependencies (BuildStage class names?) instead
	public int getPriority();
}
