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
package de.devboost.buildboost;

/**
 * A {@link BuildException} can be thrown be participants of the BuildBoost script generation process if unexpected
 * situations are detected (e.g. required artifacts are missing).
 */
public class BuildException extends Exception {

	private static final long serialVersionUID = 6725403464736524958L;

	public BuildException(String message) {
		super(message);
	}
}
