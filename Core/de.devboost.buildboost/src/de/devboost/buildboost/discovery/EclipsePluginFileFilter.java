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
package de.devboost.buildboost.discovery;

import java.io.File;
import java.io.FileFilter;

import de.devboost.buildboost.util.EclipseFeatureHelper;
import de.devboost.buildboost.util.EclipsePluginHelper;

/**
 * The {@link EclipsePluginFileFilter} accepts JARs that contain bundled Eclipse plug-ins and directories which contain
 * extracted plug-in JARs.
 */
public class EclipsePluginFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		// exclude JUnit 3, because this requires to check the bundle
		// version when resolving dependencies
		// TODO remove this once the versions are checked
		String name = file.getName();
		if (name.contains("org.junit_3")) {
			return false;
		}
		if (file.isDirectory() && EclipsePluginHelper.INSTANCE.containsManifest(file)) {
			return true;
		}
		if (file.isFile() && name.endsWith(".jar") && !EclipseFeatureHelper.INSTANCE.isFeatureJAR(file)) {
			return true;
		}
		return false;
	}
}
