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

import java.io.File;
import java.io.IOException;

/**
 * A {@link CompiledPlugin} is a plug-in that is available from a previously packaged target platform. A
 * {@link CompiledPlugin} can be a JAR or a folder containing the extracted contents of the plug-in.
 */
// TODO Maybe this class can be merged with class Plugin
@SuppressWarnings("serial")
public class CompiledPlugin extends Plugin {

	public CompiledPlugin(File location) throws IOException, InvalidMetadataException {
		super(location);
	}

	@Override
	public boolean isProject() {
		return false;
	}

	public boolean isZipped() {
		return getLocation().getName().endsWith(".jar");
	}

	public void unzip() {
		if (isZipped()) {
			String zipFileName = getLocation().getName();
			File newLocation = new File(getLocation().getParentFile(), zipFileName.substring(0,
					zipFileName.lastIndexOf('.')));
			location = newLocation;
		}
	}
}
