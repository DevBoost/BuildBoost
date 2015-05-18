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
package de.devboost.buildboost.genext.webapps.util;

import de.devboost.buildboost.artifacts.Plugin;

public class WebAppUtil {

	public boolean isWebApp(Plugin plugin) {
		String identifier = plugin.getIdentifier();
		if (identifier.endsWith(".web") || identifier.endsWith(".webapp")) {
			return true;
		}
		return false;
	}
}
