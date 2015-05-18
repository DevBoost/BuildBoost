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
package de.devboost.buildboost.artifacts;

/**
 * An {@link InvalidMetadataException} is thrown if meta data is missing from a plug-in (e.g., if it does not have a
 * symbolic name).
 */
// TODO Instead of throwing this exception, the discovery logic for plug-ins
// should check the meta data of plug-ins.
public class InvalidMetadataException extends Exception {

	private static final long serialVersionUID = -9207028531518266417L;

}
