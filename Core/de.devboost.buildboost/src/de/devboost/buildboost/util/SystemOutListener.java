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
package de.devboost.buildboost.util;

import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IBuildListener;

/**
 * A basic implementation of the {@link IBuildListener} interface that prints all build events to System.out.
 */
public class SystemOutListener implements IBuildListener {

	public void handleBuildEvent(BuildEventType type, String message) {
		System.out.println(type.name() + ": " + message);
	}
}
