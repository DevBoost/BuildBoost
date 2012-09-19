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

import java.util.List;

/**
 * An IBuildConfiguration is used by BuildBoost to obtain the list of build
 * stages. This interfaces should be implemented by the main class of all build 
 * plug-ins.
 */
public interface IBuildConfiguration {

	public List<IBuildStage> getBuildStages(String workspace);
}
